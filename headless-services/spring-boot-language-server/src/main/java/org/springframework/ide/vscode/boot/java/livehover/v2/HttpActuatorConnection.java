/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HttpActuatorConnection implements ActuatorConnection {
	
	private Gson gson;
	private RestTemplate restTemplate;
	private String actuatorUrl;

	public HttpActuatorConnection(String actuatorUrl) {
		this.actuatorUrl = actuatorUrl;
		this.restTemplate = new RestTemplateBuilder().rootUri(actuatorUrl).build();
		this.gson = new Gson();
	}

	@Override
	public String getEnvironment() {
		return restTemplate.getForObject("/env", String.class);
	}

	@Override
	public String getProcessID() {
		return getSystemProperties().getProperty("PID");
	}

	@Override
	public Properties getSystemProperties() {
		JsonObject json = gson.fromJson(getEnvironment(), JsonObject.class);
		JsonArray propertySources = json.getAsJsonArray("propertySources");
		for (JsonElement jsonElement : propertySources) {
			JsonObject obj = jsonElement.getAsJsonObject();
			if ("systemProperties".equals(obj.get("name").getAsString())) {
				JsonElement props = obj.get("properties");
				Properties p = new Properties();
				for (Entry<String, JsonElement> entry : props.getAsJsonObject().entrySet()) {
					p.put(entry.getKey(), entry.getValue().getAsJsonObject().get("value").getAsString());
				}
				return p;
			}
		}
		return null;
	}

	@Override
	public String getConditionalsReport() throws IOException {
		return restTemplate.getForObject("/conditions", String.class);
	}

	@Override
	public String getRequestMappings() throws IOException {
		return restTemplate.getForObject("/mappings", String.class);
	}

	@Override
	public String getBeans() throws IOException {
		return restTemplate.getForObject("/beans", String.class);
	}
	
	@Override
	public String getLiveMetrics(String metricName, String tags) throws IOException {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/metrics/"+metricName);
		if (tags != null && !tags.isBlank()) {
		    uriBuilder.queryParam("tag", tags);
		}
		// /{ownerId}/new cases make REST Template URI template handler to think that {ownerId} is a URI parameter which it is not.
		String url = actuatorUrl + uriBuilder.toUriString();
		return restTemplate.getForObject(URI.create(url), String.class);
	}

	@Override
	public String getMetrics(String metric, Map<String, String> tags) throws IOException {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/metrics/http.server.requests");
		if (tags != null) {
			for (Entry<String, String> e : tags.entrySet()) {
				uriBuilder.queryParam("tag", e.getKey() + ":" + e.getValue());
			}
		}
		// /{ownerId}/new cases make REST Template URI template handler to think that {ownerId} is a URI parameter which it is not.
		String url = actuatorUrl + uriBuilder.toUriString();
		return restTemplate.getForObject(URI.create(url), String.class);
	}

	@Override
	public Map<?, ?> getStartup() throws IOException {
		return null;
	}

}
