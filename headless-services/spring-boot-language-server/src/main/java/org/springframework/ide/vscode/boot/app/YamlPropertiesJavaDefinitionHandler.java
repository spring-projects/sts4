/*******************************************************************************
 * Copyright (c) 2018, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.vscode.commons.languageserver.util.DefinitionHandler;
import org.springframework.ide.vscode.commons.languageserver.util.LanguageSpecific;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.NodeRef;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContext;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import com.google.common.collect.ImmutableList;

@Component
public class YamlPropertiesJavaDefinitionHandler implements DefinitionHandler, LanguageSpecific {

	private static final Logger log = LoggerFactory.getLogger(YamlPropertiesJavaDefinitionHandler.class);

	@Autowired
	private SimpleTextDocumentService documents;

	@Autowired
	private YamlASTProvider astProvider;

	@Autowired
	private YamlStructureProvider structureProvider;

	@Autowired
	private YamlAssistContextProvider assistContextProvider;

	@Override
	public Collection<LanguageId> supportedLanguages() {
		return ImmutableList.of(LanguageId.BOOT_PROPERTIES_YAML);
	}

	@Override
	public List<LocationLink> handle(DefinitionParams definitionParams) {
		try {
			TextDocument doc = documents.get(definitionParams);
			int offset = doc.toOffset(definitionParams.getPosition());
			YamlFileAST ast = getAst(doc);
			if (ast != null) {
				YamlDocument ymlDoc = new YamlDocument(doc, structureProvider);
				YamlAssistContext assistContext = assistContextProvider.getGlobalAssistContext(ymlDoc);
				if (assistContext != null) {
					List<NodeRef<?>> astPath = ast.findPath(offset);
					final YamlPath path = YamlPath.fromASTPath(astPath);
					if (path != null) {
						YamlPath assistPath = path;
						if (assistPath.pointsAtKey()) {
							// When a path points at a key we must tramsform it to a
							// 'value-terminating path'
							// to be able to reuse the 'getHoverInfo' method on
							// YamlAssistContext (as navigation
							// into 'key' is not defined for YamlAssistContext.
							String key = path.getLastSegment().toPropString();
							assistPath = path.dropLast().append(YamlPathSegment.valueAt(key));
						}
						log.debug("handleYamlDefinition at {}", assistPath.toPropString());
						assistContext = assistPath.traverse(assistContext);
						log.debug("assistContext = {}", assistContext);

						if (assistContext != null) {
							Node node = astPath.get(astPath.size() - 1).get();
							int start = node.getStartMark().getIndex();
							int end = node.getEndMark().getIndex();
							Range originalRange = doc.toRange(start, end - start);
							if (path.pointsAtValue()) {
								log.debug("pathPointsAtValue = true");
								DocumentRegion nodeRegion = getNodeRegion(ast, offset);
								if (nodeRegion != null) {
									return assistContext.getDefinitionsForPropertyValue(nodeRegion).stream()
											.map(l -> new LocationLink(l.getUri(), l.getRange(), l.getRange(), originalRange))
											.collect(Collectors.toList());
								}
							} else {
								log.debug("pathPointsAtValue = false");
								List<Location> defs = assistContext.getDefinitionsForPropertyKey();
								log.debug("definitions = {}", defs);
								return defs.stream()
										.map(l -> new LocationLink(l.getUri(), l.getRange(), l.getRange(), originalRange))
										.collect(Collectors.toList());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return ImmutableList.of();
	}

	private DocumentRegion getNodeRegion(YamlFileAST ast, int offset) {
		if (ast != null) {
			Node n = ast.findNode(offset);
			if (n != null && n.getNodeId() == NodeId.scalar) {
				int start = n.getStartMark().getIndex();
				int end = n.getEndMark().getIndex();
				return new DocumentRegion(ast.getDocument(), start, end);
			}
		}
		return null;
	}


	private YamlFileAST getAst(IDocument doc) throws Exception {
		try {
			return astProvider.getAST(doc);
		} catch (ParserException | ScannerException e) {
			// ignore, the user just typed some crap
		}
		return null;
	}


}
