/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.test;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.boot.java.links.JavaDocumentUriProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.languageserver.testharness.Editor;

import com.google.common.collect.ImmutableSet;

public class DefinitionLinkAsserts {

	private JavaDocumentUriProvider javaDocumentUriProvider;
	private CompilationUnitCache cuCache;

	public static JavaMethod method(String fqClassName, String methodName, String... params) {
		return new JavaMethod(fqClassName, methodName, params);
	}

	public static JavaField field(String fqClassName, String name) {
		return new JavaField(fqClassName, name);
	}

	public static class JavaMethod {
		public final String fqName;
		public final String methodName;
		public final String[] params;
		public JavaMethod(String fqClassName, String methodName, String... params) {
			this.fqName = fqClassName;
			this.methodName = methodName;
			this.params = params;
		}
		@Override
		public String toString() {
			return "JavaMethod [fqName=" + fqName + ", methodName=" + methodName + ", params=" + Arrays.toString(params)
					+ "]";
		}
	}

	public static class JavaField {
		public final String fqName;
		public final String fieldName;
		public JavaField(String fqName, String fieldName) {
			super();
			this.fqName = fqName;
			this.fieldName = fieldName;
		}
		@Override
		public String toString() {
			return "JavaField [fqName=" + fqName + ", fieldName=" + fieldName + "]";
		}
	}

	public DefinitionLinkAsserts(JavaDocumentUriProvider javaDocumentUriProvider, CompilationUnitCache cuCache) {
		this.javaDocumentUriProvider = javaDocumentUriProvider;
		this.cuCache = cuCache;

	}

	public void assertLinkTargets(Editor editor, String hoverOver, IJavaProject project, Range highlightRange, JavaMethod... methods) throws Exception {
		Set<LocationLink> expectedLocations = new HashSet<>();
		for (JavaMethod method : methods) {
			Location l = getLocation(project, method);
			expectedLocations.add(new LocationLink(l.getUri(), l.getRange(), l.getRange(), highlightRange));
		}

		editor.assertLinkTargets(hoverOver, expectedLocations);
	}

	public void assertLinkTargets(Editor editor, String hoverOver, IJavaProject project, Range highlightRange, String typeFqName) throws Exception {

		Location l = getLocation(project, typeFqName);
		
		LocationLink link = new LocationLink(l.getUri(), l.getRange(), l.getRange(), highlightRange);

		editor.assertLinkTargets(hoverOver, ImmutableSet.of(link));
	}

	public void assertLinkTargets(Editor editor, String hoverOver, IJavaProject project, Range highlightRange, JavaField field) throws Exception {

		Location l = getLocation(project, field);

		LocationLink link = new LocationLink(l.getUri(), l.getRange(), l.getRange(), highlightRange);

		editor.assertLinkTargets(hoverOver, ImmutableSet.of(link));
	}

	private Location getLocation(IJavaProject project, String fqName) throws Exception {
		Location loc = new Location();
		Optional<URL> sourceUrl = SourceLinks.source(project, fqName);
		if (sourceUrl.isPresent()) {

			URI docUri = javaDocumentUriProvider.docUri(project, fqName);
			loc.setUri(docUri.toString());

			String typeName = fqName.substring(fqName.lastIndexOf('.') + 1);
			URI sourceUri = sourceUrl.get().toURI();
			Range r = cuCache.withCompilationUnit(project, sourceUri, (cu) -> {
				try {
					TextDocument doc = new TextDocument(sourceUrl.get().toString(), LanguageId.JAVA);
					doc.setText(cuCache.fetchContent(sourceUri));
					AtomicReference<Range> range = new AtomicReference<>(null);
					cu.accept(new ASTVisitor() {

						private boolean proceessTypeNode(TextDocument doc, String typeName,
								AtomicReference<Range> range, AbstractTypeDeclaration node) {
							SimpleName nameNode = node.getName();
							if (nameNode.getIdentifier().equals(typeName)) {
								try {
									range.set(doc.toRange(nameNode.getStartPosition(), nameNode.getLength()));
									return false;
								} catch (BadLocationException e) {
									throw new IllegalStateException(e);
								}
							}
							return true;
						}

						@Override
						public boolean visit(TypeDeclaration node) {
							return proceessTypeNode(doc, typeName, range, node);
						}

						@Override
						public boolean visit(EnumDeclaration node) {
							return proceessTypeNode(doc, typeName, range, node);
						}

					});
					return range.get();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			});
			if (r == null) {
				throw new IllegalStateException("Couldn't find " + fqName);
			}
			loc.setRange(r);
		}
		return loc;
	}

	private Location getLocation(IJavaProject project, JavaMethod method) throws Exception {
		Location loc = new Location();
		Optional<URL> sourceUrl = SourceLinks.source(project, method.fqName);
		if (sourceUrl.isPresent()) {

			URI docUri = javaDocumentUriProvider.docUri(project, method.fqName);
			loc.setUri(docUri.toString());

			URI sourceUri = sourceUrl.get().toURI();
			Range r = cuCache.withCompilationUnit(project, sourceUri, (cu) -> {
				try {
					AtomicReference<Range> range = new AtomicReference<>(null);
					TextDocument doc = new TextDocument(sourceUrl.get().toString(), LanguageId.JAVA);
					doc.setText(cuCache.fetchContent(sourceUri));
					cu.accept(new ASTVisitor() {

						@Override
						public boolean visit(MethodDeclaration node) {
							SimpleName nameNode = node.getName();
							if (nameNode.getIdentifier().equals(method.methodName)) {
								if (node.parameters().size() != method.params.length) {
									return false;
								}
								int i = 0;
								for (Object _p : node.parameters()) {
									if (_p instanceof SingleVariableDeclaration) {
										SingleVariableDeclaration p = (SingleVariableDeclaration) _p;
										String fqName = p.getType().resolveBinding().getErasure().getQualifiedName();
										if (!fqName.equals(method.params[i++])) {
											return false;
										}
									} else {
										return false;
									}
								}
								try {
									range.set(doc.toRange(nameNode.getStartPosition(), nameNode.getLength()));
								} catch (BadLocationException e) {
									throw new IllegalStateException(e);
								}
							}
							return false;
						}

					});
					return range.get();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			});
			if (r == null) {
				throw new IllegalStateException("Couldn't find " + method);
			}
			loc.setRange(r);
		}
		return loc;
	}

	private Location getLocation(IJavaProject project, JavaField field) throws Exception {
		Location loc = new Location();

		Optional<URL> sourceUrl = SourceLinks.source(project, field.fqName);
		if (sourceUrl.isPresent()) {

			URI sourceUri = sourceUrl.get().toURI();

			URI docUri = javaDocumentUriProvider.docUri(project, field.fqName);
			loc.setUri(docUri.toString());

			Range r = cuCache.withCompilationUnit(project, sourceUri, (cu) -> {
				try {
					AtomicReference<Range> range = new AtomicReference<>(null);
					TextDocument doc = new TextDocument(sourceUrl.get().toString(), LanguageId.JAVA);
					doc.setText(cuCache.fetchContent(sourceUri));
					cu.accept(new ASTVisitor() {

						boolean foundType = false;

						@Override
						public boolean visit(EnumConstantDeclaration node) {
							if (foundType) {
								SimpleName nameNode = node.getName();
								if (nameNode.getIdentifier().equals(field.fieldName)) {
									try {
										range.set(doc.toRange(nameNode.getStartPosition(), nameNode.getLength()));
									} catch (BadLocationException e) {
										throw new IllegalStateException(e);
									}
								}
							}
							return true;
						}

						@Override
						public boolean visit(EnumDeclaration node) {
							if (node.getName().getIdentifier()
									.equals(field.fqName.substring(field.fqName.lastIndexOf('.') + 1))) {
								foundType = true;
								return true;
							}
							return false;
						}

					});
					return range.get();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			});
			if (r == null) {
				throw new IllegalStateException("Couldn't find " + field);
			}
			loc.setRange(r);
		}
		return loc;
	}

}
