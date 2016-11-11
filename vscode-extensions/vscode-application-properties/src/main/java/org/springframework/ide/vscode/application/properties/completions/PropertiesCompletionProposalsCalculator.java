package org.springframework.ide.vscode.application.properties.completions;

import static org.springframework.ide.vscode.commons.util.StringUtil.camelCaseToHyphens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.application.properties.metadata.completions.PropertyCompletionFactory;
import org.springframework.ide.vscode.application.properties.metadata.hints.HintProvider;
import org.springframework.ide.vscode.application.properties.metadata.hints.HintProviders;
import org.springframework.ide.vscode.application.properties.metadata.hints.StsValueHint;
import org.springframework.ide.vscode.application.properties.metadata.hints.ValueHintHoverInfo;
import org.springframework.ide.vscode.application.properties.metadata.types.Type;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeParser;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil.BeanPropertyNameMode;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil.EnumCaseMode;
import org.springframework.ide.vscode.application.properties.metadata.types.TypedProperty;
import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap.Match;
import org.springframework.ide.vscode.application.properties.reconcile.PropertyNavigator;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.completion.LazyProposalApplier;
import org.springframework.ide.vscode.commons.languageserver.util.BadLocationException;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.languageserver.util.PrefixFinder;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;
import org.springframework.ide.vscode.commons.util.CollectionUtil;
import org.springframework.ide.vscode.commons.util.FuzzyMatcher;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.java.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.java.properties.parser.ParseResults;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.EmptyLine;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Key;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Node;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Value;

import com.google.common.collect.ImmutableList;

class PropertiesCompletionProposalsCalculator {
	
	private static final Pattern SPACES = Pattern.compile(
			"(\\s|\\\\\\s)*"
	);
	
	private static boolean isValuePrefixChar(char c) {
		return !Character.isWhitespace(c) && c!=',';
	}
	
	private static final PrefixFinder valuePrefixFinder = new PrefixFinder() {
		protected boolean isPrefixChar(char c) {
			return isValuePrefixChar(c);
		}

	};

	private static final PrefixFinder fuzzySearchPrefix = new PrefixFinder() {
		protected boolean isPrefixChar(char c) {
			return !Character.isWhitespace(c);
		}
	};

	private static final PrefixFinder navigationPrefixFinder = new PrefixFinder() {
		public String getPrefix(IDocument doc, int offset) {
			String prefix = super.getPrefix(doc, offset);
			//Check if character before looks like 'navigation'.. otherwise don't
			// return a navigationPrefix.
			char charBefore = getCharBefore(doc, prefix, offset);
			if (charBefore=='.' || charBefore==']') {
				return prefix;
			}
			return null;
		}
		private char getCharBefore(IDocument doc, String prefix, int offset) {
			try {
				if (prefix!=null) {
					int offsetBefore = offset-prefix.length()-1;
					if (offsetBefore>=0) {
						return doc.getChar(offsetBefore);
					}
				}
			} catch (BadLocationException e) {
				//ignore
			}
			return 0;
		}
		protected boolean isPrefixChar(char c) {
			return !Character.isWhitespace(c) && c!=']' && c!=']' && c!='.';
		}
	};
	
	private FuzzyMap<PropertyInfo> index;
	private TypeUtil typeUtil;
	private PropertyCompletionFactory completionFactory;
	private IDocument doc;
	private int offset;
	private boolean preferLowerCaseEnums;
	private AntlrParser parser;
	
	PropertiesCompletionProposalsCalculator(FuzzyMap<PropertyInfo> index, TypeUtil typeUtil, PropertyCompletionFactory completionFactory, IDocument doc, int offset, boolean preferLowerCaseEnums) {
		this.index = index;
		this.typeUtil = typeUtil;
		this.completionFactory = completionFactory;
		this.doc = doc;
		this.offset = offset;
		this.preferLowerCaseEnums = preferLowerCaseEnums;
		this.parser = new AntlrParser();
	}
	
	/**
	 * Create completions proposals in the context of a properties text editor.
	 */
	public Collection<ICompletionProposal> calculate() throws BadLocationException {
		ParseResults parseResults = parser.parse(doc.get());
		Node node = parseResults.ast.findNode(offset);
		if (node instanceof Value) {
			return getValueCompletions((Value)node);
		} else if (node instanceof Key || node instanceof EmptyLine || node == null) {
			return getPropertyCompletions();
		}
		return Collections.emptyList();
	}

	private Collection<ICompletionProposal> getNavigationProposals() {
		String navPrefix = navigationPrefixFinder.getPrefix(doc, offset);
		try {
			if (navPrefix!=null) {
				int navOffset = offset-navPrefix.length()-1; //offset of 'nav' operator char (i.e. '.' or ']').
				navPrefix = fuzzySearchPrefix.getPrefix(doc, navOffset);
				if (navPrefix!=null && !navPrefix.isEmpty()) {
					PropertyInfo prop = findLongestValidProperty(index, navPrefix);
					if (prop!=null) {
						int regionStart = navOffset-navPrefix.length();
						Collection<ICompletionProposal> hintProposals = getKeyHintProposals(prop, navOffset);
						if (CollectionUtil.hasElements(hintProposals)) {
							return hintProposals;
						}
						PropertyNavigator navigator = new PropertyNavigator(doc, null, typeUtil, new DocumentRegion(doc, regionStart, navOffset));
						Type type = navigator.navigate(regionStart+prop.getId().length(), TypeParser.parse(prop.getType()));
						if (type!=null) {
							return getNavigationProposals(type, navOffset);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Collections.emptyList();
	}

	private Collection<ICompletionProposal> getKeyHintProposals(PropertyInfo prop, int navOffset) {
		HintProvider hintProvider = prop.getHints(typeUtil, false);
		if (!HintProviders.isNull(hintProvider)) {
			String query = textBetween(doc, navOffset+1, offset);
			List<TypedProperty> hintProperties = hintProvider.getPropertyHints(query);
			if (CollectionUtil.hasElements(hintProperties)) {
				return createPropertyProposals(TypeParser.parse(prop.getType()), navOffset, query, hintProperties);
			}
		}
		return ImmutableList.of();
	}

	private String textBetween(IDocument doc, int start, int end) {
		if (end > doc.getLength()) {
			end = doc.getLength();
		}
		if (start>doc.getLength()) {
			start = doc.getLength();
		}
		if (start<0) {
			start = 0;
		}
		if (end < 0) {
			end = 0;
		}
		if (start<end) {
			try {
				return doc.get(start, end-start);
			} catch (BadLocationException e) {
			}
 		}
		return "";
	}

	/**
	 * @param type Type of the expression leading upto the 'nav' operator
	 * @param navOffset Offset of the nav operator (either ']' or '.'
	 * @param offset Offset of the cursor where CA was requested.
	 */
	private Collection<ICompletionProposal> getNavigationProposals(Type type, int navOffset) {
		try {
			char navOp = doc.getChar(navOffset);
			if (navOp=='.') {
				String prefix = doc.get(navOffset+1, offset-(navOffset+1));
				EnumCaseMode caseMode = caseMode(prefix);
				List<TypedProperty> objectProperties = typeUtil.getProperties(type, caseMode, BeanPropertyNameMode.HYPHENATED);
				   //Note: properties editor itself deals with relaxed names. So it expects the properties here to be returned in hyphenated form only.
				if (objectProperties!=null && !objectProperties.isEmpty()) {
					return createPropertyProposals(type, navOffset, prefix, objectProperties);
				}
			} else {
				//TODO: other cases ']' or '[' ?
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Collections.emptyList();
	}

	protected Collection<ICompletionProposal> createPropertyProposals(Type type, int navOffset,
			String prefix, List<TypedProperty> objectProperties) {
		ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (TypedProperty prop : objectProperties) {
			double score = FuzzyMatcher.matchScore(prefix, prop.getName());
			if (score!=0) {
				Type valueType = prop.getType();
				String postFix = propertyCompletionPostfix(typeUtil, valueType);
				DocumentEdits edits = new DocumentEdits(doc);
				edits.delete(navOffset+1, offset);
				edits.insert(offset, prop.getName()+postFix);
				proposals.add(
					completionFactory.beanProperty(doc, null, type, prefix, prop, score, edits, typeUtil)
				);
			}
		}
		return proposals;
	}

	/**
	 * Determines the EnumCaseMode used to generate completion candidates based on prefix.
	 */
	protected EnumCaseMode caseMode(String prefix) {
		EnumCaseMode caseMode;
		if ("".equals(prefix)) {
			caseMode = preferLowerCaseEnums?EnumCaseMode.LOWER_CASE:EnumCaseMode.ORIGNAL;
		} else {
			caseMode = Character.isLowerCase(prefix.charAt(0))?EnumCaseMode.LOWER_CASE:EnumCaseMode.ORIGNAL;
		}
		return caseMode;
	}

	protected static String propertyCompletionPostfix(TypeUtil typeUtil, Type type) {
		String postfix = "";
		if (type!=null) {
			if (typeUtil.isAssignableType(type)) {
				postfix = "=";
			} else if (TypeUtil.isBracketable(type)) {
				postfix = "[";
			} else if (typeUtil.isDotable(type)) {
				postfix = ".";
			}
		}
		return postfix;
	}

//	public static boolean isAssign(char assign) {
//		return assign==':'||assign=='=';
//	}
//
//	private KeyValuePair getAstNodeLine(IDocument doc, int offset) {
//		List<KeyValuePair> pairs = parser.parse(doc.get()).ast.getNodes(KeyValuePair.class);
//		return findPair(pairs, offset, 0, pairs.size() - 1);
//	}
//	
//	private KeyValuePair findPair(List<KeyValuePair> pairs, int offset, int start, int end) {
//		if (start == end) {
//			KeyValuePair pair = pairs.get(start);
//			if (pair.getOffset() <= offset && offset <= pair.getOffset() + pair.getLength()) {
//				return pair;
//			} else {
//				return null;
//			}
//		} else if (start < end ) {
//			int pivotIndex = (start + end) / 2;
//			KeyValuePair pair = pairs.get(pivotIndex);
//			if (pair.getOffset() > offset) {
//				return findPair(pairs, offset, start, pivotIndex - 1);
//			} else if (offset > pair.getOffset() + pair.getLength()) {
//				return findPair(pairs, offset, pivotIndex + 1, end);
//			} else {
//				return pair;
//			}
//		} else {
//			return null;
//		}
//	}

//	private HoverInfo getValueHoverInfo(DocumentRegion value) {
//		try {
//			String valueString = value.toString();
//			IDocument doc = value.getDocument();
//			ITypedRegion valuePartition = getPartition(value.getDocument(), value.getStart());
//			int valuePartitionStart = valuePartition.getOffset();
//			String propertyName = fuzzySearchPrefix.getPrefix(doc, valuePartitionStart); //note: no need to skip whitespace backwards.
//											//because value partition includes whitespace around the assignment
//
//			Type type = getValueType(propertyName);
//			if (TypeUtil.isArray(type) || TypeUtil.isList(type)) {
//				//It is useful to provide content assist for the values in the list when entering a list
//				type = TypeUtil.getDomainType(type);
//			}
//			if (TypeUtil.isClass(type)) {
//				//Special case. We want to provide hoverinfos more liberally than what's suggested for completions (i.e. even class names
//				//that are not suggested by the hints because they do not meet subtyping constraints should be hoverable and linkable!
//				StsValueHint hint = StsValueHint.className(valueString, typeUtil);
//				if (hint!=null) {
//					return new ValueHintHoverInfo(hint);
//				}
//			}
//			//Hack: pretend to invoke content-assist at the end of the value text. This should provide hints applicable to that value
//			// then show hoverinfo based on that. That way we can avoid duplication a lot of similar logic to compute hoverinfos and hyperlinks.
//			Collection<StsValueHint> hints = getValueHints(valueString, propertyName, EnumCaseMode.ALIASED);
//			if (hints!=null) {
//				for (StsValueHint h : hints) {
//					if (valueString.equals(h.getValue())) {
//						return new ValueHintHoverInfo(h);
//					}
//				}
//			}
//		} catch (BadLocationException e) {
//			Log.log(e);
//		}
//		return null;
//	}

	private Collection<ICompletionProposal> getValueCompletions(Value value) {
		DocumentRegion valueRegion = createRegion(doc, value).trimStart(SPACES).trimEnd(SPACES);
		String query = valuePrefixFinder.getPrefix(doc, offset, valueRegion.getStart());
		int startOfValue = offset - query.length();
		EnumCaseMode caseMode = caseMode(query);
		
		// note: no need to skip whitespace backwards.
		String propertyName = /*fuzzySearchPrefix.getPrefix(doc, pair.getOffset())*/value.getParent().getKey().decode(); 
		// because value partition includes whitespace around the assignment
		if (propertyName != null) {
			Collection<StsValueHint> valueCompletions = getValueHints(query, propertyName, caseMode);
			if (valueCompletions != null && !valueCompletions.isEmpty()) {
				ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
				for (StsValueHint hint : valueCompletions) {
					String valueCandidate = hint.getValue();
					double score = FuzzyMatcher.matchScore(query, valueCandidate);
					if (score != 0) {
						DocumentEdits edits = new DocumentEdits(doc);
						edits.delete(startOfValue, offset);
						edits.insert(offset, valueCandidate);
						proposals.add(completionFactory.valueProposal(valueCandidate, query, getValueType(propertyName),
								score, edits, new ValueHintHoverInfo(hint))
						// new ValueProposal(startOfValue, valuePrefix,
						// valueCandidate, i)
						);
					}
				}
				return proposals;
			}
		}
		return Collections.emptyList();
	}

	private DocumentRegion createRegion(IDocument doc, Node value) {
		// Trim trailing spaces (there is no leading white space already)
		int length = value.getLength();
		try {
			length = doc.get(value.getOffset(), value.getLength()).trim().length();
		} catch (BadLocationException e) {
			// ignore
		} 
		return new DocumentRegion(doc, value.getOffset(), value.getOffset() + length);
	}

	private Collection<StsValueHint> getValueHints(String query, String propertyName, EnumCaseMode caseMode) {
		Type type = getValueType(propertyName);
		if (TypeUtil.isArray(type) || TypeUtil.isList(type)) {
			//It is useful to provide content assist for the values in the list when entering a list
			type = TypeUtil.getDomainType(type);
		}
		List<StsValueHint> allHints = new ArrayList<>();
		{
			Collection<StsValueHint> hints = typeUtil.getHintValues(type, query, caseMode);
			if (CollectionUtil.hasElements(hints)) {
				allHints.addAll(hints);
			}
		}
		{
			PropertyInfo prop = index.findLongestCommonPrefixEntry(propertyName);
			if (prop!=null) {
				HintProvider hintProvider = prop.getHints(typeUtil, false);
				if (!HintProviders.isNull(hintProvider)) {
					allHints.addAll(hintProvider.getValueHints(query));
				}
			}
		}
		return allHints;
	}

	/**
	 * Determine the value type for a give propertyName.
	 */
	protected Type getValueType(String propertyName) {
		try {
			PropertyInfo prop = index.get(propertyName);
			if (prop!=null) {
				return TypeParser.parse(prop.getType());
			} else {
				prop = findLongestValidProperty(index, propertyName);
				if (prop!=null) {
					TextDocument doc = new TextDocument(null);
					doc.setText(propertyName);
					PropertyNavigator navigator = new PropertyNavigator(doc, null, typeUtil, new DocumentRegion(doc, 0, doc.getLength()));
					return navigator.navigate(prop.getId().length(), TypeParser.parse(prop.getType()));
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	private List<Match<PropertyInfo>> findMatches(String prefix) {
		List<Match<PropertyInfo>> matches = index.find(camelCaseToHyphens(prefix));
		return matches;
	}

	private Collection<ICompletionProposal> getPropertyCompletions() throws BadLocationException {
		Collection<ICompletionProposal> navProposals = getNavigationProposals();
		if (!navProposals.isEmpty()) {
			return navProposals;
		}
		return getFuzzyCompletions();
	}

	protected Collection<ICompletionProposal> getFuzzyCompletions() {
		final String prefix = fuzzySearchPrefix.getPrefix(doc, offset);
		if (prefix != null) {
			Collection<Match<PropertyInfo>> matches = findMatches(prefix);
			if (matches!=null && !matches.isEmpty()) {
				ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(matches.size());
				for (final Match<PropertyInfo> match : matches) {
					DocumentEdits docEdits;
					try {
						docEdits = LazyProposalApplier.from(() -> {
								Type type = TypeParser.parse(match.data.getType());
								DocumentEdits edits = new DocumentEdits(doc);
								edits.delete(offset-prefix.length(), offset);
								edits.insert(offset, match.data.getId() + propertyCompletionPostfix(typeUtil, type));
								return edits;
						});
						proposals.add(completionFactory.property(doc, docEdits, match, typeUtil));
					} catch (Exception e) {
						Log.log(e);
					}
				}
				return proposals;
			}
		}
		return Collections.emptyList();
	}

//	public HoverInfo getHoverInfo(IDocument doc, IRegion _region) {
//		debug("getHoverInfo("+_region+")");
//
//		//The delegate 'getHoverRegion' for spring propery editor will return smaller word regions.
//		// we must ensure to use our own region finder to identify correct property name.
//		ITypedRegion region = getHoverRegion(doc, _region.getOffset());
//		if (region!=null) {
//			String contentType = region.getType();
//			try {
//				if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
//					debug("hoverRegion = "+region);
//					PropertyInfo best = findBestHoverMatch(doc.get(region.getOffset(), region.getLength()).trim());
//					if (best!=null) {
//						return new SpringPropertyHoverInfo(documentContextFinder.getJavaProject(doc), best);
//					}
//				} else if (contentType.equals(IPropertiesFilePartitions.PROPERTY_VALUE)) {
//					return getValueHoverInfo(new DocumentRegion(doc, region));
//				}
//			} catch (Exception e) {
//				SpringPropertiesEditorPlugin.log(e);
//			}
//		}
//		return null;
//	}
//
//	public ITypedRegion getHoverRegion(IDocument document, int offset) {
//		try {
//			ITypedRegion candidate = getPartition(document, offset);
//			if (candidate!=null) {
//				String type = candidate.getType();
//				if (IDocument.DEFAULT_CONTENT_TYPE.equals(type)) {
//					return candidate;
//				} else if (IPropertiesFilePartitions.PROPERTY_VALUE.equals(type)) {
//					DocumentRegion valueRegion = new DocumentRegion(document, candidate).trimStart(ASSIGN);
//					return getValueHoverRegion(valueRegion, valueRegion.toRelative(offset));
//				}
//			}
//		} catch (Exception e) {
//			SpringPropertiesEditorPlugin.log(e);
//		}
//		return null;
//	}
//
//	private ITypedRegion getValueHoverRegion(DocumentRegion r, int offset) {
//		int len = r.length();
//		if (offset>=0 && offset<=len) {
//			int start = offset;
//			while (start>0 && isValuePrefixChar(r.charAt(start-1))) {
//				start--;
//			}
//			int end = offset;
//			while (end<len && isValuePrefixChar(r.charAt(end))) {
//				end++;
//			}
//			r = r.subSequence(start, end);
//			if (!r.isEmpty()) {
//				return r.asTypedRegion(IPropertiesFilePartitions.PROPERTY_VALUE);
//			}
//		}
//		return null;
//	}

//	/**
//	 * Search known properties for the best 'match' to show as hover data.
//	 */
//	private PropertyInfo findBestHoverMatch(String propName) {
//		//TODO: optimize, should be able to use index's treemap to find this without iterating all entries.
//		PropertyInfo best = null;
//		int bestCommonPrefixLen = 0; //We try to pick property with longest common prefix
//		int bestExtraLen = Integer.MAX_VALUE;
//		for (PropertyInfo candidate : index) {
//			int commonPrefixLen = StringUtil.commonPrefixLength(propName, candidate.getId());
//			int extraLen = candidate.getId().length()-commonPrefixLen;
//			if (commonPrefixLen==propName.length() && extraLen==0) {
//				//exact match found, can stop searching for better matches
//				return candidate;
//			}
//			//candidate is better if...
//			if (commonPrefixLen>bestCommonPrefixLen // it has a longer common prefix
//			|| commonPrefixLen==bestCommonPrefixLen && extraLen<bestExtraLen //or same common prefix but fewer extra chars
//			) {
//				bestCommonPrefixLen = commonPrefixLen;
//				bestExtraLen = extraLen;
//				best = candidate;
//			}
//		}
//		return best;
//	}


	/**
	 * Find the longest known property that is a prefix of the given name. Here prefix does not mean
	 * 'string prefix' but a prefix in the sense of treating '.' as a kind of separators. So
	 * 'prefix' is not allowed to end in the middle of a 'segment'.
	 */
	public static PropertyInfo findLongestValidProperty(FuzzyMap<PropertyInfo> index, String name) {
		int bracketPos = name.indexOf('[');
		int endPos = bracketPos>=0?bracketPos:name.length();
		PropertyInfo prop = null;
		String prefix = null;
		while (endPos>0 && prop==null) {
			prefix = name.substring(0, endPos);
			String canonicalPrefix = camelCaseToHyphens(prefix);
			prop = index.get(canonicalPrefix);
			if (prop==null) {
				endPos = name.lastIndexOf('.', endPos-1);
			}
		}
		if (prop!=null) {
			//We should meet caller's expectation that matched properties returned by this method
			// match the names exactly even if we found them using relaxed name matching.
			return prop.withId(prefix);
		}
		return null;
	}


}
