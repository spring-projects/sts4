/*******************************************************************************
 * Copyright (c) 2015, 2016 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.hamcrest.MockitoHamcrest;
import org.springframework.ide.eclipse.boot.dash.model.AbstractLaunchConfigurationsDashElement;
import org.springframework.ide.eclipse.boot.dash.model.LocalBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.LocalRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKClient;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKLaunchTracker;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKTunnel;
import org.springframework.ide.eclipse.boot.dash.test.mocks.Mocks;
import org.springframework.ide.eclipse.boot.dash.util.LaunchConfRunStateTracker;
import org.springsource.ide.eclipse.commons.core.pstore.IPropertyStore;
import org.springsource.ide.eclipse.commons.core.pstore.InMemoryPropertyStore;

import com.google.common.collect.ImmutableSet;


/**
 * Unit tests some of the methods that don't have great coverage yet
 * after running BootDashModelTests. These methods are harder to test
 * from the model (which does only minimal mocking).
 *
 * We perform focussed tests here using Mockito to essentially just test
 * those methods in isolation here using mocks for anything it uses.
 *
 * @author Kris De Volder
 */
public class AbstractLaunchConfigurationsDashElementTest extends Mocks {

	public static class TestElement extends AbstractLaunchConfigurationsDashElement<String> {

		private IProject project;

		public TestElement(String delegate, IProject project, LocalBootDashModel context) {
			super(context, delegate);
			this.project = project;
		}

		@Override
		public void launch(String runMode, ILaunchConfiguration conf) {
		}

		@Override
		public IType[] guessMainTypes() throws CoreException {
			return NO_TYPES;
		}

		@Override
		protected IPropertyStore createPropertyStore() {
			return new InMemoryPropertyStore();
		}

		@Override
		public ImmutableSet<ILaunchConfiguration> getLaunchConfigs() {
			return ImmutableSet.of();
		}

		@Override
		public IProject getProject() {
			return project;
		}

		@Override
		public String getName() {
			return delegate;
		}

		@Override
		public ImmutableSet<ILaunch> getLaunches() {
			return ImmutableSet.of();
		}

		@Override
		public Object getParent() {
			return getBootDashModel();
		}
	}

	private static final IType[] NO_TYPES = {};

	public static TestElement createElement(String testElementName, IProject project, IJavaProject javaProject, LocalRunTarget runTarget) {
//		BootProjectDashElementFactory factory = mock(BootProjectDashElementFactory.class);
//		LaunchConfDashElementFactory childFactory = mock(LaunchConfDashElementFactory.class);
//		LaunchConfRunStateTracker runStateTracker = mock(LaunchConfRunStateTracker.class);
//		when(model.getLaunchConfRunStateTracker()).thenReturn(runStateTracker);
//		IProject project = javaProject.getProject();

		LocalBootDashModel model = mock(LocalBootDashModel.class);
		LaunchConfRunStateTracker runStateTracker = mock(LaunchConfRunStateTracker.class);
		when(model.getLaunchConfRunStateTracker()).thenReturn(runStateTracker);
		TestElement element = spy(new TestElement(testElementName, project,  model));
		when(element.getTarget()).thenReturn(runTarget);
		doReturn(javaProject).when(element).getJavaProject();
		return element;
	}

	public static IType mockType(IJavaProject javaProject, String pkg, String name) {
		IType type = mock(IType.class);
		when(type.getElementName()).thenReturn(name);
		when(type.getFullyQualifiedName()).thenReturn(pkg+"."+name);
		when(type.getJavaProject()).thenReturn(javaProject);
		return type;
	}

	@Test(expected=IllegalArgumentException.class)
	public void restartWithBadArgument() throws Exception {
		String projectName = "fooProject";
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		LocalRunTarget runTarget = mock(LocalRunTarget.class);
		TestElement element = createElement(projectName, project, javaProject, runTarget);
		UserInteractions ui = mock(UserInteractions.class);

		element.restart(RunState.INACTIVE, ui);
	}

	public static IJavaProject mockJavaProject(IProject project) {
		String projectName = project.getName();
		IJavaProject jp = mock(IJavaProject.class);
		when(jp.getElementName()).thenReturn(projectName);
		when(jp.getProject()).thenReturn(project);
		return jp;
	}

	@Test
	public void restartNoMainTypes() throws Exception {
		String projectName = "fooProject";
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		LocalRunTarget runTarget = mock(LocalRunTarget.class);
		TestElement element = createElement(projectName, project, javaProject, runTarget);
		UserInteractions ui = mock(UserInteractions.class);

		when(element.guessMainTypes()).thenReturn(NO_TYPES);

		element.restart(RunState.RUNNING, ui);

		verify(element).stopSync();
		verify(ui).errorPopup(
				stringContains("Problem"),
				stringContains("Couldn't find a main type")
		);
		verifyNoMoreInteractions(ui);
	}

	@Test
	public void restartOneMainType() throws Exception {
		String projectName = "fooProject";
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		LocalRunTarget runTarget = mock(LocalRunTarget.class);
		TestElement element = createElement(projectName, project, javaProject, runTarget);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);
		IType type = mockType(javaProject, "demo", "FooApplication");

		when(element.guessMainTypes()).thenReturn(new IType[] {type});
		when(runTarget.createLaunchConfig(javaProject, type)).thenReturn(conf);

		element.restart(RunState.RUNNING, ui);

		verify(element).stopSync();
		verify(element).launch(ILaunchManager.RUN_MODE, conf);
		verifyZeroInteractions(ui);
	}

	@Test
	public void restartAndExpose() throws Exception {
		String projectName = "fooProject";
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		LocalRunTarget runTarget = mock(LocalRunTarget.class);
		TestElement element = createElement(projectName, project, javaProject, runTarget);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);
		ILaunchConfigurationWorkingCopy copyOfConf = mock(ILaunchConfigurationWorkingCopy.class);
		IType type = mockType(javaProject, "demo", "FooApplication");
		NGROKClient ngrokClient = mock(NGROKClient.class);
		NGROKTunnel tunnel = new NGROKTunnel("foo-launch", "http", "publicURLTest", "8888");

		when(element.guessMainTypes()).thenReturn(new IType[] {type});
		when(runTarget.createLaunchConfig(javaProject, type)).thenReturn(conf);
		when(conf.getWorkingCopy()).thenReturn(copyOfConf);
		when(element.getLivePort()).thenReturn(8888);
		when(ngrokClient.startTunnel("http", "8888")).thenReturn(tunnel);
		when(conf.getName()).thenReturn("foo-launch");
		when(copyOfConf.getName()).thenReturn("foo-launch");
		String eurekaInstance = "eureka instance somewhere";

		element.restartAndExpose(RunState.RUNNING, ngrokClient, eurekaInstance, ui);

		verify(element).stopSync();
		verify(element).launch(ILaunchManager.RUN_MODE, copyOfConf);
		verifyZeroInteractions(ui);

		verify(copyOfConf).setAttribute("spring.boot.prop.server.port", "18888");
		verify(copyOfConf).setAttribute("spring.boot.prop.eureka.instance.hostname", "1publicURLTest");
		verify(copyOfConf).setAttribute("spring.boot.prop.eureka.instance.nonSecurePort", "180");
		verify(copyOfConf).setAttribute("spring.boot.prop.eureka.client.service-url.defaultZone", "1" + eurekaInstance);

		NGROKClient storedNgrokClient = NGROKLaunchTracker.get("foo-launch");
		assertEquals(storedNgrokClient, ngrokClient);
		NGROKLaunchTracker.remove("foo-launch");
	}

	@Test
	public void restartTwoMainTypes() throws Exception {
		String projectName = "fooProject";
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		LocalRunTarget runTarget = mock(LocalRunTarget.class);
		TestElement element = createElement(projectName, project, javaProject, runTarget);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);
		IType fooType = mockType(javaProject, "demo", "FooApplication");
		IType barType = mockType(javaProject, "demo", "BarApplication");

		when(element.guessMainTypes()).thenReturn(new IType[] {fooType, barType});
		when(ui.chooseMainType(
				MockitoHamcrest.argThat(arrayContaining(fooType, barType)),
				any(String.class),
				any(String.class)
		)).thenReturn(barType);
		when(runTarget.createLaunchConfig(javaProject, barType)).thenReturn(conf);

		element.restart(RunState.RUNNING, ui);

		verify(element).stopSync();
		verify(ui).chooseMainType(any(IType[].class),
				stringContains("Choose"),
				stringContains("Choose", projectName)
		);
		verify(element).launch(ILaunchManager.RUN_MODE, conf);
		verifyNoMoreInteractions(ui);
	}

	@Test
	public void openConfigWithNoExistingConfs() throws Exception {
		String projectName = "fooProject";
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		LocalRunTarget runTarget = mock(LocalRunTarget.class);
		TestElement element = createElement(projectName, project, javaProject, runTarget);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);

		when(runTarget.createLaunchConfig(javaProject, null)).thenReturn(conf);
		doReturn(RunState.INACTIVE).when(element).getRunState();

		element.openConfig(ui);

		verify(ui).openLaunchConfigurationDialogOnGroup(conf, IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
		verifyNoMoreInteractions(ui);
	}

	@Test
	public void openConfigWithOneExistingConfs() throws Exception {
		String projectName = "fooProject";
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		LocalRunTarget runTarget = mock(LocalRunTarget.class);
		TestElement element = createElement(projectName, project, javaProject, runTarget);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);

		when(element.getLaunchConfigs()).thenReturn(ImmutableSet.of(conf));
		doReturn(RunState.INACTIVE).when(element).getRunState();

		element.openConfig(ui);

		verify(ui).openLaunchConfigurationDialogOnGroup(conf, IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
		verifyNoMoreInteractions(ui);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void openConfigWithTwoExistingConfs() throws Exception {
		String projectName = "fooProject";
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		LocalRunTarget runTarget = mock(LocalRunTarget.class);
		TestElement element = createElement(projectName, project, javaProject, runTarget);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf1 = mock(ILaunchConfiguration.class);
		ILaunchConfiguration conf2 = mock(ILaunchConfiguration.class);

		when(element.getLaunchConfigs()).thenReturn(ImmutableSet.of(conf1, conf2));
		doReturn(RunState.INACTIVE).when(element).getRunState();
		when(ui.chooseConfigurationDialog(anyString(), anyString(), listThat(hasItems(conf1, conf2))))
			.thenReturn(conf2);

		element.openConfig(ui);

		verify(ui).chooseConfigurationDialog(
				stringContains("Choose", "Configuration"),
				stringContains("Several"),
				(List<ILaunchConfiguration>) any()
		);
		verify(ui).openLaunchConfigurationDialogOnGroup(conf2, IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
		verifyNoMoreInteractions(ui);
	}

	private <T> List<T> listThat(Matcher<Iterable<T>> iterMatcher) {
		Object untyped = iterMatcher;
		@SuppressWarnings("unchecked")
		Matcher<List<T>> listMatcher = (Matcher<List<T>>) untyped;
		return MockitoHamcrest.argThat(listMatcher);
	}

	public static String stringContains(String... strings) {
		return MockitoHamcrest.argThat(stringContainsInOrder(Arrays.asList(strings)));
	}

}
