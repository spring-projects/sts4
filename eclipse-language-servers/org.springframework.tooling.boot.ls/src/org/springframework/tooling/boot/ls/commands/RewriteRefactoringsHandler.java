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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RewriteRefactoringsHandler extends AbstractHandler {
	
	private static class DurationTypeConverter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
		@Override
		public JsonElement serialize(Duration src, Type srcType, JsonSerializationContext context) {
			return new JsonPrimitive(src.toNanos());
		}
	
		@Override
		public Duration deserialize(JsonElement json, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			return Duration.ofNanos(json.getAsLong());
		}
	}
	
	private static Gson serializationGson = new GsonBuilder()
			.registerTypeAdapter(Duration.class, new DurationTypeConverter())
			.create();


	private static final String REWRITE_REFACTORINGS_LIST = "sts/rewrite/list";
	private static final String REWRITE_REFACTORINGS_EXEC = "sts/rewrite/execute";

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
		if (project != null) {
			List<@NonNull LanguageServer> usedLanguageServers = LanguageServiceAccessor
					.getActiveLanguageServers(serverCapabilities -> true);

			if (!usedLanguageServers.isEmpty()) {
				final String uri = project.getLocationURI().toString();
				ExecuteCommandParams commandParams = new ExecuteCommandParams();
				commandParams.setCommand(REWRITE_REFACTORINGS_LIST);
				commandParams.setArguments(List.of(uri));

				try {
					List<Object> allRewriteRecipesJson = new ArrayList<>();
					List<Object> syncRecipesJson = Collections.synchronizedList(allRewriteRecipesJson);
					CompletableFuture.allOf(usedLanguageServers.stream()
							.map(ls -> ls.getWorkspaceService().executeCommand(commandParams).thenAccept(or -> {
								if (or != null) {
									syncRecipesJson.add(or);
								}
							}).exceptionally(t -> null))
							.toArray(CompletableFuture[]::new)).thenRun(() -> {
								allRewriteRecipesJson.stream().filter(List.class::isInstance).map(List.class::cast).findFirst().ifPresent(obj -> {
									RecipeDescriptor[] descriptors = serializationGson.fromJson(serializationGson.toJson(obj), RecipeDescriptor[].class);
									
									PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
										RecipeTreeModel recipesModel = new RecipeTreeModel(descriptors);
										int returnCode = new SelectRecipesDialog(Display.getCurrent().getActiveShell(), recipesModel).open();
										if (returnCode == Window.OK) {
											try {
												RecipeDescriptor recipeToApply = recipesModel.getSelectedRecipeDescriptors();
												PlatformUI.getWorkbench().getProgressService().run(true, false, monitor -> {
													try {
														if (!usedLanguageServers.isEmpty()) {
															monitor.beginTask("Applying recipe '" + recipeToApply.displayName + "'", IProgressMonitor.UNKNOWN);
															ExecuteCommandParams cmdParams = new ExecuteCommandParams();
															cmdParams.setCommand(REWRITE_REFACTORINGS_EXEC);
															cmdParams.setArguments(List.of(
																	uri,
																	serializationGson.toJsonTree(recipeToApply)
															));
	
															CompletableFuture.allOf(usedLanguageServers.stream()
																	.map(ls -> ls.getWorkspaceService().executeCommand(cmdParams))
																	.toArray(CompletableFuture[]::new)).get();
														}
													} catch (Exception e) {
														Log.log(e);
													} finally {
														monitor.done();
													}
												});
												
											} catch (CoreException | InvocationTargetException | InterruptedException e) {
												BootLanguageServerPlugin.getDefault().getLog().error(e.getMessage(), e);
											}
										}
									});
								});
								
							});
				} catch (Exception e) {
					throw new ExecutionException("Failed to apply Rewrite recipe(s)", e);
				}
			}

		}
		return null;
	}

}
