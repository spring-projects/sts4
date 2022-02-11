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

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Range;
import org.openrewrite.Cursor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.EnumValue;
import org.openrewrite.java.tree.J.Identifier;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.JavaType.Method;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.ide.vscode.boot.java.links.JavaDocumentUriProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.boot.java.utils.ORCompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.languageserver.testharness.Editor;

import com.google.common.collect.ImmutableSet;

public class DefinitionLinkAsserts {

	private JavaDocumentUriProvider javaDocumentUriProvider;
	private ORCompilationUnitCache cuCache;

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

	public DefinitionLinkAsserts(JavaDocumentUriProvider javaDocumentUriProvider, ORCompilationUnitCache cuCache) {
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
					
					new JavaIsoVisitor<AtomicReference<Range>>() {
						@Override
						public ClassDeclaration visitClassDeclaration(ClassDeclaration classDecl, AtomicReference<Range> p) {
							if (typeName.equals(classDecl.getSimpleName())) {
								try {
									org.openrewrite.marker.Range cuRange = ORAstUtils.getRange(classDecl.getName());
									p.set(doc.toRange(cuRange.getStart().getOffset(), cuRange.length()));
									return classDecl;
								} catch (BadLocationException e) {
									throw new IllegalStateException(e);
								}
							}
							return super.visitClassDeclaration(classDecl, p);
						}
					}.visitNonNull(cu, range);
					
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
					
					new JavaIsoVisitor<AtomicReference<Range>>() {
						@Override
						public MethodDeclaration visitMethodDeclaration(MethodDeclaration m,
								AtomicReference<Range> p) {
							if (m.getSimpleName().equals(method.methodName) && m.getParameters().size() == method.params.length) {
								Method methodType = m.getMethodType();
								if (methodType != null) {
									int i = 0;
									for (JavaType paramType : methodType.getParameterTypes()) {
										FullyQualified fqType = TypeUtils.asFullyQualified(paramType);
										if (fqType == null) {
											if (!method.params[i++].equals(paramType.toString())) {
												return m;
											}
										} else {
											if (!fqType.getFullyQualifiedName().equals(method.params[i++])) {
												return m;
											} 
										}
									}
									try {
										org.openrewrite.marker.Range cuRange = ORAstUtils.getRange(m.getName());
										p.set(doc.toRange(cuRange.getStart().getOffset(), cuRange.length()));
									} catch (BadLocationException e) {
										throw new IllegalStateException(e);
									}
								}
							}
							return m;
						}
					}.visitNonNull(cu, range);
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
					
					String enumName = field.fqName.substring(field.fqName.lastIndexOf('.') + 1);
					new JavaIsoVisitor<AtomicReference<Range>>() {
						@Override
						public EnumValue visitEnumValue(EnumValue _enum, AtomicReference<Range> p) {
							Cursor delcaringTypeCursor = getCursor().dropParentUntil(ClassDeclaration.class::isInstance);
							if (delcaringTypeCursor != null) {
								ClassDeclaration declaringType = delcaringTypeCursor.getValue();
								if (enumName.equals(declaringType.getSimpleName())) {
									Identifier nameNode = _enum.getName();
									if (field.fieldName.equals(nameNode.printTrimmed())) {
										try {
											org.openrewrite.marker.Range nameNodeRange = ORAstUtils.getRange(nameNode);
											p.set(doc.toRange(nameNodeRange.getStart().getOffset(), nameNodeRange.length()));
										} catch (BadLocationException e) {
											throw new IllegalStateException(e);
										}
									}
								}
							}
							// TODO Auto-generated method stub
							return super.visitEnumValue(_enum, p);
						}
					}.visitNonNull(cu, range);
					
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
