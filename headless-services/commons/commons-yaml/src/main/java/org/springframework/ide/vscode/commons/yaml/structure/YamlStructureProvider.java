/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.structure;

import org.springframework.ide.vscode.commons.yaml.path.KeyAliases;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SRootNode;

/**
 * @author Kris De Volder
 */
public abstract class YamlStructureProvider {

	public abstract SRootNode getStructure(YamlDocument doc) throws Exception;

	public static final YamlStructureProvider withAliases(final KeyAliases keyAliases) {
		//TODO: its kind of fishy that we need this method. This is injecting some behavior
		// related to 'alias aware' traversing of the parse tree. But this behavior probably
		// doesn't belong in the parse tree but in the 'traverser'.
		//
		// So we should find a way to get rid of this method and move 'alias awareness'
		// elsewhere.
		//
		// For now, however it was the easiest way to make the parser reusable without
		// breaking Application.yml support.
		return new YamlStructureProvider() {
			public SRootNode getStructure(YamlDocument doc) throws Exception {
				return new YamlStructureParser(doc, keyAliases).parse();
			}
		};
	}

	public static final YamlStructureProvider DEFAULT = new YamlStructureProvider() {
		public SRootNode getStructure(YamlDocument doc) throws Exception {
			return new YamlStructureParser(doc, KeyAliases.NONE).parse();
		}
	};

}
