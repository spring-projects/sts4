/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.symbols;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentSymbolHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaDocumentSymbolHandler implements DocumentSymbolHandler {

	private SimpleLanguageServer server;
	private JavaProjectFinder projectFinder;

	public BootJavaDocumentSymbolHandler(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		this.server = server;
		this.projectFinder = projectFinder;
	}

	@Override
	public List<? extends SymbolInformation> handle(DocumentSymbolParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		TextDocument doc = documents.get(params.getTextDocument().getUri()).copy();
		if (doc != null) {
			try {
				return provideDocumentSymbols(doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return SimpleTextDocumentService.NO_SYMBOLS;
	}

	private List<? extends SymbolInformation> provideDocumentSymbols(TextDocument document) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);

		String[] classpathEntries = getClasspathEntries(document);
		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);

		String docURI = document.getUri();
		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		parser.setUnitName(unitName);
		parser.setSource(document.get(0, document.getLength()).toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		if (cu != null) {
			System.out.println("AST node found: " + cu.getClass().getName());
			return provideDocumentSymbolsForAnnotations(cu, document);
		}

		return null;
	}

	private List<? extends SymbolInformation> provideDocumentSymbolsForAnnotations(ASTNode node, TextDocument doc) {
		List<SymbolInformation> result = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(AnnotationTypeDeclaration node) {
				// TODO Auto-generated method stub
				return super.visit(node);
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				ITypeBinding type = node.resolveTypeBinding();
				if (type != null) {
					String qualifiedName = type.getQualifiedName();
					if (qualifiedName != null && qualifiedName.startsWith("org.springframework")) {
						provideDocumentSymbolsForSpringAnnotations(node, type, doc, result);
					}
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(AnnotationTypeMemberDeclaration node) {
				// TODO Auto-generated method stub
				return super.visit(node);
			}

			@Override
			public boolean visit(MemberValuePair node) {
				// TODO Auto-generated method stub
				return super.visit(node);
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				ITypeBinding type = node.resolveTypeBinding();
				if (type != null) {
					String qualifiedName = type.getQualifiedName();
					if (qualifiedName != null && qualifiedName.startsWith("org.springframework")) {
						provideDocumentSymbolsForSpringAnnotations(node, type, doc, result);
					}
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				ITypeBinding type = node.resolveTypeBinding();
				if (type != null) {
					String qualifiedName = type.getQualifiedName();
					if (qualifiedName != null && qualifiedName.startsWith("org.springframework")) {
						provideDocumentSymbolsForSpringAnnotations(node, type, doc, result);
					}
				}
				return super.visit(node);
			}
		};
		node.accept(visitor);

		return result;
	}

	private void provideDocumentSymbolsForSpringAnnotations(Annotation node, ITypeBinding type,
			TextDocument doc, List<SymbolInformation> resultAccumulator) {
		try {
			resultAccumulator.add(new SymbolInformation(node.toString(), SymbolKind.Interface, new Location(doc.getUri(),
					doc.toRange(node.getStartPosition(), node.getLength()))));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String[] getClasspathEntries(IDocument doc) throws Exception {
		IJavaProject project = this.projectFinder.find(doc);
		IClasspath classpath = project.getClasspath();
		Stream<Path> classpathEntries = classpath.getClasspathEntries();
		return classpathEntries
				.filter(path -> path.toFile().exists())
				.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new);
	}

}
