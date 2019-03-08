/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh.models;

import java.util.Collection;

/**
 * Represents CloudConfig information as might be retrieved from bosh director.
 */
public interface CloudConfigModel {
	Collection<String> getVMTypes();
	Collection<String> getNetworkNames();
	Collection<String> getAvailabilityZones();
	Collection<String> getDiskTypes();
	Collection<String> getVMExtensions();
}
