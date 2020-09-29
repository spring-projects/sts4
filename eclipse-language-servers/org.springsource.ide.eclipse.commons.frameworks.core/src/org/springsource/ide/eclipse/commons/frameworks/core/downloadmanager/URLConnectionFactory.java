/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class URLConnectionFactory {

	public int CONNECT_TIMEOUT = 3000;
	
	public URLConnection createConnection(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(CONNECT_TIMEOUT);
		return conn;
	}
	
}
