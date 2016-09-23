///*******************************************************************************
// * Copyright (c) 2016 Pivotal, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     Pivotal, Inc. - initial API and implementation
// *******************************************************************************/
//package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;
//
//import java.util.Collection;
//
//import javax.inject.Provider;
//
//import org.springframework.ide.eclipse.editor.support.yaml.schema.YValueHint;
//import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
//
//public class ManifestEditorActivator {
//
//	// The plug-in ID
//	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.cloudfoundry.manifest.editor"; //$NON-NLS-1$
//
//	// The shared instance
//	private static ManifestEditorActivator plugin;
//
//	/**
//	 * The constructor
//	 */
//	public ManifestEditorActivator() {
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
//	 */
//	public void start(BundleContext context) throws Exception {
//		super.start(context);
//		plugin = this;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
//	 */
//	public void stop(BundleContext context) throws Exception {
//		plugin = null;
//		super.stop(context);
//	}
//
//	/**
//	 * Returns the shared instance
//	 *
//	 * @return the shared instance
//	 */
//	public static ManifestEditorActivator getDefault() {
//		return plugin;
//	}
//
//	public static void log(Throwable e) {
//		getDefault().getLog().log(ExceptionUtil.status(e));
//	}
//	
//	
//	/*
//	 * 
//	 * "Framework" to contribute value hints into manifest editor
//	 */
//	
//	private Provider<Collection<YValueHint>> buildpackProvider;
//
//	public void setBuildpackProvider(Provider<Collection<YValueHint>> buildpackProvider) {
//		this.buildpackProvider = buildpackProvider;
//	}
//	
//	public Provider<Collection<YValueHint>> getBuildpackProvider() {
//		return this.buildpackProvider;
//	}
//}
