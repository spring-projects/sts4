/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("languageserver.boot")
public class BootLsConfigProperties {

	/**
	 * Enables/disables Jandex indexing of Java types. When disabled JDT LS will be
	 * used for type and package searches, type finding for FQ name etc.
	 */
	private boolean enableJandexIndex = false;

	public boolean isEnableJandexIndex() {
		return enableJandexIndex;
	}

	public void setEnableJandexIndex(boolean enableJandexIndex) {
		this.enableJandexIndex = enableJandexIndex;
	}


}
