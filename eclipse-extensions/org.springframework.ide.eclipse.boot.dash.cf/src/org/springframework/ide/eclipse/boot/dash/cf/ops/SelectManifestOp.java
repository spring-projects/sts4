/*******************************************************************************
 * Copyright (c) 2016, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.ops;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudData;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialogModel.ManifestType;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class SelectManifestOp extends CloudOperation {

	protected final CloudAppDashElement cde;

	public SelectManifestOp(CloudAppDashElement cde) {
		super("Select a manifest file", cde.getBootDashModel());
		this.cde = cde;
	}

	@Override
	protected void doCloudOp(final IProgressMonitor monitor) throws Exception, OperationCanceledException {

		IProject project = cde.getProject();

		if (cde == null || project == null) {
			return;
		}

		IFile manifest = cde.getDeploymentManifestFile();

		/*
		 * Commented out because manual manifest contents based on current
		 * deployment props from CF don't need to be the latest since they are
		 * not editable
		 */
		//		/*
//		 * Refresh the latest cloud application
//		 */
//		new RefreshApplications(model, Collections.singletonList(model.getAppCache().getApp(project))).run(monitor);

		CloudData cloudData = model.buildOperationCloudData(monitor, project);

		DeploymentPropertiesDialogModel dialogModel = new DeploymentPropertiesDialogModel(ui(), cloudData, project, cde.getSummaryData(), false);
		dialogModel.setSelectedManifest(manifest);
		dialogModel.setManifestType(manifest == null ? ManifestType.MANUAL : ManifestType.FILE);
		CloudApplicationDeploymentProperties props = cfUi().promptApplicationDeploymentProperties(dialogModel);

		if (props == null) {
			throw ExceptionUtil.coreException("Error loading deployment properties from the manifest YAML");
		}

		cde.setDeploymentManifestFile(props.getManifestFile());
	}

}
