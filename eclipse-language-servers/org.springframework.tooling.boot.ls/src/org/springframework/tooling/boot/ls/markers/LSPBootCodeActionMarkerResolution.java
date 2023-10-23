/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.markers;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.lsp4e.operations.codeactions.LSPCodeActionMarkerResolution;
import org.eclipse.ui.IMarkerResolution;

@SuppressWarnings("restriction")
public class LSPBootCodeActionMarkerResolution extends LSPCodeActionMarkerResolution {
	
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		IMarkerResolution[] res = super.getResolutions(marker);
		AtomicInteger relevance = new AtomicInteger(res.length);
		return Arrays.stream(res).map(r -> new DelegateMarkerResolution(marker, r, relevance.getAndDecrement()))
				.toArray(IMarkerResolution[]::new);
	}

}
