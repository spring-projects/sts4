/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import com.google.common.collect.ImmutableSet;

/**
 * An instance of this class manages a LiveSet of BootDashModels, one model per
 * RunTarget. New models are created or disposed to keep them in synch with the
 * RunTargets.
 *
 * @author Nieraj Singh
 * @author Kris De Volder
 */
public class BootDashModelManager implements Disposable {

	private LiveSetVariable<BootDashModel> models;
	private Map<String, BootDashModel> modelsPerTargetId;
	private ObservableSet<RunTarget> targets;
	private RunTargetChangeListener targetListener;
	private ListenerList elementStateListeners = new ListenerList();
	private ElementStateListener upstreamElementStateListener;

	private BootDashModelContext context;
	private BootDashViewModel viewModel;

	public BootDashModelManager(BootDashModelContext context, BootDashViewModel viewModel, ObservableSet<RunTarget> targets) {
		this.context = context;
		this.viewModel = viewModel;
		this.targets = targets;
	}

	public LiveExpression<ImmutableSet<BootDashModel>> getModels() {
		if (models == null) {
			models = new LiveSetVariable<>(new LinkedHashSet<BootDashModel>());
			modelsPerTargetId = new LinkedHashMap<>();
			targets.addListener(targetListener = new RunTargetChangeListener());
		}
		return models;
	}

	class RunTargetChangeListener implements ValueListener<ImmutableSet<RunTarget>> {

		@Override
		public void gotValue(LiveExpression<ImmutableSet<RunTarget>> exp, ImmutableSet<RunTarget> actualRunTargets) {
			synchronized (modelsPerTargetId) {
				Map<String, RunTarget> currentTargetsPerId = new LinkedHashMap<>();
				if (actualRunTargets != null) {
					for (RunTarget runTarget : actualRunTargets) {
						String id = runTarget.getId();
						Assert.isNotNull(id);
						currentTargetsPerId.put(id, runTarget);
					}
				}

				// To avoid firing unnecessary model change events, only modify
				// list of models IF there is a change
				boolean hasChanged = false;

				// Add models for new targets
				Set<BootDashModel> modelsToKeep = new LinkedHashSet<>();

				for (Entry<String, RunTarget> entry : currentTargetsPerId.entrySet()) {
					if (modelsPerTargetId.get(entry.getKey()) == null) {
						BootDashModel model = entry.getValue().createSectionModel(viewModel);
						if (model != null) {
							modelsPerTargetId.put(entry.getKey(), model);
							hasChanged = true;
						}
					}

					if (modelsPerTargetId.get(entry.getKey()) != null) {
						modelsToKeep.add(modelsPerTargetId.get(entry.getKey()));
					}
				}

				// Remove models for deleted targets
				for (Iterator<Entry<String, BootDashModel>> it = modelsPerTargetId.entrySet().iterator(); it
						.hasNext();) {
					Entry<String, BootDashModel> entry = it.next();
					if (currentTargetsPerId.get(entry.getKey()) == null) {
						it.remove();
						hasChanged = true;
					}
				}

				if (hasChanged) {
					models.replaceAll(modelsToKeep);
				}
			}
		}
	}

	public void dispose() {
		if (targetListener != null) {
			targets.removeListener(targetListener);
			targetListener = null;
		}
		if (models != null) {
			for (BootDashModel m : models.getValues()) {
				if (upstreamElementStateListener != null) {
					m.removeElementStateListener(upstreamElementStateListener);
				}
				m.dispose();
			}
			upstreamElementStateListener = null;
			models = null;
		}
	}

	public void addElementStateListener(ElementStateListener l) {
		elementStateListeners.add(l);
		ensureUpstreamStateListener();
	}

	private synchronized void ensureUpstreamStateListener() {
		if (upstreamElementStateListener == null) {
			upstreamElementStateListener = new ElementStateListener() {
				public void stateChanged(BootDashElement e) {
					for (Object o : elementStateListeners.getListeners()) {
						((ElementStateListener) o).stateChanged(e);
					}
				}
			};
			getModels().addListener(new ValueListener<ImmutableSet<BootDashModel>>() {
				public void gotValue(LiveExpression<ImmutableSet<BootDashModel>> exp, ImmutableSet<BootDashModel> models) {
					for (BootDashModel m : models) {
						m.addElementStateListener(upstreamElementStateListener);
					}
				}
			});
		}
	}

	public void removeElementStateListener(ElementStateListener l) {
		elementStateListeners.remove(l);
	}

}
