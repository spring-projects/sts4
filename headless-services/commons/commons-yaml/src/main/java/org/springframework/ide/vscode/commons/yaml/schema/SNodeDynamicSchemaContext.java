/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.CollectionUtil;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SChildBearingNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SKeyNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SSeqNode;

/**
 * Adapts an SNode so it can be used by a YamlSchema as a {@link DynamicSchemaContext}
 *
 * @author Kris De Volder
 */
public class SNodeDynamicSchemaContext extends CachingSchemaContext {

	final static Logger log = LoggerFactory.getLogger(SNodeDynamicSchemaContext.class);

	
	private SNode contextNode;
	private YamlPath contextPath;

	public SNodeDynamicSchemaContext(SNode contextNode, YamlPath contextPath) {
		this.contextNode = contextNode;
		this.contextPath = contextPath;
	}

	@Override
	protected Set<String> computeDefinedProperties() {
		try {
			if (contextNode instanceof SChildBearingNode) {
				List<SNode> children = ((SChildBearingNode)contextNode).getChildren();
				if (CollectionUtil.hasElements(children)) {
					Set<String> keys = new HashSet<>(children.size());
					for (SNode c : children) {
						if (c instanceof SKeyNode) {
							keys.add(((SKeyNode) c).getKey());
						}
					}
					return keys;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return Collections.emptySet();
	}

	@Override
	public IDocument getDocument() {
		return contextNode.getDocument();
	}

	@Override
	public YamlPath getPath() {
		return contextPath;
	}

	@Override
	public String toString() {
		return "SNodeDynamicSchemaContext("+contextPath+")";
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public boolean isMap() {
		return !computeDefinedProperties().isEmpty();
	}

	@Override
	public boolean isSequence() {
		try {
			if (contextNode instanceof SChildBearingNode) {
				List<SNode> children = ((SChildBearingNode)contextNode).getChildren();
				if (CollectionUtil.hasElements(children)) {
					for (SNode c : children) {
						if (c instanceof SSeqNode) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}

}
