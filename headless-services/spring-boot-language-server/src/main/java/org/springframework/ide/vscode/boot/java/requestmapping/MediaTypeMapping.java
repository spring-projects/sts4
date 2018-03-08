/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

/**
 * @author Martin Lippert
 */
public enum MediaTypeMapping {
	
	TEXT_PLAIN("text/plain"),
	APPLICATION_JSON("application/json"),
	APPLICATION_STREAM_JSON("application/stream+json");

	
	private String mediaType;

	private MediaTypeMapping(String mediaType) {
		this.mediaType = mediaType;
	}
	
	public String getMediaType() {
		return mediaType;
	}

}
