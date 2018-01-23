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

import org.json.JSONObject;

public class Boot20DispatcherServletMapping extends AbstractRequestMapping {

	/*

Some example entries:

[
                  {
                     "handler":"ResourceHttpRequestHandler [locations=[class path resource [META-INF/resources/], class path resource [resources/], class path resource [static/], class path resource [public/], ServletContext resource [/], class path resource []], resolvers=[org.springframework.web.servlet.resource.PathResourceResolver@7a63363d]]",
                     "predicate":"/** /favicon.ico"
                  },
                  {
                     "handler":"public java.lang.Object org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$OperationHandler.handle(javax.servlet.http.HttpServletRequest,java.util.Map<java.lang.String, java.lang.String>)",
                     "predicate":"{[/actuator/health],methods=[GET],produces=[application/vnd.spring-boot.actuator.v2+json || application/json]}"
                  },
                  {
                     "handler":"public java.lang.Object org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$OperationHandler.handle(javax.servlet.http.HttpServletRequest,java.util.Map<java.lang.String, java.lang.String>)",
                     "predicate":"{[/actuator/info],methods=[GET],produces=[application/vnd.spring-boot.actuator.v2+json || application/json]}"
                  },
                  {
                     "handler":"protected java.util.Map<java.lang.String, java.util.Map<java.lang.String, org.springframework.boot.actuate.endpoint.web.Link>> org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping.links(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)",
                     "predicate":"{[/actuator],methods=[GET],produces=[application/vnd.spring-boot.actuator.v2+json || application/json]}"
                  },
                  {
                     "handler":"public com.example.SomeData com.example.ActuatorClientTestSubjectApplication.getMethodName(java.lang.String)",
                     "predicate":"{[/path],methods=[GET]}"
                  },
                  {
                     "handler":"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)",
                     "predicate":"{[/error],produces=[text/html]}"
                  },
                  {
                     "handler":"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.error(javax.servlet.http.HttpServletRequest)",
                     "predicate":"{[/error]}"
                  },
                  {
                     "handler":"ResourceHttpRequestHandler [locations=[class path resource [META-INF/resources/webjars/]], resolvers=[org.springframework.web.servlet.resource.PathResourceResolver@78837bf]]",
                     "predicate":"/webjars/**"
                  },
                  {
                     "handler":"ResourceHttpRequestHandler [locations=[class path resource [META-INF/resources/], class path resource [resources/], class path resource [static/], class path resource [public/], ServletContext resource [/]], resolvers=[org.springframework.web.servlet.resource.PathResourceResolver@64b93bad]]",
                     "predicate":"/**"
                  }
               ]

	 */

	private JSONObject data;

	public Boot20DispatcherServletMapping(JSONObject data) {
		this.data = data;
	}

	@Override
	public String getMethodString() {
		return data.optString("handler");
	}

	@Override
	protected String getPredicateString() {
		return data.optString("predicate");
	}

}
