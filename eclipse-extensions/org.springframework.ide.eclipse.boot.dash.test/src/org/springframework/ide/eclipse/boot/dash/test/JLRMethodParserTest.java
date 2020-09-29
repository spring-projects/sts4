/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import org.springframework.ide.eclipse.boot.dash.model.actuator.JLRMethodParser;

import junit.framework.TestCase;

public class JLRMethodParserTest extends TestCase {

	private static String getFQClassName(String data) {
		return JLRMethodParser.parseFQClassName(data);
	}

	private static String getMethodName(String data) {
		return JLRMethodParser.parseMethodName(data);
	}

	public void testCase1() throws Exception {
		String data = "public java.lang.Object org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint.invoke(java.security.Principal)";
		assertEquals("org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint", getFQClassName(data));
		assertEquals("invoke", getMethodName(data));
	}

	public void testCase1b() throws Exception {
		String data = "public synchronized java.lang.Object org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint.invoke(java.security.Principal)";
		assertEquals("org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint", getFQClassName(data));
		assertEquals("invoke", getMethodName(data));
	}

	public void testCase2() throws Exception {
		String data = "java.util.Collection<demo.Reservation> demo.ReservationRestController.reservations()";
		assertEquals("demo.ReservationRestController", getFQClassName(data));
		assertEquals("reservations", getMethodName(data));
	}


	public void testCase3() throws Exception {
		String data = "public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)";
		assertEquals("org.springframework.boot.autoconfigure.web.BasicErrorController", getFQClassName(data));
		assertEquals("error", getMethodName(data));
	}

	public void testCase4() throws Exception {
		String data = "public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest) throws java.lang.Exception";
		assertEquals("org.springframework.boot.autoconfigure.web.BasicErrorController", getFQClassName(data));
		assertEquals("error", getMethodName(data));
	}

	public void testGarbage() throws Exception {
		assertNull(getFQClassName(null));
		assertNull(getFQClassName(""));
		assertNull(getFQClassName("haha"));
		assertNull(getFQClassName("String haha()"));
		assertNull(getFQClassName("public synchronized String haha()"));

		assertNull(getMethodName(null));
		assertNull(getMethodName(""));
		assertNull(getMethodName("haha"));
		assertNull(getMethodName("String haha()"));
		assertNull(getMethodName("public synchronized String haha()"));
	}

}
