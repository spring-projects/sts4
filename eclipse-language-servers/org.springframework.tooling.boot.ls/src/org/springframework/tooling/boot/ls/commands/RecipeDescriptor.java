/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.commands;

import java.util.List;
import java.util.Set;

final class RecipeDescriptor {
	
	enum CheckedState {
		UNCHECKED,
		CHECKED,
		GRAYED
	}
	
    String name;

    String displayName;

    String description;

    Set<String> tags;

    List<OptionDescriptor> options;

    List<String> languages;

    List<RecipeDescriptor> recipeList;
    
    boolean hasSubRecipes = false;
    
    RecipeDescriptor parent;
    
    CheckedState checked = CheckedState.UNCHECKED;

    record OptionDescriptor(
        String name,
        String type,
        String displayName,
        String description,
        String example,
        List<String> valid,
        boolean required,
        Object value
    ) {}
    
    record RecipeSelection(boolean selected, String id, RecipeSelection[] subselection) {}
    
}