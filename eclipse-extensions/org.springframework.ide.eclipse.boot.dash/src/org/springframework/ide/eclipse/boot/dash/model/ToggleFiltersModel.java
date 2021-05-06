/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springsource.ide.eclipse.commons.core.pstore.IPropertyStore;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStores;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Ilabelable;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableSet;

/**
 * The 'toggle filters' are fixed set of view filters that can be toggled on/off by the user.
 * This model element tracks the currently selected 'toggle' filters and the corresponding composite
 * filter that results from composing them.
 *
 * @author Kris De Volder
 */
public class ToggleFiltersModel {

	private static final Filter<BootDashElement> HIDE_SOLITARY_CONFS = new Filter<BootDashElement>() {
		public boolean accept(BootDashElement e) {
			if (e instanceof LaunchConfDashElement) {
				LaunchConfDashElement conf = (LaunchConfDashElement) e;
				return conf.getParent().getCurrentChildren().size()!=1;
			}
			return true;
		}
	};

	private static final Filter<BootDashElement> HIDE_NON_WORKSPACE_ELEMENTS = new Filter<BootDashElement>() {
		public boolean accept(BootDashElement t) {
			if (t!=null) {
				IProject p = t.getProject();
				return p!=null && p.exists();
			}
			return false;
		}
	};

	private static final Filter<BootDashElement> HIDE_LOCAL_SERVICES = new Filter<BootDashElement>() {
		@Override
		public boolean accept(BootDashElement t) {
			return !(t instanceof LocalCloudServiceDashElement);
		}
	};

	private static final Filter<BootDashElement> HIDE_LOCAL_NOT_RUNNABLE_APPS = new Filter<BootDashElement>() {
		@Override
		public boolean accept(BootDashElement t) {
			if (t instanceof BootProjectDashElement) {
				if (!t.getCurrentChildren().isEmpty()) {
					return true;
				}
				return ((BootProjectDashElement)t).hasMainMethod();
			}
			return true;
		}
	};

	public static final FilterChoice FILTER_CHOICE_HIDE_NON_WORKSPACE_ELEMENTS = new FilterChoice("hide.non-workspace",
			"Hide non-workspace elements", HIDE_NON_WORKSPACE_ELEMENTS);
	public static final FilterChoice FILTER_CHOICE_HIDE_SOLITARY_CONFS = new FilterChoice("hide.solitary-launch-config",
			"Hide solitary launch configs", HIDE_SOLITARY_CONFS, true);
	public static final FilterChoice FILTER_CHOICE_HIDE_LOCAL_SERVICES = new FilterChoice("hide.local-cloud-services",
			"Hide local cloud services", HIDE_LOCAL_SERVICES, true);
	public static final FilterChoice FILTER_CHOICE_HIDE_NOT_RUNNABLE_APPS = new FilterChoice("hide.not-runnable-apps",
			"Hide local non-runnable apps", HIDE_LOCAL_NOT_RUNNABLE_APPS, true);

	private static final String STORE_ID = "toggle-filters";
	private static final FilterChoice[] FILTERS = {
			FILTER_CHOICE_HIDE_NON_WORKSPACE_ELEMENTS,
			FILTER_CHOICE_HIDE_SOLITARY_CONFS,
			FILTER_CHOICE_HIDE_LOCAL_SERVICES,
			FILTER_CHOICE_HIDE_NOT_RUNNABLE_APPS
	};

	private final PropertyStoreApi persistentProperties;

	public ToggleFiltersModel(BootDashModelContext context) {
		this(PropertyStores.createSubStore(STORE_ID, context.getViewProperties()));
	}

	public ToggleFiltersModel(IPropertyStore propertyStore) {
		this.persistentProperties = new PropertyStoreApi(propertyStore);
		this.selectedFilters = new LiveSetVariable<>(restoreFilters(), AsyncMode.SYNC);
		this.compositeFilter = new LiveExpression<Filter<BootDashElement>>() {
			{
				dependsOn(selectedFilters);
			}
			@Override
			protected Filter<BootDashElement> compute() {
				Filter<BootDashElement> composed = Filters.acceptAll();
				for (FilterChoice chosen : selectedFilters.getValues()) {
					composed = Filters.compose(composed, chosen.getFilter());
				}
				return composed;
			}
		};

		selectedFilters.addListener(new ValueListener<ImmutableSet<FilterChoice>>() {
			public void gotValue(LiveExpression<ImmutableSet<FilterChoice>> exp, ImmutableSet<FilterChoice> value) {
				saveFilters(value);
			}
		});
	}

	public static class FilterChoice implements Ilabelable {
		private final String id;
		private final String label;
		private final Filter<BootDashElement> filter;
		private final boolean defaultEnable;

		public FilterChoice(String id, String label, Filter<BootDashElement> filter) {
			this(id, label, filter, false);
		}

		public FilterChoice(String id, String label, Filter<BootDashElement> filter, boolean defaultEnable) {
			this.id = id;
			this.label = label;
			this.filter = filter;
			this.defaultEnable = defaultEnable;
		}

		@Override
		public String toString() {
			return "FilterChoice("+getLabel()+")";
		}

		@Override
		public String getLabel() {
			return label;
		}

		public Filter<BootDashElement> getFilter() {
			return filter;
		}

		public String getId() {
			return id;
		}
	}

	private final LiveSetVariable<FilterChoice> selectedFilters;
	private final LiveExpression<Filter<BootDashElement>> compositeFilter;

	/**
	 * @return The filter that is defined by composing all the selected toggle filters.
	 */
	public LiveExpression<Filter<BootDashElement>> getFilter() {
		return compositeFilter;
	}
	private Set<FilterChoice> restoreFilters() {
		Set<FilterChoice> builder = new HashSet<>();
		for (FilterChoice filter : getAvailableFilters()) {
			if (persistentProperties.get(filter.getId(), filter.defaultEnable)) {
				builder.add(filter);
			}
		}
		return builder;
	}

	private void saveFilters(ImmutableSet<FilterChoice> filters) {
		try {
			for (FilterChoice f : getAvailableFilters()) {
				boolean active = filters.contains(f);
				if (active==f.defaultEnable) {
					//don't store default values that way if we change the default in the future then
					// users will get the new default rather than their persisted value
					persistentProperties.put(f.getId(), (String)null);
				} else {
					persistentProperties.put(f.getId(), active);
				}
			}
		} catch (Exception e) {
			//trouble saving filters... log and move on. This is not critical
			Log.log(e);
		}
	}

	public FilterChoice[] getAvailableFilters() {
		return FILTERS;
	}
	public LiveSetVariable<FilterChoice> getSelectedFilters() {
		return selectedFilters;
	}
}
