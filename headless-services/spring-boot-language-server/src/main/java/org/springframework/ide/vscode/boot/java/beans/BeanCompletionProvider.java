/*******************************************************************************
 * Copyright (c) 2017, 2025 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Udayani V
 */
public class BeanCompletionProvider implements CompletionProvider {

	private static final Logger log = LoggerFactory.getLogger(BeanCompletionProvider.class);

	private final JavaProjectFinder javaProjectFinder;
	private final SpringMetamodelIndex springIndex;
	private final RewriteRefactorings rewriteRefactorings;

	public BeanCompletionProvider(JavaProjectFinder javaProjectFinder, SpringMetamodelIndex springIndex,
			RewriteRefactorings rewriteRefactorings) {
		this.javaProjectFinder = javaProjectFinder;
		this.springIndex = springIndex;
		this.rewriteRefactorings = rewriteRefactorings;
	}

	@Override
	public void provideCompletions(ASTNode node, int offset, TextDocument doc,
			Collection<ICompletionProposal> completions) {
		if (node instanceof SimpleName) {
			try {
				// Don't look at anything inside Annotation or VariableDelcaration node
				for (ASTNode n = node; n != null; n = n.getParent()) {
					if (n instanceof Annotation
							|| n instanceof VariableDeclaration) {
						return;
					}
				}
				
				Optional<IJavaProject> optionalProject = this.javaProjectFinder.find(doc.getId());
				if (optionalProject.isEmpty()) {
					return;
				}
	
				IJavaProject project = optionalProject.get();
				TypeDeclaration topLevelClass = findParentClass(node);
		        if (topLevelClass == null) {
		            return;
		        }
		        
				if (isSpringComponent(topLevelClass)) {
		            String className = getFullyQualifiedName(topLevelClass);
					Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
					for (Bean bean : beans) {
						DocumentEdits edits = new DocumentEdits(doc, false);
						edits.replace(offset - node.toString().length(), offset, bean.getName());

						BeanCompletionProposal proposal = new BeanCompletionProposal(edits, doc, bean.getName(),
								bean.getType(), className, rewriteRefactorings);

						completions.add(proposal);
					}
				}
			} catch (Exception e) {
				log.error("problem while looking for bean completions", e);
			}
		}
	}
	
	private static boolean isSpringComponent(TypeDeclaration node) {	
	    for (IAnnotationBinding annotation : node.resolveBinding().getAnnotations()) {
	        if (isSpringComponentAnnotation(annotation)) {
	            return true;
	        }
	    }
	    return false;
	}

	private static boolean isSpringComponentAnnotation(IAnnotationBinding annotation) {
	    String annotationName = annotation.getAnnotationType().getQualifiedName();
	    if (annotationName.equals("org.springframework.stereotype.Component")) {
	        return true;
	    }
	    for (IAnnotationBinding metaAnnotation : annotation.getAnnotationType().getAnnotations()) {
	        if (metaAnnotation.getAnnotationType().getQualifiedName().equals("org.springframework.stereotype.Component")) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private static TypeDeclaration findParentClass(ASTNode node) {
		ASTNode current = node;
		while (current != null) {
	        if (current instanceof TypeDeclaration) {
	            return (TypeDeclaration) current;
	        }
	        current = current.getParent();
	    }
	    return null;
	}
	
	private static String getFullyQualifiedName(TypeDeclaration typeDecl) {
		if (typeDecl.resolveBinding() != null) {
			String qualifiedName = typeDecl.resolveBinding().getQualifiedName();
	        return qualifiedName.replaceAll("\\.(?=[^\\.]+$)", "\\$");
	    }
	    CompilationUnit cu = (CompilationUnit) typeDecl.getRoot();
	    String packageName = cu.getPackage() != null ? cu.getPackage().getName().getFullyQualifiedName() : "";
	    String typeName = typeDecl.getName().getFullyQualifiedName();
	    return packageName.isEmpty() ? typeName : packageName + "." + typeName;
	}

}
