/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.model;

import org.springsource.ide.eclipse.commons.livexp.core.LiveSets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.ReactorUtils;
import org.springframework.ide.eclipse.boot.dash.cf.debug.DebugStrategyManager;
import org.springframework.ide.eclipse.boot.dash.cf.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudData;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.UnsupportedPushProperties;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.YamlGraphDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialog;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialogModel.ManifestType;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.cf.ops.JobBody;
import org.springframework.ide.eclipse.boot.dash.cf.ops.Operation;
import org.springframework.ide.eclipse.boot.dash.cf.ops.OperationsExecution;
import org.springframework.ide.eclipse.boot.dash.cf.ops.ProjectsDeployer;
import org.springframework.ide.eclipse.boot.dash.cf.ops.ServicesRefreshOperation;
import org.springframework.ide.eclipse.boot.dash.cf.ops.TargetApplicationsRefreshOperation;
import org.springframework.ide.eclipse.boot.dash.cf.packaging.CloudApplicationArchiverStrategies;
import org.springframework.ide.eclipse.boot.dash.cf.packaging.CloudApplicationArchiverStrategy;
import org.springframework.ide.eclipse.boot.dash.cf.packaging.ICloudApplicationArchiver;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cf.ui.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlFileInput;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlInput;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel.Result;
import org.springframework.ide.eclipse.boot.dash.model.AsyncDeletable;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.OldValueDisposer;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CloudFoundryBootDashModel extends RemoteBootDashModel {

	private DebugStrategyManager cfDebugStrategies;

	public static final String APP_TO_PROJECT_MAPPING = "projectToAppMapping";

	private static final Comparator<BootDashElement> ELEMENT_COMPARATOR = new Comparator<BootDashElement>() {
		@Override
		public int compare(BootDashElement o1, BootDashElement o2) {
			int cat1 = getCategory(o1);
			int cat2 = getCategory(o2);
			if (cat1!=cat2) {
				return cat1 - cat2;
			} else {
				return o1.getName().compareTo(o2.getName());
			}
		}

		private int getCategory(BootDashElement o1) {
			if (o1 instanceof CloudAppDashElement) {
				return 1;
			} else if (o1 instanceof CloudServiceInstanceDashElement) {
				return 2;
			} else {
				//Not really possible but anyhow...
				return 999;
			}
		}
	};

	private CloudDashElementFactory elementFactory;

	private UnsupportedPushProperties unsupportedPushProperties;

	private final LiveSetVariable<CloudServiceInstanceDashElement> services = new LiveSetVariable<>(AsyncMode.SYNC);
	private final CloudDashApplications applications = new CloudDashApplications(this);
	private final ObservableSet<BootDashElement> allElements = LiveSets.union(applications.getApplications(), services);

	private void checkApiVersion() {
		this.refreshTracker.callAsync("Checking API version...", () -> {
			ClientRequests client = getClient();
			if (client!=null) {
				Version server = client.getApiVersion();
				Version supported = client.getSupportedApiVersion();
				if (server.compareTo(supported)<0) {
					throw ExceptionUtil.coreException(IStatus.WARNING, "Client supports API version "+server+
							" and is connected to server with API version "+supported+". "+
							"Things may not work as expected.");
				}
			}
			return null;
		});
	}

	final private IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				if (event.getDelta() == null && event.getSource() != ResourcesPlugin.getWorkspace()) {
					return;
				}
				/*
				 * Collect data on renamed and removed projects
				 */
				Map<IPath, IProject> renamedFrom = new HashMap<>();
				Map<IPath, IProject> renamedTo = new HashMap<>();
				List<IProject> removedProjects = new ArrayList<>();
				for (IResourceDelta delta : event.getDelta().getAffectedChildren(IResourceDelta.CHANGED | IResourceDelta.ADDED | IResourceDelta.REMOVED)) {
					IResource resource = delta.getResource();
					if (resource instanceof IProject) {
						IProject project = (IProject) resource;
						if (delta.getKind() == IResourceDelta.REMOVED) {
							if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
								renamedFrom.put(delta.getMovedToPath(), project);
							} else {
								removedProjects.add(project);
							}
						} else if (delta.getKind() == IResourceDelta.ADDED && (delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
							renamedTo.put(project.getFullPath(), project);
						}

					}
				}

				/*
				 * Update CF app cache and collect apps that have local project
				 * updated
				 */
				List<CloudAppDashElement> appsToRefresh = new ArrayList<>();
				for (IProject project : removedProjects) {
					CloudAppDashElement app = getApplication(project);
					if (app!=null && app.setProject(null)) {
						appsToRefresh.add(app);
					}
				}
				for (Map.Entry<IPath, IProject> entry : renamedFrom.entrySet()) {
					IPath path = entry.getKey();
					IProject oldProject = entry.getValue();
					IProject newProject = renamedTo.get(path);
					if (oldProject != null) {
						CloudAppDashElement app = getApplication(oldProject);
						if (app!=null && app.setProject(newProject)) {
							appsToRefresh.add(app);
						}
					}
				}

				/*
				 * Update BDEs
				 */
				for (CloudAppDashElement app : appsToRefresh) {
					notifyElementChanged(app, "resourceChanged");
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}
	};

	@Override
	public RefreshState getRefreshState() {
		return refreshTracker.refreshState.getValue();
	}

	private OldValueDisposer<reactor.core.Disposable> refreshTokenDisposer = new OldValueDisposer<>(this);

	/**
	 * This tracks the number of activeRefreshTokenListeners. It is used for debugging and testing purposes only. (To
	 * observe whether the listeners are properly disposed).
	 */
	public final AtomicInteger activeRefreshTokenListeners = new AtomicInteger();

	public CloudFoundryBootDashModel(CloudFoundryRunTarget target, BootDashModelContext context, BootDashViewModel parent) {
		super(target, parent);
		cfDebugStrategies = new DebugStrategyManager(injections().getBeans(DebugSupport.class), getViewModel());
		this.elementFactory = new CloudDashElementFactory(context, target.getPropertyStore(), this);

		this.unsupportedPushProperties = new UnsupportedPushProperties();
		addDisposableChild(target.getClientExp().onChange((exp,v) -> {
			this.refresh(ui());
			ClientRequests client = exp.getValue();
			if (client!=null && this.getRunTarget().getTargetProperties().getStoreCredentials()==StoreCredentialsMode.STORE_TOKEN) {
				activeRefreshTokenListeners.incrementAndGet();
				refreshTokenDisposer.setValue(client.getRefreshTokens().doOnNext(refreshToken -> {
					try {
						this.getRunTarget().getTargetProperties().setCredentials(CFCredentials.fromRefreshToken(refreshToken));
					} catch (CannotAccessPropertyException e) {
						Log.log(e);
					}
				})
				.doOnTerminate(activeRefreshTokenListeners::decrementAndGet)
				.subscribe());
			}
			checkApiVersion();
		}));
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
//		try {
//			if (getRunTarget().getTargetProperties().get(CloudFoundryTargetProperties.DISCONNECTED) == null
//					&& (getRunTarget().getTargetProperties().isStoreCredentials() || getRunTarget().getTargetProperties().getCredentials() != null)) {
//				// If CF target was connected previously and either password is stored or not stored but non-null then connect automatically
//				Log.async(this.connect(ConnectMode.AUTOMATIC));
//			}
//		} catch (CannotAccessPropertyException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	}

	@Override
	public ObservableSet<BootDashElement> getElements() {
		return allElements;
	}

	@Override
	public void dispose() {
		if (cfDebugStrategies!=null) {
			cfDebugStrategies.dispose();
		}
		applications.dispose();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		super.dispose();
	}

	@Override
	public void refresh(UserInteractions ui) {
		runAsynch(new TargetApplicationsRefreshOperation(this, ui), ui);
		runAsynch(new ServicesRefreshOperation(this), ui);
	}

	@Override
	public Comparator<BootDashElement> getElementComparator() {
		return ELEMENT_COMPARATOR;
	}

	@Override
	public CloudFoundryRunTarget getRunTarget() {
		return (CloudFoundryRunTarget) super.getRunTarget();
	}

	@Override
	public boolean canBeAdded(List<Object> sources) {
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
	public void performDeployment(
			final Set<IProject> projectsToDeploy,
			RunState runOrDebug
	) throws Exception {
		DebugSupport debugSuppport = getDebugSupport();
		runAsynch(new ProjectsDeployer(CloudFoundryBootDashModel.this, ui(), projectsToDeploy, runOrDebug, debugSuppport),
				ui());
	}

	public CloudAppDashElement addElement(CFApplicationDetail appDetail, IProject project) throws Exception {
		CloudAppDashElement addedElement = null;
		boolean changed = false;
		synchronized (this) {
			addedElement = applications.addApplication(appDetail.getName());
			addedElement.setDetailedData(appDetail);
			// Update the cache BEFORE updating the model, since the model
			// elements are handles to the cache
			changed = addedElement.setProject(project);

			//Should be okay to call inside synch block as the events are fired from a
			// a Job now.
		}
		if (changed) {
			notifyElementChanged(addedElement, "addElement detected setProject caused a change");
		}
		return addedElement;
	}

	@Override
	public CloudAppDashElement getApplication(String appName) {
		Set<CloudAppDashElement> apps = getApplications().getValues();
		for (CloudAppDashElement element : apps) {
			if (appName.equals(element.getName())) {
				return element;
			}
		}
		return null;
	}

	public CloudServiceInstanceDashElement getService(String serviceName) {
		ImmutableSet<CloudServiceInstanceDashElement> services = getServices().getValues();
		for (CloudServiceInstanceDashElement s : services) {
			if (s.getName().equals(serviceName)) {
				return s;
			}
		}
		return null;
	}

	private CloudAppDashElement getApplication(IProject project) {
		Set<CloudAppDashElement> apps = getApplications().getValues();
		boolean includeNonExistingProjects = !project.exists();
		for (CloudAppDashElement element : apps) {
			if (project.equals(element.getProject(includeNonExistingProjects))) {
				return element;
			}
		}
		return null;
	}

	public CloudAppDashElement ensureApplication(String appName) {
		synchronized (this) {
			return applications.addApplication(appName);
		}
	}

	public void removeApplication(String appName) {
		synchronized (this) {
			applications.removeApplication(appName);
		}
	}

	/**
	 * Perform a 'shallow' update of the application elements in this model. This only
	 * ensures that elements with the right names exist, creating needed ones and
	 * disposing removed ones, but reusing existing ones. The state of the existing elements
	 * is not updated in any way.
	 */
	public void updateAppNames(Collection<String> names) {
		applications.setAppNames(names);
	}

	public void updateElements(Collection<CFApplicationDetail> apps) throws Exception {
		if (apps == null) {
			/*
			 * Error case: set empty list of BDEs don't modify state of local to CF artifacts mappings
			 */
			applications.setAppNames(ImmutableSet.<String>of());
		} else {
			synchronized (this) {
				applications.setAppNames(getNames(apps));
				for (CFApplicationDetail appDetails : apps) {
					CloudAppDashElement app = applications.getApplication(appDetails.getName());
					app.setDetailedData(appDetails);
				}
			}
		}
	}

	private ImmutableList<String> getNames(Collection<CFApplicationDetail> apps) {
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (CFApplicationDetail app : apps) {
			builder.add(app.getName());
		}
		return builder.build();
	}


	public OperationsExecution getOperationsExecution() {
		return new OperationsExecution(this);
	}

	public void updateApplication(CFApplicationDetail appDetails) {
		CloudAppDashElement app = getApplication(appDetails.getName());
		if (app!=null) {
			app.setDetailedData(appDetails);
		}
	}

	@Override
	public void delete(Collection<BootDashElement> toRemove, UserInteractions ui) {
		if (toRemove == null || toRemove.isEmpty()) {
			return;
		}
		List<Mono<Void>> asyncDeletions = new ArrayList<>(toRemove.size());
		for (BootDashElement element : toRemove) {
			if (element instanceof AsyncDeletable) {
				asyncDeletions.add(((AsyncDeletable)element).deleteAsync());
			}
			else if (element instanceof Deletable) {
				try {
					((Deletable) element).delete();
				} catch (Exception e) {
					Log.log(e);
				}
			}
		}
		if (!asyncDeletions.isEmpty()) {
			int numElements = asyncDeletions.size();
			runAsynch("Deleting ["+numElements+"] services", "", (IProgressMonitor mon) -> {
				//Careful... deleting more elements takes more time...
				Duration timeout = Duration.ofSeconds(20*asyncDeletions.size());
				mon.beginTask("Deleting ["+numElements+"] services", numElements);
				AtomicInteger leftToDelete = new AtomicInteger(numElements);
				try {
					ReactorUtils.safeMerge(
						Flux.fromIterable(asyncDeletions)
						.map((Mono<Void> deleteOp) -> {
							return deleteOp.doOnTerminate(() -> {
								mon.worked(1);
								mon.setTaskName("Deleting ["+leftToDelete.decrementAndGet()+"] services");
							});
						}),
						5 //limit concurrency to avoid flooding/choking request broker
					)
					.block(timeout);
				} finally {
					mon.done();
				}
			}, ui);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "(" + getRunTarget().getName() + ")";
	}

	private static ITextFileBuffer getDirtyBuffer(IFile file) {
		ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
		if (buffer!=null && buffer.isDirty()) {
			return buffer;
		}
		return null;
	}

	/**
	 *
	 * @param project
	 * @param ui
	 * @param requests
	 * @param monitor
	 * @return non-null deployment properties for the application.
	 * @throws Exception
	 *             if error occurred while resolving the deployment properties
	 * @throws OperationCanceledException
	 *             if user canceled operation while resolving deployment
	 *             properties
	 */
	public CloudApplicationDeploymentProperties resolveDeploymentProperties(CloudAppDashElement cde, UserInteractions ui, IProgressMonitor monitor) throws Exception {
		IProject project = cde.getProject();
		CFApplication app = cde.getSummaryData();

		CloudData cloudData = buildOperationCloudData(monitor, project);

		CloudApplicationDeploymentProperties deploymentProperties = CloudApplicationDeploymentProperties.getFor(project, cloudData, app);
		CloudAppDashElement element = app == null ? null : getApplication(app.getName());
		final IFile manifestFile = element == null ? null : element.getDeploymentManifestFile();
		if (manifestFile != null) { // Manifest file deployment mode

			// Check if file exists in case the stored file is obsolete (e.g. no longer exists)
			if (manifestFile.exists()) {
				if (!saveManifestBeforePush(manifestFile, ui)) {
					throw new OperationCanceledException();
				} else {
					final String yamlContents = IOUtil.toString(manifestFile.getContents());
					String errorMessage = null;
					TextEdit edit = null;
					try {
						YamlGraphDeploymentProperties yamlGraph = new YamlGraphDeploymentProperties(yamlContents, deploymentProperties.getAppName(), cloudData);
						MultiTextEdit me = yamlGraph.getDifferences(deploymentProperties);
						edit = me != null && me.hasChildren() ? me : null;
						if (yamlGraph.getInheritFilePath() != null) {
							errorMessage = "'inherit' attribute is present in the manifest but is not supported. Merge with caution.";
						}
					} catch (MalformedTreeException e) {
						Log.log(e);
						errorMessage = "Failed to create text differences between local manifest file and deployment properties on CF. Merge with caution.";
						edit = new ReplaceEdit(0, yamlContents.length(),
								new Yaml(YamlGraphDeploymentProperties.createDumperOptions())
										.dump(ApplicationManifestHandler.toYaml(deploymentProperties, cloudData)));
					} catch (Throwable t) {
						Log.log(t);
						errorMessage = "Failed to parse local manifest file YAML contents. Merge with caution.";
						edit = new ReplaceEdit(0, yamlContents.length(),
								new Yaml(YamlGraphDeploymentProperties.createDumperOptions())
										.dump(ApplicationManifestHandler.toYaml(deploymentProperties, cloudData)));
					}

					/*
					 * If UI is available and there differences between manifest and
					 * current deployment properties on CF then prompt the user to
					 * perform the merge
					 */
					if (edit != null && ui != null) {
						final IDocument doc = new Document(yamlContents);
						edit.apply(doc);

						final YamlFileInput left = new YamlFileInput(manifestFile,
								BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CLOUD_ICON));
						final YamlInput right = new YamlInput("Current deployment properties from Cloud Foundry",
								BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CLOUD_ICON),
								doc.get());

						CompareConfiguration config = new CompareConfiguration();
						config.setLeftLabel(left.getName());
						config.setLeftImage(left.getImage());
						config.setRightLabel(right.getName());
						config.setRightImage(right.getImage());
						config.setLeftEditable(true);
						config.setProperty("manifest", manifestFile);

						final String message = errorMessage;

						final CompareEditorInput input = new CompareEditorInput(config) {
							@Override
							protected Object prepareInput(IProgressMonitor arg0)
									throws InvocationTargetException, InterruptedException {
								setMessage(message);
								return new DiffNode(left, right);
							}
						};
						input.setTitle("Merge Local Deployment Manifest File");

						input.run(monitor);
						ManifestDiffDialogModel model = new ManifestDiffDialogModel(input);
						Result result = cfUi().openManifestDiffDialog(model);
						if (result==null) {
							result = Result.USE_MANIFEST;
						}
						switch (result) {
						case CANCELED:
							throw new OperationCanceledException();
						case FORGET_MANIFEST:
							element.setDeploymentManifestFile(null);
							/*
							 * Use the current CF deployment properties, hence just break out of the switch
							 */
							break;
						case USE_MANIFEST:
							/*
							 * Load deployment properties from YAML text content
							 */
							final byte[] yamlBytes = left.getContent();
							List<CloudApplicationDeploymentProperties> props = new ApplicationManifestHandler(project,
									cloudData, manifestFile) {
								@Override
								protected InputStream getInputStream() throws Exception {
									return new ByteArrayInputStream(yamlBytes);
								}
							}.load(monitor);
							CloudApplicationDeploymentProperties found = null;
							for (CloudApplicationDeploymentProperties p : props) {
								if (deploymentProperties.getAppName().equals(p.getAppName())) {
									found = p;
									break;
								}
							}
							if (found == null) {
								throw ExceptionUtil.coreException(
										"Cannot load deployment properties for application '" + deploymentProperties.getAppName()
												+ "' from the manifest file '" + manifestFile.getFullPath() + "'");
							} else {
								deploymentProperties = found;
							}
							break;
							default:
						}
					}
					// TODO: refactor so that adding archive only gets called once for all properties resolving and creating cases.
					// Reason to call multiple times in different conditions is to retain the old logic when
					// switching to v2 usage and not introduce regressions with manifest diffing
					addApplicationArchive(project, deploymentProperties, cloudData, ui, monitor);
				}
			} else {
				// Still in manifest file deployment mode, but manifest file does not exist anymore therefore create properties
				deploymentProperties = createDeploymentProperties(project, ui, monitor);
			}
		} else {
			// Manual deployment mode
			addApplicationArchive(project, deploymentProperties, cloudData, ui, monitor);
		}

		// TODO: We need to clean up push and restart code. There are multiple paths that end up doing
		// the same thing, so the check below is appearing in at least two different places.
		// We should ideally only check for unsupported properties in one place: wherever we resolve
		// deployment properties regardless of which path we take (either a project deployment or app restart)
		getUnsupportedProperties().allowOrCancelIfFound(ui, deploymentProperties);

		return deploymentProperties;
	}

	/**
	 * Check for dirty manifest. If manifest is dirty, ask user if it is okay to save.
	 * If they say yes do it.
	 * @return Whether push operation is okay to proceed. (Manifest is not dirty at the end).
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private boolean saveManifestBeforePush(IFile manifestFile, UserInteractions ui) throws Exception {
		ITextFileBuffer dirtyManifest = getDirtyBuffer(manifestFile);
		if (dirtyManifest==null) {
			return true;
		} else {
			boolean save = ui.confirmOperation("Save Cf Manifest?",
					"The CF manifest at `"+manifestFile.getFullPath()+"' has unsaved changes.\n\n" +
					"Do you want to save it now?",
					new String[] {
							"Save", "Cancel"
					}, 0
			) == 0;
			if (save) {
				JobUtil.runInJob("Save dirty manifest", (progres) -> dirtyManifest.commit(progres, true)).get();
			}
			return save;
		}
	}

	/**
	 * Creates deployment properties either based on user inout via the UI if UI context is available or generates default deployment properties
	 * @param project the workspace project
	 * @param ui UI context
	 * @param monitor progress monitor
	 * @return deployment properties
	 * @throws Exception
	 */
	public CloudApplicationDeploymentProperties createDeploymentProperties(IProject project, UserInteractions ui, IProgressMonitor monitor) throws Exception {
		CloudData cloudData = buildOperationCloudData(monitor, project);
		CloudApplicationDeploymentProperties props = null;
		if (ui != null) {
			DeploymentPropertiesDialogModel dialogModel;
			dialogModel = new DeploymentPropertiesDialogModel(ui, cloudData, project, null, true);
			IFile foundManifestFile = DeploymentPropertiesDialog.findManifestYamlFile(project);
			dialogModel.setSelectedManifest(foundManifestFile);
			dialogModel.setManifestType(foundManifestFile == null ? ManifestType.MANUAL : ManifestType.FILE);

			props = cfUi().promptApplicationDeploymentProperties(dialogModel);

			addApplicationArchive(project, props, cloudData, ui, monitor);
		}
		return props;
	}

	private CfUserInteractions cfUi() {
		return injections().getBean(CfUserInteractions.class);
	}

	public void addApplicationArchive(IProject project, CloudApplicationDeploymentProperties properties, CloudData cloudData,
			UserInteractions ui, IProgressMonitor monitor) throws Exception {
		ICloudApplicationArchiver archiver = getArchiver(properties, cloudData, ui, monitor);
		if (archiver != null) {
			File archive = archiver.getApplicationArchive(monitor);
			properties.setArchive(archive);
		} else {
			throw ExceptionUtil.coreException(
					"No applicable archiver strategy found for project '"+project.getName()+"'! " +
					"Check the project's packaging type; or add " +
					"an explicit path attribute to your manifest.yml."
			);
		}
	}

	protected ICloudApplicationArchiver getArchiver(
			CloudApplicationDeploymentProperties deploymentProperties,
			CloudData cloudData,
			UserInteractions ui,
			IProgressMonitor mon
	) {
		try {
			for (CloudApplicationArchiverStrategy s : getArchiverStrategies(deploymentProperties, cloudData, ui, mon)) {
				ICloudApplicationArchiver a = s.getArchiver(mon);
				if (a != null) {
					return a;
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	protected CloudApplicationArchiverStrategy[] getArchiverStrategies(
			CloudApplicationDeploymentProperties deploymentProperties,
			CloudData cloudData,
			UserInteractions ui,
			IProgressMonitor mon
	) throws Exception {
		IProject project = deploymentProperties.getProject();

		IFile manifestFile =  deploymentProperties.getManifestFile();
		String appName = deploymentProperties.getAppName();
		ApplicationManifestHandler parser = new ApplicationManifestHandler(project, cloudData, manifestFile);

		return new CloudApplicationArchiverStrategy[] {
				CloudApplicationArchiverStrategies.fromManifest(project, appName, parser),
				CloudApplicationArchiverStrategies.packageAsJar(project, ui),
				CloudApplicationArchiverStrategies.packageMvnAsWar(project, ui)
		};
	}


	@Override
	public boolean canDelete(BootDashElement element) {
		return element instanceof Deletable || element instanceof AsyncDeletable;
	}

	@Override
	public String getDeletionConfirmationMessage(Collection<BootDashElement> value) {
		return "Are you sure that you want to delete the selected applications/services from: "
				+ getRunTarget().getName() + "? The applications/services will be permanently removed.";
	}

	public boolean isConnected() {
		return getRunTarget().isConnected();
	}

	public void setServices(Set<CloudServiceInstanceDashElement> newServices) {
		this.services.replaceAll(newServices);
	}

	public ObservableSet<CloudAppDashElement> getApplications() {
		return applications.getApplications();
	}

	public ImmutableSet<CloudAppDashElement> getApplicationValues() {
		return applications.getApplicationValues();
	}

	public ObservableSet<CloudServiceInstanceDashElement> getServices() {
		return services;
	}

	public CloudData buildOperationCloudData(IProgressMonitor monitor, IProject project) throws Exception {
		return new CloudData(getRunTarget().getDomains(monitor),  getRunTarget().getBuildpack(project), getRunTarget().getStacks(monitor));
	}

	public CloudDashElementFactory getElementFactory() {
		return elementFactory;
	}

	public ClientRequests getClient() {
		return getRunTarget().getClient();
	}

	public List<CFCloudDomain> getCloudDomains(IProgressMonitor monitor) throws Exception {
		return getRunTarget().getDomains(monitor);
	}

	/* TODO: These asynch methods probably should not be here but leaving them in the model for now as model is commonly shared across boot dash  */

	public void runAsynch(String opName, String appName, JobBody body, UserInteractions ui) {
		getOperationsExecution().runAsynch(opName, appName, body, ui);
	}

	public void runSynch(String opName, String appName, JobBody body, UserInteractions ui) throws Exception {
		CompletableFuture<Void> f = new CompletableFuture<>();
		runAsynch(opName, appName, (mon) -> {
			try {
				body.run(mon);
				f.complete(null);
			} catch (Throwable e) {
				f.completeExceptionally(e);
			}
		}, ui);
		f.get();
	}

	public void runAsynch(Operation<?> op, UserInteractions ui) {
		getOperationsExecution().runAsynch(op, ui);
	}

	public void removeService(String serviceName) {
		for (CloudServiceInstanceDashElement s : services.getValues()) {
			if (s.getName().equals(serviceName)) {
				services.remove(s);
			}
		}
	}

	public UnsupportedPushProperties getUnsupportedProperties() {
		return unsupportedPushProperties;
	}

	public DebugSupport getDebugSupport() {
		return cfDebugStrategies.getStrategy();
	}

}
