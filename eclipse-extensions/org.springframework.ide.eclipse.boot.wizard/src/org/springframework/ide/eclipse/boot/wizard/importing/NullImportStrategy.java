/*******************************************************************************
 * Copyright (c) 2013, 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.importing;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.boot.wizard.content.BuildType;

/**
 * Import stratgety used in place of a Strategy that could not be instantiated, presumably because
 * the required Eclipse plugins are not installed.
 *
 * @author Kris De Volder
 */
public class NullImportStrategy extends ImportStrategy {

	public NullImportStrategy(BuildType buildType, String name, String notInstalledMessage) {
		super(buildType, name, notInstalledMessage);
	}

	@Override
	public IRunnableWithProgress createOperation(ImportConfiguration conf) {
		throw new IllegalStateException(getNotInstalledMessage());
	}

	@Override
	public boolean isSupported() {
		return false;
	}

}
