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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion.IConversionConstants;
import org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion.LegacyProjectConverter;

/**
 * Checks entire workspace for legacy projects
 * 
 * @author Andrew Eisenberg
 * @author Leo Dos Santos
 * @since 3.0.0
 */
public class LegacyProjectsJob extends UIJob implements IConversionConstants {
    
    private final boolean warnIfNone;
    private List<IProject> legacyProjects;

    public LegacyProjectsJob(boolean warnIfNone) {
        super("Legacy STS Project Checker"); //$NON-NLS-1$
        this.warnIfNone = warnIfNone;
    }
    public LegacyProjectsJob(List<IProject> legacyProjects, boolean warnIfNone) {
        this(warnIfNone);
        this.legacyProjects = legacyProjects;
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        monitor.beginTask("Checking for legacy STS projects", 100); //$NON-NLS-1$
        IStatus status = doCheck(monitor, getDisplay().getActiveShell());
        monitor.done();
        return status;
    }
    
    private  IStatus doCheck(IProgressMonitor monitor, Shell shell) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        SubMonitor sub = SubMonitor.convert(monitor, 100);
        if (legacyProjects == null) {
            legacyProjects = findLegacyProjects();
        }
        sub.worked(30);
        if (legacyProjects.size() > 0) {
            LegacyProjectConverter converter = new LegacyProjectConverter(legacyProjects);
            if (askToConvert(shell, converter)) {
                return converter.convert(sub.newChild(70));
            } 
        } else if (warnIfNone && !LegacySTSChecker.NON_BLOCKING) {
            MessageDialog.openInformation(shell, "No legacy projects found", "No legacy projects found."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return Status.OK_STATUS;
    }
    
    private List<IProject> findLegacyProjects() {
        List<IProject> legacyProjectsList = new ArrayList<IProject>();
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : allProjects) {
            try {
                if (isLegacyProject(project, true)) {
                    legacyProjectsList.add(project);
                }
            } catch (CoreException e) {
                // shouldn't happen since we already know project is accessible
                // don't want to use the regular logging mechanism since that may 
                // load the bundle.
                e.printStackTrace();
            }
        }
        return legacyProjectsList;
    }

    public boolean askToConvert(Shell shell, LegacyProjectConverter converter) {
        if (LegacySTSChecker.NON_BLOCKING) {
            return false;
        }
        
        converter.setSelectedLegacyProjects(ListMessageDialog.openViewer(shell, converter.getAllLegacyProjects().toArray(new IProject[0])));
        return converter.getSelectedLegacyProjects() != null;
    }
    
    public static boolean isLegacyProject(IProject project, boolean isWorkspaceMigration) throws CoreException{
        return project.isAccessible() && 
                (
                        // only migrate grails projects at workspace migration time.
                        // imported grails projects are handled by the GrailsProjectVersionFixer
                        (project.hasNature(GRAILS_OLD_NATURE) && isWorkspaceMigration) ||
                        (project.hasNature(ROO_OLD_NATURE) && needsRooPrefMigration(project)) ||
                        //only migrate gradle projects at workspace migration time, GradleImportOperation deals with old natures etc. during project import.
                        (project.hasNature(GRADLE_OLD_NATURE) && isWorkspaceMigration)
                 );
    }
    
    private static boolean needsRooPrefMigration(IProject project) {
    	IFolder preferencesFolder = project.getFolder(".settings/"); //$NON-NLS-1$
        File settingsFile = preferencesFolder.getFile(ROO_NEW_PLUGIN_NAME + ".prefs").getLocation().toFile(); //$NON-NLS-1$ //$NON-NLS-2$
    	return !settingsFile.exists();
    }
    
}
