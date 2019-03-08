/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.ide.vscode.bosh.models.BoshModels;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.yaml.reconcile.ASTTypeCache;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.snippet.SchemaBasedSnippetGenerator;

public class SchemaBasedSnippetGeneratorTest {

	private BoshDeploymentManifestSchema schema = new BoshSchemas(new BoshModels((dc) -> null, (dc) -> null, (dc) -> null, new ASTTypeCache())).getDeploymentSchema();
	private YTypeUtil typeUtil = schema.getTypeUtil();
	private SchemaBasedSnippetGenerator generator = new SchemaBasedSnippetGenerator(typeUtil, SnippetBuilder::new);

	@Test
	public void toplevelSnippet() throws Exception {
		YType v2Schema = typeUtil.inferMoreSpecificType(schema.getTopLevelType(), DynamicSchemaContext.NULL);
		assertEquals(
				"name: $1\n" +
				"releases:\n" +
				"- name: $2\n" +
				"  version: $3\n" +
				"stemcells:\n" +
				"- alias: $4\n" +
				"  version: $5\n" +
				"update:\n" +
				"  canaries: $6\n" +
				"  max_in_flight: $7\n" +
				"  canary_watch_time: $8\n" +
				"  update_watch_time: $9\n" +
				"instance_groups:\n" +
				"- name: $10\n" +
				"  azs:\n" +
				"  - $11\n" +
				"  instances: $12\n" +
				"  jobs:\n" +
				"  - name: $13\n" +
				"    release: $14\n" +
				"  vm_type: $15\n" +
				"  stemcell: $16\n" +
				"  networks:\n" +
				"  - name: $17"
				,
				generator.getSnippets(v2Schema).iterator().next().getSnippet()
		);
	}

}
