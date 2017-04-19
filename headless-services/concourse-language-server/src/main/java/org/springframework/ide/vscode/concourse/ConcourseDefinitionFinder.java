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
package org.springframework.ide.vscode.concourse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.springframework.ide.vscode.commons.languageserver.definition.SimpleDefinitionFinder;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.yaml.snakeyaml.nodes.Node;

import reactor.core.publisher.Flux;

public class ConcourseDefinitionFinder extends SimpleDefinitionFinder<ConcourseLanguageServer> {

	@FunctionalInterface
	private interface Handler {
		Flux<Location> handle(Node refNode, TextDocument doc, YamlFileAST ast);
	}

	private final ConcourseModel models;
	private ASTTypeCache astTypes;
	private Map<YType, Handler> handlers = new HashMap<>();

	public ConcourseDefinitionFinder(ConcourseLanguageServer server, ConcourseModel models, PipelineYmlSchema schema) {
		super(server);
		this.models = models;
		this.astTypes = models.getAstTypeCache();
		findByPath(schema.t_resource_name, ConcourseModel.RESOURCE_NAMES_PATH);
		findByPath(schema.t_job_name, ConcourseModel.JOB_NAMES_PATH);
		findByPath(schema.t_resource_type_name, ConcourseModel.RESOURCE_TYPE_NAMES_PATH);
	}

	/**
	 * Add a handler that finds the definitions for a target node within the same document
	 * by following a {@link YamlPath} to find candidate nodes.
	 *
	 * @param refType the type inferred by the reconciler for the target node.
	 * @param definitionsPath Path that points to all nodes within the same file corresponding
	 *            to definitions of nodes of the given type.
	 */
	private void findByPath(YType refType, YamlPath definitionsPath) {
		astTypes.addInterestingType(refType);
		Handler handler = (Node refNode, TextDocument doc, YamlFileAST ast) -> {
			String name = NodeUtil.asScalar(refNode);
			if (name!=null) {
				return Flux.fromStream(definitionsPath.traverseAmbiguously(ast))
						.filter((node) -> name.equals(NodeUtil.asScalar(node)))
						.map((node) -> toLocation(doc, node))
						.filter(Optional::isPresent)
						.map(Optional::get);
			}
			return Flux.empty();
		};
		handlers.put(refType, handler);
	}

	@Override
	protected Flux<Location> findDefinitions(TextDocumentPositionParams params) {
		try {
			TextDocument doc = server.getTextDocumentService().get(params);
			if (doc!=null) {
				YamlFileAST ast = models.getSafeAst(doc, false);
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

	Optional<Location> toLocation(TextDocument doc, Node node) {
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
