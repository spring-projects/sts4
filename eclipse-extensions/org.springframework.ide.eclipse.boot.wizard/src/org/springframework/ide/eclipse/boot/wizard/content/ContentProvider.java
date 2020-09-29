/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.content;

import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;

/**
 * An instance of this interface implements some method of discovering
 * content of a particular type.
 */
public interface ContentProvider<T> {

	T[] fetch(DownloadManager downloader);

}
