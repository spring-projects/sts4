/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.content;

import java.net.URL;
import java.util.List;

import org.springframework.ide.eclipse.boot.wizard.guides.GSImportWizard;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadableItem;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.UIThreadDownloadDisallowed;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

/**
 * Interface that needs to be implemented by any content type that can be imported via
 * the generic {@link GSImportWizard}
 *
 * @author Kris De Volder
 */
public interface GSContent extends Describable, DisplayNameable {

	public String getName();
	public String getDisplayName();

	public List<CodeSet> getCodeSets() throws UIThreadDownloadDisallowed;
	public CodeSet getCodeSet(String name) throws UIThreadDownloadDisallowed;
	public URL getHomePage();

	public DownloadableItem getZip(); //Some content may not be packaged in a zip.
									  // This method should be removed from the interface.
									  //Shouldn't be needed to be used directly by client code if using codeset abstraction


	public boolean isDownloaded();

	/**
	 * If isDownloaded returns false, this may contain an explanation (if the
	 * reason for isDownloaded false was some type of error that occurred
	 * during download. The message is retained as a download error status.
	 */
	public ValidationResult downloadStatus();

	public void setDownloader(DownloadManager downloader);


}
