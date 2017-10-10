/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

/**
 * Object able to add/remove {@link FileListener} objects and fire events via these listeners
 * 
 * @author Alex Boyko
 *
 */
public interface FileObserver {
	
	public interface FileListener {
		boolean accept(String uri);
		void changed(String uri);
		void deleted(String uri);
		void created(String uri);
	}
	
	void addListener(FileListener listener);
	
	void removeListener(FileListener listener);

}
