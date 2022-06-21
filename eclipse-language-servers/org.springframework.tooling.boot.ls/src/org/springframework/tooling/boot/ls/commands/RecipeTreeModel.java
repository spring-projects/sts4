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

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;


public class RecipeTreeModel {
	
	public enum CheckedState {
		UNCHECKED,
		CHECKED,
		GRAYED
	}
	
	private RecipeDescriptor[] recipeDescriptors;
	private Map<RecipeDescriptor, RecipeDescriptor> parentMap = new IdentityHashMap<>();
	private Map<RecipeDescriptor, CheckedState> checkedMap = new IdentityHashMap<>();

	RecipeTreeModel(RecipeDescriptor[] recipeDescriptors) {
		this.recipeDescriptors = recipeDescriptors;
		for (RecipeDescriptor d : recipeDescriptors) {
			initParentMap(d);
		}
	}
	
	private void initParentMap(RecipeDescriptor d) {
		if (d.recipeList != null) {
			for (RecipeDescriptor dc : d.recipeList) {
				parentMap.put(dc, d);
				initParentMap(dc);
			}
		}
	}
	
	public void check(RecipeDescriptor d) {
		if (simpleCheck(d)) {
			inferCheckedStateFromChildren(parentMap.get(d));
		}
	}
	
	private boolean simpleCheck(RecipeDescriptor d) {
		if (checkedMap.get(d) != CheckedState.CHECKED) {
			checkedMap.put(d, CheckedState.CHECKED);
			for (RecipeDescriptor dc : d.recipeList) {
				simpleCheck(dc);
			}
			return true;
		}
		return false;
	}
	
	public void uncheck(RecipeDescriptor d) {
		if (simpleUncheck(d)) {
			inferCheckedStateFromChildren(parentMap.get(d));
		}
	}
	
	private boolean simpleUncheck(RecipeDescriptor d) {
		if (checkedMap.get(d) != CheckedState.UNCHECKED) {
			checkedMap.put(d, CheckedState.UNCHECKED);
			for (RecipeDescriptor dc : d.recipeList) {
				simpleUncheck(dc);
			}
			return true;
		}
		return false;
	}
	
	public CheckedState getCheckedState(RecipeDescriptor d) {
		CheckedState state = checkedMap.get(d);
		return state == null ? CheckedState.UNCHECKED : state;
	}
	
	private void inferCheckedStateFromChildren(RecipeDescriptor d) {
		if (d != null && d.recipeList != null) {
			boolean all = true;
			boolean none = true;
			for (RecipeDescriptor child : d.recipeList) {
				CheckedState childState = getCheckedState(child);
				if (childState == CheckedState.UNCHECKED) {
					all = false;
				} else {
					none = false;
				}
			}
			CheckedState inferredState = CheckedState.GRAYED;
			if (all) {
				inferredState = CheckedState.CHECKED;
			} else if (none) {
				inferredState = CheckedState.UNCHECKED;
			}
			if (getCheckedState(d) != inferredState) {
				checkedMap.put(d, inferredState);
				inferCheckedStateFromChildren(parentMap.get(d));
			}
		}
	}

	public RecipeDescriptor[] getRecipeDescriptors() {
		return recipeDescriptors;
	}
	
	public RecipeDescriptor getSelectedRecipeDescriptors() throws CoreException {
		RecipeDescriptor[] recipes = Arrays.stream(recipeDescriptors).map(this::copySelectedDescriptor).filter(Objects::nonNull).toArray(RecipeDescriptor[]::new);
		if (recipes.length == 0) {
			throw new CoreException(Status.error("No recipes selected"));
		} else if (recipes.length == 1) {
			return recipes[0];
		} else {
			RecipeDescriptor aggregate = new RecipeDescriptor();
			aggregate.displayName = recipes.length + " recipes";
			aggregate.description = "Multiple recipes to be applied. Number of recipes " + recipes.length;
			aggregate.tags = Arrays.stream(recipes).flatMap(r -> r.tags.stream()).collect(Collectors.toSet());
			aggregate.recipeList = Arrays.asList(recipes);
			return aggregate;
		}
	}
	
	private RecipeDescriptor copySelectedDescriptor(RecipeDescriptor d) {
		if (getCheckedState(d) != CheckedState.UNCHECKED) {
			RecipeDescriptor copy = d.getCopyWithoutSubRecipes();
			if (d.recipeList != null) {
				copy.recipeList = d.recipeList.stream().map(this::copySelectedDescriptor).filter(Objects::nonNull).collect(Collectors.toList());
			}
			return copy;
		}
		return null;
	}

}
