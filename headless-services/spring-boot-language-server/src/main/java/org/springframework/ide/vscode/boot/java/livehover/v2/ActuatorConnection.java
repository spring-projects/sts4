/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public interface ActuatorConnection {
	
	String getEnvironment();

	String getProcessID();

	Properties getSystemProperties();

	String getConditionalsReport() throws IOException;

	String getRequestMappings() throws IOException;

	String getBeans() throws IOException;

	String getMetrics(String metric, Map<String, String> tags) throws IOException;

	Map<?, ?> getStartup() throws IOException;
	
//	String getGcPausesMetrics() throws IOException;

//	String getMemoryMetrics(String metricName) throws IOException;
}
