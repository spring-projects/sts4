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

import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.UIThreadDownloadDisallowed;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

public abstract class AGSContent implements GSContent {

	protected DownloadManager downloader;

	public AGSContent(DownloadManager dl) {
		this.downloader = dl;
	}

	@Override
	public CodeSet getCodeSet(String name) throws UIThreadDownloadDisallowed {
		for (CodeSet cs : getCodeSets()) {
			if (cs.getName().equals(name)) {
				return cs;
			}
		}
		return null;
	}

	@Override
	public boolean isDownloaded() {
		return getZip().isDownloaded();
	}

	@Override
	public ValidationResult downloadStatus() {
		return ValidationResult.from(getZip().getDownloadStatus());
	}

	@Override
	public void setDownloader(DownloadManager downloader) {
		this.downloader = downloader;
	}

}
