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
package org.springframework.ide.vscode.boot.validation.generations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.web.client.RestTemplate;

public class BootVersionsFromMavenCentral {
	
	private static final String URL = "https://search.maven.org/solrsearch/select?q=g:org.springframework.boot+AND+a:spring-boot-starter-parent&core=gav&rows=200&wt=json";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Version> getBootVersions() {
		HttpHeaders headers = new HttpHeaders();

		headers.setAccept(MediaType.parseMediaTypes("application/json"));

		HttpEntity<?> entity = new HttpEntity(headers);

		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<Map> responseEntity = restTemplate.exchange(URL, HttpMethod.GET, entity, Map.class);
			Map<String, Object> json = responseEntity.getBody();
			Map<String, Object> response = (Map<String, Object>) json.get("response");
			if (response != null) {
				List<Version> versions = new ArrayList<>();
				Object docs = response.get("docs");
				if (docs instanceof List) {
					
					for (Object o : (List<?>) docs) {
						if (o instanceof Map) {
							Map<String, Object> e = (Map<String, Object>) o;
							if (e.get("v") instanceof String) {
								try {
									versions.add(SpringProjectUtil.getVersion((String) e.get("v")));
								} catch (Exception ex) {
									// ignore
								}
							}
						}
					}
				}
				Collections.sort(versions);
				return versions;
			}
		} catch (Exception e) {
			// Error case
		}
		return null;
	}
	
}
