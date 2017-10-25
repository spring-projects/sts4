/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.autowired;

import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;

/**
 * @author Martin Lippert
 */
public interface SpringBootAppProvider {

	public LiveBeansModel getBeans() throws Exception;
	public String getProcessID();
	public String getProcessName();

}
