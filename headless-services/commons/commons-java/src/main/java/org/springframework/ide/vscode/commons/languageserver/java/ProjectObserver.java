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
package org.springframework.ide.vscode.commons.languageserver.java;

import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * Projects Observer. Able to add/remove project listeners which are notified on project changes
 * 
 * @author Alex Boyko
 *
 */
public interface ProjectObserver {

	interface Listener {
		void created(IJavaProject project);
		void changed(IJavaProject project);
		void deleted(IJavaProject project);
	}	
	void addListener(Listener listener);
	void removeListener(Listener listener);

	ProjectObserver NULL = new ProjectObserver() {
		@Override
		public void addListener(Listener listener) {
		}

		@Override
		public void removeListener(Listener listener) {
		}
	};

}
