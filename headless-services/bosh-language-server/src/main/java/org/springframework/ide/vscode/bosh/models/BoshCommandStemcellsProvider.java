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

	private static final YamlTraversal STEMCELLS = YamlPath.EMPTY
			.thenValAt("Tables")
			.thenAnyChild()
			.thenValAt("Rows")
			.thenAnyChild();

	private static final YamlTraversal STEMCELL_NAMES = STEMCELLS
			.thenValAt("name");

	private static final YamlTraversal STEMCELL_OSS = STEMCELLS
			.thenValAt("os");

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

			@Override
			public Collection<StemcellData> getStemcells() {
				return STEMCELLS.traverseAmbiguously(cursor)
				.map(c -> new StemcellData(
						getStringProperty(c, "name"),
						getStringProperty(c, "version"),
						getStringProperty(c, "os")
				))
				.collect(CollectorUtil.toImmutableList());
			}

			private String getStringProperty(JSONCursor c, String prop) {
				c = YamlPath.EMPTY.thenValAt(prop).traverse(c);
				if (c!=null) {
					return c.target.asText();
				}
				return null;
			}

			@Override
			public Collection<String> getStemcellOss() {
				return getNames(STEMCELL_OSS);
			}
		};
	}

	@Override
	protected ExternalCommand getCommand() {
		return new ExternalCommand("bosh", "stemcells", "--json");
	}

}
