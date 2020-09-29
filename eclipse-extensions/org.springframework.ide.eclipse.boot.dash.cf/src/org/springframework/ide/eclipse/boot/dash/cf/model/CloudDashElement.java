/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.model;

import java.util.List;

import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.Failable;
import org.springframework.ide.eclipse.boot.dash.model.MissingLiveInfoMessages;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.actuator.ActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.actuator.JMXActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

import com.google.common.collect.ImmutableList;

public abstract class CloudDashElement<T> extends WrappingBootDashElement<T> {

	public CloudDashElement(BootDashModel bootDashModel, T delegate) {
		super(bootDashModel, delegate);
	}

	private LiveExpression<Failable<ImmutableList<RequestMapping>>> liveRequestMappings;
	private LiveExpression<Failable<LiveBeansModel>> liveBeans;
	private LiveExpression<Failable<LiveEnvModel>> liveEnv;

	@Override
	public Failable<ImmutableList<RequestMapping>> getLiveRequestMappings() {
		synchronized (this) {
			if (liveRequestMappings==null) {
				final LiveExpression<String> actuatorUrl = getActuatorUrl();
				liveRequestMappings = new AsyncLiveExpression<Failable<ImmutableList<RequestMapping>>>(Failable.error(MissingLiveInfoMessages.NOT_YET_COMPUTED), "Fetch request mappings for '"+getName()+"'") {
					@Override
					protected Failable<ImmutableList<RequestMapping>> compute() {
						String target = actuatorUrl.getValue();
						if (target!=null) {
							ActuatorClient client = JMXActuatorClient.forUrl(getTypeLookup(), () -> target);
							List<RequestMapping> list = client.getRequestMappings();
							if (list!=null) {
								return Failable.of(ImmutableList.copyOf(client.getRequestMappings()));
							}
						}
						return Failable.error(getBootDashModel().getRunTarget().getType().getMissingLiveInfoMessages().getMissingInfoMessage(getName(), "mappings"));
					}

				};
				liveRequestMappings.dependsOn(actuatorUrl);
				addElementState(liveRequestMappings);
				addDisposableChild(liveRequestMappings);
			}
		}
		return liveRequestMappings.getValue();
	}

	@Override
	public Failable<LiveBeansModel> getLiveBeans() {
		synchronized (this) {
			if (liveBeans == null) {
				final LiveExpression<String> actuatorUrl = getActuatorUrl();
				liveBeans = new AsyncLiveExpression<Failable<LiveBeansModel>>(Failable.error(MissingLiveInfoMessages.NOT_YET_COMPUTED), "Fetch beans for '"+getName()+"'") {
					@Override
					protected Failable<LiveBeansModel> compute() {
						String target = actuatorUrl.getValue();
						if (target != null) {
							ActuatorClient client = JMXActuatorClient.forUrl(getTypeLookup(), () -> target);
							LiveBeansModel beans = client.getBeans();
							if (beans != null) {
								return Failable.of(beans);
							}
						}
						return Failable.error(getBootDashModel().getRunTarget().getType().getMissingLiveInfoMessages().getMissingInfoMessage(getName(), "beans"));
					}

				};
				liveBeans.dependsOn(actuatorUrl);
				addElementState(liveBeans);
				addDisposableChild(liveBeans);
			}
		}
		return liveBeans.getValue();
	}

	@Override
	public Failable<LiveEnvModel> getLiveEnv() {
		synchronized (this) {
			if (liveEnv == null) {
				final LiveExpression<String> actuatorUrl = getActuatorUrl();
				liveEnv = new AsyncLiveExpression<Failable<LiveEnvModel>>(Failable.error(MissingLiveInfoMessages.NOT_YET_COMPUTED), "Fetch env for '"+getName()+"'") {
					@Override
					protected Failable<LiveEnvModel> compute() {
						String target = actuatorUrl.getValue();
						if (target != null) {
							ActuatorClient client = JMXActuatorClient.forUrl(getTypeLookup(), () -> target);
							LiveEnvModel env = client.getEnv();
							if (env != null) {
								return Failable.of(env);
							}
						}
						return Failable.error(getBootDashModel().getRunTarget().getType().getMissingLiveInfoMessages().getMissingInfoMessage(getName(), "env"));
					}

				};
				liveEnv.dependsOn(actuatorUrl);
				addElementState(liveEnv);
				addDisposableChild(liveEnv);
			}
		}
		return liveEnv.getValue();
	}

	protected LiveExpression<String> getActuatorUrl() {
		return LiveExpression.constant(null);
	}

	@Override
	public CloudFoundryRunTarget getTarget() {
		return (CloudFoundryRunTarget) super.getTarget();
	}

}
