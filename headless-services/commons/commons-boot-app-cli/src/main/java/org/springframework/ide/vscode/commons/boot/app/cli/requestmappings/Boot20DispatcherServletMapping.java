/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli.requestmappings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.objectweb.asm.Type;

public class Boot20DispatcherServletMapping implements RequestMapping {

/*

Example entry:

{
  "handler": "public java.lang.String com.example.demo.RestApi.aha(java.lang.String)",
  "predicate": "{[/genericHello || /aha],methods=[GET]}",
  "details": {
    "requestMappingConditions": {
      "headers": [],
      "methods": [
        "GET"
      ],
      "patterns": [
        "/genericHello",
        "/aha"
      ],
      "produces": [],
      "params": [],
      "consumes": []
    },
    "handlerMethod": {
      "name": "aha",
      "className": "com.example.demo.RestApi",
      "descriptor": "(Ljava/lang/String;)Ljava/lang/String;"
    }
  }
}
*/

	private JSONObject data;

	public Boot20DispatcherServletMapping(JSONObject data) {
		this.data = data;
	}

	private JSONObject getDetails() {
		return data.optJSONObject("details");
	}

	private JSONObject getHandlerMethod() {
		JSONObject details = getDetails();
		return details == null ? null : details.getJSONObject("handlerMethod");
	}

	private JSONObject getRequestMappingConditions() {
		JSONObject details = getDetails();
		return details == null ? null : details.getJSONObject("requestMappingConditions");
	}

	@Override
	public String getMethodString() {
		return data.optString("handler");
	}


	@Override
	public String getFullyQualifiedClassName() {
		JSONObject handlerMethod = getHandlerMethod();
		return handlerMethod == null ? null : handlerMethod.getString("className");
	}

	@Override
	public String getMethodName() {
		JSONObject handlerMethod = getHandlerMethod();
		return handlerMethod == null ? null : handlerMethod.getString("name");
	}

	@Override
	public String[] getMethodParameters() {
		JSONObject handlerMethod = getHandlerMethod();
		if (handlerMethod != null) {
			Type type = Type.getMethodType(handlerMethod.getString("descriptor"));
			Type[] argsTypes = type.getArgumentTypes();
			String[] parameterTypes = new String[argsTypes.length];
			for (int i = 0; i < argsTypes.length; i++) {
				parameterTypes[i] = argsTypes[i].getClassName();
			}
			return parameterTypes;
		}
		return new String[0];
	}

	@Override
	public String[] getSplitPath() {
		JSONObject rmConditions = getRequestMappingConditions();
		if (rmConditions == null) {
			return new String[0];
		} else {
			JSONArray jsonArray = rmConditions.getJSONArray("patterns");
			String[] paths = new String[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				paths[i] = jsonArray.getString(i);
			}
			return paths;
		}
	}

	@Override
	public Set<String> getRequestMethods() {
		JSONObject rmConditions = getRequestMappingConditions();
		if (rmConditions == null) {
			return Collections.emptySet();
		} else {
			JSONArray jsonArray = rmConditions.getJSONArray("methods");
			Set<String> methods = new HashSet<>();
			for (int i = 0; i < jsonArray.length(); i++) {
				methods.add(jsonArray.getString(i));
			}
			return methods;
		}
	}

}
