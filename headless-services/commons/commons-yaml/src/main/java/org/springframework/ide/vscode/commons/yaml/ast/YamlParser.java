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

import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;

import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.util.AnchorTrackingComposer;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javolution.io.CharSequenceReader;

public class YamlParser implements YamlASTProvider {

	public YamlParser() {
	}

	@Override
	public YamlFileAST getAST(IDocument doc) throws Exception {
		CharSequenceReader reader = new CharSequenceReader();
		reader.setInput(doc.get());
		ImmutableSet.Builder<Node> anchoredNodes = ImmutableSet.builder();
		ImmutableList<Node> nodes = composeAll(reader, (a, n) -> anchoredNodes.add(n));
		return new YamlFileAST(doc, nodes, anchoredNodes.build());
	}

	private ImmutableList<Node> composeAll(Reader yaml, BiConsumer<String, Node> anchorListener) {
		Resolver resolver = new Resolver();
		AnchorTrackingComposer composer = new AnchorTrackingComposer(new ParserImpl(new StreamReader(yaml)), resolver, anchorListener);
		ImmutableList.Builder<Node> nodes = ImmutableList.builder();
		while (composer.checkNode()) {
			nodes.add(composer.getNode());
		}
		return nodes.build();
	}

	private static class NodeIterable implements Iterable<Node> {
		private Iterator<Node> iterator;

		public NodeIterable(Iterator<Node> iterator) {
			this.iterator = iterator;
		}

		@Override
		public Iterator<Node> iterator() {
			return iterator;
		}
	}


}
