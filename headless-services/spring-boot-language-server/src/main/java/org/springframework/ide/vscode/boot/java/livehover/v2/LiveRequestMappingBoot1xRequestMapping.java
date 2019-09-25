/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import org.json.JSONObject;

import com.google.common.base.Objects;

public class LiveRequestMappingBoot1xRequestMapping extends LiveRequestMappingAbstractImpl {

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

	private JSONObject beanInfo;
	private String pathKey;

	public LiveRequestMappingBoot1xRequestMapping(String pathKey, JSONObject beanInfo) {
		this.pathKey = pathKey;
		this.beanInfo = beanInfo;
	}

	@Override
	public int hashCode() {
		return pathKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiveRequestMappingBoot1xRequestMapping other = (LiveRequestMappingBoot1xRequestMapping) obj;
		return Objects.equal(this.pathKey, other.pathKey)
			&& Objects.equal(this.getMethodString(), other.getMethodString());
	}

	@Override
	public String getMethodString() {
		return beanInfo.optString("method");
	}

	@Override
	protected String getPredicateString() {
		return pathKey;
	}

}