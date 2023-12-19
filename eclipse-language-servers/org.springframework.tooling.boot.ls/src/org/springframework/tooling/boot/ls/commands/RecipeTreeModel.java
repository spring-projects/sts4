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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.springframework.tooling.boot.ls.commands.RecipeDescriptor.CheckedState;
import org.springframework.tooling.boot.ls.commands.RecipeDescriptor.RecipeSelection;


public class RecipeTreeModel {
	
	private static final String REWRITE_REFACTORINGS_LIST = "sts/rewrite/list";
	private static final String REWRITE_REFACTORINGS_SUBLIST = "sts/rewrite/sublist";
	
	private RecipeDescriptor[] recipeDescriptors;

	final private WorkspaceService workspaceService;
	final private String recipeFilter;

	RecipeTreeModel(WorkspaceService workspaceService, String recipeFilter) {
		this.workspaceService = workspaceService;
		this.recipeFilter = recipeFilter;
	}
	
	public void check(RecipeDescriptor d) {
		if (simpleCheck(d)) {
			inferCheckedStateFromChildren(d.parent);
		}
	}
	
	private boolean simpleCheck(RecipeDescriptor d) {
		if (d.checked != CheckedState.CHECKED) {
			d.checked = CheckedState.CHECKED;
			if (d.recipeList != null) {
				for (RecipeDescriptor dc : d.recipeList) {
					simpleCheck(dc);
				}
			}
			return true;
		}
		return false;
	}
	
	public void uncheck(RecipeDescriptor d) {
		if (simpleUncheck(d)) {
			inferCheckedStateFromChildren(d.parent);
		}
	}
	
	private boolean simpleUncheck(RecipeDescriptor d) {
		if (d.checked != CheckedState.UNCHECKED) {
			d.checked = CheckedState.UNCHECKED;
			if (d.recipeList != null) {
				for (RecipeDescriptor dc : d.recipeList) {
					simpleUncheck(dc);
				}
			}
			return true;
		}
		return false;
	}
	
	private void inferCheckedStateFromChildren(RecipeDescriptor d) {
		if (d != null && d.recipeList != null) {
			boolean all = true;
			boolean none = true;
			for (RecipeDescriptor child : d.recipeList) {
				CheckedState childState = child.checked;
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
			if (d.checked != inferredState) {
				d.checked = inferredState;
				inferCheckedStateFromChildren(d.parent);
			}
		}
	}

	public RecipeDescriptor[] getRecipeDescriptors() {
		return recipeDescriptors;
	}
	
	public RecipeSelection[] getRecipeSelection() throws CoreException {
		List<RecipeSelection> rootSelected = new ArrayList<>();
		for (int i = 0; i < recipeDescriptors.length; i++) {
			if (recipeDescriptors[i].checked != CheckedState.UNCHECKED) {
				rootSelected.add(new RecipeSelection(true, recipeDescriptors[i].name, createRecipeSelection(recipeDescriptors[i])));
			}
		}
		if (rootSelected.isEmpty()) {
			throw new CoreException(Status.error("No recipes selected"));
		}
		return rootSelected.toArray(new RecipeSelection[rootSelected.size()]);
	}
	
	private RecipeSelection[] createRecipeSelection(RecipeDescriptor d) {
		if (d.recipeList != null) {
			return d.recipeList.stream()
					.map(s -> new RecipeSelection(s.checked != CheckedState.UNCHECKED, s.name, createRecipeSelection(s)))
					.toArray(RecipeSelection[]::new);
		}
		return null;
	}
	
	CompletableFuture<Void> fetchSubrecipes(RecipeDescriptor descriptor) {
		RecipeDescriptor d = descriptor;
		LinkedList<Integer> indexPath = new LinkedList<>();
		for (; d.parent != null; d = d.parent) {
			indexPath.addFirst(d.parent.recipeList.indexOf(d));
		}
		ExecuteCommandParams commandParams = new ExecuteCommandParams();
		commandParams.setCommand(REWRITE_REFACTORINGS_SUBLIST);
		commandParams.setArguments(List.of(d.name, indexPath));
		return workspaceService.executeCommand(commandParams).thenAccept(json -> {
			RecipeDescriptor[] fetchedDescriptors = RewriteRefactoringsHandler.SERIALIZATION_GSON.fromJson(RewriteRefactoringsHandler.SERIALIZATION_GSON.toJson(json), RecipeDescriptor[].class);
			for (RecipeDescriptor fd : fetchedDescriptors) {
				fd.parent = descriptor;
				fd.checked = descriptor.checked != CheckedState.UNCHECKED ? CheckedState.CHECKED : CheckedState.UNCHECKED;
			}
			descriptor.recipeList = Arrays.asList(fetchedDescriptors);
		});
	}
	
	CompletableFuture<Void> fetchRootRecipes() {
		ExecuteCommandParams commandParams = new ExecuteCommandParams();
		commandParams.setCommand(REWRITE_REFACTORINGS_LIST);
		commandParams.setArguments(List.of(recipeFilter));
		return workspaceService.executeCommand(commandParams).thenAccept(json -> {
			recipeDescriptors = RewriteRefactoringsHandler.SERIALIZATION_GSON.fromJson(RewriteRefactoringsHandler.SERIALIZATION_GSON.toJson(json), RecipeDescriptor[].class);
		});
	}
	
	String getSelectedRecipeDisplayName() {
		List<RecipeDescriptor> rootSelected = new ArrayList<>();
		for (int i = 0; i < recipeDescriptors.length; i++) {
			if (recipeDescriptors[i].checked != CheckedState.UNCHECKED) {
				rootSelected.add(recipeDescriptors[i]);
			}
		}
		if (rootSelected.isEmpty()) {
			return "No Recipes Selected";
		} else if (rootSelected.size() == 1) {
			return rootSelected.get(0).displayName;
		} else {
			return "%s recipes".formatted(rootSelected.size());
		}
	}
	
}
