/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapter.PropertyChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;
import org.springframework.ide.vscode.commons.protocol.java.TypeDescriptorData;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Helps to parse JSON for <code>Either<TypeDescriptorData, TypeData></code>.
 * Creates either {@link TypeData} or {@link TypeDescriptorData} based on
 * {@link JsonObject} properties.
 * 
 * @author Alex Boyko
 *
 */
public class TypeHierarchyResponseAdapter implements TypeAdapterFactory {

	private static final TypeToken<Either<TypeDescriptorData, TypeData>> ELEMENT_TYPE = new TypeToken<Either<TypeDescriptorData, TypeData>>() {
	};

	@SuppressWarnings("unchecked")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		Predicate<JsonElement> rightChecker = new PropertyChecker("classpathEntry", JsonObject.class)
				.or(new PropertyChecker("bindingKey", JsonPrimitive.class));
		Predicate<JsonElement> leftChecker = new PropertyChecker("fqName", JsonPrimitive.class)
				.and(rightChecker.negate());
		TypeAdapter<Either<TypeDescriptorData, TypeData>> elementTypeAdapter = new EitherTypeAdapter<>(gson,
				ELEMENT_TYPE, leftChecker, rightChecker);
		return (TypeAdapter<T>) new CollectionTypeAdapter<>(gson, ELEMENT_TYPE.getType(), elementTypeAdapter,
				ArrayList::new);
	}

}
