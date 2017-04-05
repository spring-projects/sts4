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
package org.springframework.ide.vscode.commons.yaml.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.CompletionItemKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.completion.ScoreableProposal;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SKeyNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SNodeType;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SRootNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SSeqNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.springframework.ide.vscode.commons.yaml.util.YamlIndentUtil;

/**
 * Implements {@link ICompletionEngine} for .yml file, based on a YamlAssistContextProvider
 * which has to to be injected into engine via its contructor.
 *
 * @author Kris De Volder
 */
public class YamlCompletionEngine implements ICompletionEngine {

	final static Logger logger = LoggerFactory.getLogger(YamlCompletionEngine.class);


	private final YamlAssistContextProvider contextProvider;
	protected final YamlStructureProvider structureProvider;

	public YamlCompletionEngine(YamlStructureProvider structureProvider, YamlAssistContextProvider contextProvider) {
		Assert.isNotNull(structureProvider);
		Assert.isNotNull(contextProvider);
		this.structureProvider= structureProvider;
		this.contextProvider = contextProvider;
	}

	protected final YamlAssistContext getGlobalContext(YamlDocument doc) {
		return contextProvider.getGlobalAssistContext(doc);
	}

	protected CompletionFactory proposalFactory() {
		return CompletionFactory.DEFAULT;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument _doc, int offset) throws Exception {
		YamlDocument doc = new YamlDocument(_doc, structureProvider);
		if (!doc.isCommented(offset)) {
			SRootNode root = doc.getStructure();
			SNode current = root.find(offset);
			SNode contextNode = getContextNode(doc, current, offset);
			List<ICompletionProposal> all = new ArrayList<>(getPreciseCompletions(offset, doc, current, contextNode));
			all.addAll(addIndentations(getRelaxedCompletions(offset, doc, contextNode, current)));
			return all;
		}
		return Collections.emptyList();
	}

	private Collection<? extends ICompletionProposal> addIndentations(
			Collection<? extends ICompletionProposal> completions) {
		if (!completions.isEmpty()) {
			List<ICompletionProposal> transformed = new ArrayList<>();
			for (ICompletionProposal p : completions) {
				transformed.add(indented(p));
			}
			return transformed;
		}
		return Collections.emptyList();
	}

	public ICompletionProposal indented(ICompletionProposal proposal) {
		ScoreableProposal transformed = new ScoreableProposal() {

			DocumentEdits indentedEdit = null;

			@Override
			public synchronized DocumentEdits getTextEdit() {
				if (indentedEdit==null) {
					indentedEdit = proposal.getTextEdit();
					indentedEdit.indentFirstEdit(YamlIndentUtil.INDENT_STR);
				}
				return indentedEdit;
			}

			@Override
			public String getLabel() {
				return "➔ "+proposal.getLabel();
			}

			@Override
			public CompletionItemKind getKind() {
				return proposal.getKind();
			}

			@Override
			public Renderable getDocumentation() {
				return proposal.getDocumentation();
			}

			@Override
			public String getDetail() {
				return proposal.getDetail();
			}

			@Override
			public double getBaseScore() {
				if (proposal instanceof ScoreableProposal) {
					return ((ScoreableProposal) proposal).getBaseScore();
				}
				return 0;
			}
		};
		transformed.deemphasize();
		return transformed;
	}

	private Collection<? extends ICompletionProposal> getRelaxedCompletions(int offset, YamlDocument doc, SNode preciseContextNode, SNode currentNode) throws Exception {
		if (preciseContextNode!=null) {
			SNode contextNode = getRelaxedContextNode(preciseContextNode, currentNode);
			YamlAssistContext context = getContext(doc, contextNode);
			if (context!=null) {
				return context.getCompletions(doc, currentNode, offset);
			}
		}
		return Collections.emptyList();
	}

	private boolean isBarrenKey(SNode node) throws Exception {
		if (node.getNodeType()==SNodeType.KEY) {
			SKeyNode keyNode = (SKeyNode) node;
			String value = keyNode.getSimpleValue();
			return value.trim().isEmpty();
		}
		return false;
	}

	private SNode getRelaxedContextNode(SNode preciseContextNode, SNode currentNode) throws Exception {
		while (currentNode!=null) {
			if (currentNode.getParent()==preciseContextNode) {
				if (isBarrenKey(currentNode)) {
					return currentNode;
				} else {
					return null;
				}
			}
			currentNode = currentNode.getParent();
		}
		return currentNode;
	}

	protected Collection<ICompletionProposal> getPreciseCompletions(int offset, YamlDocument doc, SNode current, SNode contextNode)
			throws Exception {
		if (contextNode!=null) {
			YamlAssistContext context = getContext(doc, contextNode);
			if (context==null && isDubiousKey(contextNode, offset)) {
				current = current.getParent();
				contextNode = contextNode.getParent();
				context = getContext(doc, contextNode);
			}
			if (context!=null) {
				return context.getCompletions(doc, current, offset);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * A 'dubious' key is when the cursor is positioned right after a key's ':' character.
	 * This key is 'dubious' in that if the user would type a non-whitespace character next...
	 * then that key is no longer a key but parses as a 'value' instead.
	 */
	private boolean isDubiousKey(SNode node, int offset) {
		if (node.getNodeType()==SNodeType.KEY) {
			SKeyNode key = (SKeyNode)node;
			return key.getColonOffset()+1==offset;
		}
		return false;
	}

	protected YamlAssistContext getContext(YamlDocument doc, SNode contextNode) {
		try {
			if (contextNode!=null) {
				YamlPath contextPath = contextNode.getPath();
				return contextPath.traverse(getGlobalContext(doc));
			}
		} catch (Exception e) {
			logger.error("Error obtaining YamlAssistContext", e);
		}
		return null;
	}

	protected SNode getContextNode(YamlDocument doc, SNode node, int offset) throws Exception {
		if (node==null) {
			return null;
		} else if (node.getNodeType()==SNodeType.KEY) {
			//slight complication. The area in the key and value of a key node represent different
			// contexts for content assistance
			SKeyNode keyNode = (SKeyNode)node;
			if (keyNode.isInValue(offset)) {
				return keyNode;
			} else {
				return keyNode.getParent();
			}
		} else if (node.getNodeType()==SNodeType.RAW) {
			//Treat raw node as a 'key node'. This is basically assuming that is misclasified
			// by structure parser because the ':' was not yet typed into the document.

			//Complication: if line with cursor is empty or the cursor is inside the indentation
			// area then the structure may not reflect correctly the context. This is because
			// the correct context depends on text the user has not typed yet.(which will change the
			// indentation level of the current line. So we must use the cursorIndentation
			// rather than the structure-tree to determine the 'context' node.
			int cursorIndent = doc.getColumn(offset);
			int nodeIndent = node.getIndent();
			int currentIndent = YamlIndentUtil.minIndent(cursorIndent, nodeIndent);
			while (node.getIndent()==-1 || (node.getIndent()>=currentIndent && node.getNodeType()!=SNodeType.DOC)) {
				node = node.getParent();
			}
			return node;
		} else if (node.getNodeType()==SNodeType.SEQ) {
			SSeqNode seqNode = (SSeqNode)node;
			if (seqNode.isInValue(offset)) {
				return seqNode;
			} else {
				return seqNode.getParent();
			}
		} else if (node.getNodeType()==SNodeType.DOC) {
			return node;
		}
		return null;
	}

	protected YamlPath getContextPath(YamlDocument doc, SNode node, int offset) throws Exception {
		if (node==null) {
			return YamlPath.EMPTY;
		} else if (node.getNodeType()==SNodeType.KEY) {
			//slight complication. The area in the key and value of a key node represent different
			// contexts for content assistance
			SKeyNode keyNode = (SKeyNode)node;
			if (keyNode.isInValue(offset)) {
				return keyNode.getPath();
			} else {
				return keyNode.getParent().getPath();
			}
		} else if (node.getNodeType()==SNodeType.RAW) {
			//Treat raw node as a 'key node'. This is basically assuming that is misclasified
			// by structure parser because the ':' was not yet typed into the document.

			//Complication: if line with cursor is empty or the cursor is inside the indentation
			// area then the structure may not reflect correctly the context. This is because
			// the correct context depends on text the user has not typed yet.(which will change the
			// indentation level of the current line. So we must use the cursorIndentation
			// rather than the structur-tree to determine the 'context' node.
			int cursorIndent = doc.getColumn(offset);
			int nodeIndent = node.getIndent();
			int currentIndent = YamlIndentUtil.minIndent(cursorIndent, nodeIndent);
			while (node.getIndent()==-1 || (node.getIndent()>=currentIndent && node.getNodeType()!=SNodeType.DOC)) {
				node = node.getParent();
			}
			return node.getPath();
		} else if (node.getNodeType()==SNodeType.SEQ) {
			SSeqNode seqNode = (SSeqNode)node;
			if (seqNode.isInValue(offset)) {
				return seqNode.getPath();
			} else {
				return seqNode.getParent().getPath();
			}
		} else if (node.getNodeType()==SNodeType.DOC) {
			return node.getPath();
		} else {
			throw new IllegalStateException("Missing case");
		}
	}

}
