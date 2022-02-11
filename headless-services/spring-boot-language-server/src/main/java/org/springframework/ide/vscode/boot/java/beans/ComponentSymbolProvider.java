/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public class ComponentSymbolProvider extends AbstractSymbolProvider {

	@Override
	protected void addSymbolsPass1(Annotation node, FullyQualified annotationType, Collection<FullyQualified> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		try {
			EnhancedSymbolInformation enhancedSymbol = createSymbol(node, annotationType, metaAnnotations, doc);
			context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));
		}
		catch (Exception e) {
			Log.log(e);
		}
	}

	protected EnhancedSymbolInformation createSymbol(Annotation node, FullyQualified annotationType, Collection<FullyQualified> metaAnnotations, TextDocument doc) throws BadLocationException {
		String annotationTypeName = annotationType.getFullyQualifiedName();
		Collection<String> metaAnnotationNames = metaAnnotations.stream()
				.map(FullyQualified::getFullyQualifiedName)
				.collect(Collectors.toList());
		String beanName = getBeanName(node);
		String beanType = getBeanType(node);

		Range r = ORAstUtils.getRange(node);
		SymbolInformation symbol = new SymbolInformation(
				beanLabel("+", annotationTypeName, metaAnnotationNames, beanName, beanType), SymbolKind.Interface,
				new Location(doc.getUri(), doc.toRange(r.getStart().getOffset(), r.length())));

		SymbolAddOnInformation[] addon = new SymbolAddOnInformation[] {new BeansSymbolAddOnInformation(beanName)};

		return new EnhancedSymbolInformation(symbol, addon);
	}

	protected String beanLabel(String searchPrefix, String annotationTypeName, Collection<String> metaAnnotationNames, String beanName, String beanType) {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append("@");
		symbolLabel.append(searchPrefix);
		symbolLabel.append(' ');
		symbolLabel.append('\'');
		symbolLabel.append(beanName);
		symbolLabel.append('\'');
		symbolLabel.append(" (@");
		symbolLabel.append(annotationTypeName);
		if (!metaAnnotationNames.isEmpty()) {
			symbolLabel.append(" <: ");
			boolean first = true;
			for (String ma : metaAnnotationNames) {
				if (!first) {
					symbolLabel.append(", ");
				}
				symbolLabel.append("@");
				symbolLabel.append(ma);
				first = false;
			}
		}
		symbolLabel.append(") ");
		symbolLabel.append(beanType);
		return symbolLabel.toString();
	}

	private String getBeanName(Annotation node) {
		J parent = ORAstUtils.getParent(node);
		if (parent instanceof ClassDeclaration) {
			ClassDeclaration type = (ClassDeclaration) parent;

			String beanName = type.getSimpleName();
			return BeanUtils.getBeanNameFromType(beanName);
		}
		return null;
	}

	private String getBeanType(Annotation node) {
		J parent = ORAstUtils.getParent(node);
		if (parent instanceof ClassDeclaration) {
			ClassDeclaration type = (ClassDeclaration) parent;
			String returnType = type.getType().getFullyQualifiedName();
			return returnType;
		}
		return null;
	}

}
