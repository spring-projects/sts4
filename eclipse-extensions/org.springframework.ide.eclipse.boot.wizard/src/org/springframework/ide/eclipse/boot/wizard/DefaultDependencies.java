/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;

/**
 * Component responsible for storing/loading dependencies selected by default.
 * It is also capable to select the default dependencies if appropriate model is
 * passed.
 * 
 * @author Alex Boyko
 *
 */
public class DefaultDependencies {
	
	private static final String PREF_DEFAULT_DEPENDENCIES = "dependencies";
	
	private static final String DEPENDECIES_DELIMITER = " ";

	private String PREFIX = PreferredSelections.class.getName()+".";
	
	private IPreferenceStore store;

	public DefaultDependencies(IPreferenceStore store) {
		Assert.isNotNull(store);
		this.store = store;
	}

	protected String key(String id) {
		String key = PREFIX+id;
		return key;
	}

	private void put(String id, String value) {
		String key = key(id);
		store.setValue(key, value);
	}

	private String get(String name, String dflt) {
		String key = key(name);
		String v = store.getString(key);
		if (StringUtils.isNotBlank(v)) {
			return v;
		}
		return dflt;
	}

	/**
	 * Stores currently selection dependencies as default dependencies 
	 * 
	 * @param model dependency selection model
	 * @return <code>true</code> if dependencies were stored, i.e. currently stored are different from the current ones. 
	 */
	public boolean save(HierarchicalMultiSelectionFieldModel<Dependency> model) {
		Set<String> ids = getDependciesIdSet();
		String[] currentSelection = model.getCurrentSelection().stream().map(Dependency::getId).toArray(String[]::new);
		if (ids.size() == currentSelection.length && ids.containsAll(Arrays.asList(currentSelection))) {
			// Nothing to store no changes detected
			return false;
		} else {
			// Store dependencies ids as current selection has differences from currently stored dependencies
			put(PREF_DEFAULT_DEPENDENCIES, String.join(DEPENDECIES_DELIMITER, currentSelection));
			return true;
		}
	}
	
	/**
	 * Selects stored default dependencies on the passed selection model 
	 * 
	 * @param model dependency selection model
	 */
	public void restore(HierarchicalMultiSelectionFieldModel<Dependency> model) {
		Arrays.asList(get(PREF_DEFAULT_DEPENDENCIES, "").split(DEPENDECIES_DELIMITER)).forEach(id -> {
			model.getCategories().stream().filter(category -> {
				Optional<Dependency> matchedDependency = Arrays.asList(model.getContents(category).getChoices()).stream().filter(dependency -> {
					return id.equals(dependency.getId());
				}).findFirst();
				matchedDependency.ifPresent(d -> {
					model.setSelection(category, d, true);
				});
				return matchedDependency.isPresent();
			}).findFirst();
		}); 
	}
	
	/**
	 * Finds check-box models from the selection model corresponding to stored default dependencies (ids)
	 * 
	 * @param model dependency selection model
	 * @return check-box models of stored default dependencies
	 */
	public List<CheckBoxModel<Dependency>> getDependencies(HierarchicalMultiSelectionFieldModel<Dependency> model) {
		Set<String> ids = getDependciesIdSet();
		List<CheckBoxModel<Dependency>> dependencies = Collections.synchronizedList(new ArrayList<>(ids.size()));
		model.getCategories().parallelStream().forEach(category -> {
			model.getContents(category).getCheckBoxModels().stream().filter(checkboxModel -> {
				return ids.contains(checkboxModel.getValue().getId());
			}).forEach(dependencies::add);
		});
		return dependencies;
	}
	
	/**
	 * Set of stored default dependencies
	 * 
	 * @return ids of default dependencies
	 */
	public Set<String> getDependciesIdSet() {
		return new HashSet<>(Arrays.asList(get(PREF_DEFAULT_DEPENDENCIES, "").split(DEPENDECIES_DELIMITER)));
	}

}
