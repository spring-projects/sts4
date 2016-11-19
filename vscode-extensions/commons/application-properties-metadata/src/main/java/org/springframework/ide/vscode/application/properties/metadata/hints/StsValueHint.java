package org.springframework.ide.vscode.application.properties.metadata.hints;

import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil;
import org.springframework.ide.vscode.application.properties.metadata.util.DeprecationUtil;
import org.springframework.ide.vscode.commons.java.IJavaElement;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfo;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.HtmlBuffer;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.yaml.util.DescriptionProviders;

/**
 * Sts version of {@link ValueHint} contains similar data, but accomoates
 * a html snippet to be computed lazyly for the description.
 * <p>
 * This is meant to support using data pulled from JavaDoc in enums as description.
 * This data is a html snippet, whereas the data derived from spring-boot metadata is
 * just plain text.
 *
 * @author Kris De Volder
 */
public class StsValueHint {


	private final String value;
	private final HoverInfo description;
	private final Deprecation deprecation;

	/**
	 * Create a hint with a textual description.
	 * <p>
	 * This constructor is private. Use one of the provided
	 * static 'create' methods instead.
	 */
	private StsValueHint(String value, HoverInfo description, Deprecation deprecation) {
		this.value = value==null?"null":value.toString();
		Assert.isLegal(!this.value.startsWith("StsValueHint"));
		this.description = description;
		this.deprecation = deprecation;
	}

	/**
	 * Creates a hint out of an IJavaElement.
	 */
	public static StsValueHint create(String value, IJavaElement javaElement) {
		return new StsValueHint(value, javaDocSnippet(javaElement), DeprecationUtil.extract(javaElement)) {
			@Override
			public IJavaElement getJavaElement() {
				return javaElement;
			}
		};
	}

	public static StsValueHint create(String value) {
		return new StsValueHint(value, DescriptionProviders.NO_DESCRIPTION, null);
	}

	public static StsValueHint create(ValueHint hint) {
		return new StsValueHint(""+hint.getValue(), textSnippet(hint.getDescription()), null);
	}

	public static StsValueHint className(String fqName, TypeUtil typeUtil) {
		try {
			IJavaProject jp = typeUtil.getJavaProject();
			if (jp!=null) {
				IType type = jp.findType(fqName);
				if (type!=null) {
					return create(type);
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	public static StsValueHint create(IType klass) {
		return new StsValueHint(klass.getFullyQualifiedName(), javaDocSnippet(klass), DeprecationUtil.extract(klass)) {
			@Override
			public IJavaElement getJavaElement() {
				return klass;
			}
		};
	}

	/**
	 * Create a html snippet from a text snippet.
	 */
	private static HoverInfo textSnippet(String description) {
		if (StringUtil.hasText(description)) {
			return DescriptionProviders.text(description);
		}
		return DescriptionProviders.NO_DESCRIPTION;
	}

	public String getValue() {
		return value;
	}

	public HoverInfo getDescription() {
		return description;
	}
	public HoverInfo getDescriptionProvider() {
		return description;
	}

	public static HoverInfo javaDocSnippet(IJavaElement je) {
		try {
			IJavadoc jdoc = je.getJavaDoc();
			if (jdoc != null) {
				return new HoverInfo() {

					@Override
					public void renderAsMarkdown(StringBuilder buffer) {
						// TODO not correct md
						buffer.append(jdoc.markdown());
					}

					@Override
					public void renderAsHtml(HtmlBuffer buffer) {
						buffer.raw(jdoc.html());
					}
				};
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return DescriptionProviders.NO_DESCRIPTION;
	}

	@Override
	public String toString() {
		return "StsValueHint("+value+")";
	}

	public Deprecation getDeprecation() {
		return deprecation;
	}

	public IJavaElement getJavaElement() {
		return null;
	}

	public StsValueHint prefixWith(String prefix) {
		StsValueHint it = this;
		return new StsValueHint(prefix+getValue(), description, deprecation) {
			@Override
			public IJavaElement getJavaElement() {
				return it.getJavaElement();
			}
		};
	}


}
