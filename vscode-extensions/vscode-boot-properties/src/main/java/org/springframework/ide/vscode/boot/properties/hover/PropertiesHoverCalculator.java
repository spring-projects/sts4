package org.springframework.ide.vscode.boot.properties.hover;

import static org.springframework.ide.vscode.boot.properties.tools.CommonLanguageTools.SPACES;
import static org.springframework.ide.vscode.boot.properties.tools.CommonLanguageTools.getValueHints;
import static org.springframework.ide.vscode.boot.properties.tools.CommonLanguageTools.getValueType;
import static org.springframework.ide.vscode.commons.util.Renderables.*;

import java.util.Collection;
import java.util.Optional;

import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndex;
import org.springframework.ide.vscode.application.properties.metadata.hints.StsValueHint;
import org.springframework.ide.vscode.application.properties.metadata.types.Type;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil.EnumCaseMode;
import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.boot.properties.tools.PropertyRenderableProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.BadLocationException;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.languageserver.util.IRegion;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.java.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.java.properties.parser.ParseResults;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Key;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Node;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

class PropertiesHoverCalculator {
	
	private FuzzyMap<PropertyInfo> index;
	private TypeUtil typeUtil;
	private IJavaProject project;
	private IDocument doc;
	private int offset;
	private AntlrParser parser;
	
	PropertiesHoverCalculator(FuzzyMap<PropertyInfo> index, TypeUtil typeUtil, IJavaProject project, IDocument doc, int offset) {
		this.index = index;
		this.typeUtil = typeUtil;
		this.project = project;
		this.doc = doc;
		this.offset = offset;
		this.parser = new AntlrParser();
	}
	
	Tuple2<Renderable, IRegion> calculate() {
		ParseResults parseResults = parser.parse(doc.get());
		Node node = parseResults.ast.findNode(offset);
		if (node instanceof Value) {
			return getValueHover((Value)node);
		} else if (node instanceof Key) {
			return getPropertyHover((Key)node);
		}
		return null;
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
	
	private Tuple2<Renderable, IRegion> getPropertyHover(Key property) {
		PropertyInfo best = findBestHoverMatch(property.decode());
		if (best == null) {
			return null;
		} else {
			Renderable renderable = new PropertyRenderableProvider(project, best).getRenderable();
			DocumentRegion region = createRegion(doc, property);
			return Tuples.of(renderable, region.asRegion());
		}
	}
	
	private Tuple2<Renderable, IRegion> getValueHover(Value value) {
		DocumentRegion valueRegion = createRegion(doc, value).trimStart(SPACES).trimEnd(SPACES);
		if (valueRegion.getStart() <= offset && offset < valueRegion.getEnd()) {
			String valueString = valueRegion.toString();
			String propertyName = value.getParent().getKey().decode();
			Type type = getValueType(index, typeUtil, propertyName);
			if (TypeUtil.isArray(type) || TypeUtil.isList(type)) {
				//It is useful to provide content assist for the values in the list when entering a list
				type = TypeUtil.getDomainType(type);
			}
			if (TypeUtil.isClass(type)) {
				//Special case. We want to provide hoverinfos more liberally than what's suggested for completions (i.e. even class names
				//that are not suggested by the hints because they do not meet subtyping constraints should be hoverable and linkable!
				StsValueHint hint = StsValueHint.className(valueString, typeUtil);
				if (hint!=null) {
					return Tuples.of(createRenderable(hint), valueRegion.asRegion());
				}
			}
			//Hack: pretend to invoke content-assist at the end of the value text. This should provide hints applicable to that value
			// then show hoverinfo based on that. That way we can avoid duplication a lot of similar logic to compute hoverinfos and hyperlinks.
			Collection<StsValueHint> hints = getValueHints(index, typeUtil, valueString, propertyName, EnumCaseMode.ALIASED);
			if (hints!=null) {
				Optional<StsValueHint> hint = hints.stream().filter(h -> valueString.equals(h.getValue())).findFirst();
				if (hint.isPresent()) {
					return Tuples.of(createRenderable(hint.get()), valueRegion.asRegion());
				}
			}
		}
		return null;
	}
	
	private Renderable createRenderable(StsValueHint hint) {
		Renderable description = hint.getDescription();
		/*
		 * HACK: javadoc comment from HTML javadoc provider coming from
		 * generated HTML javadoc is very rich and decorating it further
		 * with some header like labels just makes it look worse
		 */
		if (description.toHtml().indexOf("<h") == -1) {
			Builder<Renderable> renderableBuilder = ImmutableList.builder();
			renderableBuilder.add(bold(text(hint.getValue())));
			renderableBuilder.add(paragraph(description));
			return concat(renderableBuilder.build());
		} else {
			return description;
		}
	}

	/**
	 * Search known properties for the best 'match' to show as hover data.
	 */
	private PropertyInfo findBestHoverMatch(String propName) {
		PropertyInfo propertyInfo = index.get(propName);
		if (propertyInfo == null) {
			propertyInfo = SpringPropertyIndex.findLongestValidProperty(index, propName);
		}
		return propertyInfo;
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
	}

}
