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

import java.util.Optional;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.springframework.ide.vscode.commons.languageserver.definition.SimpleDefinitionFinder;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.yaml.snakeyaml.nodes.Node;

import reactor.core.publisher.Flux;

public class ConcourseDefinitionFinder extends SimpleDefinitionFinder<ConcourseLanguageServer> {

	private final ConcourseModel models;
	private final PipelineYmlSchema schema;
	private ASTTypeCache astTypes;

	public ConcourseDefinitionFinder(ConcourseLanguageServer server, ConcourseModel models, PipelineYmlSchema schema) {
		super(server);
		this.models = models;
		this.schema = schema;
		this.astTypes = models.getAstTypeCache();
		astTypes.addInterestingType(schema.t_resource_name);
	}

	@Override
	protected Flux<Location> findDefinitions(TextDocumentPositionParams params) {
		try {
			TextDocument doc = server.getTextDocumentService().get(params);
			if (doc!=null) {
				YamlFileAST ast = models.getSafeAst(doc, false);
				Node refNode = ast.findNode(doc.toOffset(params.getPosition()));
				if (refNode!=null) {
					YType type = astTypes.getType(ast, refNode);
					if (schema.t_resource_name==type) {
						String name = NodeUtil.asScalar(refNode);
						return Flux.fromStream(models.getResourceDefinitionNodes(ast, name))
								.map((node) -> toLocation(doc, node))
								.filter(Optional::isPresent)
								.map(Optional::get);
					}
				}
			}
		} catch (Exception e) {
			return Flux.error(e);
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
