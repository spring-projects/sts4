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
package org.springframework.ide.eclipse.boot.dash.test.actuator;

import static org.junit.Assert.fail;

import java.util.List;

import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;

public class RequestMappingAsserts {

	public static RequestMapping assertRequestMappingWithPath(List<RequestMapping> mappings, String string) {
		StringBuilder builder = new StringBuilder();
		for (RequestMapping m : mappings) {
			builder.append(m.getPath()+"\n");
			if (m.getPath().equals(string)) {
				return m;
			}
		}
		fail(
				"Expected path not found: "+string+"\n" +
				"Found:\n" +
				builder
		);
		return null;
	}

}
