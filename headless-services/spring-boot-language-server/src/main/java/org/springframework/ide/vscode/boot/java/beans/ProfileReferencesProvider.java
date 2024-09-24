/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.handlers.ReferenceProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;

/**
 * @author Martin Lippert
 */
public class ProfileReferencesProvider implements ReferenceProvider {

	private final SpringMetamodelIndex springIndex;
	private final SpringSymbolIndex symbolIndex;

	public ProfileReferencesProvider(SpringMetamodelIndex springIndex, SpringSymbolIndex symbolIndex) {
		this.springIndex = springIndex;
		this.symbolIndex = symbolIndex;
	}

	@Override
	public List<? extends Location> provideReferences(CancelChecker cancelToken, IJavaProject project, ASTNode node, Annotation annotation, ITypeBinding type, int offset) {

		cancelToken.checkCanceled();

		try {
			// case: @Value("prefix<*>")
			if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideReferences(project, ((StringLiteral) node).getLiteralValue());
				}
			}
			// case: @Value(value="prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideReferences(project, ((StringLiteral) node).getLiteralValue());
				}
			}
			// case: @Qualifier({"prefix<*>"})
			else if (node instanceof StringLiteral && node.getParent() instanceof ArrayInitializer) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideReferences(project, ((StringLiteral) node).getLiteralValue());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private List<? extends Location> provideReferences(IJavaProject project, String value) {
		Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
		
		// beans with name
		Stream<Location> beanLocations = Arrays.stream(beans)
				.filter(bean -> bean.getName().equals(value))
				.map(bean -> bean.getLocation());
		
		String profileSymbolsSearch = "@Profile(";
		List<WorkspaceSymbol> profileSymbols = symbolIndex.getAllSymbols(profileSymbolsSearch);
		
		String valuePhrase = "\"" + value + "\"";
		
		Stream<Location> qualifierLocations = profileSymbols.stream()
				.filter(symbol -> symbol.getName().contains(valuePhrase))
				.map(symbol -> symbol.getLocation())
				.filter(location -> location.isLeft())
				.map(location -> location.getLeft());
		
		return Stream.concat(qualifierLocations, beanLocations).toList();
	}

}
