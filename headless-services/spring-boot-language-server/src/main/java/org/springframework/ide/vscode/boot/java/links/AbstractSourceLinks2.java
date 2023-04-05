package org.springframework.ide.vscode.boot.java.links;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Stack;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.javadoc.TypeUrlProviderFromContainerUrl;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.Region;

public abstract class AbstractSourceLinks2 implements SourceLinks {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractSourceLinks2.class);

	private CompilationUnitCache cuCache;

	private JavaProjectFinder projectFinder;

	protected AbstractSourceLinks2(CompilationUnitCache cuCache, JavaProjectFinder projectFinder) {
		this.cuCache = cuCache;
		this.projectFinder = projectFinder;
	}

	@Override
	public Optional<String> sourceLinkUrlForFQName(IJavaProject project, String fqName) {
		Optional<String> url = project == null ? Optional.empty() : getSourceLinkUrlForFQName(project, fqName);
		if (!url.isPresent()) {
			for (IJavaProject jp : projectFinder.all()) {
				if (jp != project) {
					url = getSourceLinkUrlForFQName(jp, fqName);
					if (url.isPresent()) {
						break;
					}
				}
			}
		}
		return url;
	}

	private Optional<String> getSourceLinkUrlForFQName(IJavaProject project, String fqName) {
		IJavaModuleData classpathResource = project.getIndex().findClasspathResourceContainer(fqName);
		if (classpathResource != null) {
			File file = classpathResource.getContainer();
			if (file.isDirectory()) {
				return javaSourceLinkUrl(project, fqName, classpathResource);
			} else {
				return jarSourceLinkUrl(project, fqName, classpathResource);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> sourceLinkUrlForClasspathResource(String path) {
		return SourceLinks.sourceLinkUrlForClasspathResource(this, projectFinder, path);
	}

	private Optional<String> javaSourceLinkUrl(IJavaProject project, String fqName, IJavaModuleData folderModuleData) {
		IClasspath classpath = project.getClasspath();
		return SourceLinks.sourceFromSourceFolder(fqName, classpath)
			.map(sourcePath -> javaSourceLinkUrl(project, sourcePath, fqName));
	}

	private String javaSourceLinkUrl(IJavaProject project, Path sourcePath, String fqName) {
		Optional<String> linkOptional = sourceLinkForResourcePath(sourcePath);
		if (linkOptional.isPresent()) {
			Optional<String> positionLink = findCU(project, sourcePath.toUri()).map(cu -> positionLink(cu, fqName));
			return positionLink.isPresent() ? linkOptional.get() + positionLink.get() : linkOptional.get();
		}
		return null;
	}

	abstract protected String positionLink(CompilationUnitDeclaration cu, String fqName);

	private Optional<CompilationUnitDeclaration> findCU(IJavaProject project, URI uri) {
		return cuCache == null ? Optional.empty() : cuCache.withCompilationUnitDeclaration(project, uri, compilationUnit -> Optional.ofNullable(compilationUnit));
	}

	abstract protected Optional<String> jarLinkUrl(IJavaProject project, String fqName, IJavaModuleData jarModuleData);

	private Optional<String> jarSourceLinkUrl(IJavaProject project, String fqName, IJavaModuleData jarModuleData) {
		return jarLinkUrl(project, fqName, jarModuleData).map(sourceUrl -> {
			Optional<String> positionLink = findCUForFQNameFromJar(project, jarModuleData, fqName).map(cu -> positionLink(cu, fqName));
			return positionLink.isPresent() ? sourceUrl + positionLink.get() : sourceUrl;
		});
	}

	private Optional<CompilationUnitDeclaration> findCUForFQNameFromJar(IJavaProject project, IJavaModuleData jarModuleData, String fqName) {
		return IClasspathUtil.sourceContainer(project.getClasspath(), jarModuleData.getContainer()).map(url -> {
			try {
				return TypeUrlProviderFromContainerUrl.JAR_SOURCE_URL_PROVIDER.url(url, fqName, jarModuleData.getModule());
			} catch (Exception e) {
				log.warn("Failed to determine source URL from url={} fqName={}", url, fqName, e);
				return null;
			}
		}).map(sourceUrl -> {
			try {
				return sourceUrl.toURI();
			} catch (URISyntaxException e) {
				throw new IllegalStateException(e);
			}
		}).map(sourcePath -> findCU(project, sourcePath).orElse(null));
	}

	protected Region findTypeRegion(CompilationUnitDeclaration cu, String fqName) {
		if (cu == null) {
			return null;
		}
		int[] values = new int[] {0, -1};
		int lastDotIndex = fqName.lastIndexOf('.');
		String packageName = fqName.substring(0, lastDotIndex);
		String typeName = fqName.substring(lastDotIndex + 1);
		String[] tokens = new String[cu.currentPackage.tokens.length];
		for (int i = 0; i < cu.currentPackage.tokens.length; i++) {
			tokens[i] = new String(cu.currentPackage.tokens[i]);
		}
		String cuPackageName = String.join(".", tokens);
		if (packageName.equals(cuPackageName)) {
			Stack<String> visitedType = new Stack<>();
			
			cu.traverse(new ASTVisitor() {
				
				private boolean visitDeclaration(TypeDeclaration node) {
					visitedType.push(new String(node.name));
					if (values[1] < 0) {
						if (String.join("$", visitedType.toArray(new String[visitedType.size()])).equals(typeName)) {
							values[0] = node.sourceStart;
							values[1] = node.sourceEnd;
						}
					}
					return values[1] < 0;
				}

				@Override
				public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
					return visitDeclaration(localTypeDeclaration);
				}

				@Override
				public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
					return visitDeclaration(memberTypeDeclaration);
				}

				@Override
				public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
					return visitDeclaration(typeDeclaration);
				}

				@Override
				public void endVisit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
					visitedType.pop();
					super.endVisit(localTypeDeclaration, scope);
				}

				@Override
				public void endVisit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
					visitedType.pop();
					super.endVisit(memberTypeDeclaration, scope);
				}

				@Override
				public void endVisit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
					visitedType.pop();
					super.endVisit(typeDeclaration, scope);
				}

			}, new CompilationUnitScope(cu, new CompilerOptions(CompilationUnitCache.createCompilerOptions())), false);
		}
		return values[1] < 0 ? null : new Region(values[0], values[1]);
	}

}
