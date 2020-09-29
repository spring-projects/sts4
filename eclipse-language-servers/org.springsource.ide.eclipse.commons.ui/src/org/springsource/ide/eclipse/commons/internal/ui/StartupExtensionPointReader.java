/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.IIdeUiStartup;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class StartupExtensionPointReader implements IStartup {

	private static final String EXTENSION_ID_STARTUP = "com.springsource.sts.ide.ui.startup";

	private static final String ELEMENT_STARTUP = "startup";

	private static final String ELEMENT_CLASS = "class";

	public static void runStartupExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_STARTUP);
		IExtension[] extensions = extensionPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().compareTo(ELEMENT_STARTUP) == 0) {
					runStartupExtension(element);
				}
			}
		}
	}

	private static void runStartupExtension(IConfigurationElement configurationElement) {
		try {
			Object object = WorkbenchPlugin.createExtension(configurationElement, ELEMENT_CLASS);
			if (!(object instanceof IIdeUiStartup)) {
				StatusHandler.log(new Status(IStatus.ERROR, UiPlugin.PLUGIN_ID, "Could not load "
						+ object.getClass().getCanonicalName() + " must implement "
						+ IIdeUiStartup.class.getCanonicalName()));
				return;
			}

			IIdeUiStartup startup = (IIdeUiStartup) object;
			startup.lazyStartup();
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiPlugin.PLUGIN_ID, "Could not load startup extension", e));
		}

	}

	public void earlyStartup() {
		runStartupExtensions();
	}

}
