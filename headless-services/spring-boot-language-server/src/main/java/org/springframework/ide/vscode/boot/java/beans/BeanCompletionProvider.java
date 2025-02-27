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

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.FuzzyMatcher;
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
		if (node instanceof SimpleName || node instanceof Block || node instanceof FieldAccess) {
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
		        
		        String prefix = "";
		        
	        	// Empty SimpleName usually comes from unresolved FieldAccess, i.e. `this.owner` where `owner` field is not defined
		        if (node instanceof SimpleName sn) {
		        	if (sn.getLength() == 0
		        			&& sn.getParent() instanceof Assignment assign 
			        		&& assign.getLeftHandSide() instanceof FieldAccess fa
			        		&& fa.getExpression() instanceof ThisExpression) {
		        		prefix = fa.getName().toString();
		        	} else {
		        		prefix = sn.toString();
		        	}
		        } else if (node instanceof FieldAccess fa && fa.getExpression() instanceof ThisExpression) {
		        	int start = fa.getExpression().getStartPosition() + fa.getExpression().getLength();
		        	while (start < doc.getLength() && doc.getChar(start) != '.') {
		        		start++;
		        	}
		        	prefix = doc.get(start + 1, offset - start - 1);
		        }
		        
				if (AnnotationHierarchies.get(node).isAnnotatedWith(topLevelClass.resolveBinding(), Annotations.COMPONENT)) {
		            String className = getFullyQualifiedName(topLevelClass);
					Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
					ITypeBinding topLevelBeanType = topLevelClass.resolveBinding();
					Set<String> declaredFiledsTypes = Arrays.stream(topLevelBeanType.getDeclaredFields())
							.map(vd -> vd.getType())
							.filter(Objects::nonNull)
							.map(t -> t.getQualifiedName())
							.collect(Collectors.toSet());
					for (Bean bean : beans) {
						// If current class is a bean - ignore it
						if (className.equals(bean.getType())) {
							continue;
						}
						// Filter out beans already injected into this class
						if (declaredFiledsTypes.contains(bean.getType())) {
							continue;
						}
						double score = FuzzyMatcher.matchScore(prefix, bean.getName());
						if (score > 0) {
							DocumentEdits edits = new DocumentEdits(doc, false);
							if (node instanceof Block) {
								edits.insert(offset, bean.getName());
							} else {
								edits.replace(offset - prefix.length(), offset, bean.getName());
							}

							BeanCompletionProposal proposal = new BeanCompletionProposal(edits, doc, bean.getName(),
									bean.getType(), className, score, rewriteRefactorings);

							completions.add(proposal);
						}
					}
				}
			} catch (Exception e) {
				log.error("problem while looking for bean completions", e);
			}
		}
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
		ITypeBinding binding = typeDecl.resolveBinding();
		if (binding != null) {
			String qualifiedName = binding.getQualifiedName();
	        return qualifiedName/*.replaceAll("\\.(?=[^\\.]+$)", "\\$")*/;
	    }
	    CompilationUnit cu = (CompilationUnit) typeDecl.getRoot();
	    String packageName = cu.getPackage() != null ? cu.getPackage().getName().getFullyQualifiedName() : "";
	    String typeName = typeDecl.getName().getFullyQualifiedName();
	    return packageName.isEmpty() ? typeName : packageName + "." + typeName;
	}

}
