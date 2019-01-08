/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;

import reactor.core.Disposable;

public interface SpringBootApp extends Disposable {

	String[] getClasspath() throws Exception;
	String getJavaCommand() throws Exception;
	String getProcessName() throws Exception;
	String getProcessID();
	String getHost() throws Exception;
	String getPort() throws Exception;
	String getContextPath() throws Exception;

	boolean isSpringBootApp();
	boolean isSpringApp();

	String getEnvironment() throws Exception;
	Collection<RequestMapping> getRequestMappings() throws Exception;

	LiveBeansModel getBeans();
	boolean providesNonBootLiveBeans();

	List<String> getActiveProfiles();
	Optional<List<LiveConditional>> getLiveConditionals() throws Exception;
	Properties getSystemProperties() throws Exception;

	default String getSystemProperty(String string) throws Exception {
		Object r = getSystemProperties().get(string);
		if (r instanceof String) {
			return (String) r;
		}
		return null;
	}
}
