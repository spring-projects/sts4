/*******************************************************************************
 * Copyright (c) 2014-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.util.Collection;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

/**
 * Parser for checking a 'Enum' style values.
 *
 * @author Kris De Volder
 */
public class EnumValueParser implements ValueParser {

	private String typeName;
	private Provider<Collection<String>> values;
	

	public EnumValueParser(String typeName, String... values) {
		this(typeName, ImmutableSet.copyOf(values));
	}

	public EnumValueParser(String typeName, Collection<String> values) {
		this(typeName, () -> values);
	}
	
	public EnumValueParser(String typeName, Provider<Collection<String>> values) {
		this.typeName = typeName;
		this.values = values;
	}

	public Object parse(String str) {
		// IMPORTANT: check the text FIRST before fetching values
		// from the hints provider, as the hints provider may be expensive when resolving values
		if (!StringUtil.hasText(str)) {
			throw new IllegalArgumentException(createBlankTextErrorMessage());
		}
		
		Collection<String> values = this.values.get();
		//If values is not known (null) then just assume the str is acceptable.
		if (values==null || values.contains(str)) {
			return str;
		} else {
			throw new IllegalArgumentException(createErrorMessage(str, values));
		}
	}

	protected String createBlankTextErrorMessage() {
		return "'"+typeName+"'" + " cannot be blank.";
	}

	protected String createErrorMessage(String parseString, Collection<String> values) {
		return "'"+parseString+"' is not valid for Enum '"+typeName+"'. Valid values are: "+values;
	}

}
