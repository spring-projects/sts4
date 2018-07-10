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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;

/**
 * A abstract base class which attempts to capture commonalities between
 * Local and Remote connections to SpringBootApp using JMX.
 */
public abstract class SpringBootApp {

	public abstract String getProcessID();
	public abstract String getProcessName();
	public abstract boolean isSpringBootApp();
	public abstract String[] getClasspath() throws IOException;
	public abstract String getJavaCommand() throws IOException;
	public abstract String getHost() throws Exception;
	public abstract String getPort() throws Exception;

	public abstract Optional<List<LiveConditional>> getLiveConditionals() throws Exception;
	public abstract Collection<RequestMapping> getRequestMappings() throws Exception;
	public abstract LiveBeansModel getBeans();
	public abstract List<String> getActiveProfiles();
	public abstract String getEnvironment() throws Exception;


}
