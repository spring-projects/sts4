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
 * Represents Stemcells information as might be retrieved from bosh director.
 */
public interface StemcellsModel {
	Collection<String> getStemcellNames();
	Collection<String> getStemcellOss();
	Collection<StemcellData> getStemcells();
	Collection<String> getVersions();
}
