/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

@Component
public class RestTemplateFactory {
	
	private static final Logger log = LoggerFactory.getLogger(RestTemplateFactory.class);
	
	private BootJavaConfig config;

	public RestTemplateFactory(BootJavaConfig config) {
		this.config = config;
	}
	
	public RestTemplate createRestTemplate(String host) {
		String proxyUrl = config.getRawSettings().getString("http", "proxy");
		if (proxyUrl == null || proxyUrl.isBlank()) {
			proxyUrl = getProxyUrlFromEnv();
		}
		OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
		if (proxyUrl != null && !proxyUrl.isBlank()) {
			Set<String> exclusions = config.getRawSettings().getStringSet("http", "proxy-exclusions");
			if (!"localhost".equals(host) && !"127.0.0.1".equals(host) && !exclusions.contains(host)) {
				try {
					URL url = new URL(proxyUrl);
					if (url.getProtocol().startsWith("http")) {
						clientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url.getHost(), url.getPort() < 0 ? "https".equals(url.getProtocol()) ? 443 : 80 : url.getPort() )));
					} else if (url.getProtocol().startsWith("sock")) {
						clientBuilder.proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(url.getHost(), url.getPort())));
					}
					String username = null, password = null;
					if (url.getUserInfo() == null || url.getUserInfo().isBlank()) {
						username = config.getRawSettings().getString("http", "proxy-user");
						password = config.getRawSettings().getString("http", "proxy-password");
					} else {
						String userInfo = url.getUserInfo();
						int idx = userInfo.indexOf(':');
						if (idx > 0 && idx < userInfo.length()) {
							username = userInfo.substring(0, idx);
							password = userInfo.substring(idx + 1);
						}
					}
					if (username != null && password != null && !username.isEmpty()) {
						final String user = username;
						final String pass = password;
						clientBuilder.proxyAuthenticator(new Authenticator() {
							@Override
							public Request authenticate(Route route, Response response) throws IOException {
								String credential = Credentials.basic(user, pass);
								return response.request().newBuilder().header("Proxy-Authorization", credential)
										.build();
							}
						});
					}
				} catch (MalformedURLException e) {
					log.error("", e);
				}
			}
		}
		return new RestTemplate(new OkHttp3ClientHttpRequestFactory(clientBuilder.build()));
	}
	
	private static String getProxyUrlFromEnv() {
		log.info("Environment size: " + System.getenv().size());
		log.info("Env var https_proxy: " + System.getenv("https_proxy"));
		String proxyUrl = System.getenv("https_proxy");
		if (proxyUrl == null || proxyUrl.isBlank()) {
			proxyUrl = System.getenv("HTTPS_PROXY");
		}
		if (proxyUrl == null || proxyUrl.isBlank()) {
			proxyUrl = System.getenv("http_proxy");
		}
		if (proxyUrl == null || proxyUrl.isBlank()) {
			proxyUrl = System.getenv("HTTP_PROXY");
		}
		log.info("Proxy URL from env: " + proxyUrl);
		return proxyUrl;
		
	}
	
}
