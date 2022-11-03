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
package org.springframework.ide.vscode.commons.rewrite.config;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openrewrite.config.ClasspathScanningLoader;
import org.openrewrite.config.Environment;
import org.openrewrite.config.ResourceLoader;

import static java.util.Collections.emptyList;

public class StsEnvironment extends Environment {
	
	final private Supplier<Stream<CodeActionRepository>> codeActionRepos; 

	public StsEnvironment(Collection<? extends ResourceLoader> resourceLoaders) {
		super(resourceLoaders);
		codeActionRepos = () -> resourceLoaders.stream().filter(StsResourceLoader.class::isInstance).map(StsResourceLoader.class::cast).flatMap(l -> l.listCodeActionDescriptorsRepositories().stream());
	}
	
	public static class Builder extends Environment.Builder {
		
		final private Properties props; 

		public Builder(Properties properties) {
			super(properties);
			this.props = properties;
		}

		@Override
		public org.openrewrite.config.Environment.Builder scanRuntimeClasspath(String... acceptPackages) {
			return load(new StsClasspathScanningLoader(props, acceptPackages));
		}

		@Override
		public org.openrewrite.config.Environment.Builder scanClassLoader(ClassLoader classLoader) {
			return load(new StsClasspathScanningLoader(props, classLoader));
		}

		@Override
		public Environment.Builder scanJar(Path jar, Collection<Path> dependencies, ClassLoader classLoader) {
			List<ClasspathScanningLoader> list = new ArrayList<>();
			for (Path dep : dependencies) {
				ClasspathScanningLoader classpathScanningLoader = new ClasspathScanningLoader(dep, props, emptyList(), classLoader);
				list.add(classpathScanningLoader);
			}
			return load(new StsClasspathScanningLoader(jar, props, list, classLoader), list);
		}

		public org.openrewrite.config.Environment.Builder scanPath(Path dir, Collection<Path> dependencies, ClassLoader classLoader) {
			List<ClasspathScanningLoader> list = new ArrayList<>();
			for (Path dep : dependencies) {
				ClasspathScanningLoader classpathScanningLoader = new ClasspathScanningLoader(dep, props, emptyList(), classLoader);
				list.add(classpathScanningLoader);
			}
			return load(new StsClasspathScanningLoader(dir, props, list, classLoader));
		}
		
        @SuppressWarnings("unchecked")
		public StsEnvironment build() {
        	try {
	        	Field f = Environment.Builder.class.getDeclaredField("resourceLoaders");
	        	f.setAccessible(true);
	            return new StsEnvironment((Collection<ResourceLoader>) f.get(this));
        	} catch (Exception e) {
        		throw new IllegalStateException(e);
        	}
        }

		
	}
	
	public List<RecipeCodeActionDescriptor> listCodeActionDescriptors() {
		return codeActionRepos.get().flatMap(r -> r.getCodeActionDescriptors().stream()).collect(Collectors.toList());
	}
	
    public static Builder builder() {
        return new Builder(new Properties());
    }

}
