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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;
import org.springframework.tooling.boot.ls.commands.RecipeDescriptor.RecipeSelection;
import org.springsource.ide.eclipse.commons.core.CoreUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("restriction")
public class RewriteRefactoringsHandler extends AbstractHandler {
	
	public enum RecipeFilter {
		ALL,
		BOOT_UPGRADE,
		NON_BOOT_UPGRADE
	}
	
	static final Gson SERIALIZATION_GSON = new GsonBuilder()
			.setPrettyPrinting()
			.create();


	private static final String REWRITE_REFACTORINGS_EXEC = "sts/rewrite/execute";
	
	private RecipeFilter recipeFilter;
	
	public RewriteRefactoringsHandler() {
		this(RecipeFilter.ALL);
	}
	
	public RewriteRefactoringsHandler(RecipeFilter recipeFilter) {
		this.recipeFilter = recipeFilter;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		Object o = selection.getFirstElement();
		IProject project = null;
		if (o instanceof IResource) {
			project = ((IResource) o).getProject();
		} else if (o instanceof IProject) {
			project = (IProject) o;
		} else if (o instanceof IAdaptable) {
			project = ((IAdaptable) o).getAdapter(IProject.class);
		}
		try {
			if (project != null && CoreUtil.promptForProjectSave(project)) {
				LanguageServerDefinition def = LanguageServersRegistry.getInstance().getDefinition(BootLanguageServerPlugin.BOOT_LS_DEFINITION_ID);
				Assert.isLegal(def != null, "No definition found for Boot Language Server");
				final String uri = project.getLocationURI().toASCIIString();

				try {
					
					LanguageServers.forProject(project).withPreferredServer(def).computeFirst(ls -> {
							
							PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
								RecipeTreeModel recipesModel = new RecipeTreeModel(ls.getWorkspaceService(), recipeFilter.toString());
								int returnCode = new SelectRecipesDialog(Display.getCurrent().getActiveShell(), recipesModel).open();
								if (returnCode == Window.OK) {
									try {
										final RecipeSelection[] recipeSelection = recipesModel.getRecipeSelection();
										PlatformUI.getWorkbench().getProgressService().run(true, false, monitor -> {
											try {
												monitor.beginTask("Applying recipe '%s'...".formatted(recipesModel.getSelectedRecipeDisplayName()), IProgressMonitor.UNKNOWN);
												ExecuteCommandParams cmdParams = new ExecuteCommandParams();
												cmdParams.setCommand(REWRITE_REFACTORINGS_EXEC);
												cmdParams.setArguments(List.of(
													uri,
													SERIALIZATION_GSON.toJsonTree(recipeSelection)
												));
												
												ls.getWorkspaceService().executeCommand(cmdParams).get();
											} catch (Exception e) {
												throw new InvocationTargetException(e);
											} finally {
												monitor.done();
											}
										});
										
									} catch (CoreException | InvocationTargetException | InterruptedException e) {
										BootLanguageServerPlugin.getDefault().getLog().error(e.getMessage(), e);
										MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Failed to apply Rewrite recipe(s). See error log for more details.");
									}
								}
							});
							return null;
					});
					
				} catch (Exception e) {
					throw new ExecutionException("Failed to apply Rewrite recipe(s)", e);
				}
			}
		} catch (Exception e) {
			throw new ExecutionException("Failed to save project resource(s)", e);
		}
		return null;
	}
	
	public static class UpgradeBootVersion extends RewriteRefactoringsHandler {
		public UpgradeBootVersion() {
			super(RecipeFilter.BOOT_UPGRADE);
		}
	}
	
	public static class RefactorBootProject extends RewriteRefactoringsHandler {
		public RefactorBootProject() {
			super(RecipeFilter.NON_BOOT_UPGRADE);
		}
	}
}
