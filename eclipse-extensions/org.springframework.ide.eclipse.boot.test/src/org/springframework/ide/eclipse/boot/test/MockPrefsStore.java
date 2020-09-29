/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import java.io.IOException;
import java.lang.reflect.Field;

import org.eclipse.jface.preference.PreferenceStore;

/**
 * @author Kris De Volder
 */
public class MockPrefsStore extends PreferenceStore {

	@Override
	public void save() throws IOException {
		//Override save or it will throw unless we provide a file to save to.
		try {
			Field f = PreferenceStore.class.getDeclaredField("dirty");
			f.setAccessible(true);
			f.set(this, false);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

}
