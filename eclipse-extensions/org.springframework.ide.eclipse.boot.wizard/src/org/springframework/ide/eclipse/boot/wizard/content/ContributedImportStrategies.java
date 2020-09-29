/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.content;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.boot.wizard.BootWizardActivator;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategy;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategyFactory;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategyHolder;

/**
 * An instance of this class is responsible for reading the contributions to
 * extension point "org.springframework.ide.eclipse.wizard.import.strategy" and
 * adding them to the corresponding {@link BuildType}.
 *
 * @author Kris De Volder
 */
public class ContributedImportStrategies {

	private static final String EXTENSION_POINT = "org.springframework.ide.eclipse.wizard.import.strategy";

	private boolean isInitialized;

	public synchronized void initialize() {
		if (!isInitialized) {
			isInitialized = true;
			initializeFromExtensions();
		}
	}


	private void initializeFromExtensions() {
		try {
			IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

			for (IConfigurationElement element : elements) {
				element.createExecutableExtension("class");
				BuildType buildType = getBuildType(element);
				ImportStrategyHolder strategyHolder = new ImportStrategyHolder(
						buildType,
						getFactory(element),
						getNotInstalledMessage(element),
						getName(element)
				);
				buildType.addStrategy(strategyHolder);
			}
		} catch (Exception e) {
			BootWizardActivator.log(e);
		}
	}


	private String getName(IConfigurationElement element) {
		return element.getAttribute("name");
	}

	private String getNotInstalledMessage(IConfigurationElement element) {
		return element.getAttribute("notInstalledMessage");
	}

	private ImportStrategyFactory getFactory(final IConfigurationElement element) {
		//Why wrap a factory in another factory?
		// This is so that if either creation of the factory, or creation of the instance fails, we
		// can catch the exceptions in the caller of the (wrapped) factory.
		//In other words, this makes sure that getFactory is 'safe' and won't throw exceptions.
		return new ImportStrategyFactory() {
			@Override
			public ImportStrategy create(BuildType buildType, String name, String notInstalledMessage) throws Exception {
				ImportStrategyFactory factory = (ImportStrategyFactory) element.createExecutableExtension("class");
				return factory.create(buildType, name, notInstalledMessage);
			}
		};
	}


	private BuildType getBuildType(IConfigurationElement element) {
		return BuildType.valueOf(element.getAttribute("buildType"));
	}

}
