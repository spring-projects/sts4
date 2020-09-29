/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.DependencyGroup;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springframework.ide.eclipse.boot.wizard.DefaultDependencies;
import org.springframework.ide.eclipse.boot.wizard.DependencyFilterBox;
import org.springframework.ide.eclipse.boot.wizard.DependencyTooltipContent;
import org.springframework.ide.eclipse.boot.wizard.HierarchicalMultiSelectionFieldModel;
import org.springframework.ide.eclipse.boot.wizard.MultiSelectionFieldModel;
import org.springframework.ide.eclipse.boot.wizard.NewSpringBootWizardModel;
import org.springframework.ide.eclipse.boot.wizard.PopularityTracker;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 *
 * Model for a local project that contains relevant data downloaded from initializr relevant to this
 * project. For example, it contains dependencies from initializr that pertain to the project's boot version.
 */
public class InitializrModel  {

	public static final Object JOB_FAMILY = "EditStartersModel.JOB_FAMILY";

	public final DependencyFilterBox searchBox = new DependencyFilterBox();
	private final ISpringBootProject bootProject;
	private final PopularityTracker popularities;
	private final DefaultDependencies defaultDependencies;

	public final HierarchicalMultiSelectionFieldModel<Dependency> dependencies = new HierarchicalMultiSelectionFieldModel<>(
			Dependency.class, "dependencies").label("Dependencies:");

	/**
	 * Create EditStarters dialog model and initialize it based on a project
	 * selection.
	 *
	 */
	public InitializrModel(ISpringBootProject bootProject,
			AddStartersPreferences preferences) throws Exception {
		this.popularities = new PopularityTracker(preferences.getPreferenceStore());
		this.defaultDependencies = new DefaultDependencies(preferences.getPreferenceStore());
		this.bootProject = bootProject;
	}

	public void updateDependencyCount() {
		List<Dependency> selected = dependencies.getCurrentSelection();

		for (Dependency s : selected) {
			popularities.incrementUsageCount(s);
		}
	}

	public void downloadDependencies() throws Exception {
		SpringBootStarters starters = bootProject.getStarterInfos();
		if (starters != null) {
			for (DependencyGroup dgroup : starters.getDependencyGroups()) {
				String catName = dgroup.getName();

				// Setup template links variable values
				Map<String, String> variables = new HashMap<>();
				variables.put(InitializrServiceSpec.BOOT_VERSION_LINK_TEMPLATE_VARIABLE, starters.getBootVersion());

				// Create all dependency boxes
				for (Dependency dep : dgroup.getContent()) {
					if (starters.contains(dep.getId())) {
						dependencies.choice(catName, dep.getName(), dep,
								() -> DependencyTooltipContent.generateHtmlDocumentation(dep, variables),
								DependencyTooltipContent.generateRequirements(dep), LiveExpression.constant(true));

						boolean selected = false;

						dependencies.setSelection(catName, dep, selected);
					}
				}
			}
		}
	}


	/**
	 * Retrieves the most popular dependencies based on the number of times they have
	 * been used to create a project. This similar to how it works in {@link NewSpringBootWizardModel}
	 * except that we add the initially selected elements regardless of their usage count and
	 * then use the usage count to backfill any remaining spots.
	 *
	 * @param howMany is an upper limit on the number of most popular items to be returned.
	 * @return An array of the most popular dependencies. May return fewer items than requested.
	 */
	public List<CheckBoxModel<Dependency>> getMostPopular(int howMany) {
		ArrayList<CheckBoxModel<Dependency>> result = new ArrayList<>();
		Set<Dependency> seen = new HashSet<>();
		for (CheckBoxModel<Dependency> cb : dependencies.getAllBoxes()) {
			if (cb.getSelection().getValue()) {
				if (seen.add(cb.getValue())) {
					result.add(cb);
				}
			}
		}
		if (result.size() < howMany) {
			//space for adding some not yet selected 'popular' selections
			List<CheckBoxModel<Dependency>> popular = popularities.getMostPopular(dependencies, howMany);
			Iterator<CheckBoxModel<Dependency>> iter = popular.iterator();
			while (result.size() < howMany && iter.hasNext()) {
				CheckBoxModel<Dependency> cb = iter.next();
				if (seen.add(cb.getValue())) {
					result.add(cb);
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Retrieves currently set default dependencies
	 * @return list of default dependencies check-box models
	 */
	public List<CheckBoxModel<Dependency>> getDefaultDependencies() {
		return defaultDependencies.getDependencies(dependencies);
	}

	public boolean saveDefaultDependencies() {
		return defaultDependencies.save(dependencies);
	}

	/**
	 * Retrieves frequently used dependencies.
	 *
	 * @param numberOfMostPopular max number of most popular dependencies
	 * @return list of frequently used dependencies
	 */
	public List<CheckBoxModel<Dependency>> getFrequentlyUsedDependencies(int numberOfMostPopular) {
		List<CheckBoxModel<Dependency>> dependencies = getDefaultDependencies();
		Set<String> defaultDependecyIds = defaultDependencies.getDependciesIdSet();
		getMostPopular(numberOfMostPopular).stream().filter(checkboxModel -> {
			return !defaultDependecyIds.contains(checkboxModel.getValue().getId());
		}).forEach(dependencies::add);
		// Sort alphabetically
		dependencies.sort(new Comparator<CheckBoxModel<Dependency>>() {
			@Override
			public int compare(CheckBoxModel<Dependency> d1, CheckBoxModel<Dependency> d2) {
				return d1.getLabel().compareTo(d2.getLabel());
			}
		});
		return dependencies;
	}


	/**
	 * Convenience method for easier scripting of the wizard model (used in testing). Not used
	 * by the UI itself. If the dependencyId isn't found in the wizard model then an IllegalArgumentException
	 * will be raised.
	 */
	public void removeDependency(String dependencyId) {
		for (String catName : dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> cat = dependencies.getContents(catName);
			for (Dependency dep : cat.getChoices()) {
				if (dependencyId.equals(dep.getId())) {
					cat.unselect(dep);
					return; //dep found and unselected
				}
			}
		}
		throw new IllegalArgumentException("No such dependency: "+dependencyId);
	}

	/**
	 * Convenience method for easier scripting of the wizard model (used in testing). Not used
	 * by the UI itself. If the dependencyId isn't found in the wizard model then an IllegalArgumentException
	 * will be raised.
	 */
	public void addDependency(String dependencyId){
		for (String catName : dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> cat = dependencies.getContents(catName);
			for (Dependency dep : cat.getChoices()) {
				if (dependencyId.equals(dep.getId())) {
					cat.select(dep);
					return; //dep found and added to selection
				}
			}
		}
		throw new IllegalArgumentException("No such dependency: "+dependencyId);
	}

	public ISpringBootProject getProject() {
		return bootProject;
	}

	public void onDependencyChange(Runnable runnable) {
		ValueListener<Boolean> selectionListener = (exp, val) -> {
			runnable.run();
		};

		for (String cat : dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> dependencyGroup = dependencies.getContents(cat);
			dependencyGroup.addSelectionListener(selectionListener);
		}
	}
}
