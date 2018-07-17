/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.kubernetes;

import java.net.URI;


public class DockerImage {

	public static String SCHEME = "docker";

	private URI uri;

	public DockerImage(String image) {
		this.uri = URI.create(SCHEME + ":" + image);
	}

	public URI getUri() {
		return uri;
	}

}
