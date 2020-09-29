/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.io.ByteArrayInputStream;

import org.eclipse.compare.ResourceNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

/**
 * Compare and Merge YAML file input
 *
 * @author Alex Boyko
 *
 */
public class YamlFileInput extends ResourceNode {

	private Image image;

	public YamlFileInput(IFile manifestFile, Image image) {
		super(manifestFile);
		this.image = image;
	}

	@Override
	public String getType() {
		return "yml";
	}

	@Override
	public void setContent(final byte[] contents) {
		super.setContent(contents);
		Job job = new Job("Saving changes to deployment manifest file '" + getResource().getFullPath() + "'") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					((IFile)getResource()).setContents(new ByteArrayInputStream(contents), true, true, monitor);
				} catch (CoreException e) {
					e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(getResource());
		job.schedule();
	}

	@Override
	public Image getImage() {
		if (image == null) {
			return super.getImage();
		} else {
			return BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.MANIFEST_ICON);
		}
	}

}
