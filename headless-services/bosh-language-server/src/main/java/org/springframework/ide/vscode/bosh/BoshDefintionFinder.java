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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.springframework.ide.vscode.commons.languageserver.definition.SimpleDefinitionFinder;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.ast.YamlAstCache;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.reconcile.ASTTypeCache;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.yaml.snakeyaml.nodes.Node;

import reactor.core.publisher.Flux;

public class BoshDefintionFinder extends SimpleDefinitionFinder<BoshLanguageServer> {

	//TODO: lots of common code between BoshDefintionFinder and ConcourseDefinitionFinder.
	// should be possible to pull up into a common super class.

	private final YamlAstCache asts;
	private final ASTTypeCache astTypes;
	private final BoshDeploymentManifestSchema schema;

	private Map<YType, Handler> handlers = new HashMap<>();

	@FunctionalInterface
	private interface Handler {
		Flux<Location> handle(Node refNode, TextDocument doc, YamlFileAST ast);
	}

	public BoshDefintionFinder(BoshLanguageServer server, BoshDeploymentManifestSchema schema, YamlAstCache asts, ASTTypeCache astTypes) {
		super(server);
		this.schema = schema;
		this.asts = asts;
		this.astTypes = astTypes;
		for (Pair<YType, YType> defAndRef : schema.getDefAndRefTypes()) {
			YType def = defAndRef.getLeft();
			if (def!=null) {
				YType ref = defAndRef.getRight();
				if (ref!=null) {
					findByType(def, ref);
				}
			}
		}
	}

	@Override
	protected Flux<Location> findDefinitions(TextDocumentPositionParams params) {
		try {
			TextDocument doc = server.getTextDocumentService().get(params);
			if (doc!=null) {
				YamlFileAST ast = asts.getSafeAst(doc, false);
				if (ast!=null) {
					Node refNode = ast.findNode(doc.toOffset(params.getPosition()));
					if (refNode!=null) {
						YType type = astTypes.getType(ast, refNode);
						if (type!=null) {
							Handler handler = handlers.get(type);
							if (handler!=null) {
								return handler.handle(refNode, doc, ast);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Flux.empty();
	}

	/**
	 * Add a handler that finds the definitions for a target node within the same document
	 * by retrieving nodes of a given type as candidates.
	 */
	protected void findByType(YType def, YType ref) {
		astTypes.addInterestingType(def);
		astTypes.addInterestingType(ref);
		Handler handler = (Node refNode, TextDocument doc, YamlFileAST ast) -> {
			String uri = doc.getUri();
			if (uri!=null) {
				String name = NodeUtil.asScalar(refNode);
				if (name!=null) {
					Collection<Node> candidates = astTypes.getNodes(uri, def);
					return Flux.fromIterable(candidates)
							.filter((node) -> name.equals(NodeUtil.asScalar(node)))
							.map((node) -> toLocation(doc, node))
							.filter(Optional::isPresent)
							.map(Optional::get);
				}
			}
			return Flux.empty();
		};
		handlers.put(ref, handler);
	}

	protected Optional<Location> toLocation(TextDocument doc, Node node) {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		try {
			return Optional.of(new Location(doc.getUri(), doc.toRange(start, end-start)));
		} catch (BadLocationException e) {
			Log.log(e);
			return Optional.empty();
		}
	}

}
