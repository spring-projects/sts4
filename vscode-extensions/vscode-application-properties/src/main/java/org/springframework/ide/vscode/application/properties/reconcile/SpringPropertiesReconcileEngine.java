/*******************************************************************************
 * Copyright (c) 2014-2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.application.properties.reconcile;

//import static org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine.isAssign;
import static org.springframework.ide.vscode.application.properties.reconcile.ApplicationPropertiesProblemType.PROP_DEPRECATED;
import static org.springframework.ide.vscode.application.properties.reconcile.ApplicationPropertiesProblemType.PROP_SYNTAX_ERROR;
import static org.springframework.ide.vscode.application.properties.reconcile.ApplicationPropertiesProblemType.PROP_UNKNOWN_PROPERTY;
import static org.springframework.ide.vscode.application.properties.reconcile.SpringPropertyProblem.problem;
import static org.springframework.ide.vscode.commons.util.StringUtil.commonPrefix;

import java.util.regex.Pattern;

import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndex;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.Type;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeParser;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.application.properties.quickfix.ReplaceDeprecatedPropertyQuickfix;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.BadLocationException;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.ValueParser;
import org.springframework.ide.vscode.java.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.java.properties.parser.ParseResults;
import org.springframework.ide.vscode.java.properties.parser.Parser;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.KeyValuePair;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Node;
import org.springframework.ide.vscode.java.properties.parser.PropertiesFileEscapes;

/**
 * Implements reconciling algorithm for {@link SpringPropertiesReconcileStrategy}.
 * <p>
 * The code in here could have been also part of the {@link SpringPropertiesReconcileStrategy}
 * itself, however isolating it here allows it to me more easily unit tested (no dependencies
 * on ISourceViewer which is difficult to 'mock' in testing harness.
 *
 * @author Kris De Volder
 */
public class SpringPropertiesReconcileEngine implements IReconcileEngine {

	/**
	 * Regexp that matches a ',' surrounded by whitespace, including escaped whitespace / newlines
	 */
	private static final Pattern COMMA = Pattern.compile(
			"(\\s|\\\\\\s)*,(\\s|\\\\\\s)*"
	);

	private static final Pattern SPACES = Pattern.compile(
			"(\\s|\\\\\\s)*"
	);

	private SpringPropertyIndexProvider fIndexProvider;
	private TypeUtilProvider typeUtilProvider;
	private final DelimitedListReconciler commaListReconciler = new DelimitedListReconciler(COMMA, this::reconcileType);
	private Parser parser = new AntlrParser();

	public SpringPropertiesReconcileEngine(SpringPropertyIndexProvider provider, TypeUtilProvider typeUtilProvider) {
		this.fIndexProvider = provider;
		this.typeUtilProvider = typeUtilProvider;
	}
	
	public void reconcile(IDocument doc, IProblemCollector problemCollector) {
		FuzzyMap<PropertyInfo> index = fIndexProvider.getIndex(doc);
		problemCollector.beginCollecting();
		try {
			ParseResults results = parser.parse(doc.get());
			DuplicateNameChecker duplicateNameChecker = new DuplicateNameChecker(problemCollector);
			
			results.syntaxErrors.forEach(syntaxError -> {
				problemCollector.accept(problem(PROP_SYNTAX_ERROR, syntaxError.getMessage(), syntaxError.getOffset(),
						syntaxError.getLength()));
			});
			
			if (index==null || index.isEmpty()) {
				//don't report errors when index is empty, simply don't check (otherwise we will just reprot
				// all properties as errors, but this not really useful information since the cause is
				// some problem putting information about properties into the index.
				return;
			}
			
			results.ast.getNodes(KeyValuePair.class).forEach(pair -> {
				try {
					DocumentRegion propertyNameRegion = createRegion(doc, pair.getKey());
					String keyName = PropertiesFileEscapes.unescape(propertyNameRegion.toString());
					duplicateNameChecker.check(propertyNameRegion);
					PropertyInfo validProperty = SpringPropertyIndex.findLongestValidProperty(index, keyName);
					if (validProperty!=null) {
						//TODO: Remove last remnants of 'IRegion trimmedRegion' here and replace
						// it all with just passing around 'fullName' DocumentRegion. This may require changes
						// in PropertyNavigator (probably these changes are also for the better making it simpler as well)
						if (validProperty.isDeprecated()) {
							problemCollector.accept(problemDeprecated(propertyNameRegion, validProperty));
						}
						int offset = validProperty.getId().length() + propertyNameRegion.getStart();
						PropertyNavigator navigator = new PropertyNavigator(doc, problemCollector, typeUtilProvider.getTypeUtil(doc), propertyNameRegion);
						Type valueType = navigator.navigate(offset, TypeParser.parse(validProperty.getType()));
						if (valueType!=null) {
							reconcileType(doc, valueType, pair.getValue(), problemCollector);
						}
					} else { //validProperty==null
						//The name is invalid, with no 'prefix' of the name being a valid property name.
						PropertyInfo similarEntry = index.findLongestCommonPrefixEntry(propertyNameRegion.toString());
						CharSequence validPrefix = commonPrefix(similarEntry.getId(), keyName);
						problemCollector.accept(problemUnkownProperty(propertyNameRegion, similarEntry, validPrefix));
					} //end: validProperty==null
				} catch (Exception e) {
					Log.log(e);
				}
			});
		} catch (Throwable e2) {
			Log.log(e2);
		} finally {
			problemCollector.endCollecting();
		}
	}
	
	protected SpringPropertyProblem problemDeprecated(DocumentRegion region, PropertyInfo property) {
		SpringPropertyProblem p = problem(PROP_DEPRECATED,
				TypeUtil.deprecatedPropertyMessage(
						property.getId(), null,
						property.getDeprecationReplacement(),
						property.getDeprecationReason()
				),
				region
		);
		p.setPropertyName(property.getId());
		p.setMetadata(property);
		p.setProblemFixer(ReplaceDeprecatedPropertyQuickfix.FIXER);
		return p;
	}

	protected SpringPropertyProblem problemUnkownProperty(DocumentRegion fullNameRegion,
			PropertyInfo similarEntry, CharSequence validPrefix) {
		String fullName = fullNameRegion.toString();
		SpringPropertyProblem p = problem(PROP_UNKNOWN_PROPERTY,
				"'"+fullName+"' is an unknown property."+suggestSimilar(similarEntry, validPrefix, fullName),
				fullNameRegion.subSequence(validPrefix.length())
		);
		p.setPropertyName(fullName);
		return p;
	}

	private void reconcileType(IDocument doc, Type expectType, Node value, IProblemCollector problems) {
		// Trim start and end spaces from the value node
		reconcileType(createRegion(doc, value).trimStart(SPACES).trimEnd(SPACES), expectType,
				problems);
	}

	private DocumentRegion createRegion(IDocument doc, Node value) {
		// Trim trailing spaces (there is no leading white space already)
		int length = value.getLength();
		try {
			length = doc.get(value.getOffset(), value.getLength()).length();
		} catch (BadLocationException e) {
			// ignore
		} 
		return new DocumentRegion(doc, value.getOffset(), value.getOffset() + length);
	}
	
	private void reconcileType(DocumentRegion region, Type expectType, IProblemCollector problems) {
		TypeUtil typeUtil = typeUtilProvider.getTypeUtil(region.getDocument());
		ValueParser parser = typeUtil.getValueParser(expectType);
		if (parser!=null) {
			try {
				String valueStr = PropertiesFileEscapes.unescape(region.toString());
				if (!valueStr.contains("${")) {
					//Don't check strings that look like they use variable substitution.
					parser.parse(valueStr);
				}
			} catch (Exception e) {
				problems.accept(problem(ApplicationPropertiesProblemType.PROP_VALUE_TYPE_MISMATCH,
						"Expecting '"+typeUtil.niceTypeName(expectType)+"'",
						region));
			}
		} else if (TypeUtil.isList(expectType)||TypeUtil.isArray(expectType)) {
			commaListReconciler.reconcile(region, expectType, problems);
		}
	}

	private String suggestSimilar(PropertyInfo similarEntry, CharSequence validPrefix, CharSequence fullName) {
		int matchedChars = validPrefix.length();
		int wrongChars = fullName.length()-matchedChars;
		if (wrongChars<matchedChars) {
			return " Did you mean '"+similarEntry.getId()+"'?";
		} else {
			return "";
		}
	}

}
