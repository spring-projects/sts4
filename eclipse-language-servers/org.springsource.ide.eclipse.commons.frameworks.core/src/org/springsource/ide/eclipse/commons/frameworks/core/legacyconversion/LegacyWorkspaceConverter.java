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
import java.io.IOException;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;

/**
 * Converts legacy 2.x workspace preferences into 3.x preferences
 * @author Andrew Eisenberg
 * @since 3.0.0
 */
public class LegacyWorkspaceConverter extends AbstractLegacyConverter implements IConversionConstants {
    private static final IPreferenceStore PREFERENCE_STORE = FrameworkCoreActivator.getDefault().getPreferenceStore();
    
    public boolean shouldAutoConvert() {
        return ! PREFERENCE_STORE.getBoolean(LEGACY_MIGRATION_ALREADY_DONE);
    }
    
    public IStatus convert(IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        SubMonitor sub = SubMonitor.convert(monitor, 4);
        
        IStatus[] statuses = new IStatus[4];
        
        statuses[0] = copyPluginPreferences(sub);
        // nothing to do for grails any more
//        statuses[1] = convertGrailsWorkspacePreferences(sub);
        statuses[1] = Status.OK_STATUS;
        statuses[2] = convertRooWorkspacePreferences(sub);
        statuses[3] = convertSTSPreferences(sub);
        
        IStatus result = new MultiStatus(FrameworkCoreActivator.PLUGIN_ID, 0, statuses, 
                "Result of converting legacy STS 2.x workspace preferences to 3.x", null); //$NON-NLS-1$
 //       FrameworkCoreActivator.getDefault().getLog().log(result);
        if (result.isOK()) {
            PREFERENCE_STORE.setValue(LEGACY_MIGRATION_ALREADY_DONE, true);
        }
        return result;
    }
    
    
    /**
     * Copies preferences inside of the 
     * @param sub
     * @return
     */
    private IStatus copyPluginPreferences(SubMonitor sub) {
        sub.subTask("Copying plugin preferences for legacy STS workspace"); //$NON-NLS-1$
        try {
            File prefsFolder = InternalPlatform.getDefault().getRuntimeInstance().getStateLocation().toFile();
            prefsFolder = new File(prefsFolder, ".settings");
            for (int i = 0; i < STS_OLD_WORKSPACE_PREFS.length; i++) {
                if (STS_NEW_WORKSPACE_PREFS[i].equals("???")) { //$NON-NLS-1$
                    continue;
                }
                copyPreferencesFile(new File(prefsFolder, STS_OLD_WORKSPACE_PREFS[i] + ".prefs"), new File(prefsFolder, STS_NEW_WORKSPACE_PREFS[i] + ".prefs"), STS_OLD_WORKSPACE_PREFS[i], STS_NEW_WORKSPACE_PREFS[i]); //$NON-NLS-1$ //$NON-NLS-2$
                InstanceScope.INSTANCE.getNode(STS_NEW_WORKSPACE_PREFS[i]).sync();
            }
        } catch (Exception e) {
            return new Status(IStatus.ERROR, FrameworkCoreActivator.PLUGIN_ID, "Failed to convert legacy STS workspace preferences", e); //$NON-NLS-1$
        }
        return new Status(IStatus.OK, FrameworkCoreActivator.PLUGIN_ID, "Converted legacy STS plugin preferences"); //$NON-NLS-1$
    }

    private IStatus convertSTSPreferences(SubMonitor sub) {
        sub.subTask("Converting STS plugin state locations"); //$NON-NLS-1$
        try {
            copyPluginStateLocation(STS_OLD_CONTENT_CORE, STS_NEW_CONTENT_CORE);
            copyPluginStateLocation(STS_OLD_CORE, STS_NEW_CORE);
            copyPluginStateLocation(STS_OLD_IDE_UI, STS_NEW_IDE_UI);            
            return new Status(IStatus.OK, FrameworkCoreActivator.PLUGIN_ID, "Converted legacy STS plugin state locations"); //$NON-NLS-1$
        } catch (IOException e) {
            return new Status(IStatus.ERROR, FrameworkCoreActivator.PLUGIN_ID, "Failed to convert legacy STS plugin state locations", e); //$NON-NLS-1$
        } finally {
            sub.worked(1);
        }
    }

    // TODO FIXADE Probably safe to delete
//    private IStatus convertGrailsWorkspacePreferences(SubMonitor sub) {
//        sub.subTask("Converting Grails plugin state locations"); //$NON-NLS-1$
//        try {
//            copyPluginStateLocation(GRAILS_OLD_PREFERENCE_PREFIX, GRAILS_NEW_PREFERENCE_PREFIX);
//            return new Status(IStatus.OK, FrameworkCoreActivator.PLUGIN_ID, "Converted legacy Grails plugin state locations"); //$NON-NLS-1$
//        } catch (IOException e) {
//            return new Status(IStatus.ERROR, FrameworkCoreActivator.PLUGIN_ID, "Failed to convert legacy Grails plugin state locations", e); //$NON-NLS-1$
//        } finally {
//            sub.worked(1);
//        }
//    }

    private IStatus convertRooWorkspacePreferences(SubMonitor sub) {
		sub.subTask("Converting Roo plugin state locations"); //$NON-NLS-1$
		try {
			// Let RooInstallManager migrate the roo.installs content
			// copyPluginStateLocation(ROO_OLD_PLUGIN_NAME, ROO_NEW_PLUGIN_NAME); 
			copyPluginStateLocation(ROO_OLD_UI_NAME, ROO_NEW_UI_NAME);
			return new Status(IStatus.OK, FrameworkCoreActivator.PLUGIN_ID, "Converted legacy Roo plugin state locations"); //$NON-NLS-1$
		} catch (IOException e) {
			return new Status(IStatus.ERROR, FrameworkCoreActivator.PLUGIN_ID, "Failed to convert legacy Roo plugin state locations", e); //$NON-NLS-1$
		} finally {
			sub.worked(1);
		}
    }
}
