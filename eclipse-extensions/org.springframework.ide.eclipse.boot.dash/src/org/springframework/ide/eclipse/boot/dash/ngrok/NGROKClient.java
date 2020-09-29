/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.ngrok;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Martin Lippert
 */
public class NGROKClient {

	private static int CREATE_TUNNEL_TIMEOUT_SECONDS = 6;

	private String path;

	private NGROKProcess process;
	private NGROKTunnel tunnel;

	public NGROKClient(String path) {
		this.path = path;

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				if (process != null) {
					process.terminate();
				}
			}
		}));
	}

	public NGROKTunnel getTunnel() {
		return tunnel;
	}

	public String getURL() {
		return process != null ? process.getApiURL() : null;
	}

	public NGROKTunnel startTunnel(String proto, String addr) throws Exception {
		process = NGROKProcess.startNGROK(path, proto, addr);
		if (process != null) {

			boolean success = false;
			int seconds = 0;

			while (!success && seconds < CREATE_TUNNEL_TIMEOUT_SECONDS) {
				NGROKTunnel[] tunnels = retrieveTunnels();
				if (tunnels != null && tunnels.length > 0) {
					for (int i = 0; i < tunnels.length; i++) {
						if (tunnels[i].getAddr().endsWith(addr) && tunnels[i].getProto().equals(proto)) {
							tunnel = tunnels[i];
							return tunnel;
						}
					}
				}
				seconds++;
				Thread.sleep(1000);
			}
		}

		return null;
	}

	public void shutdown() {
		if (tunnel != null) {
			shutdownTunnel(tunnel);
			tunnel = null;
		}

		if (process != null) {
			process.terminate();
			process = null;
		}
	}

	private NGROKTunnel[] retrieveTunnels() {
		NGROKTunnel[] result = null;
		try {
			String response = Request.Get(process.getApiURL() + "/api/tunnels").execute().returnContent().asString();

			JSONObject jsonResponse = new JSONObject(response);

			JSONArray tunnels = jsonResponse.getJSONArray("tunnels");
			if (tunnels != null) {
				result = new NGROKTunnel[tunnels.length()];
				for (int i = 0; i < result.length; i++) {
					JSONObject tunnel = tunnels.getJSONObject(i);
					String name = tunnel.getString("name");
					String proto = tunnel.getString("proto");
					String public_url = tunnel.getString("public_url");
					String addr = tunnel.getJSONObject("config").getString("addr");

					result[i] = new NGROKTunnel(name, proto, public_url, addr);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e);
			// do nothing, might be the case that the ngrok process is not yet up
		}

		return result;
	}

	private void shutdownTunnel(NGROKTunnel ngrokTunnel) {
		try {
			String deleteURL = process.getApiURL() + "/api/tunnels/" + URLEncoder.encode(ngrokTunnel.getName(), "UTF-8");
			HttpResponse response = Request.Delete(deleteURL).execute().returnResponse();
			if (response.getStatusLine().getStatusCode() != 204) {
				System.err.println("errro closing tunnel");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
