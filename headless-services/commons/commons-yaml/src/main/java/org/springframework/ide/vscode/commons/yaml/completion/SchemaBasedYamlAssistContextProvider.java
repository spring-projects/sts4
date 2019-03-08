/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.completion;

import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;

/**
 * A {@link YamlAssistContextProvider} that creates {@link YamlAssistContext}s from {@link YamlSchema}
 *
 * @author Kris De Volder
 */
public class SchemaBasedYamlAssistContextProvider implements YamlAssistContextProvider {

	private YamlSchema schema;

	public SchemaBasedYamlAssistContextProvider(YamlSchema schema) {
		this.schema = schema;
	}

	@Override
	public YamlAssistContext getGlobalAssistContext(YamlDocument doc) {
		return new TopLevelAssistContext() {
			@Override
			protected YamlAssistContext getDocumentContext(int documentSelector) {
				return new YTypeAssistContext(this, documentSelector, schema.getTopLevelType(), schema.getTypeUtil());
			}

			@Override
			public YamlDocument getDocument() {
				return doc;
			}
		};
	}
}
