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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.Map.Entry;

import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;

/**
 * Shared methods between the two converter classes
 * @author Andrew Eisenberg
 * @since 3.0.0
 */
public abstract class AbstractLegacyConverter implements IConversionConstants {
    public abstract IStatus convert(IProgressMonitor monitor);
    
    protected void copyPluginStateLocation(String from,
            String to) throws IOException {
        File oldPreferencesFolder = FrameworkCoreActivator.getDefault().getStateLocation().removeLastSegments(1).append(from).toFile();
        if (oldPreferencesFolder.exists() && oldPreferencesFolder.isDirectory()) {
            File newPreferencesFolder = FrameworkCoreActivator.getDefault().getStateLocation().removeLastSegments(1).append(to).toFile();
            copyDirectory(oldPreferencesFolder, newPreferencesFolder);
        }
    }

    /**
     * @param oldFolder
     * @param newFolder
     * @throws IOException
     */
    private void copyDirectory(File oldFolder, File newFolder)
            throws IOException {
        if (!newFolder.exists()) {
            newFolder.mkdir();
        }
        
        // copy everything and delete old so we never have to do this again
        for (File oldFile : oldFolder.listFiles()) {
            String oldName = oldFile.getName();
            String newName = oldName.replace(GRAILS_OLD_PLUGIN_NAME, GRAILS_NEW_PLUGIN_NAME);
            newName = newName.replace(ROO_OLD_PLUGIN_NAME, ROO_NEW_PLUGIN_NAME);
            File newFile = new File(newFolder, newName);
            if (oldFile.isDirectory()) {
                copyDirectory(new File(oldFolder, oldName), new File(newFolder, newName));
            } else {
                copyFile(oldFile, newFile);
            }
        }
    }
    /**
     * Copies a preference file from the old location to the new one, replacing prefixes of the appropriate settings
     * 
     * @param settingsFile the original settings file to copy
     * @param newSettingsFile the new settings file to create
     * @param oldPrefix old prefix of keys to migrate set to null if not using prefixes
     * @param newPrefix new key prefix
     */
    protected void copyPreferencesFile(File settingsFile, File newSettingsFile, String oldPrefix, String newPrefix) throws IOException,
            CoreException, FileNotFoundException {
        if (settingsFile.exists()) {
            Properties oldProps = new Properties();
            oldProps.load(new FileInputStream(settingsFile));
            Properties newProps = new Properties();
            for (Entry<Object, Object> prop : oldProps.entrySet()) {
                String oldKey = prop.getKey().toString();
                String newKey;
                if (oldPrefix != null && oldKey.startsWith(oldPrefix)) {
                    newKey = oldKey.replace(oldPrefix, newPrefix);
                } else {
                    newKey = oldKey;
                }
                newProps.put(newKey, prop.getValue());
            }
            newProps.store(new FileOutputStream(newSettingsFile), ""); //$NON-NLS-1$
        }
    }
    
    public static void copyFile(File sourceFile, File destFile)
            throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
