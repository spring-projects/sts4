/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.content;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadableItem;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.UIThreadDownloadDisallowed;

/**
 * 'Standalon' content item that contains just a single codeset taken from a zip file downloaded from
 * a URL.
 * <p>
 * These content items might be created from links found inside of guides or tutorial
 * webpages.
 *
 */
public class GSZipFileCodeSet extends AGSContent {

	private final URI zipUrl;

	public GSZipFileCodeSet(URI zipUrl) {
		super(null); //downloader may not be known yet until added to content manager.
		this.zipUrl = zipUrl;
	}

	@Override
	public String getDescription() {
		return "No description";
	}

	@Override
	public String getName() {
		URL url = getHomePage();
		return zipUrl.getPath();
	}

	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public List<CodeSet> getCodeSets() throws UIThreadDownloadDisallowed {
		return Arrays.asList((CodeSet)new ZipFileCodeSet(getName(), getZip(), new Path("/")));
	}

	@Override
	public URL getHomePage() {
		//Isolated zip file. No home page!
		return null;
	}

	private URL zipUrl() {
		try {
			return zipUrl.toURL();
		} catch (MalformedURLException e) {
			throw new Error(e);
		}
	}

	@Override
	public DownloadableItem getZip() {
		return new DownloadableItem(zipUrl(), downloader);
	}

}
