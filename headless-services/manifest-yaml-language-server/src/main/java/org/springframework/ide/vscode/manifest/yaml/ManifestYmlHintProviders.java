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
package org.springframework.ide.vscode.manifest.yaml;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

public interface ManifestYmlHintProviders {

	Callable<Collection<YValueHint>> getBuildpackProviders();

	Callable<Collection<YValueHint>> getServicesProvider();

	Callable<Collection<YValueHint>> getDomainsProvider();

	Callable<Collection<YValueHint>> getStacksProvider();

}
