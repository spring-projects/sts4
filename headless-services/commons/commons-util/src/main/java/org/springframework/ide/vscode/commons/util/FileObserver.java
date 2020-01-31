/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.util.List;
import java.util.function.Consumer;

import reactor.core.Disposable;

/**
 * Object able to add/remove {@link FileListener} objects and fire events via these listeners
 * 
 * @author Alex Boyko
 *
 */
public interface FileObserver {
	
	String onFilesCreated(List<String> globPattern, Consumer<String[]> handler);
	
	String onFilesChanged(List<String> globPattern, Consumer<String[]> handler);
	
	String onFilesDeleted(List<String> globPattern, Consumer<String[]> handler);
	
	boolean unsubscribe(String subscriptionId);

	default Disposable onAnyChange(List<String> globPattern, Consumer<String[]> handler) {
		String[] ids = {
				onFilesChanged(globPattern, handler),
				onFilesCreated(globPattern, handler),
				onFilesDeleted(globPattern, handler)
		};
		return () -> {
			for (String id : ids) {
				unsubscribe(id);
			}
		};
	}
	
}
