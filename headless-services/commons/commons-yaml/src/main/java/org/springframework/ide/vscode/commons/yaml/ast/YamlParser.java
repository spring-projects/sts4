/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.yaml.ast;

import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;

import javolution.io.CharSequenceReader;

public class YamlParser implements YamlASTProvider {

	public YamlParser() {
	}

	@Override
	public YamlFileAST getAST(IDocument doc) throws Exception {
		CharSequenceReader reader = new CharSequenceReader();
		reader.setInput(doc.get());
		Iterable<Node> nodes = new Yaml().composeAll(reader);
		return new YamlFileAST(doc, ImmutableList.copyOf(nodes));
	}

}
