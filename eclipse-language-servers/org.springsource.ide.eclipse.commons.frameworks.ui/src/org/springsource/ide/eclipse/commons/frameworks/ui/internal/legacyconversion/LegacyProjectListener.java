/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.legacyconversion;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;
import org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion.IConversionConstants;

/**
 * Listens for legacy STS projects appearing in the workspace
 * 
 * @author Andrew Eisenberg
 * @since 3.0.0
 */
public class LegacyProjectListener implements IResourceChangeListener, IConversionConstants {

    public static final LegacyProjectListener LISTENER = new LegacyProjectListener();

    private boolean shouldPerformCheck() {
    	return FrameworkCoreActivator.getDefault().getPreferenceStore().getBoolean(AUTO_CHECK_FOR_LEGACY_STS_PROJECTS);
    }

    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            List<IProject> projects = getProjects(event.getDelta());
            if (!projects.isEmpty() && shouldPerformCheck()) {
                LegacyProjectsJob job = new LegacyProjectsJob(projects, false);
                job.schedule();
            }
        }
    }

    private List<IProject> getProjects(IResourceDelta delta) {
        final List<IProject> projects = new ArrayList<IProject>();
        try {
            delta.accept(new IResourceDeltaVisitor() {
                public boolean visit(IResourceDelta innerDelta) throws CoreException {
                    if (innerDelta.getKind() == IResourceDelta.ADDED && innerDelta.getResource().getType() == IResource.PROJECT) {
                        IProject project = (IProject) innerDelta.getResource();
                        if (LegacyProjectsJob.isLegacyProject(project, false)) {
                            projects.add(project);
                        }
                    }
                    // only continue for the workspace root
                    return innerDelta.getResource().getType() == IResource.ROOT;
                }
            });
        } catch (CoreException e) {
            FrameworkCoreActivator.getDefault().getLog().log(e.getStatus());
        }
        return projects;
    }
    
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(LegacyProjectListener.LISTENER);
   }
}
