/*******************************************************************************
 * Copyright (c) 2016, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.ide.eclipse.boot.dash.test.BootDashModelTest.waitForJobsToComplete;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.createFile;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFCloudDomainData;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFDomainStatus;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudData;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialog;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialogModel.ManifestType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.boot.test.util.TestBracketter;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.util.Log;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;

import junit.framework.AssertionFailedError;


/**
 * Tests for {@link DeploymentPropertiesDialogModel}
 *
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class DeploymentPropertiesDialogModelTests {

	private static final String CF_SERVER_ID = "org.eclipse.languageserver.languages.cloudfoundrymanifest";

	private static final long DISCONNECT_TIMEOUT = 10000;

	private static final long TIMEOUT = 3000;

	private static final int DONT_SAVE_BUTTON_ID = 1;

	private static final int SAVE_BUTTON_ID = 0;

	private static final String UNKNOWN_DEPLOYMENT_MANIFEST_TYPE_MUST_BE_EITHER_FILE_OR_MANUAL = "Unknown deployment manifest type. Must be either 'File' or 'Manual'.";
	private static final String MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME = "Manifest does not contain deployment properties for application with name ''{0}''.";
	private static final String MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED = "Manifest does not have any application defined.";
	private static final String ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY = "Enter deployment manifest YAML manually.";
	private static final String CURRENT_GENERATED_DEPLOYMENT_MANIFEST = "Current generated deployment manifest.";
	private static final String CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM = "Choose an existing deployment manifest YAML file from the local file system.";
	private static final String DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED = "Deployment manifest file not selected.";
	private static final String MANIFEST_YAML_ERRORS = "Deployment manifest YAML has errors.";

	public static final String DEFAULT_BUILDPACK = "java_buildpack_offline";

	public static final List<CFCloudDomain> SPRING_CLOUD_DOMAINS = Arrays.<CFCloudDomain>asList(new CFCloudDomainData("springsource.org", CFDomainType.HTTP, CFDomainStatus.SHARED));

	public static CloudData createCloudData() {
		return new CloudData(SPRING_CLOUD_DOMAINS, DEFAULT_BUILDPACK, ImmutableList.of());
	}

	private static BootProjectTestHarness projects;
	private static IProject dumbProject;
	private UserInteractions ui;
	private DeploymentPropertiesDialogModel model;

	private Shell shell;

	////////////////////////////////////////////////////////////

	@Rule public TestName name = new TestName();
	@Rule public TestBracketter bracketer = new TestBracketter();

	@BeforeClass
	public static void beforeAll() throws Exception {
		StsTestUtil.deleteAllProjects();
		waitForJobsToComplete();
		projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
		dumbProject = projects.createProject("dumbProject");
		IFile manifest = dumbProject.getFile("manifest.yml");
		manifest.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(manifest), "CfManifestYMLEditor");
		waitForJobsToComplete();
		waitUntilFileConnected(manifest);
	}

	@AfterClass
	public static void afterAll() throws Exception {
		StsTestUtil.deleteAllProjects();
		waitForJobsToComplete();
		waitForServerShutdown();
	}

	@Before
	public void setup() throws Exception {
		Log.info("STARTED test: " + name.getMethodName());
		StsTestUtil.deleteAllProjectsExcept(dumbProject.getName());
//		StsTestUtil.deleteAllProjects();
		this.ui = mock(UserInteractions.class);
	}

	@After
	public void tearDown() throws Exception {
		if (shell != null) {
			shell.dispose();
		}
		waitForJobsToComplete();
		disposeModel();
//		LanguageServiceAccessor.clearStartedServers();
		waitForJobsToComplete();
//		Thread.sleep(3000);
		Log.info("FINISHED test: " + name.getMethodName());
	}

	private static void waitForServerShutdown() throws Exception {
		ACondition.waitFor("ls servers shutdown", DISCONNECT_TIMEOUT, () -> {
			List<LanguageServer> servers = LanguageServiceAccessor.getActiveLanguageServers(x -> true);
			assertTrue("still running server: "+servers, servers.isEmpty());
		});
	}

	private void disposeModel() throws Exception {
		if (model != null) {
			IEditorInput fileInput = model.getFileYamlEditor().getEditorInput();
			IEditorInput manualInput = model.getManualYamlEditor().getEditorInput();
			model.dispose();
			if (fileInput instanceof IFileEditorInput) {
				List<LanguageServerWrapper> servers = getCfLanguageServers(((IFileEditorInput) fileInput).getFile(), null);
				if (!servers.isEmpty()) {
					waitUntilFileDisconnedted(((IFileEditorInput)fileInput).getFile());
				}
			}
			if (manualInput instanceof IFileEditorInput) {
				List<LanguageServerWrapper> servers = getCfLanguageServers(((IFileEditorInput) manualInput).getFile(), null);
				if (!servers.isEmpty()) {
					waitUntilFileDisconnedted(((IFileEditorInput)manualInput).getFile());
				}
			}
			model = null;
			waitForJobsToComplete();
		}
	}

	private void waitUntilFileDisconnedted(IFile file) throws Exception {
		waitForJobsToComplete();
		if (file.exists()) {
			ACondition.waitFor(file.toString() + " disconnected from LS", DISCONNECT_TIMEOUT, () -> {
				LanguageServerWrapper wrapper = getCfLanguageServer(file);
				assertFalse(wrapper.isConnectedTo(file.getLocationURI()));
			});
		}
		FileEditorInput editorInput = new FileEditorInput(file);
		TextFileDocumentProvider docProvider = (TextFileDocumentProvider) DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
		assertNull(docProvider.getDocument(editorInput));
		waitForJobsToComplete();
		ITextFileBufferManager textFileBufferManager = FileBuffers.getTextFileBufferManager();
//		textFileBufferManager.disconnect(file.getFullPath(), LocationKind.LOCATION, new NullProgressMonitor());
		assertNull(textFileBufferManager.getFileBuffer(file.getFullPath(), LocationKind.LOCATION));
		waitForJobsToComplete();
	}

	private static void waitUntilFileConnected(IFile file) throws Exception {
		waitForJobsToComplete();
		ACondition.waitFor(file.toString() + " connected to LS", DISCONNECT_TIMEOUT, () -> {
			LanguageServerWrapper wrapper = getCfLanguageServer(file);
			assertTrue(wrapper.isConnectedTo(file.getLocationURI()));
		});
		waitForJobsToComplete();
	}

	private static List<LanguageServerWrapper> getCfLanguageServers(IFile file, StringBuilder available ) throws Exception {
		Collection<LanguageServerWrapper> wrappers = LanguageServiceAccessor.getLSWrappers(file, cap -> true);
		List<LanguageServerWrapper> found = new ArrayList<>();
		for (LanguageServerWrapper wrapper : wrappers) {
			if (CF_SERVER_ID.equals(wrapper.serverDefinition.id)) {
				found.add(wrapper);
			}
			if (available!=null) {
				available.append(wrapper.serverDefinition.id+" ");
			}
		}
		return found;
	}

	private static LanguageServerWrapper getCfLanguageServer(IFile file) throws Exception {
		StringBuilder available = new StringBuilder();
		List<LanguageServerWrapper> found = getCfLanguageServers(file, available);
		if (found.isEmpty()) {
			throw new NoSuchElementException("No CF language server wrapper found in: [ "+available+"]");
		} else if (found.size()>1) {
			throw new AssertionFailedError(
					"Found more than one ls: "+
							found.stream()
							.map(w -> w.serverDefinition.id)
							.collect(Collectors.toList())
			);
		}
		return found.get(0);
	}

	private void createDialogModel(IProject project, CFApplication deployedApp) throws Exception {
		model = new DeploymentPropertiesDialogModel(ui, createCloudData(), project, deployedApp, true);
		model.initFileModel();
		model.initManualModel();
		shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		shell.setSize(400, 400);
		shell.open();
		try {
			model.getFileYamlEditor().setContext(DeploymentPropertiesDialog.CONTEXT_DEPLOYMENT_PROPERTIES_DIALOG);
			model.getManualYamlEditor().setContext(DeploymentPropertiesDialog.CONTEXT_DEPLOYMENT_PROPERTIES_DIALOG);
			model.getFileYamlEditor().createControl(shell);
			model.fileYamlEditorControlCreated();
			model.getManualYamlEditor().createControl(shell);
			model.manualYamlEditorControlCreated();
			waitUntilFileConnected(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
			waitUntilFileConnected(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_MANUAL_MANIFEST_YML));
		} catch (CoreException e) {
			Log.log(e);
		}
	}

	private static CFApplication createCfApp(String name, int memory) {
		CFApplication cfApp = mock(CFApplication.class);
		Mockito.when(cfApp.getName()).thenReturn(name);
		Mockito.when(cfApp.getMemory()).thenReturn(memory);
		Mockito.when(cfApp.getBuildpackUrl()).thenReturn(DEFAULT_BUILDPACK);
		Mockito.when(cfApp.getCommand()).thenReturn(null);
		Mockito.when(cfApp.getDiskQuota()).thenReturn(DeploymentProperties.DEFAULT_MEMORY);
		Mockito.when(cfApp.getEnvAsMap()).thenReturn(Collections.emptyMap());
		Mockito.when(cfApp.getGuid()).thenReturn(UUID.randomUUID());
		Mockito.when(cfApp.getInstances()).thenReturn(DeploymentProperties.DEFAULT_INSTANCES);
		Mockito.when(cfApp.getRunningInstances()).thenReturn(DeploymentProperties.DEFAULT_INSTANCES);
		Mockito.when(cfApp.getServices()).thenReturn(Collections.emptyList());
		Mockito.when(cfApp.getStack()).thenReturn(null);
		Mockito.when(cfApp.getState()).thenReturn(CFAppState.STARTED);
		Mockito.when(cfApp.getTimeout()).thenReturn(null);
		Mockito.when(cfApp.getUris()).thenReturn(Arrays.asList(new String[] {"myapp." + SPRING_CLOUD_DOMAINS.get(0).getName()}));
		return cfApp;
	}


	/**
	 * Select manifest in the model and wait for cf editor to connect,
	 */
	private void selectAndWait(IFile manifest) throws Exception {
		model.setSelectedManifest(manifest);
		waitUntilFileConnected(manifest);
	}

	////////////////////////////////////////////////////////////////////

	@Test public void testNoTypeSelected() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);

		assertFalse(model.isManualManifestType());
		assertFalse(model.isFileManifestType());

		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(UNKNOWN_DEPLOYMENT_MANIFEST_TYPE_MUST_BE_EITHER_FILE_OR_MANUAL, validationResult.msg);

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertTrue(deploymentProperties == null);
	}

	@Test public void testManualTypeSelected() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);
		model.type.setValue(ManifestType.MANUAL);

		assertTrue(model.isManualManifestType());
		assertFalse(model.isManualManifestReadOnly());

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(project.getName(), model.getManualSelectedAppName()));

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(project.getName(), deploymentProperties.getAppName());
	}

	@Test public void testManualTypeForDeployedApp() throws Exception {
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp("my-test-app", 512);

		createDialogModel(project, deployedApp);
		model.type.setValue(ManifestType.MANUAL);

		assertTrue(model.isManualManifestType());
		assertTrue(model.isManualManifestReadOnly());

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CURRENT_GENERATED_DEPLOYMENT_MANIFEST, validationResult.msg);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(deployedApp.getName(), model.getManualSelectedAppName()));

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(deploymentProperties.getAppName(), deployedApp.getName());
		assertEquals(deployedApp.getMemory(), deploymentProperties.getMemory());
	}

	@Test(expected = IllegalStateException.class)
	public void testManualTypeManifestTextWhenAppDeployed() throws Exception {
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp("my-test-app", 512);

		createDialogModel(project, deployedApp);
		model.type.setValue(ManifestType.MANUAL);

		assertTrue(model.isManualManifestType());
		assertTrue(model.isManualManifestReadOnly());

		model.setManualManifest("some text");

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
			assertEquals(IStatus.ERROR, validationResult.status);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(null, model.getManualSelectedAppName()));
	}

	@Test public void testManualTypeSetManifestText() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);
		model.type.setValue(ManifestType.MANUAL);

		assertTrue(model.isManualManifestType());
		assertFalse(model.isManualManifestReadOnly());

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(project.getName(), model.getManualSelectedAppName()));

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(project.getName(), deploymentProperties.getAppName());
		assertNull(deploymentProperties.getManifestFile());

		String newText = "Some text";
		model.setManualManifest(newText);
		assertEquals(newText, model.getManualDocument().get());

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
		});
	}

	@Test public void testFileManifestFileNotSelected() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);
		model.type.setValue(ManifestType.FILE);
		// With LS approach there would be annotation model for dumb manifest yaml file

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
		});
	}

	@Test public void testFileManifestNonYamlFileSelected() throws Exception {
		IProject project = projects.createProject("p1");
		IFile file = createFile(project, "manifest.yml", "Some text content!");
		createDialogModel(project, null);
		model.type.setValue(ManifestType.FILE);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));

		ACondition.waitFor("reconcile to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
		});
	}

	@Test public void testFileManifestFolderSelected() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);
		model.type.setValue(ManifestType.FILE);
		model.setSelectedManifest(project);

		ACondition.waitFor("reconcile to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
		});
	}

	@Test public void testFileManifestFileSelected() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		IFile file = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n"
		);
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.type.setValue(ManifestType.FILE);

		ACondition.waitFor("reconcile to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			System.out.println("DOC: " + model.getFileDocument().get());
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
			assertEquals(IStatus.INFO, validationResult.status);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(appNameFromFile, model.getFileSelectedAppName()));

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(appNameFromFile, deploymentProperties.getAppName());
		IFile manifestFile = deploymentProperties.getManifestFile();
		assertNotNull(manifestFile);
		assertEquals("manifest.yml", manifestFile.getName());
	}

	@Test public void testSwitchingManifestTypeAndFiles() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		IFile validFile = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n"
		);
		IFile invalidFile = createFile(project, "text.yml", "Some text");

		createDialogModel(project, null);
		model.type.setValue(ManifestType.MANUAL);
		waitForJobsToComplete();

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		});

		model.type.setValue(ManifestType.FILE);
		waitForJobsToComplete();

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
		});

		selectAndWait(validFile);
		waitForJobsToComplete();
		System.out.println("New manifest set");
		System.out.println(model.getFileYamlEditor().getViewer().getDocument().get());
		System.out.println("Before conditional wait");
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			System.out.println(model.getFileYamlEditor().getViewer().getDocument().get());
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		});

		model.type.setValue(ManifestType.MANUAL);
		waitForJobsToComplete();
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		});

		model.type.setValue(ManifestType.FILE);
		waitForJobsToComplete();
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		});

		model.setSelectedManifest(project);
		waitForJobsToComplete();
		waitUntilFileDisconnedted(validFile);
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
		});

		model.setSelectedManifest(invalidFile);
		waitForJobsToComplete();
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
		});
	}

	@Test public void testValidSingleAppFileSelectedForDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);

		IFile validFileSingleName = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: " + appName + "\n" +
				"  memory: 512M\n"
		);

		createDialogModel(project, deployedApp);
		model.type.setValue(ManifestType.FILE);
		waitForJobsToComplete();

		selectAndWait(validFileSingleName);
		waitForJobsToComplete();

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		});
	}

	@Test public void testInvalidSingleAppFileSelectedForDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);

		IFile invalidFileSingleName = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);

		createDialogModel(project, deployedApp);
		model.type.setValue(ManifestType.FILE);

		selectAndWait(invalidFileSingleName);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MessageFormat.format(MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME, appName), validationResult.msg);
		});
	}

	@Test public void testValidMultiAppFileSelectedForDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);

		IFile validFileMultiName = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: " + project.getName() + "\n" +
				"  memory: 512M\n" +
				"- name: " + appName + "\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);

		createDialogModel(project, deployedApp);
		model.type.setValue(ManifestType.FILE);

		selectAndWait(validFileMultiName);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(deployedApp.getName(), model.getFileSelectedAppName()));

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(deployedApp.getName(), deploymentProperties.getAppName());
	}

	@Test public void testInvalidMultiAppFileSelectedForDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);

		IFile invalidFileMultiName = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: " + project.getName() + "\n" +
				"  memory: 512M\n" +
				"- name: anotherApp\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);

		createDialogModel(project, deployedApp);
		model.type.setValue(ManifestType.FILE);

		selectAndWait(invalidFileMultiName);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MessageFormat.format(MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME, appName), validationResult.msg);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(null, model.getFileSelectedAppName()));

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNull(deploymentProperties);
	}

	@Test public void testSwitchingWithDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);

		IFile validFileMultiName = createFile(project, "valid-manifest.yml",
				"applications:\n" +
				"- name: " + project.getName() + "\n" +
				"  memory: 512M\n" +
				"- name: " + appName + "\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);

		IFile invalidFileMultiName = createFile(project, "invalid-manifest.yml",
				"applications:\n" +
				"- name: " + project.getName() + "\n" +
				"  memory: 512M\n" +
				"- name: anotherApp\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);

		createDialogModel(project, deployedApp);

		model.type.setValue(ManifestType.FILE);
		selectAndWait(validFileMultiName);
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(appName, model.getFileSelectedAppName()));
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		});

		selectAndWait(invalidFileMultiName);
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(null, model.getFileSelectedAppName()));
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MessageFormat.format(MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME, appName), validationResult.msg);
		});

		model.setSelectedManifest(project);
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(null, model.getFileSelectedAppName()));

		model.type.setValue(ManifestType.MANUAL);
		assertTrue(model.isManualManifestReadOnly());
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(appName, model.getManualSelectedAppName()));
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CURRENT_GENERATED_DEPLOYMENT_MANIFEST, validationResult.msg);
		});

		selectAndWait(invalidFileMultiName);
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(null, model.getFileSelectedAppName()));
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CURRENT_GENERATED_DEPLOYMENT_MANIFEST, validationResult.msg);
		});

		model.type.setValue(ManifestType.FILE);
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MessageFormat.format(MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME, appName), validationResult.msg);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(null, model.getFileSelectedAppName()));
	}

	@Test(expected = OperationCanceledException.class)
	public void testCancel() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);
		model.type.setValue(ManifestType.MANUAL);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		});

		model.cancelPressed();

		assertTrue(model.isCanceled());

		model.getDeploymentProperties();
	}

	@Test public void testManifestFileLabel() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		IFile file = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n"
		);
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.setManifestType(ManifestType.FILE);

		assertEquals(file.getFullPath().toOSString(), model.getFileLabel().getValue());

		model.getFileDocument().set("some text");

		assertEquals(file.getFullPath().toOSString() + "*", model.getFileLabel().getValue());
	}

	@Test public void testDiscardCancelWithDirtyManifestFile() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		String text = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n";
		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));

		model.setManifestType(ManifestType.FILE);

		model.getFileDocument().set("some text");

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
		});

		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(DONT_SAVE_BUTTON_ID);

		model.cancelPressed();
		waitForJobsToComplete();

		assertTrue(model.isCanceled());
		assertEquals(text, IOUtil.toString(file.getContents()));

		/*
		 * Test document provider is not connected to the file input anymore
		 */
//		disposeModel();
//		FileEditorInput editorInput = new FileEditorInput(file);
//		TextFileDocumentProvider docProvider = (TextFileDocumentProvider) DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
//		assertNull(docProvider.getDocument(editorInput));
	}

	@Test public void testErrorsInYaml() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		String text = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n";
		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.setManifestType(ManifestType.FILE);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		});

		model.getFileDocument().set("applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512MXX\n");

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MANIFEST_YAML_ERRORS, validationResult.msg);
		});

	}

	@Test public void testSaveCancelWithDirtyManifestFile() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		String text = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n";
		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.setManifestType(ManifestType.FILE);

		String newText = "some text";
		model.getFileDocument().set(newText);
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
			assertEquals(IStatus.ERROR, validationResult.status);
		});

		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(SAVE_BUTTON_ID);

		model.cancelPressed();

		assertTrue(model.isCanceled());
		assertEquals(newText, IOUtil.toString(file.getContents()));

		/*
		 * Test document provider is not connected to the file input anymore
		 */
//		disposeModel();
//		FileEditorInput editorInput = new FileEditorInput(file);
//		TextFileDocumentProvider docProvider = (TextFileDocumentProvider) DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
//		assertNull(docProvider.getDocument(editorInput));
	}

	@Test public void testDiscardOkWithDirtyManifestFile() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		int memory = 512;
		String text = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: " + memory + "M\n";
		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.setManifestType(ManifestType.FILE);

		int newMemory = 256;
		String newText = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: " + newMemory + "M\n";
		model.getFileDocument().set(newText);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
			assertEquals(IStatus.INFO, validationResult.status);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(appNameFromFile, model.getFileSelectedAppName()));

		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(DONT_SAVE_BUTTON_ID);

		model.okPressed();

		assertFalse(model.isCanceled());
		assertEquals(text, IOUtil.toString(file.getContents()));

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(appNameFromFile, deploymentProperties.getAppName());
		assertEquals(newMemory, deploymentProperties.getMemory());


		/*
		 * Test document provider is not connected to the file input anymore
		 */
//		disposeModel();
//		FileEditorInput editorInput = new FileEditorInput(file);
//		TextFileDocumentProvider docProvider = (TextFileDocumentProvider) DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
//		assertNull(docProvider.getDocument(editorInput));
	}

	@Test public void testSaveOkWithDirtyManifestFile() throws Exception {
		IProject project = projects.createProject("p1");
		String appName = "app-name-from-file";
		int memory = 512;
		String text = "applications:\n" +
				"- name: " + appName + "\n" +
				"  memory: " + memory + "M\n";
		String newAppName = "new-app";
		int newMemory = 768;
		String newText = "applications:\n" +
				"- name: " + newAppName + "\n" +
				"  memory: " + newMemory + "M\n";

		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.setManifestType(ManifestType.FILE);

		model.getFileDocument().set(newText);
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
			assertEquals(IStatus.INFO, validationResult.status);
		});

		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(newAppName, model.getFileSelectedAppName()));

		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(SAVE_BUTTON_ID);

		model.okPressed();

		assertFalse(model.isCanceled());
		assertEquals(newText, IOUtil.toString(file.getContents()));

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(newAppName, deploymentProperties.getAppName());
		assertEquals(newMemory, deploymentProperties.getMemory());

		/*
		 * Test document provider is not connected to the file input anymore
		 */
//		disposeModel();
//		FileEditorInput editorInput = new FileEditorInput(file);
//		TextFileDocumentProvider docProvider = (TextFileDocumentProvider) DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
//		assertNull(docProvider.getDocument(editorInput));

	}

	@Test public void testDiscardOnDirtyManifestFileSwitch() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		int memory = 512;
		String text = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: " + memory + "M\n";
		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.setManifestType(ManifestType.FILE);
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
			assertEquals(IStatus.INFO, validationResult.status);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(appNameFromFile, model.getFileSelectedAppName()));

		model.getFileDocument().set("some text");
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
			assertEquals(IStatus.ERROR, validationResult.status);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(null, model.getFileSelectedAppName()));

		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(DONT_SAVE_BUTTON_ID);

		model.setSelectedManifest(project);
		waitUntilFileDisconnedted(file);

		assertEquals(text, IOUtil.toString(file.getContents()));

		selectAndWait(file);
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(appNameFromFile, model.getFileSelectedAppName()));
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
			assertEquals(IStatus.INFO, validationResult.status);
		});

		assertEquals(text, model.getFileDocument().get());

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(appNameFromFile, deploymentProperties.getAppName());
		assertEquals(memory, deploymentProperties.getMemory());
	}

	@Test public void testSaveOnDirtyManifestFileSwitch() throws Exception {
		IProject project = projects.createProject("p1");
		String appName = "app-name-from-file";
		int memory = 512;
		String text = "applications:\n" +
				"- name: " + appName + "\n" +
				"  memory: " + memory + "M\n";
		String newAppName = "new-app";
		int newMemory = 768;
		String newText = "applications:\n" +
				"- name: " + newAppName + "\n" +
				"  memory: " + newMemory + "M\n";

		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.setManifestType(ManifestType.FILE);

		model.getFileDocument().set(newText);
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
			assertEquals(IStatus.INFO, validationResult.status);
		});

		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(SAVE_BUTTON_ID);

		model.setSelectedManifest(project);
		waitUntilFileDisconnedted(file);

		assertEquals(newText, IOUtil.toString(file.getContents()));

		selectAndWait(file);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
			assertEquals(IStatus.INFO, validationResult.status);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(newAppName, model.getFileSelectedAppName()));

		assertEquals(newText, model.getFileDocument().get());

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(newAppName, deploymentProperties.getAppName());
		assertEquals(newMemory, deploymentProperties.getMemory());
	}

	@Test public void testRecoverFromInvalidManifest() throws Exception {
		IProject project = projects.createProject("p1");
		String newAppName = "new-app";
		int newMemory = 768;
		String newText = "applications:\n" +
				"- name: " + newAppName + "\n" +
				"  memory: " + newMemory + "M\n";

		IFile file = createFile(project, "manifest.yml", "");
		createDialogModel(project, null);
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.setManifestType(ManifestType.FILE);

		model.getFileDocument().set(newText);
		assertTrue(model.getFileLabel().getValue().endsWith("*"));
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
			assertEquals(IStatus.INFO, validationResult.status);
		});

		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(DONT_SAVE_BUTTON_ID);

		model.okPressed();

		assertFalse(model.isCanceled());
		assertEquals("", IOUtil.toString(file.getContents()));
		assertTrue(model.getFileLabel().getValue().endsWith("*"));


		// Input disconnect at this point - needs to be reset as well.
		model.setSelectedManifest(null);
		waitUntilFileDisconnedted(file);
		selectAndWait(file);
		model.getFileDocument().set(newText);
		assertTrue(model.getFileLabel().getValue().endsWith("*"));
		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
			assertEquals(IStatus.INFO, validationResult.status);
		});
		ACondition.waitFor("app name reconcile", TIMEOUT, () -> assertEquals(newAppName, model.getFileSelectedAppName()));

		Mockito.reset(ui);
		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(SAVE_BUTTON_ID);

		model.okPressed();

		assertFalse(model.isCanceled());
		assertEquals(newText, IOUtil.toString(file.getContents()));

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertEquals(newAppName, deploymentProperties.getAppName());
		assertEquals(newMemory, deploymentProperties.getMemory());

		/*
		 * Test document provider is not connected to the file input anymore
		 */
//		disposeModel();
//		FileEditorInput editorInput = new FileEditorInput(file);
//		TextFileDocumentProvider docProvider = (TextFileDocumentProvider) DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
//		assertNull(docProvider.getDocument(editorInput));

	}

	@Test public void testManualManifestYamlError() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);
		// Set resource annotation model
//		model.setManualResourceAnnotationModel(new AnnotationModel());
		model.type.setValue(ManifestType.MANUAL);

		assertTrue(model.isManualManifestType());
		assertFalse(model.isManualManifestReadOnly());

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		});

		Annotation resourceAnnotation = new Annotation(DeploymentPropertiesDialogModel.LSP_ERROR_ANNOTATION_TYPE, false, "Some error");
		model.getManualResourceAnnotationModel().addAnnotation(resourceAnnotation, new Position(0,0));

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MANIFEST_YAML_ERRORS, validationResult.msg);
		});

		model.getManualResourceAnnotationModel().removeAnnotation(resourceAnnotation);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		});
	}

	@Test public void testFileManifestYamlError() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		IFile file = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n"
		);
		createDialogModel(project, null);
		// Set resource annotation model
		selectAndWait(file);
		waitUntilFileDisconnedted(project.getFolder(".settings").getFile(DeploymentPropertiesDialogModel.DUMMY_FILE_MANIFEST_YML));
		model.type.setValue(ManifestType.FILE);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		});

		Annotation resourceAnnotation = new Annotation(DeploymentPropertiesDialogModel.LSP_ERROR_ANNOTATION_TYPE, false, "Some error");
		model.getFileResourceAnnotationModel().addAnnotation(resourceAnnotation, new Position(0,0));

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.ERROR, validationResult.status);
			assertEquals(MANIFEST_YAML_ERRORS, validationResult.msg);
		});

		model.getFileResourceAnnotationModel().removeAnnotation(resourceAnnotation);

		ACondition.waitFor("validation to occur", TIMEOUT, () -> {
			ValidationResult validationResult = model.getValidator().getValue();
			assertEquals(IStatus.INFO, validationResult.status);
			assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		});
	}
}
