/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.ProxyConfiguration;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.OneTimePasscodeTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.RefreshTokenGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.springframework.ide.vscode.commons.cloudfoundry.client.target.CFCredentials;

/**
 * TODO: Remove this class when the 'thread leak bug' in V2 client is fixed.
 *
 * At the moment each time {@link SpringCloudFoundryClient} is create a threadpool
 * is created by the client and it is never cleaned up. The only way we have
 * to mitigate this leak is to try and create as few clients as possible.
 * <p>
 * So we have a permanent cache of clients here that is reused.
 * <p>
 * When the bug is fixed then this should no longer be necessary and we can removed this cache
 * and just create the client as needed.
 *
 * @author Kris De Volder
 */
public class CloudFoundryClientCache {

	public class CFClientProvider {

		final ConnectionContext connection;
		final TokenProvider tokenProvider;

		//Note the three client objects below are 'stateless' wrappers and it would be
		// fine to recreate as needed instead of store them

		final CloudFoundryClient client;
		final ReactorUaaClient uaaClient;
		final ReactorDopplerClient doppler;

		private ProxyConfiguration getProxy(String host) {
			// TODO: enable proxy support for vscode. The code below retrieves proxy via Eclipse proxy service thus
			// not applicable when this is used outside of Eclipse
//			try {
//				if (StringUtils.hasText(host)) {
//					URL url = new URL("https://"+host);
//					// In certain cases, the activator would have stopped and the plugin may
//					// no longer be available. Usually onl happens on shutdown.
//					BootDashActivator plugin = BootDashActivator.getDefault();
//					if (plugin != null) {
//						IProxyService proxyService = plugin.getProxyService();
//						if (proxyService != null) {
//							IProxyData[] selectedProxies = proxyService.select(url.toURI());
//
//							// No proxy configured or not found
//							if (selectedProxies == null || selectedProxies.length == 0) {
//								return null;
//							}
//
//							IProxyData data = selectedProxies[0];
//							int proxyPort = data.getPort();
//							String proxyHost = data.getHost();
//							String user = data.getUserId();
//							String password = data.getPassword();
//							if (proxyHost!=null) {
//								return ProxyConfiguration.builder()
//										.host(proxyHost)
//										.port(proxyPort==-1?Optional.empty():Optional.of(proxyPort))
//										.username(Optional.ofNullable(user))
//										.password(Optional.ofNullable(password))
//										.build();
////							return proxyHost != null ? new HttpProxyConfiguration(proxyHost, proxyPort,
////									data.isRequiresAuthentication(), user, password) : null;
//							}
//						}
//					}
//				}
//			} catch (Exception e) {
//				Log.log(e);
//			}
			return null;
		}

		public CFClientProvider(Params params) {
			long sslTimeout = Long.getLong("sts.bootdash.cf.client.ssl.handshake.timeout", 60); //TODO: make a preference for this?
			Optional<Boolean> keepAlive = getBooleanSystemProp("http.keepAlive");
			debug("cf client keepAlive = "+keepAlive);
			connection = DefaultConnectionContext.builder()
					.proxyConfiguration(Optional.ofNullable(getProxy(params.host)))
					.apiHost(params.host)
					.sslHandshakeTimeout(Duration.ofSeconds(sslTimeout))
					.keepAlive(keepAlive)
					.skipSslValidation(params.skipSsl)
					.build();

			tokenProvider = createTokenProvider(params);

			client = ReactorCloudFoundryClient.builder()
					.connectionContext(connection)
					.tokenProvider(tokenProvider)
					.build();

			uaaClient = ReactorUaaClient.builder()
					.connectionContext(connection)
					.tokenProvider(tokenProvider)
					.build();

			doppler = ReactorDopplerClient.builder()
					.connectionContext(connection)
					.tokenProvider(tokenProvider)
					.build();
		}

		private TokenProvider createTokenProvider(Params params) {
			CFCredentials creds = params.credentials;
			switch (creds.getType()) {
			case PASSWORD:
				return PasswordGrantTokenProvider.builder()
						.username(params.username)
						.password(creds.getSecret())
						.build();
			case REFRESH_TOKEN:
				return RefreshTokenGrantTokenProvider.builder()
						.token(creds.getSecret())
						.build();
			case TEMPORARY_CODE:
				return OneTimePasscodeTokenProvider.builder()
						.passcode(creds.getSecret())
						.build();
			default:
				throw new IllegalStateException("BUG! Missing switch case?");
			}
		}

		private Optional<Boolean> getBooleanSystemProp(String name) {
			String str = System.getProperty(name);
			if (str!=null) {
				return Optional.of(Boolean.valueOf(str));
			}
			return Optional.empty();
		}
	}

	private static final boolean DEBUG = true;

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	public static class Params {
		public final String username;
		public final CFCredentials credentials;
		public final String host;
		public final boolean skipSsl;
		public Params(String username, CFCredentials credentials, String host, boolean skipSsl) {
			super();
			this.username = username;
			this.credentials = credentials;
			this.host = host;
			this.skipSsl = skipSsl;
		}

		@Override
		public String toString() {
			return "Params [username=" + username + ", host=" + host + ", skipSsl=" + skipSsl
					+ "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			result = prime * result + ((credentials == null) ? 0 : credentials.hashCode());
			result = prime * result + (skipSsl ? 1231 : 1237);
			result = prime * result + ((username == null) ? 0 : username.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Params other = (Params) obj;
			if (host == null) {
				if (other.host != null)
					return false;
			} else if (!host.equals(other.host))
				return false;
			if (credentials == null) {
				if (other.credentials != null)
					return false;
			} else if (!credentials.equals(other.credentials))
				return false;
			if (skipSsl != other.skipSsl)
				return false;
			if (username == null) {
				if (other.username != null)
					return false;
			} else if (!username.equals(other.username))
				return false;
			return true;
		}
	}

	private Map<Params, CFClientProvider> cache = new HashMap<>();

	private int clientCount = 0;

	public synchronized CFClientProvider getOrCreate(String username, CFCredentials credentials, String host, boolean skipSsl) {
		Params params = new Params(username, credentials, host, skipSsl);
		CFClientProvider client = cache.get(params);
		if (client==null) {
			clientCount++;
			debug("Creating client ["+clientCount+"]: "+params);
			cache.put(params, client = create(params));
		} else {
			debug("Reusing client ["+clientCount+"]: "+params);
		}
		return client;
	}

	protected CFClientProvider create(Params params) {
		return new CFClientProvider(params);
	}

}
