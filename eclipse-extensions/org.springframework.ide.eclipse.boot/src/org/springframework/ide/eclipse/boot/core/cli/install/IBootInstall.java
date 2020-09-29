/*******************************************************************************
 *  Copyright (c) 2013, 2017 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.cli.install;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Common interface for anything that represents an installation of spring boot.
 *
 * @author Kris De Volder
 */
public interface IBootInstall {

	String getUrl(); //Url identifying this installation.
	File getHome() throws Exception;
	File[] getBootLibJars() throws Exception;
	String getName();
	IStatus validate();
	String getVersion();

	/**
	 * Gets a specific Boot CLI extension
	 * @param extension the extension to get
	 * @return the extension or <code>null</code> if extension isn't installed
	 */
	<T extends IBootInstallExtension> T getExtension(Class<T> extension);

	/**
	 * Get a liveExp that watches changes of a given extension.
	 */
	<T extends IBootInstallExtension> LiveExpression<T> getExtensionExp(Class<T> extension);


	/**
	 * Returns supported extensions
	 * @return
	 */
	Collection<Class<? extends IBootInstallExtension>> supportedExtensions();

	/**
	 * Install extension into Spring Boot CLI
	 * @param extension class identifier of the extension
	 * @return runnable that installs the extension
	 */
	void installExtension(Class<? extends IBootInstallExtension> extension)  throws Exception;

	/**
	 * Removes extension from Spring Boot CLI
	 * @param extension extension
	 * @return runnable that removes the extension
	 */
	void uninstallExtension(IBootInstallExtension extension)  throws Exception;

	/**
	 * Checks this BootInstall, verifying whether given extension is auto-installable. See
	 * {@link AutoInstallDescription} for more details about the expected result.
	 */
	AutoInstallDescription checkAutoInstallable(Class<? extends IBootInstallExtension> extension);

	/**
	 * For installs that are zipped or non-local this deletes the cached info (i.e. unzipped and locally downloaded copy
	 * of the data. For locally configured installations this does nothing.
	 */
	void clearCache();

	/**
	 * Refresh any cached information about a given installed extension (or do nothing if there isn't any cached information).
	 */
	void refreshExtension(Class<? extends IBootInstallExtension> extension);

	boolean mayRequireDownload();
}
