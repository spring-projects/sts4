/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.github.auth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.springframework.ide.eclipse.boot.wizard.github.GithubClient;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager.DownloadService;

/**
 * For now we need to authenticate to be able to download guides from github.
 * This is because the github repos for guides are private at the moment.
 *
 * @author Kris De Volder
 */
public class AuthenticatedDownloader implements DownloadService {

	GithubClient client = new GithubClient();

	//@Override
	public void fetch(URL url, OutputStream writeTo) throws IOException {
		client.fetch(url, writeTo);
	}

}
