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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;
import org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion.IConversionConstants;
import org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion.LegacyWorkspaceConverter;
import org.springsource.ide.eclipse.commons.frameworks.ui.FrameworkUIActivator;

/**
 * Checks for legacy STS projects in the workspace at startup.
 * Must be very careful not to accidentally load the rest of STS if
 * no legacy projects are found
 * 
 * @author Andrew Eisenberg
 * @since 3.0.0
 */
public class LegacySTSChecker implements IStartup, IConversionConstants {
    // set to true during testing mode
    public static boolean NON_BLOCKING = false;
    private static final IPreferenceStore PREFERENCE_STORE = FrameworkCoreActivator.getDefault().getPreferenceStore();

    /**
     * This entry to the checker comes at the startup of the workbench
     */
    public void earlyStartup() {
        PREFERENCE_STORE.setDefault(AUTO_CHECK_FOR_LEGACY_STS_PROJECTS, true);
        
        if (shouldPerformProjectCheck()) {
            Job job = new LegacyProjectsJob(false);
            job.schedule();
            ResourcesPlugin.getWorkspace().addResourceChangeListener(LegacyProjectListener.LISTENER, IResourceChangeEvent.POST_CHANGE);
        }
        
        if (shouldPerformWorkspaceMigration()) {
            new UIJob("Convert legacy STS 2.x preferences") { //$NON-NLS-1$
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    
                    IStatus status = new LegacyWorkspaceConverter().convert(monitor);
                    IStatus status2 = new PerspectiveMigrator().migratePerspective(GRAILS_OLD_PERSPECTIVE_ID, GRAILS_NEW_PERSPECTIVE_ID, monitor);
                    MultiStatus statuses = new MultiStatus(FrameworkUIActivator.PLUGIN_ID, 0, new IStatus[] { status, status2 }, "Legacy workspace migration", null); 
                    return statuses;
                }

            }.schedule();
        }
    }

    private boolean shouldPerformProjectCheck() {
        return FrameworkCoreActivator.getDefault().getPreferenceStore().getBoolean(AUTO_CHECK_FOR_LEGACY_STS_PROJECTS);
    }

    private boolean shouldPerformWorkspaceMigration() {
        return ! PREFERENCE_STORE.getBoolean(LEGACY_MIGRATION_ALREADY_DONE);
    }
}
