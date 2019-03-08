/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.jandex;

import java.io.File;

import org.jboss.jandex.IndexView;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;

import com.google.common.base.Supplier;

class ModuleJandexIndex implements IJavaModuleData {

	private Supplier<IndexView> index;

	private File container;

	private String module;

	public ModuleJandexIndex(File container, String module, Supplier<IndexView> index) {
		this.container = container;
		this.module = module;
		this.index = index;
	}

	public Supplier<IndexView> getIndex() {
		return index;
	}

	@Override
	public File getContainer() {
		return container;
	}

	@Override
	public String getModule() {
		return module;
	}

	@Override
	public String toString() {
		return "ModuleJandexIndex [container=" + container + ", module=" + module + "]";
	}

}
