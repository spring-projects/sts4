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
package org.springframework.ide.vscode.boot.java.metrics.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Map;

import org.junit.Test;
import org.springframework.ide.vscode.boot.java.livehover.v2.StartupMetricsModel;

import com.google.gson.Gson;

public class StartupMetricsTest {
	
	@Test
	public void testParser() throws Exception {
		Gson gson = new Gson();
		Map<?,?> mapContent = gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/test-files/startup.json")), Map.class);
		StartupMetricsModel startupMetricsModel = StartupMetricsModel.parse(mapContent);
		assertNotNull(startupMetricsModel);
		assertEquals(419, startupMetricsModel.getStartupEvents().size());
		assertEquals(Duration.ofNanos(10298253), startupMetricsModel.getBeanInstanciationTime("ownerController"));
	}

}
