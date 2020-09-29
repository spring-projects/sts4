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
package org.springframework.ide.eclipse.boot.dash.cf.packaging;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Instances of this interface is responsible for initializing and instance of {@link ICloudApplicationArchiver}.
 * @author Kris De Volder
 */
public interface CloudApplicationArchiverStrategy {

	/**
	 * A strategy may or may not always be applicable in a given situation. In that case it may return null when
	 * asked to produce a ICloudApplicationArchiver.
	 */
	ICloudApplicationArchiver getArchiver(IProgressMonitor mon);

}
