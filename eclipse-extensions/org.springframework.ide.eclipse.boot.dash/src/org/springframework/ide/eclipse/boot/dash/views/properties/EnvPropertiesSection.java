/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Failable;
import org.springframework.ide.eclipse.boot.dash.model.MissingLiveInfoMessages;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.ActiveProfiles;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.Property;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.PropertyOrigin;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.PropertySource;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.PropertySources;
import org.springsource.ide.eclipse.commons.livexp.ui.util.TreeElementWrappingContentProvider;

/**
 * Live env property section
 *
 *
 */
public class EnvPropertiesSection extends LiveDataPropertiesSection<LiveEnvModel> {

	private SearchableTreeControl searchableTree;

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		searchableTree.getTreeViewer().setInput(getBootDashElement());
	}

	private class LiveEnvContentProvider implements ITreeContentProvider {

		LiveEnvContentProvider() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof BootDashElement) {
				LiveEnvModel liveEnv = data.hasFailed() ? null : data.getValue();
				if (liveEnv != null) {
					List<Object> elements = new ArrayList<>();

					ActiveProfiles activeProfiles = liveEnv.getActiveProfiles();
					if (activeProfiles != null) {
						elements.add(activeProfiles);
					}
					PropertySources propertySources = liveEnv.getPropertySources();
					if (propertySources != null) {
						elements.add(propertySources);
					}
					return elements.toArray();
				}
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {

			if (parentElement instanceof ActiveProfiles) {
				return ((ActiveProfiles) parentElement).getProfiles().toArray();
			} else if (parentElement instanceof PropertySources) {
				return ((PropertySources) parentElement).getPropertySources().toArray();
			} else if (parentElement instanceof PropertySource) {
				return ((PropertySource) parentElement).getProperties().toArray();
			} else if (parentElement instanceof Property) {
				Property property = (Property) parentElement;
				PropertyOrigin origin = property.getOrigin();
				if (origin != null) {
					return new Object[] { origin };
				}
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return new Object[0];
		}

		@Override
		public boolean hasChildren(Object element) {
			Object[] children = getChildren(element);
			return children != null && children.length > 0;
		}
	}

	@Override
	protected Control createSectionDataControls(Composite parent) {
		LabelProvider labelProvider = new LiveEnvLabelProvider();
		ITreeContentProvider treeContentProvider = new TreeElementWrappingContentProvider(new LiveEnvContentProvider());

		searchableTree = new SearchableTreeControl(getWidgetFactory());

		searchableTree.createControls(parent, treeContentProvider, labelProvider);

		return searchableTree.getComposite();
	}

	@Override
	protected void refreshDataControls() {
		searchableTree.refresh();
	}

	@Override
	protected Failable<LiveEnvModel> fetchData() {
		BootDashElement bde = getBootDashElement();
		if (bde == null) {
			return Failable.error(MissingLiveInfoMessages.noSelectionMessage("environment properties"));
		} else {
			return bde.getLiveEnv();
		}
	}
}
