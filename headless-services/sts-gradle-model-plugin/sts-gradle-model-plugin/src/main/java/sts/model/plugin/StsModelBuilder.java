/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package sts.model.plugin;

import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

public class StsModelBuilder implements ToolingModelBuilder  {

	@Override
	public StsToolingModel buildAll(String modelName, Project project) {
        DefaultModel model = new DefaultModel();
        if (project.getVersion() != null) {
            model.version = project.getVersion().toString();
        }
        if (project.getGroup() != null) {
        	model.group = project.getGroup().toString();
        }
        if (project.getName() != null) {
        	model.artifact = project.getName();
        }
        return model;
	}

	@Override
	public boolean canBuild(String modelName) {
		return StsToolingModel.class.getName().equals(modelName);
	}

}
