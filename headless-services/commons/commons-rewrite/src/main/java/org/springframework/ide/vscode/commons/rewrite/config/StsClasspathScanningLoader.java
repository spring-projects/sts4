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

import static org.openrewrite.internal.RecipeIntrospectionUtils.constructRecipe;
import static org.openrewrite.internal.RecipeIntrospectionUtils.recipeDescriptorFromRecipe;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nullable;

import org.openrewrite.Recipe;
import org.openrewrite.config.CategoryDescriptor;
import org.openrewrite.config.DeclarativeRecipe;
import org.openrewrite.config.RecipeDescriptor;
import org.openrewrite.config.RecipeExample;
import org.openrewrite.config.ResourceLoader;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.internal.RecipeIntrospectionUtils;
import org.openrewrite.style.NamedStyles;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class StsClasspathScanningLoader implements ResourceLoader, StsResourceLoader {
	
    private final List<Recipe> recipes = new ArrayList<>();
    private final List<NamedStyles> styles = new ArrayList<>();

    private final List<RecipeDescriptor> recipeDescriptors = new ArrayList<>();
    private final List<CategoryDescriptor> categoryDescriptors = new ArrayList<>();
    private final List<RecipeExample> recipeExamples = new ArrayList<>();
    
    private final List<CodeActionRepository> codeActionRepos = new ArrayList<>(); 

	public StsClasspathScanningLoader(Path p, Properties properties, ClassLoader classLoader) {
		if (Files.isDirectory(p)) {
			String dir = p.toString();
			
	        scanClasses(new ClassGraph()
	                .acceptPaths(dir)
	                .ignoreParentClassLoaders()
	                .overrideClassLoaders(classLoader), classLoader);

	        scanYaml(new ClassGraph()
	                .acceptPaths(dir)
	                .ignoreParentClassLoaders()
	                .overrideClassLoaders(classLoader)
	                .acceptPaths("META-INF/rewrite"), properties, classLoader);

		} else {
	        String jarName = p.toFile().getName();

	        scanClasses(new ClassGraph()
	                .acceptJars(jarName)
	                .ignoreParentClassLoaders()
	                .overrideClassLoaders(classLoader), classLoader);

	        scanYaml(new ClassGraph()
	                .acceptJars(jarName)
	                .ignoreParentClassLoaders()
	                .overrideClassLoaders(classLoader)
	                .acceptPaths("META-INF/rewrite"), properties, classLoader);
		}
		
	}
	
    public StsClasspathScanningLoader(Properties properties, String[] acceptPackages) {
        scanClasses(new ClassGraph().acceptPackages(acceptPackages), getClass().getClassLoader());
        scanYaml(new ClassGraph().acceptPaths("META-INF/rewrite"), properties, null);
    }

    /**
     * Construct a ClasspathScanningLoader scans the provided classload for recipes
     *
     * @param properties Yaml placeholder properties
     * @param classLoader Limit scan to classes loadable by this classloader
     */
    public StsClasspathScanningLoader(Properties properties, ClassLoader classLoader) {
        scanClasses(new ClassGraph()
                 .ignoreParentClassLoaders()
                 .overrideClassLoaders(classLoader), classLoader);

        scanYaml(new ClassGraph()
                 .ignoreParentClassLoaders()
                 .overrideClassLoaders(classLoader)
                 .acceptPaths("META-INF/rewrite"), properties, classLoader);
    }
    
    /**
     * This must be called _after_ scanClasses or the descriptors of declarative recipes will be missing any
     * non-declarative recipes they depend on that would be discovered by scanClasses
     */
    private void scanYaml(ClassGraph classGraph, Properties properties, @Nullable ClassLoader classLoader) {
        try (ScanResult scanResult = classGraph.enableMemoryMapping().scan()) {
            List<YamlResourceLoader> yamlResourceLoaders = new ArrayList<>();

            scanResult.getResourcesWithExtension("yml").forEachInputStreamIgnoringIOException((res, input) -> {
                yamlResourceLoaders.add(new YamlResourceLoader(input, res.getURI(), properties, classLoader));
            });
            // Extract in two passes so that the full list of recipes from all sources are known when computing recipe descriptors
            // Otherwise recipes which include recipes from other sources in their recipeList will have incomplete descriptors
            for(YamlResourceLoader resourceLoader : yamlResourceLoaders) {
                recipes.addAll(resourceLoader.listRecipes());
                categoryDescriptors.addAll(resourceLoader.listCategoryDescriptors());
                styles.addAll(resourceLoader.listStyles());
                recipeExamples.addAll(resourceLoader.listRecipeExamples());
            }
            for(YamlResourceLoader resourceLoader : yamlResourceLoaders) {
                recipeDescriptors.addAll(resourceLoader.listRecipeDescriptors(recipes));
            }
        }
    }

    private void scanClasses(ClassGraph classGraph, ClassLoader classLoader) {
        try (ScanResult result = classGraph
                .ignoreClassVisibility()
                .overrideClassLoaders(classLoader)
                .scan()) {

            for (ClassInfo classInfo : result.getSubclasses(Recipe.class.getName())) {
                Class<?> recipeClass = classInfo.loadClass();
                if (recipeClass.getName().equals(DeclarativeRecipe.class.getName()) || recipeClass.getEnclosingClass() != null) {
                    continue;
                }
                try {
                    Recipe recipe = constructRecipe(recipeClass);
                    recipeDescriptors.add(recipeDescriptorFromRecipe(recipe));
                    recipes.add(recipe);
                } catch (Exception e) {
//                    logger.warn("Unable to configure {}", recipeClass.getName(), e);
                }
            }
            for (ClassInfo classInfo : result.getSubclasses(NamedStyles.class.getName())) {
                Class<?> styleClass = classInfo.loadClass();
                try {
                    Constructor<?> constructor = RecipeIntrospectionUtils.getZeroArgsConstructor(styleClass);
                    if(constructor != null) {
                        constructor.setAccessible(true);
                        styles.add((NamedStyles) constructor.newInstance());
                    }
                } catch (Exception e) {
//                    logger.warn("Unable to configure {}", styleClass.getName(), e);
                }
            }
            
            for (ClassInfo classInfo : result.getSubclasses(CodeActionRepository.class.getName())) {
            	Class<?> codeActionRepoClass = classInfo.loadClass();
                Constructor<?> primaryConstructor = RecipeIntrospectionUtils.getZeroArgsConstructor(codeActionRepoClass);
                if (primaryConstructor == null) {
                	//TODO: error!!!
                } else {
                	try {
						CodeActionRepository repo = (CodeActionRepository) primaryConstructor.newInstance();
						codeActionRepos.add(repo);
					} catch (Exception e) {
						e.printStackTrace();
					}
                }

            }
        }
    }

    @Override
    public Collection<Recipe> listRecipes() {
        return recipes;
    }

    @Override
    public Collection<RecipeDescriptor> listRecipeDescriptors() {
        return recipeDescriptors;
    }

    @Override
    public Collection<CategoryDescriptor> listCategoryDescriptors() {
        return categoryDescriptors;
    }

    @Override
    public Collection<NamedStyles> listStyles() {
        return styles;
    }

    @Override
    public Collection<RecipeExample> listRecipeExamples() {
        return recipeExamples;
    }

	public List<CodeActionRepository> listCodeActionDescriptorsRepositories() {
		return codeActionRepos;
	}
	
}
