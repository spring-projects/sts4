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
package org.springframework.ide.vscode.bosh;

import org.junit.Test;
import org.springframework.ide.vscode.bosh.snippets.SchemaBasedSnippetGenerator;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.yaml.ast.YamlAstCache;
import org.springframework.ide.vscode.commons.yaml.reconcile.ASTTypeCache;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;

public class SchemaBasedSnippetGeneratorTest {

	private ASTTypeCache astTypes = new ASTTypeCache();
	private YamlAstCache asts = new YamlAstCache();
	private BoshDeploymentManifestSchema schema = new BoshDeploymentManifestSchema(asts, astTypes, (dc) -> null, (dc) -> null, (dc) -> null);
	private YTypeUtil typeUtil = schema.getTypeUtil();
	private SchemaBasedSnippetGenerator generator = new SchemaBasedSnippetGenerator(typeUtil, SnippetBuilder::new);

	@Test
	public void toplevelSnippet() throws Exception {
		YType v2Schema = typeUtil.inferMoreSpecificType(schema.getTopLevelType(), DynamicSchemaContext.NULL);
		System.out.println(generator.getSnippets(v2Schema).iterator().next());
	}

}
