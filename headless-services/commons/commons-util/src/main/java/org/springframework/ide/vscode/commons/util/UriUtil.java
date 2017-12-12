/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class UriUtil {

	public static URI toUri(File file) {
		try {
			return new URI("file", "", file.getAbsoluteFile().toURI().getPath(), null); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (URISyntaxException e) {
			Log.log(e);
			return file.getAbsoluteFile().toURI();
		}
	}

}
