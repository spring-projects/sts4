/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.prefs;

import org.springframework.ide.eclipse.boot.dash.remoteapps.RemoteBootAppsDataHolder;
import org.springframework.ide.eclipse.boot.dash.remoteapps.RemoteBootAppsDataHolder.RemoteAppData;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import com.google.common.collect.ImmutableSet;

public class RemoteAppsFromPrefsDataContributor implements RemoteBootAppsDataHolder.Contributor {

	public static final RemoteAppsFromPrefsDataContributor INSTANCE = new RemoteAppsFromPrefsDataContributor();
	private RemoteAppsPrefs prefs = new RemoteAppsPrefs();
	
	private RemoteAppsFromPrefsDataContributor() {}
	
	private ObservableSet<RemoteAppData> remoteApps = ObservableSet.create(() -> ImmutableSet.copyOf(prefs.getRemoteAppData()));
	{
		Disposable d = RemoteAppsPrefs.addListener(remoteApps::refresh);
		//TODO: wire up disposing the above disposable.
		//Note: at the time this code was written proper disposing wasn't done because the 
		//SimpleDIContext does not yet support it and ...
		//This does not really matter because this data contributor is only a singleton.
	}

	@Override
	public ObservableSet<RemoteAppData> getRemoteApps() {
		return remoteApps;
	}

}
