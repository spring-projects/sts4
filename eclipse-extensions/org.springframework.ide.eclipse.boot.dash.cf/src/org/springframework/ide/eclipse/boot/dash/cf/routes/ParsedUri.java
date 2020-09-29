/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.routes;

/**
 * Represents the result of 'parsing; a route uri by a 'dumb' parser.
 * <p>
 * Dumb parser is unaware of dynamic infos about existing domains and so it
 * doesn't have the ability to split host and domain name apart as the uri
 * syntax alone is not sufficient to determine where the host-name ends and
 * the domain name begins.
 *
 * @author Kris De Volder
 */
public class ParsedUri {

	private String hostAndDomain; // These aren't split apart by the parser, because a dumb parser can't know where to split
	private String path;
	private Integer port;

	public ParsedUri(String uri) {
		//Format: ${hostAndDomain}:${port}/${path} (where the slash is considered a part of the path)
		int slash = uri.indexOf('/');
		if (slash>=0) {
			path = uri.substring(slash);
			uri = uri.substring(0, slash);
		}
		int colon = uri.indexOf(':');
		if (colon>=0) {
			port = Integer.parseInt(uri.substring(colon+1));
			uri = uri.substring(0, colon);
		}
		hostAndDomain = uri;
	}

	@Override
	public String toString() {
		return "ParsedUri [hostAndDomain=" + hostAndDomain + ", path=" + path + ", port=" + port + "]";
	}

	public String getHostAndDomain() {
		return hostAndDomain;
	}

	public String getPath() {
		return path;
	}

	public Integer getPort() {
		return port;
	}
}
