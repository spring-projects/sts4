/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.util;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

public class DumpOutput implements IStreamListener {

	private final String label;
	private boolean first = true;

	public DumpOutput(String streamName) {
		this.label = streamName;
	}

	@Override
	public void streamAppended(String text, IStreamMonitor monitor) {
		//TODO: this might look messy on windows
		if (first) {
			System.out.print("\n"+label);
			first = false;
		}
		System.out.print(text.replaceAll("\n", "\n"+label));
	}

}

