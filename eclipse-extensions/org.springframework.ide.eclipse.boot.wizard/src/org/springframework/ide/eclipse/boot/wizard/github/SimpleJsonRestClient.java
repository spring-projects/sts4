/*******************************************************************************
 *  Copyright (c) 2013-2017 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.github;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.ide.eclipse.boot.wizard.github.auth.Credentials;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Poor man's rest client, just uses built-in JRE {@link URLConnection} apis instead of
 * more fancy libs like appache http client or jersey.
 */
public class SimpleJsonRestClient {

	private Credentials credentials;

	public class Response {

		private HttpURLConnection conn;

		byte[] buf = new byte[1024];

		public Response(HttpURLConnection conn) {
			this.conn = conn;
		}

		public <T> T readEntity(Class<T> type) throws IOException {
			return new ObjectMapper().readValue(conn.getInputStream(), type);
		}

		public Map<String, List<String>> getHeaders() {
			return conn.getHeaderFields();
		}
	}

	public class WebTarget {

		private String url;

		public WebTarget(String url) {
			this.url = url;
		}

		public WebTarget resolveTemplates(Map<String, Object> vars) {
			try {
				for (Entry<String, Object> entry : vars.entrySet()) {
					url = url.replace("{"+entry.getKey()+"}", URLEncoder.encode(entry.getValue().toString(), "UTF8"));
				}
				return this;
			} catch (UnsupportedEncodingException e) {
				//shouldn't be possible... but anyhow
				throw ExceptionUtil.unchecked(e);
			}
		}

		public Response get() {
			try {
				URL url = new URL(this.url);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				credentials.apply(conn);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json, text/plain");
				return new Response(conn);
			} catch (Exception e) {
				throw ExceptionUtil.unchecked(e);
			}
		}

		public <T> T get(Class<T> type) throws IOException {
			return get().readEntity(type);
		}
	}


	public SimpleJsonRestClient(Credentials c) {
		this.credentials = c;
	}



	public WebTarget target(String url) {
		return new WebTarget(url);
	}

}
