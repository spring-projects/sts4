/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

/**
 * General properties section for Spring Cloud CLI local cloud service
 *
 * @author Alex Boyko
 *
 */
public class LocalCloudServiceGeneralPropertiesSection extends AbstractBdeGeneralPropertiesSection {

	@Override
	protected BootDashElementPropertyControl[] createPropertyControls() {
		return new BootDashElementPropertyControl[] {
				new RunStatePropertyControl(),
				new UrlPropertyControl<>(BootDashElement.class, "URL:", (e) -> e.getUrl()),
				new TagsPropertyControl(),
		};
	}

}
