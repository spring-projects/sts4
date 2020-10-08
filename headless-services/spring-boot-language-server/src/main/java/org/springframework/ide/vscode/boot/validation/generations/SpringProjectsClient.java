/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.boot.validation.generations.json.GenerationsEmbedded;
import org.springframework.ide.vscode.boot.validation.generations.json.JsonHalEmbedded;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjects;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjectsEmbedded;
import org.springframework.web.client.RestTemplate;

public class SpringProjectsClient {

	private final String url;

	public SpringProjectsClient(String url) {
		this.url = url;
	}

	public SpringProjects getSpringProjects() throws Exception {
		return getEmbedded(url, SpringProjectsEmbedded.class);
	}

	public Generations getGenerations(String generationsUrl) throws Exception {
		return getEmbedded(generationsUrl, GenerationsEmbedded.class);
	}

	private <T> T getEmbedded(String url, Class<? extends JsonHalEmbedded<T>> clazz) throws Exception {
		if (url != null) {
			JsonHalEmbedded<T> embedded = get(url, clazz);
			if (embedded != null) {
				return embedded.get_embedded();
			}
		}
		return null;
	}

	private <T> T get(String url, Class<T> clazz) throws Exception {
		HttpHeaders headers = new HttpHeaders();

		headers.setAccept(MediaType.parseMediaTypes("application/hal+json"));

		@SuppressWarnings({ "rawtypes", "unchecked" })
		HttpEntity<?> entity = new HttpEntity(headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, clazz);

		return response.getBody();
	}

}
