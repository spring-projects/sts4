/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml;

/**
 * @author Alex Boyko
 */
public class XmlConfigConstants {
	
	public static final String BEANS_NAMESPACE = "http://www.springframework.org/schema/beans";
	
	public static final String BEAN_ELEMENT = 				"bean";
	public static final String CONSTRUCTOR_ARG_ELEMENT = 	"constructor-arg";
	public static final String ARG_TYPE_ELEMENT = 			"arg-type";
	public static final String VALUE_ELEMENT = 				"value";
	public static final String PROPERTY_ELEMENT = 			"property";
	public static final String REF_ELEMENT = 				"ref";
	public static final String IDREF_ELEMENT = 				"idref";
	public static final String ALIAS_ELEMENT = 				"alias";
	public static final String REPLACED_METHOD_ELEMENT = 	"replaced-method";
	public static final String ENTRY_ELEMENT = 				"entry";
	public static final String LOOKUP_METHOD_ELEMENT = 		"lookup-method";
	
	public static final String CLASS_ATTRIBUTE = 		"class";
	public static final String TYPE_ATTRIBUTE = 		"type";
	public static final String NAME_ATTRIBUTE = 		"name";
	public static final String REF_ATTRIBUTE = 			"ref";
	public static final String MATCH_ATTRIBUTE = 		"match";
	public static final String PARENT_ATTRIBUTE = 		"parent";
	public static final String DEPENDS_ON_ATTRIBUTE = 	"depends-on";
	public static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";
	public static final String BEAN_ATTRIBUTE = 		"bean";
	public static final String REPLACER_ATTRIBUTE = 	"replacer";
	public static final String VALUE_REF_ATTRIBUTE = 	"value-ref";
	public static final String KEY_REF_ATTRIBUTE = 		"key-ref";
	public static final String LOCAL_ATTRIBUTE = 		"local";
	public static final String VALUE_ATTRIBUTE = 		"value";


	public static final String CONTEXT_NAMESPACE = "http://www.springframework.org/schema/context";
	
	public static final String COMPONENT_SCAN_ELEMENT = 	"component-scan";
	public static final String BASE_PACKAGE_ATTRIBUTE = 	"base-package";
	public static final String NAME_GENERATOR_ATTRIBUTE = 	"name-generator";
	public static final String SCOPE_RESOLVER_ATTRIBUTE = 	"scope-resolver";

	public static final String UTIL_NAMESPACE = "http://www.springframework.org/schema/util";

	public static final String VALUE_TYPE_ATTRIBUTE = 	"value-type";
	public static final String KEY_TYPE_ATTRIBUTE = 	"key-type";

}
