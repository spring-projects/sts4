/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.yaml.reconcile;

import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.*;

import java.util.ArrayList;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

/**
 * @author Kris De Volder
 */
public enum SpringPropertiesProblemType implements ProblemType {

	// Naming:
	//   YAML_* for all problems in .yml files.
	//   PROP_* for all problems in .properties files.
	// All enum values must start with one or the other (or some stuff will break!).

//	PROP_INVALID_BEAN_NAVIGATION("Accessing a 'bean property' in a type that doesn't have properties (e.g. like String or Integer)"),
//	PROP_INVALID_INDEXED_NAVIGATION("Accessing a property using [] in a type that doesn't support that"),
//	PROP_EXPECTED_DOT_OR_LBRACK("Unexpected character found where a '.' or '[' was expected"),
//	PROP_NO_MATCHING_RBRACK("Found a '[' but no matching ']'"),
//	PROP_NON_INTEGER_IN_BRACKETS("Use of [..] navigation with non-integer value"),
//	PROP_VALUE_TYPE_MISMATCH("Expecting a value of a certain type, but value doesn't parse as such"),
//	PROP_INVALID_BEAN_PROPERTY("Accessing a named property in a type that doesn't provide a property accessor with that name"),
//	PROP_UNKNOWN_PROPERTY(WARNING, "Property-key not found in any configuration metadata on the project's classpath"),
//	PROP_DEPRECATED(WARNING, "Property is marked as Deprecated"),
//	PROP_DUPLICATE_KEY("Multiple assignments to the same property value"),

	YAML_SYNTAX_ERROR("Error parsing the input using snakeyaml"),
	YAML_UNKNOWN_PROPERTY(WARNING, "Property-key not found in the configuration metadata on the project's classpath"),
	YAML_VALUE_TYPE_MISMATCH("Expecting a value of a certain type, but value doesn't parse as such"),
	YAML_EXPECT_SCALAR("Expecting a 'scalar' value but found something more complex."),
	YAML_EXPECT_TYPE_FOUND_SEQUENCE("Found a 'sequence' node where a non 'list-like' type is expected"),
	YAML_EXPECT_TYPE_FOUND_MAPPING("Found a 'mapping' node where a type that can't be treated as a 'property map' is expected"),
	YAML_EXPECT_MAPPING("Expecting a 'mapping' node but found something else"),
	YAML_EXPECT_BEAN_PROPERTY_NAME("Expecting a 'bean property' name but found something more complex"),
	YAML_INVALID_BEAN_PROPERTY("Accessing a named property in a type that doesn't provide a property accessor with that name"),
	YAML_DEPRECATED(WARNING, "Property is marked as Deprecated"),
	YAML_DUPLICATE_KEY("A mapping node contains multiple entries for the same key");

	private final ProblemSeverity defaultSeverity;
	private String description;
	private String label;

	private SpringPropertiesProblemType(ProblemSeverity defaultSeverity, String description, String label) {
		this.description = description;
		this.defaultSeverity = defaultSeverity;
		this.label = label;
	}

	private SpringPropertiesProblemType(ProblemSeverity defaultSeverity, String description) {
		this(defaultSeverity, description, null);
	}

	private SpringPropertiesProblemType(String description) {
		this(ERROR, description);
	}

	public ProblemSeverity getDefaultSeverity() {
		return defaultSeverity;
	}


	public static SpringPropertiesProblemType[] forProperties() {
		return withPrefix("PROP_");
	}


	private static SpringPropertiesProblemType[] withPrefix(String prefix) {
		SpringPropertiesProblemType[] allValues = values();
		ArrayList<SpringPropertiesProblemType> values = new ArrayList<SpringPropertiesProblemType>(allValues.length);
		for (SpringPropertiesProblemType v : allValues) {
			if (v.toString().startsWith(prefix)) {
				values.add(v);
			}
		}
		return values.toArray(new SpringPropertiesProblemType[values.size()]);
	}

	public String getLabel() {
		if (label==null) {
			label = createDefaultLabel();
		}
		return label;
	}

	public String getDescription() {
		return description;
	}

	private String createDefaultLabel() {
		String label = this.toString().substring(5).toLowerCase().replace('_', ' ');
		return Character.toUpperCase(label.charAt(0)) + label.substring(1);
	}

	@Override
	public String getCode() {
		return name();
	}

// TODO: obsolete? We should simply keep the problemtype implementations for yaml / props editor totally separate
//	public static final SpringPropertiesProblemType[] FOR_YAML = FOR(EditorType.YAML);
//	public static final SpringPropertiesProblemType[] FOR_PROPERTIES = FOR(EditorType.PROP);
//	public static SpringPropertiesProblemType[] FOR(EditorType et) {
//		return withPrefix(et.getProblemTypePrefix());
//	}
//	public EditorType getEditorType() {
//		String string = this.toString();
//		for (EditorType et : EditorType.values()) {
//			String prefix = et.getProblemTypePrefix();
//			if (string.startsWith(prefix)) {
//				return et;
//			}
//		}
//		throw new IllegalStateException("Bug: unknown editor type for "+this);
//	}
}
