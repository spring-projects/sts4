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
package org.springframework.ide.vscode.yaml.util;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Parser for checking a 'Enum' style values.
 *
 * @author Kris De Volder
 */
public class EnumValueParser implements ValueParser {

	private String typeName;
	private Set<String> values;

	public EnumValueParser(String typeName, String... values) {
		this(typeName, ImmutableSet.copyOf(values));
	}

	public EnumValueParser(String typeName, Collection<String> values) {
		this.typeName = typeName;
		this.values = ImmutableSet.copyOf(values);
	}

	public Object parse(String str) {
		if (values.contains(str)) {
			return str;
		} else {
			throw new IllegalArgumentException("'"+str+"' is not valid for Enum '"+typeName+"'");
		}
	}

}
