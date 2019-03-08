/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.JsonElement;

public class GsonUtil {
	
	public static Stream<JsonElement> getFromElements(JsonElement target) {
		if (target.isJsonArray()) {
			return Streams.fromIterable(target.getAsJsonArray());
		} else if (target.isJsonObject()) {
			List<JsonElement> vals = new ArrayList<>();
			Set<Entry<String, JsonElement>> entrySet = target.getAsJsonObject().entrySet();
			if (entrySet != null) {
				for (Entry<String, JsonElement> entry : entrySet) {
					vals.add(entry.getValue());
				}
			}
			return vals.stream();
		} else {
			return Stream.empty();
		}
	}

	public static Stream<JsonElement> getFromIndex(JsonElement target, int index) {
		if (target.isJsonArray()) {
			return Streams.fromNullable(target.getAsJsonArray().get(index));
		} else {
			return Stream.empty();
		}
	}

	public static Stream<JsonElement> getFromKey(JsonElement target, String key) {
		if (target.isJsonObject()) {
			return Streams.fromNullable(target.getAsJsonObject().get(key));
		} else {
			return Stream.empty();
		}
	}

	public static String getAsString(JsonElement target) {
		// Need to check if it is a primitive before get as string, otherwise
		// exceptions can be thrown. See the JsonElement javadoc
		if (target != null && target.isJsonPrimitive()) {
			return target.getAsString();
		}
		return null;
	}
}
