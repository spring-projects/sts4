/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.runtarget;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshTunnelFactory;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.CloudFoundryTargetWizardModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.RunTargetWizard;
import org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.JmxSshTunnelManager;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.DefaultWizardModelUserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.MissingLiveInfoMessages;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WizardModelUserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRemoteRunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.util.UiUtil;
import org.springframework.ide.eclipse.boot.util.ProcessTracker;
import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.gson.Gson;

/**
 * @author Kris De Volder
 */
public class CloudFoundryRunTargetType extends AbstractRemoteRunTargetType<CloudFoundryTargetProperties> implements Disposable {

	private static final ImageDescriptor SMALL_ICON = BootDashActivator.getImageDescriptor("icons/cloud_obj.png");

	private WizardModelUserInteractions interactions;

	public CloudFoundryRunTargetType(SimpleDIContext injections) {
		super(injections, "Cloud Foundry");
		injections.assertDefinitionFor(SshTunnelFactory.class);
		injections.assertDefinitionFor(JmxSshTunnelManager.class);

		//TODO: Should be injected and merged with other user interactions, but required too much
		// refactoring to implement in limited time
		this.interactions = new DefaultWizardModelUserInteractions();
	}

	@Override
	public CompletableFuture<?> openTargetCreationUi(LiveSetVariable<RunTarget> targets) {
		try {
			CloudFoundryTargetWizardModel model = new CloudFoundryTargetWizardModel(this, clientFactory(),
					targets.getValues(), context(), interactions);
			RunTargetWizard wizard = new RunTargetWizard(model);
			Shell shell = UiUtil.getShell();
			if (shell != null) {
				WizardDialog dialog = new WizardDialog(shell, wizard);
				if (dialog.open() == Dialog.OK) {
					CloudFoundryRunTarget target = wizard.getRunTarget();
					if (target != null) {
						targets.add(target);
					}
				}
			}
			return CompletableFuture.completedFuture(null);
		} catch (Exception e) {
			Log.log(e);
			CompletableFuture<Void> f = new CompletableFuture<Void>();
			f.completeExceptionally(e);
			return f;
		}
	}



	private CloudFoundryClientFactory clientFactory() {
		return injections.getBean(CloudFoundryClientFactory.class);
	}

	private BootDashModelContext context() {
		return injections.getBean(BootDashModelContext.class);
	}

	@Override
	public RunTarget createRunTarget(CloudFoundryTargetProperties props) {
		return new CloudFoundryRunTarget((CloudFoundryTargetProperties) props, this, clientFactory());
	}

	@Override
	public ImageDescriptor getIcon() {
		return BootDashActivator.getImageDescriptor("icons/cloud-ready.png");
	}
	@Override
	public ImageDescriptor getDisconnectedIcon() {
		return BootDashActivator.getImageDescriptor("icons/cloud-inactive.png");
	}

	@Override
	public String getDefaultNameTemplate() {
		return "%o : %s - [%a]";
	}

	@Override
	public String getTemplateHelpText() {
		return
				"Enter a template pattern. The following variable substitution are defined:\n" +
				"   '%u': username\n" +
				"   '%o': organization\n" +
				"   '%s': space\n" +
				"   '%a': API URL\n" +
				"\n" +
				"To escape a variable name simply repeat the '%' sign. E.g. '%%u'";
	}

	@Override
	public CloudFoundryTargetProperties parseParams(String serializedTargetParams) {
		Gson gson = new Gson();
		@SuppressWarnings("unchecked")
		Map<String,String> map = gson.fromJson(serializedTargetParams, Map.class);
		return new CloudFoundryTargetProperties(new TargetProperties(map, this), this, injections);
	}

	@Override
	public String serialize(CloudFoundryTargetProperties props) {
		return props.toJson();
	}

	@Override
	public UserInteractions ui() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MissingLiveInfoMessages getMissingLiveInfoMessages() {
		return new MissingLiveInfoMessages() {
			@Override
			public HtmlSnippet getMissingInfoMessage(String appName, String actuatorEndpoint) {

				return buffer -> {
					buffer.raw("<p>");
					buffer.raw("<b>");
					buffer.text(appName);
					buffer.raw("</b>");
					buffer.text(" must be running with JMX and actuator endpoint enabled:");
					buffer.raw("</p>");

					buffer.raw("<ol>");

					buffer.raw("<li>");
					buffer.text("Enable actuator ");
					buffer.raw("<b>");
					buffer.text(actuatorEndpoint);
					buffer.raw("</b>");
					buffer.text(" endpoint in the application.");
					buffer.raw("</li>");

					buffer.raw("<li>");
					buffer.text("Select ");
					buffer.raw("<b>");
					buffer.text("Enable JMX SSH Tunnel");
					buffer.raw("</b>");
					buffer.text(" in the deployment dialog.");
					buffer.raw("</li>");
					buffer.raw("</ol>");

					buffer.href(EXTERNAL_DOCUMENT_LINK, "See documentation");
				};

			}
		};
	}

	@Override
	public void dispose() {
	}
}
