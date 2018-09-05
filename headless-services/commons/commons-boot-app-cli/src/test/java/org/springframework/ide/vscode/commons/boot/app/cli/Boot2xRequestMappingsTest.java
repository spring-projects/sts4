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
package org.springframework.ide.vscode.commons.boot.app.cli;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMappingsParser20;

public class Boot2xRequestMappingsTest {

	@Test
	public void testWebRms() throws Exception {
		String json = IOUtils.toString(Boot2xRequestMappingsTest.class.getResourceAsStream("/live-rm-beans/rms-boot2-web.json"));
		Collection<RequestMapping> rms = RequestMappingsParser20.parse(new JSONObject(json));
		assertEquals(10, rms.size());
	}

	@Test
	public void testWebFluxAnnotationRms() throws Exception {
		String json = IOUtils.toString(Boot2xRequestMappingsTest.class.getResourceAsStream("/live-rm-beans/rms-boot2-webflux.json"));
		Collection<RequestMapping> rms = RequestMappingsParser20.parse(new JSONObject(json));
		assertEquals(7, rms.size());
	}

}
