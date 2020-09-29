/*******************************************************************************
 * Copyright (c) 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.api.ProjectDeploymentTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.livexp.DisposingFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.ModifiableModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class GenericRemoteBootDashModel<Client, Params> extends RemoteBootDashModel {

	private LiveSetVariable<String> existingAppIds = new LiveSetVariable<>();

	DisposingFactory<String, GenericRemoteAppElement> elementFactory = new DisposingFactory<String, GenericRemoteAppElement>(existingAppIds) {
		@Override
		protected GenericRemoteAppElement create(String appId) {
			return new GenericRemoteAppElement(GenericRemoteBootDashModel.this, GenericRemoteBootDashModel.this, appId,
					GenericRemoteBootDashModel.this.getPropertyStore());
		}
	};

	private final ObservableSet<BootDashElement> elements;

	public GenericRemoteBootDashModel(RemoteRunTarget<Client, Params> target, BootDashViewModel parent) {
		super(target, parent);
		elements = ObservableSet.<BootDashElement>builder()
		.refresh(AsyncMode.ASYNC)
		.compute(() -> fetchApps())
		.build();
		elements.dependsOn(getRunTarget().getClientExp());
		addElementStateListener(element -> refresh(ui()));
	}

	private ImmutableSet<BootDashElement> fetchApps() throws Exception {
		return refreshTracker.call("Fetching apps...", () ->  {
			Collection<App> apps = getRunTarget().fetchApps();
			Set<String> validAppIds = new HashSet<>();
			for (App app : apps) {
				validAppIds.add(app.getName());
			}
			existingAppIds.replaceAll(validAppIds);
			Builder<BootDashElement> bdes = ImmutableSet.builder();
			for (App app : apps) {
				GenericRemoteAppElement bde = elementFactory.createOrGet(app.getName());
				app.setContext(bde);
				bde.setAppData(app);
				bdes.add(bde);
			}
			return bdes.build();
		});
	}

	@Override
	public ObservableSet<BootDashElement> getElements() {
		return elements;
	}

	@Override
	public void refresh(UserInteractions ui) {
		elements.refresh();
	}

	@SuppressWarnings("unchecked")
	@Override
	public RemoteRunTarget<Client, Params> getRunTarget() {
		return (RemoteRunTarget<Client, Params>) super.getRunTarget();
	}

	public LiveExpression<Integer> refreshCount() {
		return elements.refreshCount();
	}

	@Override
	public RefreshState getRefreshState() {
		return refreshTracker.refreshState.getValue();
	}

	@Override
	public void delete(Collection<BootDashElement> collection, UserInteractions ui) {
		List<CompletableFuture<?>> futures = new ArrayList<>();
		for (BootDashElement d : collection) {
			if (d instanceof Deletable) {
				futures.add(refreshTracker.callAsync("Deleting "+d.getName(), () -> {
					try {
						((Deletable) d).delete();
					} catch (Exception e) {
						Log.log(e);
					}
					return null;
				}));
			}
		}
		for (CompletableFuture<?> f : futures) {
			try {
				f.get();
			} catch (Exception e) {
				Log.log(e);
			}
		}
		refresh(ui());
	}

	@Override
	public boolean canDelete(BootDashElement element) {
		if (element instanceof Deletable) {
			Deletable re = (Deletable) element;
			return re.canDelete();
		}
		return false;
	}

	@Override
	public String getDeletionConfirmationMessage(Collection<BootDashElement> value) {
		return null; // no confirmation asked.
	}

	@Override
	public boolean canBeAdded(List<Object> sources) {
		if (getRunTarget() instanceof ProjectDeploymentTarget) {
			if (sources != null && !sources.isEmpty() && getRunTarget().isConnected()) {
				for (Object obj : sources) {
					// IMPORTANT: to avoid drag/drop into the SAME target, be
					// sure
					// all sources are from a different target
					if (getProject(obj) == null || !isFromDifferentTarget(obj)) {
						return false;
					}
				}
				return true;
			}
		}

		return false;
	}

	protected boolean isFromDifferentTarget(Object dropSource) {
		if (dropSource instanceof BootDashElement) {
			return ((BootDashElement) dropSource).getBootDashModel() != this;
		}
		// If not a boot element that is being dropped, it is an element
		// external to the boot dash view (e.g. project from project explorer)
		return true;
	}

	@Override
	public void performDeployment(Set<IProject> toDeploy, RunState runOrDebug) throws Exception {
		if (!toDeploy.isEmpty()) {
			RemoteRunTarget<Client, Params> _target = getRunTarget();
			if (_target instanceof ProjectDeploymentTarget) {
				ProjectDeploymentTarget target = (ProjectDeploymentTarget) _target;
				refreshTracker.run("Creating deployment", () -> {
					target.performDeployment(toDeploy, runOrDebug);
				});
				refresh(ui());
			}
		}
	}

}
