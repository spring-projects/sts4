/*******************************************************************************
 * Copyright (c) 2015, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.cloudfoundry.manifest.ls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CfTargetsInfo;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CfTargetsInfo.Target;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CfTargetsInfo.TargetDiagnosticMessages;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

/**
 * Helper methods to attach listener to the BootDash model and keep editor in sync with
 * all connected cloudfoundry targets.
 * <p>
 * Usage: just call the 'start()' method once, at the first time the information is desired.
 */
public class BootDashTargetInfoSynchronizer {
	
	private static AtomicBoolean started = new AtomicBoolean(false);

	public static void start() {
		if (started.compareAndSet(false, true)) {
			model().getRunTargets().addListener((e,v) -> {
				// On target changes, add the client listener in each target so that when the client changes, a notification is sent
				addClientChangeListeners(e.getValue());
			});
		}
	}

	private static final ValueListener<ClientRequests> clientsChangedListener = (exp, client) -> {
		if (client instanceof DefaultClientRequestsV2) {
			addRefreshTokenListener((DefaultClientRequestsV2) client);
		}
	};

	/**
	 * Add a listener to be notified when the refresh token becomes available OR
	 * changes
	 */
	static private void addRefreshTokenListener(DefaultClientRequestsV2 client) {
		if (client != null && client.getRefreshTokens() != null && model() != null
				&& model().getRunTargets() != null) {
			client.getRefreshTokens().doOnNext((token) ->
			        // Although the refresh token change is for ONE client (i.e. one target)
			        // compute cloud target information for ALL currently connected targets
			        // as the manifest editor is updated with the full up-to-date list of connected
			        // boot dash targets
					updateCloudTargetsInManifestEditor(model().getRunTargets().getValue())
				).subscribe();
			client.getRefreshTokens().doOnComplete(() ->
				updateCloudTargetsInManifestEditor(model().getRunTargets().getValue())
			).subscribe();
		}
	}

	static private void updateCloudTargetsInManifestEditor(ImmutableSet<RunTarget> value) {
		Set<RunTarget> toUpdate = value == null ? ImmutableSet.of() : value;

		CfTargetsInfo targetsInfo = asTargetsInfo(toUpdate);
		CloudFoundryManifestLanguageServer.setCfTargetLoginOptions(targetsInfo);
	}

	private static BootDashViewModel model() {
		return BootDashActivator.getDefault().getModel();
	}

	private static void addClientChangeListeners(ImmutableSet<RunTarget> targets) {
		if (targets != null) {
			for (RunTarget runTarget : targets) {
				if (runTarget instanceof CloudFoundryRunTarget) {
					((CloudFoundryRunTarget) runTarget).getClientExp().addListener(clientsChangedListener);
				}
			}
		}
	}

	private static CfTargetsInfo asTargetsInfo(Collection<RunTarget> targets) {
		List<CfTargetsInfo.Target> collectedTargets = new ArrayList<>();
		for (RunTarget runTarget : targets) {
			if (runTarget instanceof CloudFoundryRunTarget) {

				CloudFoundryRunTarget cloudFoundryRunTarget = (CloudFoundryRunTarget) runTarget;
				if (cloudFoundryRunTarget.isConnected()) {
					String token = cloudFoundryRunTarget.getClient().getRefreshToken();
					if (token != null) {
						CloudFoundryTargetProperties properties = cloudFoundryRunTarget.getTargetProperties();
						String target = properties.getUrl();
						String org = properties.getOrganizationName();
						String space = properties.getSpaceName();
						boolean sslDisabled = properties.skipSslValidation();

						CfTargetsInfo.Target integrationTarget = new Target();

						integrationTarget.setApi(target);
						integrationTarget.setOrg(org);
						integrationTarget.setSpace(space);
						integrationTarget.setSslDisabled(sslDisabled);
						integrationTarget.setRefreshToken(token);
						collectedTargets.add(integrationTarget);
					}
				}
			}
		}

		CfTargetsInfo targetsInfo = new CfTargetsInfo();
		targetsInfo.setCfTargets(collectedTargets);
		targetsInfo.setDiagnosticMessages(getDiagnosticMessages());
		return targetsInfo ;
	}

	// NOTE: using ':' to separate the "shorter" part of the message from the longer. The longer part may be shown in the UI by expanding the hover info
	private static final String TARGET_SOURCE = "Boot Dashboard";
	private static final String NO_ORG_SPACE = "Boot Dashboard - No org/space selected: Verify Cloud Foundry target connection in Boot Dashboard or login via 'cf' CLI";
	// Make this a "generic" message, instead of using "Boot Dash" prefix as it shows general instructions when there are not targets
	private static final String NO_TARGETS = "No Cloud Foundry targets found: Create a target in Boot Dashboard or login via 'cf' CLI";
	private static final String CONNECTION_ERROR = "Boot Dashboard - Error connecting to Cloud Foundry target: Verify network connection or that existing target has valid credentials.";

	private static TargetDiagnosticMessages getDiagnosticMessages() {
		TargetDiagnosticMessages messages = new TargetDiagnosticMessages();
		messages.setConnectionError(CONNECTION_ERROR);
		messages.setNoOrgSpace(NO_ORG_SPACE);
		messages.setNoTargetsFound(NO_TARGETS);
		messages.setTargetSource(TARGET_SOURCE);
		return messages;
	}


}
