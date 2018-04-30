/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.links;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.javadoc.TypeUrlProviderFromContainerUrl;
import org.springframework.ide.vscode.commons.util.text.Region;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Base logic for {@link SourceLinks} independent of any client 
 * 
 * @author Alex Boyko
 *
 */
public abstract class AbstractSourceLinks implements SourceLinks {
	
	private static Supplier<Logger> LOG = Suppliers.memoize(() -> LoggerFactory.getLogger(AbstractSourceLinks.class));
	
	private BootJavaLanguageServerComponents server;
	
	protected AbstractSourceLinks(BootJavaLanguageServerComponents server) {
		this.server = server;
	}

	@Override
	public Optional<String> sourceLinkUrlForFQName(IJavaProject project, String fqName) {
		Optional<File> classpathResource = project.getIndex().findClasspathResourceContainer(fqName);
		if (classpathResource.isPresent()) {
			File file = classpathResource.get();
			if (file.isDirectory()) {
				return javaSourceLinkUrl(project, fqName, file);
			} else if (file.getName().endsWith(JAR)) {
				return jarSourceLinkUrl(project, fqName, file);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> sourceLinkUrlForClasspathResource(IJavaProject project, String path) {
		int idx = path.lastIndexOf(CLASS);
		if (idx >= 0) {
			Path p = Paths.get(path.substring(0, idx));
			return sourceLinkUrlForFQName(project, p.toString().replace(File.separator, "."));
		}
		return Optional.empty();
	}
	
	private Optional<String> javaSourceLinkUrl(IJavaProject project, String fqName, File containerFolder) {
		IClasspath classpath = project.getClasspath();
		return IClasspathUtil.getSourceFolders(classpath)
			.map(sourceFolder -> {
				try {
					return sourceFolder.toURI().toURL();
				} catch (MalformedURLException e) {
					LOG.get().warn("Failed to convert source folder " + sourceFolder + "to URI." + fqName, e);
					return null;
				}
			})
			.map(url -> {
				try {
					return TypeUrlProviderFromContainerUrl.SOURCE_FOLDER_URL_SUPPLIER.url(url, fqName);
				} catch (Exception e) {
					LOG.get().warn("Failed to determine source URL from url=" + url + " fqName=" + fqName, e);
					return null;
				}
			})
			.map(url -> {
				try {
					return Paths.get(url.toURI());
				} catch (URISyntaxException e) {
					LOG.get().warn("Failed to convert URL " + url + " to path." + fqName, e);
					return null;
				}
			})
			.filter(sourcePath -> sourcePath != null && Files.exists(sourcePath))
			.findFirst()
			.map(sourcePath -> javaSourceLinkUrl(project, sourcePath, fqName));
	}

	private String javaSourceLinkUrl(IJavaProject project, Path sourcePath, String fqName) {
		Optional<String> linkOptional = sourceLinkForResourcePath(sourcePath);
		if (linkOptional.isPresent()) {
			Optional<String> positionLink = findCUForJavaSourceFile(sourcePath).map(cu -> positionLink(cu, fqName));
			return positionLink.isPresent() ? linkOptional.get() + positionLink.get() : linkOptional.get();
		}
		return null;
	}
	
	abstract protected String positionLink(CompilationUnit cu, String fqName);
	
	private Optional<CompilationUnit> findCUForJavaSourceFile(Path resourcePath) {
		Optional<CompilationUnit> cu = findCUfromCache(resourcePath.toUri().toString());
		if (cu == null) {
			try {
				char[] bytes = new String(Files.readAllBytes(resourcePath), Charset.defaultCharset()).toCharArray();
				String uri = resourcePath.toUri().toString();
				String unitName = resourcePath.getFileName().toString();
				cu = Optional.ofNullable(CompilationUnitCache.parse(bytes, uri, unitName, new String[0]));
			} catch (Exception e) {
				LOG.get().warn("Failed to create CompilationUnit from " + resourcePath, e);
				cu = Optional.empty();
			}
		}
		return cu;
	}
	
	private Optional<CompilationUnit> findCUfromCache(String uri) {
		Optional<CompilationUnit> cu = null;
		if (server != null && server.getCompilationUnitCache() != null) {
			TextDocument doc = server.getTextDocumentService().get(uri);
			if (doc != null) {
				cu = server.getCompilationUnitCache().withCompilationUnit(doc, compilationUnit -> compilationUnit == null ? null : Optional.of(compilationUnit));
			}
		}
		return cu;
	}
	
	abstract protected Optional<String> jarUrl(IJavaProject project, String fqName, File jarFile);
	
	private Optional<String> jarSourceLinkUrl(IJavaProject project, String fqName, File jarFile) {
		return jarUrl(project, fqName, jarFile).map(sourceUrl -> {
			Optional<String> positionLink = findCUForFQNameFromJar(project, jarFile, sourceUrl, fqName).map(cu -> positionLink(cu, fqName));
			return positionLink.isPresent() ? sourceUrl + positionLink.get() : sourceUrl;
		});
	}
	
	private Optional<CompilationUnit> findCUForFQNameFromJar(IJavaProject project, File jarFile, String clientSourceUri, String fqName) {
		Optional<CompilationUnit> cu = findCUfromCache(clientSourceUri);
		if (cu == null) {
			cu = project.sourceContainer(jarFile)
					.map(url -> {
						try {
							return TypeUrlProviderFromContainerUrl.JAR_SOURCE_URL_PROVIDER.url(url, fqName);
						} catch (Exception e) {
							LOG.get().warn("Failed to determine source URL from url=" + url + " fqName=" + fqName, e);
							return null;
						}
					})
					.map(sourceUrl -> {
						InputStream openStream = null;
						try {
							openStream = sourceUrl.openStream();
							char[] bytes = IOUtils.toCharArray(openStream);
							String uri = sourceUrl.toURI().toString();
							String unitName = fqName;
							return CompilationUnitCache.parse(bytes, uri, unitName, new String[0]);
						} catch (Exception e) {
							LOG.get().warn("Failed to create CompilationUnit from " + sourceUrl, e);
							return null;
						} finally {
							if (openStream != null) {
								try {
									openStream.close();
								} catch (IOException e) {
									LOG.get().error("Failed to close stream from " + sourceUrl, e);
								}
							}
						}
					});
		}
		return cu;
	}
	
	protected Region findTypeRegion(CompilationUnit cu, String fqName) {
		if (cu == null) {
			return null;
		}
		int[] values = new int[] {0, -1};
		int lastDotIndex = fqName.lastIndexOf('.');
		String packageName = fqName.substring(0, lastDotIndex);
		String typeName = fqName.substring(lastDotIndex + 1);
		if (packageName.equals(cu.getPackage().getName().getFullyQualifiedName())) {
			Stack<String> visitedType = new Stack<>();
			cu.accept(new ASTVisitor() {
				
				private boolean visitDeclaration(AbstractTypeDeclaration node) {
					visitedType.push(node.getName().getIdentifier());
					if (values[1] < 0) {
						if (String.join("$", visitedType.toArray(new String[visitedType.size()])).equals(typeName)) {
							values[0] = node.getName().getStartPosition();
							values[1] = node.getName().getLength();
						}
					}
					return values[1] < 0;
				}

				@Override
				public boolean visit(TypeDeclaration node) {
					return visitDeclaration(node);
				}

				@Override
				public boolean visit(AnnotationTypeDeclaration node) {
					return visitDeclaration(node);
				}

				@Override
				public void endVisit(AnnotationTypeDeclaration node) {
					visitedType.pop();
					super.endVisit(node);
				}

				@Override
				public void endVisit(TypeDeclaration node) {
					visitedType.pop();
					super.endVisit(node);
				}

			});
		}
		return values[1] < 0 ? null : new Region(values[0], values[1]);
	}
	
}
