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
package org.springframework.tooling.boot.ls.commands;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;

class RecipeDescriptor {
	
    String name;

    String displayName;

    String description;

    Set<String> tags;

    Duration estimatedEffortPerOccurrence;

    List<OptionDescriptor> options;

    List<String> languages;

    List<RecipeDescriptor> recipeList;

    URI source;
    
    RecipeDescriptor getCopyWithoutSubRecipes() {
    	RecipeDescriptor copy = new RecipeDescriptor();
    	copy.name = name;
    	copy.displayName = displayName;
    	copy.description = description;
    	copy.tags = tags;
    	copy.estimatedEffortPerOccurrence = estimatedEffortPerOccurrence;
    	copy.options = options;
    	copy.languages = languages;
    	copy.source = source;
    	return copy;
    }
        
    static class OptionDescriptor {

        String name;

        String type;

        String displayName;

        String description;

        String example;

        List<String> valid;

        boolean required;

        Object value;
    }
}