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
package org.springframework.ide.vscode.bosh.models;

import java.util.Collection;
import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlTraversal;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

public class BoshCommandStemcellsProvider extends BoshCommandBasedModelProvider<StemcellsModel> {

	YamlTraversal STEMCELL_NAMES = YamlPath.EMPTY
			.thenValAt("Tables")
			.thenAnyChild()
			.thenValAt("Rows")
			.thenAnyChild()
			.thenValAt("name");

	@Override
	public StemcellsModel getModel(DynamicSchemaContext dc) throws Exception {
		JSONCursor cursor = new JSONCursor(getJsonTree());
		return new StemcellsModel() {

			@Override
			public Collection<String> getStemcellNames() {
				return getNames(STEMCELL_NAMES);
			}

			private Collection<String> getNames(YamlTraversal path) {
				return path.traverseAmbiguously(cursor)
				.flatMap((cursor) -> {
					String text = cursor.target.asText();
					if (StringUtil.hasText(text)) {
						return Stream.of(text);
					} else {
						return Stream.empty();
					}
				})
				.collect(CollectorUtil.toImmutableSet());
			}
		};
	}

	@Override
	protected ExternalCommand getCommand() {
		return new ExternalCommand("bosh", "stemcells", "--json");
	}

}
