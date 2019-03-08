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
package org.springframework.ide.vscode.commons.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeSet;

import com.google.common.net.MediaType;

public class MimeTypes {

	public static String[] getKnownMimeTypes() {
		try {
			Field f = MediaType.class.getDeclaredField("KNOWN_TYPES");
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<MediaType, MediaType> map = (Map<MediaType, MediaType>) f.get(null);
			TreeSet<String> mediaTypes = new TreeSet<>();
			for (MediaType m : map.keySet()) {
				mediaTypes.add(m.toString());
			}
			return mediaTypes.toArray(new String[mediaTypes.size()]);
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

}
