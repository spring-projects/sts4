/*******************************************************************************
 * Copyright (c) 2015,2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.validation.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

public class BootMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	private MarkerResolutionRegistry registry = MarkerResolutionRegistry.DEFAULT_INSTANCE;

	/**
	 * Override default MarkerResolutionRegistry, mainly to make this class more easy to unit test.
	 */
	public void setRegistry(MarkerResolutionRegistry registry) {
		this.registry = registry;
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		return registry.generator(marker).getResolutions(marker);
	}


	@Override
	public boolean hasResolutions(IMarker marker) {
		return registry.generator(marker).hasResolutions(marker);
	}
}
