/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class RateLimitResponse {

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class RateLimit {
		private int limit;
		private int remaining;
		private int reset;
		public int getLimit() {
			return limit;
		}
		public void setLimit(int limit) {
			this.limit = limit;
		}
		public int getRemaining() {
			return remaining;
		}
		public void setRemaining(int remaining) {
			this.remaining = remaining;
		}
		public int getReset() {
			return reset;
		}
		public void setReset(int reset) {
			this.reset = reset;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class RateLimits {

		private RateLimit core;
		private RateLimit search;

		public RateLimit getCore() {
			return core;
		}
		public void setCore(RateLimit core) {
			this.core = core;
		}
		public RateLimit getSearch() {
			return search;
		}
		public void setSearch(RateLimit search) {
			this.search = search;
		}
	}

	private RateLimits resources;

	public RateLimits getResources() {
		return resources;
	}

	public void setResources(RateLimits resources) {
		this.resources = resources;
	}

	public RateLimit getRate() {
		return getResources().getCore();
	}
}
