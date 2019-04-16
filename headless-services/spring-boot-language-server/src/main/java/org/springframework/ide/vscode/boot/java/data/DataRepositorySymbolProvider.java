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
package org.springframework.ide.vscode.boot.java.data;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.beans.BeanUtils;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

/**
 * @author Martin Lippert
 */
public class DataRepositorySymbolProvider extends AbstractSymbolProvider {

	private static final Logger log = LoggerFactory.getLogger(DataRepositorySymbolProvider.class);

	@Override
	protected void addSymbolsPass1(TypeDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
		// this checks spring data repository beans that are defined as extensions of the repository interface
		Tuple4<String, String, String, DocumentRegion> repositoryBean = getRepositoryBean(typeDeclaration, doc);
		if (repositoryBean != null) {
			try {
				SymbolInformation symbol = new SymbolInformation(
						beanLabel(true, repositoryBean.getT1(), repositoryBean.getT2(), repositoryBean.getT3()),
						SymbolKind.Interface,
						new Location(doc.getUri(), doc.toRange(repositoryBean.getT4())));

				SymbolAddOnInformation[] addon = new SymbolAddOnInformation[] {new BeansSymbolAddOnInformation(repositoryBean.getT1())};
				EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(symbol, addon);

				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));
			} catch (BadLocationException e) {
				log.error("error creating data repository symbol for a specific range", e);
			}
		}
	}

	protected String beanLabel(boolean isFunctionBean, String beanName, String beanType, String markerString) {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append("@+");
		symbolLabel.append(' ');
		symbolLabel.append('\'');
		symbolLabel.append(beanName);
		symbolLabel.append('\'');

		markerString = markerString != null && markerString.length() > 0 ? " (" + markerString + ") " : " ";
		symbolLabel.append(markerString);

		symbolLabel.append(beanType);
		return symbolLabel.toString();
	}

	private static Tuple4<String, String, String, DocumentRegion> getRepositoryBean(TypeDeclaration typeDeclaration, TextDocument doc) {
		ITypeBinding resolvedType = typeDeclaration.resolveBinding();

		if (resolvedType != null) {
			return getRepositoryBean(typeDeclaration, doc, resolvedType);
		}
		else {
			return null;
		}
	}

	private static Tuple4<String, String, String, DocumentRegion> getRepositoryBean(TypeDeclaration typeDeclaration, TextDocument doc,
			ITypeBinding resolvedType) {

		ITypeBinding[] interfaces = resolvedType.getInterfaces();
		for (ITypeBinding resolvedInterface : interfaces) {
			String simplifiedType = null;
			if (resolvedInterface.isParameterizedType()) {
				simplifiedType = resolvedInterface.getBinaryName();
			}
			else {
				simplifiedType = resolvedType.getQualifiedName();
			}

			if (Constants.REPOSITORY_TYPE.equals(simplifiedType)) {
				String beanName = getBeanName(typeDeclaration);
				String beanType = resolvedInterface.getName();

				String domainType = null;
				if (resolvedInterface.isParameterizedType()) {
					ITypeBinding[] typeParameters = resolvedInterface.getTypeArguments();
					if (typeParameters != null && typeParameters.length > 0) {
						domainType = typeParameters[0].getName();
					}
				}
				DocumentRegion region = ASTUtils.nodeRegion(doc, typeDeclaration.getName());

				return Tuples.of(beanName, beanType, domainType, region);
			}
			else {
				Tuple4<String, String, String, DocumentRegion> result = getRepositoryBean(typeDeclaration, doc, resolvedInterface);
				if (result != null) {
					return result;
				}
			}
		}

		ITypeBinding superclass = resolvedType.getSuperclass();
		if (superclass != null) {
			return getRepositoryBean(typeDeclaration, doc, superclass);
		}
		else {
			return null;
		}
	}

	private static String getBeanName(TypeDeclaration typeDeclaration) {
		String beanName = typeDeclaration.getName().toString();
		return BeanUtils.getBeanNameFromType(beanName);
	}

}
