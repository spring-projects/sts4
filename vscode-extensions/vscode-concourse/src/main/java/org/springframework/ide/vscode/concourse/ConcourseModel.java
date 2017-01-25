/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.concourse;

import static org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment.anyChild;
import static org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment.valueAt;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocumentContentChange;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.path.ASTRootCursor;
import org.springframework.ide.vscode.commons.yaml.path.NodeCursor;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;
import org.springframework.ide.vscode.concourse.util.CollectorUtil;
import org.springframework.ide.vscode.concourse.util.StaleFallbackCache;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableMultiset.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;

import reactor.core.publisher.Flux;

/**
 * ConcourseModels is responsible for extracting various bits of information
 * out of .yml documents and caching them for use by various tools (reconcile engine
 * and completion engine).
 */
public class ConcourseModel {

	private static final YamlPath RESOURCE_NAMES_PATH = new YamlPath(
		valueAt("resources"),
		anyChild(),
		valueAt("name")
	);

	private static final YamlPath JOB_NAMES_PATH = new YamlPath(
		valueAt("jobs"),
		anyChild(),
		valueAt("name")
	);

	private static final YamlPath RESOURCE_TYPE_NAMES_PATH = new YamlPath(
			valueAt("resource_types"),
			anyChild(),
			valueAt("name")
	);

	private static final YamlPath RESOURCES_PATH = new YamlPath(
		anyChild(), // skip over the root node which contains multiple doces
		valueAt("resources"),
		anyChild()
	);

	private final YamlParser parser;
	private final StaleFallbackCache<String, YamlFileAST> asts = new StaleFallbackCache<>();

	private final ASTTypeCache astTypes = new ASTTypeCache();


	public ConcourseModel(SimpleTextDocumentService documents) {
		Yaml yaml = new Yaml();
		this.parser = new YamlParser(yaml);
		documents.onDidChangeContent(this::documentChanged);
	}

	private void documentChanged(TextDocumentContentChange changeEvent) {
		String uri = changeEvent.getDocument().getUri();
		if (uri!=null) {
			asts.invalidate(uri);
		}
	}

	/**
	 * Returns the resource names that are defined by given IDocument. If the contents
	 * of IDocument is not currently parseable then this may return stale information
	 * retained from a previous successful parse.
	 * <p>
	 * It may also return null if its not currently possible to obtain the list of resource
	 * names (e.g. because there hasn't been a successful parse yet and current document contents
	 * can not be parsed).
	 */
	public Multiset<String> getResourceNames(IDocument doc) {
		return getStringsFromAst(doc, RESOURCE_NAMES_PATH);
	}

	/**
	 * Get the resource type tag associated with a given resourceName in the given document.
	 * <p>
	 * If the content of IDocument is not currently parseable then this may return stale information
	 * retained from a previous successful parse.
	 * <p>
	 * It may also return null if its not currently possible to obtain type of the resource. E.g
	 * because there is no such resource, the resource has no valid type tag, or the document
	 * was never successfully parsed.
	 */
	public String getResourceType(IDocument doc, String resourceName) {
		return getFromAst(doc, (ast) -> {
			Node resource = RESOURCES_PATH.traverseAmbiguously(new ASTRootCursor(ast))
			.map((cursor) -> ((NodeCursor)cursor).getNode())
			.filter((resourceNode) -> resourceName.equals(NodeUtil.getScalarProperty(resourceNode, "name")))
			.findFirst().orElse(null);
			if (resource!=null) {
				return NodeUtil.getScalarProperty(resource, "type");
			}
			return null;
		});
	}

	/**
	 * Returns the job names that are defined by given IDocument. If the contents
	 * of IDocument is not currently parseable then this may return stale information
	 * retained from a previous successful parse.
	 * <p>
	 * It may also return null if its not currently possible to obtain the list of resource
	 * names (e.g. because there hasn't been a successful parse yet and current document contents
	 * can not be parsed).
	 */
	public Multiset<String> getJobNames(IDocument doc) {
		return getStringsFromAst(doc, JOB_NAMES_PATH);
	}

	private Multiset<String> getStringsFromAst(IDocument doc, YamlPath path) {
		return getFromAst(doc, (ast) -> {
			Node root = ast.get(0);
			return path
				.traverseAmbiguously(root)
				.map(NodeUtil::asScalar)
				.filter((string) -> string!=null)
				.collect(CollectorUtil.toMultiset());
		});
	}

	public Multiset<String> getResourceTypeNames(IDocument doc) {
		Collection<YValueHint> hints = getResourceTypeNameHints(doc);
		if (hints!=null) {
			return ImmutableMultiset.copyOf(YTypeFactory.values(hints));
		}
		return null;
	}

	public Collection<YValueHint> getResourceTypeNameHints(IDocument doc) {
		Multiset<String> userDefined = getStringsFromAst(doc, RESOURCE_TYPE_NAMES_PATH);
		if (userDefined!=null) {
			Builder<YValueHint> builder = ImmutableMultiset.builder();
			builder.addAll(YTypeFactory.hints(userDefined));
			builder.addAll(Arrays.asList(PipelineYmlSchema.BUILT_IN_RESOURCE_TYPES));
			return builder.build();
		}
		return null;
	}


	private <T> T getFromAst(IDocument doc, Function<YamlFileAST, T> astFunction) {
		try {
			if (doc!=null) {
				String uri = doc.getUri();
				if (uri!=null) {
					YamlFileAST ast = getAst(doc, true);
					return astFunction.apply(ast);
				}
			}
		} catch (YAMLException e) {
			// ignore: found garbage in the doc. Can't compute stuff and that's to be expected.
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	public YamlFileAST getSafeAst(IDocument doc) {
		return getSafeAst(doc, true);
	}

	public YamlFileAST getAst(IDocument doc, boolean allowStaleAst) throws Exception {
		return getAstProvider(allowStaleAst).getAST(doc);
	}

	public YamlASTProvider getAstProvider(boolean allowStaleAsts) {
		return (IDocument doc) -> {
			String uri = doc.getUri();
			if (uri!=null) {
				return asts.get(uri, allowStaleAsts, () -> {
					return parser.getAST(doc);
				});
			}
			return null;
		};
	}

	public YamlFileAST getSafeAst(IDocument doc, boolean allowStaleAst) {
		if (doc!=null) {
			try {
				return getAst(doc, allowStaleAst);
			} catch (Exception e) {
				//ignored
			}
		}
		return null;
	}

	public ASTTypeCache getAstTypeCache() {
		return astTypes;
	}

	public Stream<Node> getResourceDefinitionNodes(YamlFileAST ast, String name) {
		return RESOURCE_NAMES_PATH.prepend(YamlPathSegment.anyChild())
				.traverseAmbiguously(ast)
				.filter(node -> name.equals(NodeUtil.asScalar(node)));
	}

}
