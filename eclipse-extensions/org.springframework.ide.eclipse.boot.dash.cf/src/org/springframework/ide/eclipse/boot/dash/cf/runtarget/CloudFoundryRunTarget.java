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
package org.springframework.ide.eclipse.boot.dash.cf.runtarget;

import static org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.BootDashCfColumns.JMX_SSH_TUNNEL;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.DEBUGGING;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.INACTIVE;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.RUNNING;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.STARTING;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.DEFAULT_PATH;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.INSTANCES;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.NAME;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.PROJECT;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.TAGS;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.DebuggableTarget;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshClientSupport;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cf.ui.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.AbstractBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.OldValueDisposer;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

import com.google.common.collect.ImmutableSet;

public class CloudFoundryRunTarget extends AbstractRunTarget<CloudFoundryTargetProperties>
implements RunTargetWithProperties<CloudFoundryTargetProperties>, RemoteRunTarget<ClientRequests, CloudFoundryTargetProperties>, DebuggableTarget {

	private CloudFoundryTargetProperties targetProperties;

	// Cache these to avoid frequent client calls
	private List<CFCloudDomain> domains;
	private List<CFSpace> spaces;
	private List<CFStack> stacks;

	private OldValueDisposer<ClientRequests> cachedClientDisposer;
	private CloudFoundryClientFactory clientFactory;

	public CloudFoundryRunTarget(CloudFoundryTargetProperties targetProperties, CloudFoundryRunTargetType runTargetType, CloudFoundryClientFactory clientFactory) {
		super(runTargetType, CloudFoundryTargetProperties.getId(targetProperties),
				CloudFoundryTargetProperties.getName(targetProperties));
		this.targetProperties = targetProperties;
		this.clientFactory = clientFactory;
		this.cachedClientDisposer = new OldValueDisposer<>(this);
		cachedClient().onChange((_e, v) -> {
			try {
				if (getClient() != null) {
					persistBuildpacks(getClient().getBuildpacks());
				}
			} catch (Exception e) {
				Log.log(e);
			}
		});
	}

	private LiveVariable<ClientRequests> cachedClient() {
		return cachedClientDisposer.getVar();
	}

	public static final EnumSet<RunState> RUN_GOAL_STATES = EnumSet.of(INACTIVE, STARTING, RUNNING, DEBUGGING);
	private static final BootDashColumn[] DEFAULT_COLUMNS = { RUN_STATE_ICN, NAME, PROJECT, INSTANCES, DEFAULT_PATH, TAGS, JMX_SSH_TUNNEL };

	private static final String APPS_MANAGER_HOST = "APPS_MANAGER_HOST";
	private static final String BUILDPACKS = "BUILDPACKS";

	@Override
	public ClientRequests getClient() {
		return cachedClient().getValue();
	}

	@Override
	public void connect(ConnectMode mode) throws Exception {
		try {
			this.domains = null;
			this.spaces = null;
			this.stacks = null;
			boolean createClient = getTargetProperties().getCredentials()!=null;
			if (mode==ConnectMode.INTERACTIVE && getTargetProperties().getCredentials()==null) {
				updatePasswordAndConnect();
			}
 			if (createClient) {
				cachedClient().setValue(createClient());
			}
		} catch (Exception e) {
			cachedClient().setValue(null);
			throw e;
		}
	}

	private CfUserInteractions cfUi() {
		return getType().injections().getBean(CfUserInteractions.class);
	}

	private UserInteractions ui() {
		return getType().injections().getBean(UserInteractions.class);
	}

	public boolean updatePasswordAndConnect() throws Exception {
		final StoreCredentialsMode storePassword = this.getTargetProperties().getStoreCredentials();
		PasswordDialogModel passwordDialogModel = new PasswordDialogModel(this.getClientFactory(), this.getTargetProperties(), storePassword);
		cfUi().openPasswordDialog(passwordDialogModel);
		if (passwordDialogModel.isOk()) {
			this.getTargetProperties().setStoreCredentials(
					passwordDialogModel.getEffectiveStoreMode());
			CFCredentials credentials = passwordDialogModel.getCredentials();
			// The credentials cannot be null or empty string - enforced by the dialog
			try {
				this.getTargetProperties().setCredentials(credentials);
				cachedClient().setValue(createClient());
			} catch (CannotAccessPropertyException e) {
				ui().warningPopup("Failed Storing Password",
						"Failed to store password in Secure Storage for " + this.getId()
								+ ". Secure Storage is most likely locked. Current password will be kept until disconnect.");
				// Set "remember password" to false. Password hasn't been stored.
				this.getTargetProperties().setStoreCredentials(StoreCredentialsMode.STORE_NOTHING);
			}
			return true;
		}
		return false;
	}

	@Override
	public void disconnect() {
		this.domains = null;
		this.spaces = null;
		this.stacks = null;
		cachedClient().setValue(null);
	}
	@Override
	public void dispose() {
		disconnect();
		super.dispose();
	}

	protected void persistBuildpacks(List<CFBuildpack> buildpacks) throws Exception {
		PropertyStoreApi properties = getPersistentProperties();

		if (properties != null) {

			String[] buildpackVals = null;
			if (buildpacks != null && !buildpacks.isEmpty()) {
				buildpackVals = new String[buildpacks.size()];
				for (int i = 0; i < buildpacks.size(); i++) {
					buildpackVals[i] = buildpacks.get(i).getName();
				}
			}

			properties.put(BUILDPACKS, buildpackVals);
		}
	}

	@Override
	public boolean isConnected() {
		return cachedClient().getValue() != null;
	}

	public Version getCCApiVersion() {
		try {
			ClientRequests client = getClient();
			if (client!=null) {
				return client.getApiVersion();
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	public CloudFoundryClientFactory getClientFactory() {
		return clientFactory;
	}

	protected ClientRequests createClient() throws Exception {
		return clientFactory.getClient(this.getTargetProperties());
	}

	@Override
	public BootDashColumn[] getDefaultColumns() {
		return DEFAULT_COLUMNS;
	}

	@Override
	public AbstractBootDashModel createSectionModel(BootDashViewModel parent) {
		return new CloudFoundryBootDashModel(this, parent.getContext(), parent);
	}

	@Override
	public CloudFoundryTargetProperties getTargetProperties() {
		return targetProperties;
	}

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return false;
	}

	@Override
	public boolean requiresCredentials() {
		return true;
	}

	public synchronized List<CFCloudDomain> getDomains( IProgressMonitor monitor)
			throws Exception {
		if (domains == null) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
			subMonitor.beginTask("Refreshing list of domains for " + getName(), 5);

			domains = getClient().getDomains();

			subMonitor.worked(5);
		}
		return domains;
	}

	public synchronized List<CFStack> getStacks(IProgressMonitor monitor)
			throws Exception {
		if (stacks == null) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
			subMonitor.beginTask("Refreshing list of stacks for " + getName(), 5);

			stacks = getClient().getStacks();

			subMonitor.worked(5);
		}
		return stacks;
	}

	public synchronized List<CFSpace> getSpaces(ClientRequests requests, IProgressMonitor monitor) throws Exception {
		if (spaces == null) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 10);

			subMonitor.beginTask("Refreshing list of spaces for " + getName(), 5);
			spaces = requests.getSpaces();
			subMonitor.worked(5);
		}
		return spaces;
	}

	public String getUrl() {
		String url = targetProperties.getUrl();
		while (url.endsWith("/")) {
			url = url.substring(0, url.length()-1);
		}
		return url;
	}

	public SshClientSupport getSshClientSupport() throws Exception {
		ClientRequests client = getClient();
		return client.getSshClientSupport();
	}

	/**
	 * Returns list of cached buildpacks. It does NOT fetch updated buildpacks from CF as this method may
	 * be called in cases where fast list of buildpacks is required (e.g values that show in pop-up UI).
	 * <p/>
	 * Do NOT use this method to fetch buildpacks from CF
	 * @return Collection of CACHED buildpacks, or null if none cached yet.
	 * @throws Exception
	 */
	public Collection<String> getBuildpackValues() throws Exception {
		PropertyStoreApi properties = getPersistentProperties();
		if (properties != null) {
			String[] buildPackVals = properties.get(BUILDPACKS, (String[]) null);
			if (buildPackVals != null) {
				return Arrays.asList(buildPackVals);
			}
		}
		return null;
	}

	public String getBuildpack(IProject project) {
		// Only support Java project for now
		IJavaProject javaProject = JavaCore.create(project);

		if (javaProject != null) {
			try {


				Collection<String> buildpacks = getBuildpackValues();
				if (buildpacks != null) {
					String javaBuildpack = null;
					// Only chose a java build iff ONE java buildpack exists
					// that contains the java_buildpack pattern.

					for (String bp : buildpacks) {
						// iterate through all buildpacks to make sure only
						// ONE java buildpack exists
						if (bp.contains("java_buildpack")) {
							if (javaBuildpack == null) {
								javaBuildpack = bp;
							} else {
								// If more than two buildpacks contain
								// "java_buildpack", do not chose any. Let CF buildpack
								// detection decided which one to chose.
								javaBuildpack = null;
								break;
							}
						}
					}
					return javaBuildpack;
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}

		return null;
	}

	@Override
	public String getTemplateVar(char name) {
		switch (name) {
		case 'o':
			return getTargetProperties().getOrganizationName();
		case 's':
			return getTargetProperties().getSpaceName();
		case 'a':
			return getTargetProperties().getUrl();
		case 'u':
			return getTargetProperties().getUsername();
		default:
			return super.getTemplateVar(name);
		}
	}

	public String getAppsManagerHost() {
		PropertyStoreApi props = getPersistentProperties();
		if (props != null) {
			String appsManagerURL = props.get(APPS_MANAGER_HOST);
			if (appsManagerURL != null) {
				return appsManagerURL;
			}
		}
		return getAppsManagerHostDefault();
	}

	public void setAppsManagerHost(String appsManagerURL) throws Exception {
		getPersistentProperties().put(APPS_MANAGER_HOST, appsManagerURL);
	}

	public String getAppsManagerURL() {
		String host = getAppsManagerHost();
		CloudFoundryTargetProperties targetProperties = getTargetProperties();

		String org = targetProperties.getOrganizationGuid();
		String space = targetProperties.getSpaceGuid();

		if (host != null && host.length() > 0 && org != null && org.length() > 0 && space != null && space.length() > 0) {
			return host + "/organizations/" + org + "/spaces/" + space;
		} else {
			return null;
		}
	}

	public String getAppsManagerHostDefault() {
		String url = getUrl();
		if (url != null && url.contains("//api.")) {
			return url.replace("//api.", "//console.");
		}
		else {
			return null;
		}
	}

	@Override
	public LiveExpression<ClientRequests> getClientExp() {
		return cachedClient();
	}

	@Override
	public void performDoubleClickAction(UserInteractions ui) {
		openCloudAdminConsole(ui);
	}

	public void openCloudAdminConsole(UserInteractions ui) {
		String appsManagerURL = getAppsManagerURL();
		if (appsManagerURL != null && appsManagerURL.length() > 0) {
			UiUtil.openUrl(appsManagerURL);
		}
		else {
			ui.errorPopup("can't find unique identificators",
					"The Cloud Target that you selected doesn't contain required information about the organization and the space yet (recently added unique identifiers). Please remove the target and add it again to fix this.");
		}
	}

	@Override
	public CloudFoundryTargetProperties getParams() {
		return this.targetProperties;
	}

	@Override
	public Collection<App> fetchApps() {
		//TODO: migrate CF to use GenericRemoteBootDashModel. And then this should be implement based on
		// how CFBootDashModel fetches apps.
		return ImmutableSet.of();
	}

	@Override
	public CloudFoundryRunTargetType getType() {
		return (CloudFoundryRunTargetType) super.getType();
	}

	@Override
	public boolean isDebuggingSupported() {
		return true;
	}
}
