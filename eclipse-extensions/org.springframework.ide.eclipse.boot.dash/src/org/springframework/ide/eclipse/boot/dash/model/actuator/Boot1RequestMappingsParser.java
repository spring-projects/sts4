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
package org.springframework.ide.eclipse.boot.dash.model.actuator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;

public class Boot1RequestMappingsParser implements RequestMappingsParser {

	/*
	   There are two styles of entries:

	   1) key is a 'path' String. May contain patters like "**"
		   "/** /favicon.ico":{
	          "bean":"faviconHandlerMapping"
	       }

	   2) key is a 'almost json' String
	       "{[/bye],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}":{
	          "bean":"requestMappingHandlerMapping",
	          "method":"public java.lang.String demo.MyController.bye()"
	       }
			 */



	@Override
	public List<RequestMapping> parse(JSONObject obj, TypeLookup typeLookup) throws JSONException {
		@SuppressWarnings("unchecked")
		Iterator<String> keys = obj.keys();
		List<RequestMapping> result = new ArrayList<>();
		while (keys.hasNext()) {
			String rawKey = keys.next();
			JSONObject value = obj.getJSONObject(rawKey);
			Collection<RequestMapping1x> mappings = RequestMapping1x.create(rawKey, value.optString("method"), typeLookup);
			result.addAll(mappings);
		}
		return result;
	}

}
