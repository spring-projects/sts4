/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import static org.springframework.ide.eclipse.boot.wizard.starters.PathSelectors.path;
import static org.springframework.ide.eclipse.boot.wizard.starters.PathSelectors.pattern;
import static org.springframework.ide.eclipse.boot.wizard.starters.PathSelectors.rootFiles;
import static org.springframework.ide.eclipse.boot.wizard.starters.eclipse.ResourceCompareInput.fromFile;
import static org.springframework.ide.eclipse.boot.wizard.starters.eclipse.ResourceCompareInput.fromWorkspaceResource;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrl;
import org.springframework.ide.eclipse.boot.wizard.BootWizardActivator;
import org.springframework.ide.eclipse.boot.wizard.preferences.PreferenceConstants;
import org.springframework.ide.eclipse.boot.wizard.starters.eclipse.ResourceCompareInput;
import org.springframework.ide.eclipse.maven.pom.PomPlugin;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * Model that contains a comparison between local and intializr projects, as well as
 * the Eclipse editor input equivalent for integration with Eclipse compare/merge editor.
 *
 */
public class AddStartersCompareModel implements Disposable {

	private final ISpringBootProject bootProject;
	private final InitializrProjectDownloader projectDownloader;

	private LiveVariable<AddStartersCompareResult> comparison = new LiveVariable<>(null);

	private LiveVariable<ResourceCompareInput> editorInput = new LiveVariable<>(null);

	private LiveVariable<AddStartersTrackerState> downloadTracker = new LiveVariable<>(
			AddStartersTrackerState.NOT_STARTED);
	private final InitializrModel initializrModel;

	public AddStartersCompareModel(InitializrProjectDownloader projectDownloader, InitializrModel initializrModel) {
		this.bootProject = initializrModel.getProject();
		this.projectDownloader = projectDownloader;
		this.initializrModel = initializrModel;
	}

	/**
	 * Comparison that contains a left and right side (local project and project downloaded from
	 * initializr)
	 */
	public LiveExpression<AddStartersCompareResult> getComparison() {
		return comparison;
	}

	/**
	 * Eclipse compare editor input used to integrate with the Eclipse compare framework
	 */
	public LiveVariable<ResourceCompareInput> getCompareEditorInput() {
		return editorInput;
	}

	public LiveVariable<AddStartersTrackerState> getStateTracker() {
		return downloadTracker;
	}

	@Override
	public void dispose() {
		disposeTrackers();
		this.projectDownloader.dispose();
	}

	public void generateComparison(IProgressMonitor monitor) {
		try {
			monitor.beginTask("Downloading 'starter.zip' from Initializr Service", IProgressMonitor.UNKNOWN);
			downloadTracker.setValue(AddStartersTrackerState.IS_DOWNLOADING);
			List<Dependency> dependencies = initializrModel.dependencies.getCurrentSelection();
			File generatedProject = projectDownloader.getProject(dependencies, bootProject);
			IProject project = bootProject.getProject();
			boolean editable = true;
			LocalProject localProject = new LocalProject(project, editable);

			AddStartersCompareResult result = new AddStartersCompareResult(localProject, generatedProject);
			comparison.setValue(result);
			downloadTracker.setValue(AddStartersTrackerState.DOWNLOADING_COMPLETED);
			generateEditorInput(result, monitor);

		} catch (Exception e) {
			downloadTracker.setValue(AddStartersTrackerState.error(e));
			Log.log(e);
		} finally {
			monitor.done();
		}
	}

	public void initTrackers() {
		comparison = new LiveVariable<>(null);
		editorInput = new LiveVariable<>(null);
		downloadTracker = new LiveVariable<>(AddStartersTrackerState.NOT_STARTED);
	}

	public void disposeTrackers() {
		this.comparison.dispose();
		this.editorInput.dispose();
		this.downloadTracker.dispose();
	}

	static class AddStartersTrackerState {

		public static final AddStartersTrackerState NOT_STARTED = new AddStartersTrackerState("");

		public static final AddStartersTrackerState IS_DOWNLOADING = new AddStartersTrackerState(
				"Downloading project from Spring Initializr. Please wait....");

		public static final AddStartersTrackerState DOWNLOADING_COMPLETED = new AddStartersTrackerState(
				"Project downloaded from Spring Initializr successfully.");

		private final String message;
		private final Exception error;

		public AddStartersTrackerState(String message, Exception error) {
			this.message = message;
			this.error = error;
		}

		public AddStartersTrackerState(String message) {
			this(message, null);
		}

		public Exception getError() {
			return this.error;
		}

		public String getMessage() {
			return this.message;
		}

		public static AddStartersTrackerState error(Exception error) {
			String message = "Failed to download project from Spring Initializr";
			if (error != null) {
				message += ": " + error.getMessage();
			}
			return new AddStartersTrackerState(message, error);
		}
	}

	private void generateEditorInput(AddStartersCompareResult comparison, IProgressMonitor monitor)  {
		Predicate<String> filter = s -> true;
		for (String glob : excludeGlobPatterns()) {
			filter = filter.and(pattern(glob).negate());
		}

		filter = filter.and(rootFiles()
				.or(path("HELP.md"))
				.or(path("src/main/resources/application.properties"))
				.or(path("src/main/resources/application.yml"))
				.or(path("src/main/resources/static/"))
				.or(path("src/main/resources/templates/"))
				.or(pattern("src/main/resources/static/*"))
				.or(pattern("src/main/resources/templates/*")));

		ResourceCompareInput compareEditorInput = new ResourceCompareInput(comparison.getConfiguration(), filter);
		setResources(compareEditorInput, comparison);
		compareEditorInput.setTitle(
				"Compare local project on the left with generated project from Spring Initializr on the right");
		compareEditorInput.getCompareConfiguration().setProperty(PomPlugin.POM_STRUCTURE_ADDITIONS_COMPARE_SETTING, true);
		compareEditorInput.getCompareConfiguration().setProperty(ResourceCompareInput.OPEN_DIFF_NODE_COMPARE_SETTING, diffFileToOpenInitially());

		try {
			monitor.beginTask(
					"Calculating differences between project '"
							+ comparison.getLocalResource().getProject().getName() + "' and 'starter.zip'",
					IProgressMonitor.UNKNOWN);

			compareEditorInput.run(monitor);
			editorInput.setValue(compareEditorInput);
		} catch (Exception e) {
			downloadTracker.setValue(AddStartersTrackerState.error(e));
			Log.log(e);
		}
	}

	private String diffFileToOpenInitially() {
		if (bootProject != null) {
			switch (bootProject.buildType()) {
			case InitializrUrl.MAVEN_PROJECT:
				return "pom.xml";
			case InitializrUrl.GRADLE_PROJECT:
				return "build.gradle";
			}
		}
		return null;
	}

	private String[] excludeGlobPatterns() {
		String globStr = BootWizardActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.ADD_STARTERS_EXCLUDE_RESOURCES_FROM_COMPARE).trim();
		return globStr.split("\\s*,\\s*");
	}

	/**
	 * Sets the "left" and "right" resources to compare in the compare editor input
	 *
	 * @param input
	 * @param inputFromModel
	 * @throws Exception
	 */
	private void setResources(ResourceCompareInput input, AddStartersCompareResult inputFromModel)  {
		IProject leftProject = inputFromModel.getLocalResource().getProject();
		input.setSelection(fromFile(inputFromModel.getDownloadedProject()), fromWorkspaceResource(leftProject));
	}
}
