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
package org.springframework.ide.vscode.commons.rewrite.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openrewrite.Recipe;
import org.openrewrite.config.Environment;
import org.openrewrite.config.RecipeDescriptor;
import org.openrewrite.config.ResourceLoader;

public class StsEnvironment {

	final private Supplier<Stream<CodeActionRepository>> codeActionRepos;
	final private Environment env;

	private StsEnvironment(Environment env, List<CodeActionRepoLoader> loaders) {
		this.env = env;
		codeActionRepos = () -> loaders.stream().flatMap(l -> l.listCodeActionDescriptorsRepositories().stream());
	}

	public static class Builder {

		private List<CodeActionRepoLoader> loaders = new ArrayList<>();
		private Environment.Builder envBuilder = new Environment.Builder(new Properties());;

		public Builder scanRuntimeClasspath(String... acceptPackages) {
			loaders.add(new CodeActionRepoLoader(acceptPackages));
			envBuilder.scanRuntimeClasspath(acceptPackages);
			return this;
		}

		public Builder scanJar(Path jar, ClassLoader classLoader) {
			loaders.add(new CodeActionRepoLoader(jar, classLoader));
			envBuilder.scanJar(jar, Collections.emptyList(), classLoader);
			return this;
		}

		public StsEnvironment build() {
			return new StsEnvironment(envBuilder.build(), loaders);
		}

		public void load(ResourceLoader loader) {
			envBuilder.load(loader, Collections.emptyList());
		}

	}

	public List<RecipeCodeActionDescriptor> listCodeActionDescriptors() {
		return codeActionRepos.get().flatMap(r -> r.getCodeActionDescriptors().stream()).collect(Collectors.toList());
	}
	
	public Collection<Recipe> listRecipes() {
		return env.listRecipes();
	}
	
	public Collection<RecipeDescriptor> listRecipeDescriptors() {
		return env.listRecipeDescriptors();
	}

	public static Builder builder() {
		return new Builder();
	}

}
