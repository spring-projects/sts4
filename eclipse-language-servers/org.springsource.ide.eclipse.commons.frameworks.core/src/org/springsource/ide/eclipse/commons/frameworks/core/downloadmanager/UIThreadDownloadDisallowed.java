/*******************************************************************************
 *  Copyright (c) 2013 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager;


/**
 * Exception thrown by the DownloadManager when a download operation
 * is attempted in the UIThread. This is bad practice as it will
 * freeze the UI until the download is complete.
 * 
 * @author Kris De Volder
 */
public class UIThreadDownloadDisallowed extends Exception {

	public UIThreadDownloadDisallowed(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
