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
package org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a published plugin model, which among other things,
 * contains a list of published versions.
 * <p>
 * It specifies the latest version of the plugin as it would be installed IF the
 * plugin were installed without specifying a version number. NOTE that this is
 * NOT the same as being the most recent version of the plugin that is
 * available. There may be newer milestone versions available OR newer versions
 * that are only compatible with newer runtimes. Therefore this model provides two
 * API for retrieving new versions:
 * <li>
 * The latest version available for the given version of a runtime used by a
 * project. This is the version that is installed if a user installs the plugin
 * WITHOUT specifying a version number.</li>
 * <li>
 * The most recent version added, which may be a milestone or only available for
 * newer versions of a runtime, which a user must manually specify when installing
 * the plugin. As the list of versions are ORDERED, the most recent version
 * added version should be the last version that was added to the list.</li>
 * </p>
 * <p>
 * This model represents both a published plugin as well as an in-place plugin.
 * In-place plugins usually only have one child version (i.e the only version
 * that is available), and this version is considered the latest version.
 * </p>
 * 
 * @author Nieraj Singh
 * @author Andrew Eisenberg
 */
public class Plugin {

	/**
	 * never null
	 */
	private List<PluginVersion> versions = new ArrayList<PluginVersion>();

	private PluginVersion latestVersion;

	private String name;

	/**
	 * Default is always false, meaning when created it is assumed it is
	 * published
	 */
	private boolean isInplace = false;

	public Plugin(String name) {
		this.name = name;
	}

	/**
	 * Returns a COPY of an ORDERED list of versions, where the first entry is
	 * the oldest and the last the most recent. The last entry does NOT
	 * necessarily mean the version that will get install if the plugin were
	 * to be installed without specifying a version number. It just means the
	 * most recent version that is available for this plugin. The
	 * "latest version" (the version that installs when no version number
	 * is specified) may in fact be an older version than the most recent
	 * version that was added.
	 * 
	 * @return all versions of this plugin, including new milestones. List is
	 *        ordered, with the most recent entry being the last entry. Its
	 *        never null, and should not be empty as a plugin has at least one
	 *        version
	 */
	public List<PluginVersion> getVersions() {
		return new ArrayList<PluginVersion>(versions);
	}

	/**
	 * Ordered addition of a version. If the version doesn't already exist and
	 * is not null, it is added at the end of the list and true is returned if
	 * successfully added.
	 * <p>
	 * Any version that is successfully added also has its parent set to this
	 * instance.
	 * </p>
	 * 
	 * @param version
	 *           to add to the end of the list. The version must correspond to
	 *           the same plugin (i.e must have the same plugin name)
	 * @return true if successfully added. False otherwise
	 */
	public boolean addVersion(PluginVersion version) {
		if (version != null && !versions.contains(version)
				&& getName().equals(version.getName())) {
			versions.add(version);
			version.setParent(this);
			return true;
		}
		return false;
	}

	public void removeVersion(PluginVersion version) {
		versions.remove(version);
	}

	/**
	 * Gets the version model for the given ID, or null if not found
	 * 
	 * @param versionID
	 * @return
	 */
	public PluginVersion getVersion(String versionID) {
		if (versionID == null) {
			return null;
		}
		for (PluginVersion version : getVersions()) {
			if (versionID.equals(version.getVersion())) {
				return version;
			}
		}
		return null;
	}

	/**
	 * 
	 * @return true if it is in place, false if it is published.
	 */
	public boolean isInPlace() {
		return isInplace;
	}

	/**
	 * Set to true ONLY if this plugin has not been published.
	 * 
	 * @param isInplace
	 *           true if it is in-place, false if it is published
	 */
	public void setIsInPlace(boolean isInplace) {
		this.isInplace = isInplace;
	}

	public boolean isInstalled() {
		return getInstalled() != null;
	}

	public PluginVersion getInstalled() {
		for (PluginVersion version : getVersions()) {
			if (version.isInstalled()) {
				return version;
			}
		}
		return null;
	}

	/**
	 * Will set the latest version IF the latest version already exists in the
	 * list of version, OR if it does not exist, it is successfully added at the
	 * end of the list. Returns true if latest version successfully set. False
	 * otherwise.
	 * <p>
	 * Note that this CANNOT be null.
	 * </p>
	 * 
	 * @param latestVersion
	 *           latest released version
	 * @return true if successfully set, false otherwise
	 */
	public boolean setLatestReleasedVersion(PluginVersion latestVersion) {
		if (versions.contains(latestVersion) || addVersion(latestVersion)) {
			this.latestVersion = latestVersion;
			//Note: it is possible that latestVersion was a 'duplicate' element. So although it
			// is equals it may not be the same object. That means the parent could remain null
			// unless we explicitly set it here. This causes NPE later on.
			// See: STS-2490
			this.latestVersion.setParent(this);
			return true;
		}
		return false;
	}

	/**
	 * This may be different and more recent thatn the lastest released version
	 * 
	 * @return the most recent version of the plugin that is available. This may
	 *        OR may not be the same as the latest version available.
	 */
	public PluginVersion getMostRecentVersionAdded() {
		List<PluginVersion> children = getVersions();
		if (children != null && !children.isEmpty()) {
			return children.get(children.size() - 1);
		}
		return null;
	}

	/**
	 * Gets the latest version of this plugin, but may NOT necessarily be the
	 * most recent version. The "latest version" is defined as the version
	 * that installs when NO version number is specified when installing the
	 * plugin. This version is set when this plugin model is first
	 * created , and is considered the "latest version" for the particular
	 * version of the runtime that a project is using, although newer versions may
	 * exist. This cannot be null.
	 * 
	 * @return latest version. Should not be null.
	 */
	public PluginVersion getLatestReleasedVersion() {
		return latestVersion;
	}

	/**
	 * Given a plugin, check if it is a version of this pluging. Return false if
	 * it is not a version of this plugin.
	 * 
	 * @param pluginToCheck
	 * @return true if version. False otherwise.
	 */
	public boolean isVersion(PluginVersion pluginToCheck) {
		if (pluginToCheck == null) {
			return false;
		}
		String nameToCheck = pluginToCheck.getName();
		String versionToCheck = pluginToCheck.getVersion();
		List<PluginVersion> versions = getVersions();
		if (versions != null && versionToCheck != null && nameToCheck != null
				&& nameToCheck.equals(getName())) {
			for (PluginVersion ver : versions) {
				if (versionToCheck.equals(ver.getVersion())) {
					return true;
				}
			}
		}
		return false;

	}

	public String getName() {
		return name;
	}

	/**
	 * Determines whether an update exists for the installed version. The
	 * lastest version available is the version that installs when the
	 * install-plugin command is run without specifying a version number.
	 * However, this may not necessarily be the newest version, as newer
	 * milestone versions may exist, as well as versions that are only
	 * compatible with newer runtimes.
	 * <p>
	 * If a plugin is installed to the latest version, but newer milestone
	 * versions are available, this method returns false. It only returns true
	 * if the plugin is installed at an older version, but if the user were to
	 * run the install plugin command, a more recent released version would be
	 * installed instead.
	 * </p>
	 * 
	 * @return true if and only if the plugin is installed and a newer released
	 *        version exists. If the plugin is not installed or it is installed
	 *        with the latest released version, this returns false.
	 */
	public boolean hasUpdate() {
		// Should never be null
		PluginVersion latestVersion = getLatestReleasedVersion();
		PluginVersion installedVersion = getInstalled();
		if (installedVersion == null) {
			return false;
		}
		return isVersionHigher(latestVersion, installedVersion);
	}

	/**
	 * Checks if the first plugin is higher than the second plugin argument.
	 * False otherwise, including if one or the other or both arguments are
	 * null. True if and only if both version are NOT null and the first
	 * version is higher than the second.
	 * 
	 * @param data1
	 * @param data2
	 * @return true if data1 is higher than data2
	 */
	public static boolean isVersionHigher(PluginVersion data1,
			PluginVersion data2) {
		String version1 = data1.getVersion();
		String version2 = data2.getVersion();
		if (version1 != null && version2 != null) {
			return version1.compareToIgnoreCase(version2) > 0;
		}
		return false;
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (isInplace) {
            sb.append(" (in-place)");
        }
        return sb.toString();
    }
	
	

}
