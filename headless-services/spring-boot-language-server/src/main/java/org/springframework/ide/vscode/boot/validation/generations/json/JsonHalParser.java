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
package org.springframework.ide.vscode.boot.validation.generations.json;

import com.google.gson.Gson;

public class JsonHalParser {

	public <T> T getEmbedded(String json, Class<? extends JsonHalEmbedded<T>> clazz) throws Exception {
		if (json != null) {
			Gson gson = new  Gson();
			JsonHalEmbedded<T> embedded = gson.fromJson(json, clazz);
			return embedded.get_embedded();
		}
		return null;
	}
}
