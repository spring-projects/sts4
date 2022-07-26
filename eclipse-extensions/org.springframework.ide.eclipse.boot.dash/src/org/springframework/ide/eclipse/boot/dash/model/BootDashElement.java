/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.EnumSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public interface BootDashElement extends App, Taggable {
	IJavaProject getJavaProject();
	IProject getProject();
	RunState getRunState();


	/**
	 * Return the port this element is running on. If the port can not
	 * be determined or the app is not running this returns -1.
	 * <p>
	 * Deprecated: should use getLivePorts.
	 */
	@Deprecated
	int getLivePort();

	/**
	 * @return The host the app is running on. May return null if
	 *    app is not running or host is not known.
	 */
	String getLiveHost();


	/**
	 * Get the request mappings from a running process. May return null if
	 * request mappings can not be determined. (So 'null' means 'unknown', whereas
	 * an empty list means 'no request mappings').
	 */
	Failable<ImmutableList<RequestMapping>> getLiveRequestMappings();

	/**
	 * Get the beans from a running process. May return null if beans cannot be
	 * determined. (Thus, <code>null</code> means unknown, whereas an empty list
	 * means 'no beans')
	 */
	Failable<LiveBeansModel> getLiveBeans();

	/**
	 * Get the env from a running process. May return null if env cannot be
	 * determined. (Thus, <code>null</code> means unknown, whereas an empty list
	 * means 'no env')
	 */
	Failable<LiveEnvModel> getLiveEnv();

	/**
	 * Get the 'active' launch configuration. This may be null.
	 * <p>
	 * If only one existing configuration is associated with this element then
	 * it is automatically considered as the 'active' configuration.
	 * <p>
	 * If there are no configurations associated with this element then the active configuration
	 * is undefined (null).
	 * <p>
	 * If more than one configuration exists then the 'preferred config' is used to decide which one
	 * of the existing elements should be considered as 'active'.
	 *
	 * TODO: isn't this supposed to be obsolete? Remove?
	 *
	 * @return active configuration or null.
	 */
	ILaunchConfiguration getActiveConfig();

	/**
	 * The 'default' path is used by some actions to quickly open
	 * the app in a browser view. This is just a stored value. There is no guarantee
	 * that it actually exists on the given element when it is running (i.e. it may
	 * or may not be the path of a RequestMapping returned from getLiveRequestMappings.
	 */
	String getDefaultRequestMappingPath();
	void setDefaultRequestMappingPath(String defaultPath);

	BootDashModel getBootDashModel();

	void stop() throws Exception;
	void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception;
	void openConfig(UserInteractions ui);
	int getActualInstances();
	int getDesiredInstances();

	ImmutableSet<BootDashElement> getCurrentChildren();
	ObservableSet<BootDashElement> getChildren();
	ImmutableSet<ILaunchConfiguration> getLaunchConfigs();
	ImmutableSet<Integer> getLivePorts();

	/**
	 * Fetch the parent of a BDE. If this is a nested BDE then the parent will be
	 * another {@link BootDashElement}. If the element is one owned directly by a
	 * {@link BootDashModel} then the parent is that model.
	 */
	Object getParent();
	BootDashColumn[] getColumns();
	boolean projectHasDevtoolsDependency();
	boolean projectHasClasspathProperty(ClasspathPropertyTester tester);


	String getUrl();

	/**
	 * @return Subset of the runstate that a user can request when changing a
	 *         DashBoardElement's 'run-state'. Essentially, this allows
	 *         determining whether a given BootDahsElement can support the
	 *         'stop', 'run' and 'debug' operations which request that the
	 *         element be brought into a given run-state.
	 */
	EnumSet<RunState> supportedGoalStates();

	default ImageDescriptor getRunStateImageDecoration() { return null; }
	default ImageDescriptor getCustomRunStateIcon() { return null; }
	default Image getPropertiesTitleIconImage() { return null; }
	default String getProtocol() { return "http"; }
	default boolean isDevtoolsGreenColor() { return projectHasDevtoolsDependency(); }
	default RefreshState getRefreshState() { return RefreshState.READY; }
	default boolean hasClasspathProperty(ClasspathPropertyTester tester) { return projectHasClasspathProperty(tester); }
}
