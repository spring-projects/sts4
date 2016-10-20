package org.springframework.ide.vscode.application.properties.metadata.types;

import javax.inject.Provider;

import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.ide.vscode.commons.util.HtmlSnippet;
import org.springframework.ide.vscode.yaml.schema.YTypedProperty;
import org.springframework.ide.vscode.yaml.util.DescriptionProviders;

/**
 * Represents a property on a Type that can be accessed by name.
 *
 * @author Kris De Volder
 */
public class TypedProperty implements YTypedProperty {

	/**
	 * The name of the property
	 */
	private final String name;

	/**
	 * The type of value associated with the property.
	 */
	private final Type type;

	/**
	 * Provides a description for this property.
	 */
	private final Provider<HtmlSnippet> descriptionProvider;

	private final Deprecation deprecation;

	public TypedProperty(String name, Type type, Deprecation deprecation) {
		this(name, type, DescriptionProviders.NO_DESCRIPTION, deprecation);
	}

	public TypedProperty(String name, Type type, Provider<HtmlSnippet> descriptionProvider, Deprecation deprecation) {
		this.name = name;
		this.type = type;
		this.descriptionProvider = descriptionProvider;
		this.deprecation = deprecation;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return name + "::" + type;
	}

	@Override
	public HtmlSnippet getDescription() {
		//TODO: real implementation that somehow gets this from somewhere (i.e. the JavaDoc)
		// Note that presently the application.yml and application.properties editor do not actually
		// use this description provider but produce hover infos in a different way (so this is only
		// used in Schema-based content assist, reconciling and hovering.
		//So in that sense putting a good implementation here is kind of pointless right now.
		//More refactoring needs to be done to also make use of this.
		return descriptionProvider.get();
	}

	public static Type typeOf(TypedProperty typedProperty) {
		if (typedProperty!=null) {
			return typedProperty.getType();
		}
		return null;
	}

	public boolean isDeprecated() {
		return deprecation!=null;
	}

	public String getDeprecationReplacement() {
		if (deprecation!=null) {
			return deprecation.getReplacement();
		}
		return null;
	}

	public String getDeprecationReason() {
		if (deprecation!=null) {
			return deprecation.getReason();
		}
		return null;
	}

	public Deprecation getDeprecation() {
		return deprecation;
	}
}
