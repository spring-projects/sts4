/*******************************************************************************
 *  Copyright (c) 2017, 2020 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.cli.install;

import org.springframework.ide.eclipse.boot.core.cli.BootCliCommand;
import org.springframework.ide.eclipse.boot.util.version.Version;

/**
 * Interface for Boot CLI extension
 *
 * @author Alex Boyko
 *
 */
public interface IBootInstallExtension {

	/**
	 * Spring Boot CLI command to install extension
	 */
	static String INSTALL_COMMAND = "install";

	static String UNINSTALL_COMMAND = "uninstall";

	/**
	 * Creates Boot CLI command to execute for the extension
	 * @return the Boot CLI extension command
	 * @throws Exception
	 */
	BootCliCommand createCommand() throws Exception;

	/**
	 * Version of the extension
	 * @return the version
	 */
	Version getVersion();

}
