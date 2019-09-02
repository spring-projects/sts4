/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.Arrays;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public class LiveBean {

	private final String id;
	private final String[] aliases;
	private final String scope;
	private final String type;
	private final String resource;
	private final String[] dependencies;

	public static class Builder {
		private String id;
		private String[] aliases = {};
		private String scope;
		private String type;
		private String resource;
		private String[] dependencies = {};

		public LiveBean build() {
			return new LiveBean(id, aliases, scope, type, resource, dependencies);
		}

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public Builder dependencies(String... deps) {
			this.dependencies = deps;
			return this;
		}

		private Builder resource(String resource) {
			this.resource = resource;
			return this;
		}

		public Builder fileResource(String path) {
			return resource("file ["+path+"]");
		}

		public Builder classpathResource(String path) {
			return resource("class path resource ["+path+"]");
		}

	}

	protected LiveBean(String id, String[] aliases, String scope, String type, String resource, String[] dependencies) {
		super();
		this.id = id;
		this.aliases = aliases;
		this.scope = scope;
		this.type = type;
		this.resource = "null".equals(resource) ? null : resource;
		this.dependencies = dependencies;
	}

	public String getId() {
		return id;
	}

	public String[] getAliases() {
		return aliases;
	}

	public String getScope() {
		return scope;
	}

	public String getType() {
		return type;
	}

	public String getType(boolean stripCGLib) {
		String type = this.type;

		if (type != null) {
			if (stripCGLib) {
				int chop = type.indexOf("$$EnhancerBySpringCGLIB$$");
				if (chop >= 0) {
					type = type.substring(0, chop);
				}

				chop = type.indexOf("$$Lambda$");
				if (chop >= 0) {
					type = type.substring(0, chop);
				}
			}
		}

		return type;
	}

	public String getResource() {
		return resource;
	}

	public String[] getDependencies() {
		return dependencies;
	}

	@Override
	public String toString() {
		return "LiveBean [id=" + id + ", type=" + type + ", dependencies=" + Arrays.toString(dependencies) + "]";
	}

	public static Builder builder() {
		return new Builder();
	}

}
