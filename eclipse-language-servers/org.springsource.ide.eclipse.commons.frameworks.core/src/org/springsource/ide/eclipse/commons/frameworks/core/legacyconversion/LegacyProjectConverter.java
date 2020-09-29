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
package org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;

/**
 * Converts legacy maven projects to the new m2e
 * @author Andrew Eisenberg
 * @since 3.0.0
 */
public class LegacyProjectConverter extends AbstractLegacyConverter implements IConversionConstants {
    
    private final List<IProject> allLegacyProjects;
    private IProject[] selectedLegacyProjects;
    
    /**
     * Converts a single project
     */
    public LegacyProjectConverter(IProject legacyProject) {
        allLegacyProjects = Collections.singletonList(legacyProject);
        selectedLegacyProjects = new IProject[] { legacyProject };
    }

    public LegacyProjectConverter(List<IProject> legacyProjects) {
        this.allLegacyProjects = legacyProjects;
    }
    
    public List<IProject> getAllLegacyProjects() {
        return allLegacyProjects;
    }

    public IProject[] getSelectedLegacyProjects() {
        return selectedLegacyProjects;
    }

    public void setSelectedLegacyProjects(IProject[] selectedLegacyProjects) {
        this.selectedLegacyProjects = selectedLegacyProjects;
    }

    public IStatus convert(IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        SubMonitor sub = SubMonitor.convert(monitor, selectedLegacyProjects.length);
        IStatus[] statuses = new IStatus[selectedLegacyProjects.length];
        int i = 0;
        for (IProject project : selectedLegacyProjects) {
            if (project.isAccessible()) {
                sub.subTask("Converting " + project.getName()); //$NON-NLS-1$
                if (sub.isCanceled()) {
                    throw new OperationCanceledException();
                }
                statuses[i++] = convert(project, monitor);
            } else {
                // project was closed before job started.
                statuses[i++] = Status.OK_STATUS;
            }
            sub.worked(1);
        }
        
        return new MultiStatus(FrameworkCoreActivator.PLUGIN_ID, 0, statuses, "Result of converting legacy maven projects", null); //$NON-NLS-1$
    }


    private IStatus convert(IProject project, IProgressMonitor monitor) {
        SubMonitor sub = SubMonitor.convert(monitor, 1);
        // grab project rule
        Job.getJobManager().beginRule(ResourcesPlugin.getWorkspace().getRoot(), sub);
        try {
            if (project.hasNature(GRAILS_OLD_NATURE)) {
                convertGrailsProject(project, sub);
            } else if (project.hasNature(ROO_OLD_NATURE)) {
                convertRooProject(project, sub);
            } else if (project.hasNature(GRADLE_OLD_NATURE)) {
            	convertGradleProject(project, sub);
            }
        } catch (Exception e) {
            return new Status(IStatus.ERROR, FrameworkCoreActivator.PLUGIN_ID, "Failed to convert " + project.getName(), e); //$NON-NLS-1$
        } finally {
            // release rule
            Job.getJobManager().endRule(ResourcesPlugin.getWorkspace().getRoot());
        }
        sub.worked(1);
        return new Status(IStatus.OK, FrameworkCoreActivator.PLUGIN_ID, "Converted " + project.getName()); //$NON-NLS-1$
    }

    private static void convertGradleProject(IProject project, SubMonitor sub) throws Exception {
        // nature
        IProjectDescription description = project.getDescription();
        String[] ids = description.getNatureIds();
        List<String> newIds = new ArrayList<String>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            if (!ids[i].equals(GRADLE_OLD_NATURE) && !ids[i].equals(GRADLE_NEW_NATURE)) {
                newIds.add(ids[i]);
            } else {
                newIds.add(GRADLE_NEW_NATURE);
            }
        }
        description.setNatureIds(newIds.toArray(new String[0]));
        project.setDescription(description, sub);
    
        // project preferences
        // DO NOTHING: gradle tooling handles these itself by reading in both old and new locations.

        // classpath container
        IJavaProject javaProject = JavaCore.create(project);
        IClasspathEntry[] classpath = javaProject.getRawClasspath();
        List<IClasspathEntry> newClasspath = new ArrayList<IClasspathEntry>();
        for (int i = 0; i < classpath.length; i++) {
        	IClasspathEntry entry = classpath[i];
        	if (entry.getEntryKind()==IClasspathEntry.CPE_CONTAINER) {
        		String path = entry.getPath().toString();
        		if (path.contains(GRADLE_OLD_PREFIX)) {
                    entry = JavaCore.newContainerEntry(new Path(path.replace(GRADLE_OLD_PREFIX, GRADLE_NEW_PREFIX)), 
                    		entry.getAccessRules(), entry.getExtraAttributes(), entry.isExported());
                }
        	}
            newClasspath.add(entry);
        }
        javaProject.setRawClasspath(newClasspath.toArray(new IClasspathEntry[0]), sub);
	}

	private void convertGrailsProject(IProject project, SubMonitor sub) throws Exception {
        // nature
        IProjectDescription description = project.getDescription();
        String[] ids = description.getNatureIds();
        List<String> newIds = new ArrayList<String>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            if (!ids[i].equals(GRAILS_OLD_NATURE) && !ids[i].equals(GRAILS_NEW_NATURE)) {
                newIds.add(ids[i]);
            } else {
                newIds.add(GRAILS_NEW_NATURE);
            }
        }
        description.setNatureIds(newIds.toArray(new String[0]));
        project.setDescription(description, sub);
    
        // project preferences
        IFolder preferencesFolder = project.getFolder(".settings/"); //$NON-NLS-1$
        File settingsFile = preferencesFolder.getFile(GRAILS_OLD_PLUGIN_NAME + ".prefs").getLocation().toFile(); //$NON-NLS-1$ //$NON-NLS-2$
        File newSettingsFile = preferencesFolder.getFile(GRAILS_NEW_PLUGIN_NAME + ".prefs").getLocation().toFile(); //$NON-NLS-1$ //$NON-NLS-2$
        copyPreferencesFile(settingsFile, newSettingsFile, GRAILS_OLD_PREFERENCE_PREFIX, GRAILS_NEW_PREFERENCE_PREFIX);
        InstanceScope.INSTANCE.getNode(GRAILS_OLD_PLUGIN_NAME).sync();
        preferencesFolder.refreshLocal(IResource.DEPTH_ONE, sub);

        // classpath container
        IJavaProject javaProject = JavaCore.create(project);
        IClasspathEntry[] classpath = javaProject.getRawClasspath();
        List<IClasspathEntry> newClasspath = new ArrayList<IClasspathEntry>();
        for (int i = 0; i < classpath.length; i++) {
            if (classpath[i].getPath().toString().equals(GRAILS_OLD_CONTAINER)) {
                newClasspath.add(JavaCore.newContainerEntry(new Path(GRAILS_NEW_CONTAINER), classpath[i].getAccessRules(), convertGrailsClasspathAttributes(classpath[i]), classpath[i].isExported()));
            } else if (classpath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                newClasspath.add(JavaCore.newSourceEntry(classpath[i].getPath(), classpath[i].getInclusionPatterns(), 
                        classpath[i].getExclusionPatterns(), classpath[i].getOutputLocation(), convertGrailsClasspathAttributes(classpath[i])));
            } else {
                newClasspath.add(classpath[i]);
            }
        }
        javaProject.setRawClasspath(newClasspath.toArray(new IClasspathEntry[0]), sub);
    }

    private IClasspathAttribute[] convertGrailsClasspathAttributes(
            IClasspathEntry entry) {
        IClasspathAttribute[] oldAttributes = entry.getExtraAttributes();
        if (oldAttributes == null || oldAttributes.length == 0) {
            return new IClasspathAttribute[0];
        }
        IClasspathAttribute[] newAttributes = new IClasspathAttribute[oldAttributes.length];
        for (int i = 0; i < oldAttributes.length; i++) {
            if (oldAttributes[i].getName().equals(GRAILS_OLD_ATTRIBUTE)) {
                newAttributes[i] = JavaCore.newClasspathAttribute(GRAILS_NEW_ATTRIBUTE, oldAttributes[i].getValue());
            } else {
                newAttributes[i] = oldAttributes[i];
            }
        }
        
        return newAttributes;
    }

    private void convertRooProject(IProject project, SubMonitor sub) throws Exception {
        IFolder preferencesFolder = project.getFolder(".settings/"); //$NON-NLS-1$
        File settingsFile = preferencesFolder.getFile(ROO_OLD_PLUGIN_NAME + ".prefs").getLocation().toFile(); //$NON-NLS-1$ //$NON-NLS-2$
        File newSettingsFile = preferencesFolder.getFile(ROO_NEW_PLUGIN_NAME + ".prefs").getLocation().toFile(); //$NON-NLS-1$ //$NON-NLS-2$
        copyPreferencesFile(settingsFile, newSettingsFile, ROO_OLD_PLUGIN_NAME, ROO_NEW_PLUGIN_NAME);
        InstanceScope.INSTANCE.getNode(ROO_OLD_PLUGIN_NAME).sync();
        preferencesFolder.refreshLocal(IResource.DEPTH_ONE, sub);
    }
}
