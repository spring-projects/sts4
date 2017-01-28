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

import static org.springframework.ide.vscode.commons.util.ExceptionUtil.getSimpleError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.util.CollectionUtil;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.FuzzyMatcher;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.ValueParseException;
import org.springframework.ide.vscode.commons.yaml.hover.YPropertyInfoTemplates;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment;
import org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment.YamlPathSegmentType;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;
import org.springframework.ide.vscode.commons.yaml.schema.SNodeDynamicSchemaContext;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YTypedProperty;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SChildBearingNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.vscode.commons.yaml.util.YamlIndentUtil;

import com.google.common.collect.ImmutableList;

public class YTypeAssistContext extends AbstractYamlAssistContext {

	final static Logger logger = LoggerFactory.getLogger(YTypeAssistContext.class);

	final private YTypeUtil typeUtil;
	final private YType type;
	final private YamlAssistContext parent;

	public YTypeAssistContext(YTypeAssistContext parent, YamlPath contextPath, YType YType, YTypeUtil typeUtil) {
		super(parent.getDocument(), parent.documentSelector, contextPath);
		this.parent = parent;
		this.typeUtil = typeUtil;
		this.type = typeUtil.inferMoreSpecificType(YType, getSchemaContext());
	}

	public YTypeAssistContext(TopLevelAssistContext parent, int documentSelector, YType type, YTypeUtil typeUtil) {
		super(parent.getDocument(), documentSelector, YamlPath.EMPTY);
		this.type = type;
		this.typeUtil = typeUtil;
		this.parent = parent;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(YamlDocument doc, SNode node, int offset) throws Exception {
		String query = getPrefix(doc, node, offset);
		List<ICompletionProposal> valueCompletions = getValueCompletions(doc, offset, query);
		if (!valueCompletions.isEmpty()) {
			return valueCompletions;
		}
		return getKeyCompletions(doc, offset, query);
	}

	public List<ICompletionProposal> getKeyCompletions(YamlDocument doc, int offset, String query) throws Exception {
		int queryOffset = offset - query.length();
		SNode contextNode = getContextNode();
		DynamicSchemaContext dynamicCtxt = getSchemaContext();
		List<YTypedProperty> properties = typeUtil.getProperties(type);
		if (CollectionUtil.hasElements(properties)) {
			ArrayList<ICompletionProposal> proposals = new ArrayList<>(properties.size());
			Set<String> definedProps = dynamicCtxt.getDefinedProperties();
			for (YTypedProperty p : properties) {
				String name = p.getName();
				double score = FuzzyMatcher.matchScore(query, name);
				if (score!=0) {
					YamlPath relativePath = YamlPath.fromSimpleProperty(name);
					YamlPathEdits edits = new YamlPathEdits(doc);
					if (!definedProps.contains(name)) {
						//property not yet defined
						YType YType = p.getType();
						edits.delete(queryOffset, query);
						if (queryOffset>0 && !Character.isWhitespace(doc.getChar(queryOffset-1))) {
							//See https://www.pivotaltracker.com/story/show/137722057
							edits.insert(queryOffset, " ");
						}
						edits.createPathInPlace(contextNode, relativePath, queryOffset, appendTextFor(YType));
						proposals.add(completionFactory().beanProperty(doc.getDocument(),
								contextPath.toPropString(), getType(),
								query, p, score, edits, typeUtil)
						);
					} else {
						//property already defined
						// instead of filtering, navigate to the place where its defined.
						deleteQueryAndLine(doc, query, queryOffset, edits);
						//Cast to SChildBearingNode cannot fail because otherwise definedProps would be the empty set.
						edits.createPath((SChildBearingNode) contextNode, relativePath, "");
						proposals.add(
							completionFactory().beanProperty(doc.getDocument(),
								contextPath.toPropString(), getType(),
								query, p, score, edits, typeUtil)
							.deemphasize() //deemphasize because it already exists
						);
					}
				}
			}
			return proposals;
		}
		return Collections.emptyList();
	}

	/**
	 * Computes the text that should be appended at the end of a completion
	 * proposal depending on what type of value is expected.
	 */
	protected String appendTextFor(YType type) {
		//Note that proper indentation after each \n" is added automatically
		//to align with the parent. The strings created here only need to contain
		//indentation spaces to indent *more* than the parent node.
		if (type==null) {
			//Assume its some kind of pojo bean
			return "\n"+YamlIndentUtil.INDENT_STR;
		} else if (typeUtil.isMap(type)) {
			//ready to enter nested map key on next line
			return "\n"+YamlIndentUtil.INDENT_STR;
		} if (typeUtil.isSequencable(type)) {
			//ready to enter sequence element on next line
			return "\n- ";
		} else if (typeUtil.isAtomic(type)) {
			//ready to enter whatever on the same line
			return " ";
		} else {
			//Assume its some kind of pojo bean
			return "\n"+YamlIndentUtil.INDENT_STR;
		}
	}

	private List<ICompletionProposal> getValueCompletions(YamlDocument doc, int offset, String query) {
		YValueHint[] values=null;
		try {
			values = typeUtil.getHintValues(type, getSchemaContext());
		} catch (Exception e) {
			return ImmutableList.of(completionFactory().errorMessage(query, getMessage(e)));
		}
		if (values!=null) {
			ArrayList<ICompletionProposal> completions = new ArrayList<>();
			for (YValueHint value : values) {
				double score = FuzzyMatcher.matchScore(query, value.getValue());
				if (score!=0 && !value.equals(query)) {
					int queryStart = offset-query.length();
					DocumentEdits edits = new DocumentEdits(doc.getDocument());
					edits.delete(queryStart, offset);
					if (!Character.isWhitespace(doc.getChar(queryStart-1))) {
						edits.insert(offset, " ");
					}
					edits.insert(offset, value.getValue());
					completions.add(completionFactory().valueProposal(value.getValue(), query, value.getLabel(), type, score, edits, typeUtil));
				}
			}
			return completions;
		}
		return Collections.emptyList();
	}

	private String getMessage(Exception _e) {
		Throwable e = ExceptionUtil.getDeepestCause(_e);

		// If value parse exception, do not append any additional information
		if (e instanceof ValueParseException) {
			return ExceptionUtil.getMessageNoAppendedInformation(e);
		} else {
			return ExceptionUtil.getMessage(e);
		}
	}

	@Override
	public YamlAssistContext traverse(YamlPathSegment s) throws Exception {
		if (s.getType()==YamlPathSegmentType.VAL_AT_KEY) {
			if (typeUtil.isSequencable(type) || typeUtil.isMap(type)) {
				return contextWith(s, typeUtil.getDomainType(type));
			}
			String key = s.toPropString();
			Map<String, YTypedProperty> subproperties = typeUtil.getPropertiesMap(type);
			if (subproperties!=null) {
				return contextWith(s, getType(subproperties.get(key)));
			}
		} else if (s.getType()==YamlPathSegmentType.VAL_AT_INDEX) {
			if (typeUtil.isSequencable(type)) {
				return contextWith(s, typeUtil.getDomainType(type));
			}
		}
		return null;
	}

	private YType getType(YTypedProperty prop) {
		if (prop!=null) {
			return prop.getType();
		}
		return null;
	}

	private YamlAssistContext contextWith(YamlPathSegment s, YType nextType) {
		if (nextType!=null) {
			return new YTypeAssistContext(this, contextPath.append(s), nextType, typeUtil);
		}
		return null;
	}


	@Override
	public String toString() {
		return "TypeContext("+contextPath.toPropString()+"::"+type+")";
	}


	@Override
	public Renderable getHoverInfo() {
		if (parent!=null) {
			return parent.getHoverInfo(contextPath.getLastSegment());
		}
		return null;
	}

	public YType getType() {
		return type;
	}

	@Override
	public Renderable getHoverInfo(YamlPathSegment lastSegment) {
		//Hoverinfo is only attached to YTypedProperties so...
		switch (lastSegment.getType()) {
		case VAL_AT_KEY:
		case KEY_AT_KEY:
			YTypedProperty prop = getProperty(lastSegment.toPropString());
			if (prop!=null) {
				return YPropertyInfoTemplates.createHover(contextPath.toPropString(), getType(), prop);
			}
			break;
		default:
		}
		return null;
	}

	protected DynamicSchemaContext getSchemaContext() {
		try {
			SNode contextNode = getContextNode();
			YamlPath fullContextPath = contextPath.prepend(YamlPathSegment.valueAt(documentSelector));
			return new SNodeDynamicSchemaContext(contextNode, fullContextPath);
		} catch (Exception e) {
			Log.log(e);
			return DynamicSchemaContext.NULL;
		}
	}

	@Override
	public Renderable getValueHoverInfo(YamlDocument doc, DocumentRegion documentRegion) {
		//By default we don't provide value-specific hover, so just show the same hover
		// as the assistContext the value is in. This is likely more interesting than showing nothing at all.
		return getHoverInfo();
	}

	private YTypedProperty getProperty(String name) {
		return typeUtil.getPropertiesMap(getType()).get(name);
	}
}
