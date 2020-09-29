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
package org.springframework.ide.eclipse.boot.dash.cf.views.properties;

import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.properties.AbstractBdeGeneralPropertiesSection;
import org.springframework.ide.eclipse.boot.dash.views.properties.AppPropertyControl;
import org.springframework.ide.eclipse.boot.dash.views.properties.BootDashElementPropertyControl;
import org.springframework.ide.eclipse.boot.dash.views.properties.DefaultPathPropertyControl;
import org.springframework.ide.eclipse.boot.dash.views.properties.InstancesPropertyControl;
import org.springframework.ide.eclipse.boot.dash.views.properties.ProjectPropertyControl;
import org.springframework.ide.eclipse.boot.dash.views.properties.ReadOnlyStringPropertyControl;
import org.springframework.ide.eclipse.boot.dash.views.properties.RunStatePropertyControl;
import org.springframework.ide.eclipse.boot.dash.views.properties.TagsPropertyControl;
import org.springframework.ide.eclipse.boot.dash.views.properties.UrlPropertyControl;

/**
 * Properties view section for Cloud Foundry app elements
 *
 * @author Alex Boyko
 */
public class CloudFoundryAppPropertiesSection extends AbstractBdeGeneralPropertiesSection {

	@Override
	protected BootDashElementPropertyControl[] createPropertyControls() {
		return new BootDashElementPropertyControl[] {
				new RunStatePropertyControl(),
				new AppPropertyControl(),
				new ProjectPropertyControl(),
				new InstancesPropertyControl(),
				new UrlPropertyControl<>(BootDashElement.class, "URL:", ((e) -> e.getUrl())),
				new DefaultPathPropertyControl(),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Healthcheck:", (e) -> e.getHealthCheck(), true),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Healthcheck Http Endpoint:", (e) -> e.getHealthCheckHttpEndpoint(), true),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Jmx Ssh Tunnel:", (e) -> e.getJmxSshTunnelStatus().getValue().getLabel(), false),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Jmx Local Port:", (e) -> e.getCfJmxPort()>0 ? (""+e.getCfJmxPort()) : "", false),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Jmx URL:", (e) -> e.getJmxUrl(), false),
				new TagsPropertyControl()
		};
	}

}
