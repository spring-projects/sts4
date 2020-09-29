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
package org.springframework.ide.eclipse.boot.dash.cf.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextViewerConfiguration;
import org.eclipse.ui.part.FileEditorInput;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.AppNameAnnotation;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.AppNameAnnotationModel;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.AppNameAnnotationSupport;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudData;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.launch.properties.EmbeddedEditor;
import org.springsource.ide.eclipse.commons.livexp.core.AbstractDisposable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("restriction")
public class DeploymentPropertiesDialogModel extends AbstractDisposable {

	public static final String LSP_ERROR_ANNOTATION_TYPE = "org.eclipse.ui.workbench.texteditor.error";

	public static final String DUMMY_FILE_MANIFEST_YML = ".file-manifest.yml";
	public static final String DUMMY_MANUAL_MANIFEST_YML = ".manual-manifest.yml";
	public static final String UNKNOWN_DEPLOYMENT_MANIFEST_TYPE_MUST_BE_EITHER_FILE_OR_MANUAL = "Unknown deployment manifest type. Must be either 'File' or 'Manual'.";
	public static final String NO_SUPPORT_TO_DETERMINE_APP_NAMES = "Support for determining application names is unavailable";
	public static final String MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME = "Manifest does not contain deployment properties for application with name ''{0}''.";
	public static final String APPLICATION_NAME_NOT_SELECTED = "Application name not selected";
	public static final String MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED = "Manifest does not have any application defined.";
	public static final String ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY = "Enter deployment manifest YAML manually.";
	public static final String CURRENT_GENERATED_DEPLOYMENT_MANIFEST = "Current generated deployment manifest.";
	public static final String CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM = "Choose an existing deployment manifest YAML file from the local file system.";
	public static final String DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED = "Deployment manifest file not selected.";
	public static final String MANIFEST_YAML_ERRORS = "Deployment manifest YAML has errors.";

	public static enum ManifestType {
		FILE,
		MANUAL
	}

	private UserInteractions ui;

	private abstract class AbstractSubModel extends AbstractDisposable {

		EmbeddedEditor editor;

		AppNameAnnotationSupport appNameAnnotationSupport;

		final LiveVariable<Boolean> editorControlCreated;

		final LiveVariable<IResource> selectedFile;

		final LiveExpression<FileEditorInput> editorInput;

		LiveExpression<AppNameAnnotationModel> appNameAnnotationModel;

		LiveExpression<IAnnotationModel> resourceAnnotationModel;

		LiveExpression<List<String>> applicationNames;

		LiveExpression<Boolean> errorsInYaml;

		LiveExpression<String> selectedAppName;

		/**
		 * When dialog closed and viewer disposed live expression would still keep the document which will be used to detrmine DeploymentProperties
		 */
		final LiveExpression<IDocument> document;

		protected IFile getFile() {
			IResource r = selectedFile.getValue();
			return r instanceof IFile ? (IFile) r : null;
		}

		protected void saveOrDiscardIfNeeded() {
			FileEditorInput input = editorInput.getValue();
			if (input != null) {
				saveOrDiscardIfNeeded(input);
			}
		}

		protected void saveOrDiscardIfNeeded(FileEditorInput file) {
			if (editor.isDirty()) {
				if (ui.confirmOperation("Changes Detected", "Manifest file '" + file.getFile().getFullPath().toOSString()
								+ "' has been changed. Do you want to save changes or discard them?", new String[] {"Save", "Don't Save"}, 0) == 0) {
					editor.doSave(new NullProgressMonitor());
				}
			}
		}

		abstract String getManifestContents();

		/**
		 * Return manifest from which contents are takes as an {@link IFile}
		 * Return null if manifest content doesn't come from a file
		 * @return
		 */
		abstract IFile getManifest();

		CloudApplicationDeploymentProperties getDeploymentProperties() throws Exception {
			CloudApplicationDeploymentProperties deploymentProperties = toDeploymentProperties(cloudData,
					getManifestContents(), project,
					deployedApp == null ? selectedAppName.getValue() : getDeployedAppName());
			if (deploymentProperties != null) {
				deploymentProperties.setManifestFile(getManifest());
			}
			return deploymentProperties;
		}

		protected AbstractSubModel(String fixedAppName) {
			IPreferenceStore preferenceStore = JavaPlugin.getDefault().getCombinedPreferenceStore();
			this.editor = new EmbeddedEditor(editor -> new ExtensionBasedTextViewerConfiguration(editor, preferenceStore), preferenceStore, false, true);

			editorControlCreated = new LiveVariable<>(false);

			selectedFile = new LiveVariable<>();

			editorControlCreated.addListener((exp, value) -> {
				if (appNameAnnotationSupport == null && value != null && value.booleanValue()) {
					appNameAnnotationSupport = new AppNameAnnotationSupport(editor.getViewer(),
							editor.getAnnotationAccess(), editor.getSharedColors(), fixedAppName);
				}
			});

			editorInput = new LiveExpression<FileEditorInput>() {

				{
					dependsOn(selectedFile);
				}

				@Override
				protected FileEditorInput compute() {
					if (editor != null) {
						IFile file = getFile();
						FileEditorInput currentInput = getValue();
						boolean changed = currentInput == null || !currentInput.getFile().equals(file);
						if (changed) {
							if (currentInput != null) {
								saveOrDiscardIfNeeded(currentInput);
							}
							try {
								if (file != null) {
									FileEditorInput input = new FileEditorInput(file);
									editor.setInput(input);
									return input;
								} else {
									editor.setInput(null);
									return null;
								}
							} catch (CoreException e) {
								Log.log(e);
							}
						}
					}
					return null;
				}

			};

			appNameAnnotationModel = new LiveExpression<AppNameAnnotationModel>() {
				{
					dependsOn(editorControlCreated);
					dependsOn(editorInput);
				}

				@Override
				protected AppNameAnnotationModel compute() {
					if (editor != null && editor.getViewer() != null) {
						return AppNameAnnotationSupport.getAppNameAnnotationModel(editor.getViewer());
					}
					return null;
				}

			};

			resourceAnnotationModel = new LiveExpression<IAnnotationModel>() {

				{
					dependsOn(editorControlCreated);
					dependsOn(editorInput);
				}

				@Override
				protected IAnnotationModel compute() {
					if (editor != null && editor.getViewer() != null) {
						return editor.getViewer().getAnnotationModel();
					}
					return null;
				}

			};

			applicationNames = new LiveExpression<List<String>>() {

				private AppNameAnnotationModel attachedTo = null;
				private AnnotationModelListener listener = new AnnotationModelListener() {
					@Override
					public void modelChanged(AnnotationModelEvent event) {
						refresh();
					}
				};

				{
					dependsOn(appNameAnnotationModel);
				}

				@Override
				protected List<String> compute() {
					AppNameAnnotationModel annotationModel = appNameAnnotationModel.getValue();
					if (annotationModel != null) {
						attachListener(annotationModel);
						List<String> applicationNames = new ArrayList<>();
						for (Iterator<Annotation> itr = annotationModel.getAnnotationIterator(); itr.hasNext();) {
							Annotation next = itr.next();
							if (next instanceof AppNameAnnotation) {
								AppNameAnnotation a = (AppNameAnnotation) next;
								applicationNames.add(a.getText());
							}
						}
						return applicationNames;
					}
					return Collections.emptyList();
				}

				synchronized private void attachListener(AppNameAnnotationModel annotationModel) {
					if (attachedTo == annotationModel) {
						return;
					}
					if (attachedTo != null) {
						attachedTo.removeAnnotationModelListener(listener);
					}
					if (annotationModel != null) {
						annotationModel.addAnnotationModelListener(listener);
					}
					attachedTo = annotationModel;
				}

			};

			errorsInYaml = new LiveExpression<Boolean>() {

				private IAnnotationModel attachedTo = null;
				private AnnotationModelListener listener = new AnnotationModelListener() {
					@Override
					public void modelChanged(AnnotationModelEvent event) {
						refresh();
					}
				};

				{
					dependsOn(resourceAnnotationModel);
				}

				{
					onDispose((d) -> {
						if (attachedTo != null) {
							attachedTo.removeAnnotationModelListener(listener);
						}
					});
				}

				@Override
				protected Boolean compute() {
					IAnnotationModel annotationModel = resourceAnnotationModel.getValue();
					if (annotationModel != null) {
						attachListener(annotationModel);
						for (Iterator<Annotation> itr = annotationModel.getAnnotationIterator(); itr.hasNext();) {
							Annotation next = itr.next();
							if (LSP_ERROR_ANNOTATION_TYPE.equals(next.getType())) {
								return Boolean.TRUE;
							}
						}
					}
					return Boolean.FALSE;
				}

				synchronized private void attachListener(IAnnotationModel annotationModel) {
					if (attachedTo == annotationModel) {
						return;
					}
					if (attachedTo != null) {
						attachedTo.removeAnnotationModelListener(listener);
					}
					if (annotationModel != null) {
						annotationModel.addAnnotationModelListener(listener);
					}
					attachedTo = annotationModel;
				}
			};

			selectedAppName = new LiveExpression<String>() {

				private AppNameAnnotationModel attachedTo = null;
				private AnnotationModelListener listener = new AnnotationModelListener() {
					@Override
					public void modelChanged(AnnotationModelEvent event) {
						refresh();
					}
				};

				{
					dependsOn(appNameAnnotationModel);
				}

				{
					onDispose((d) -> {
						if (attachedTo != null) {
							attachedTo.removeAnnotationModelListener(listener);
						}
					});
				}

				@Override
				protected String compute() {
					AppNameAnnotationModel annotationModel = appNameAnnotationModel.getValue();
					if (annotationModel != null) {
						attachListener(annotationModel);
						AppNameAnnotation a = annotationModel.getSelectedAppAnnotation();
						if (a != null) {
							return a.getText();
						}
					}
					return null;
				}

				synchronized private void attachListener(AppNameAnnotationModel annotationModel) {
					if (attachedTo == annotationModel) {
						return;
					}
					if (attachedTo != null) {
						attachedTo.removeAnnotationModelListener(listener);
					}
					if (annotationModel != null) {
						annotationModel.addAnnotationModelListener(listener);
					}
					attachedTo = annotationModel;
				}

			};

			document = new LiveExpression<IDocument>(new Document("")) {
				{
					dependsOn(editorInput);
					dependsOn(editorControlCreated);
				}

				@Override
				protected IDocument compute() {
					IDocument doc = editor == null || editor.getViewer() == null ? null : editor.getViewer().getDocument();
					return doc;
				}
			};

			onDispose((d) -> {
				appNameAnnotationSupport.dispose();
				editorControlCreated.dispose();
				editorInput.dispose();
				applicationNames.dispose();
				appNameAnnotationModel.dispose();
				errorsInYaml.dispose();
				resourceAnnotationModel.dispose();
				selectedAppName.dispose();
				document.dispose();
				editor.dispose();
			});
		}

	}

	public class FileDeploymentPropertiesDialogModel extends AbstractSubModel {

		final private LiveExpression<String> fileLabel = new LiveExpression<String>() {
			{
				dependsOn(editorInput);
			}

			@Override
			protected String compute() {
				FileEditorInput input = editorInput.getValue();
				if (input != null) {
					return editorInput.getValue().getFile().getFullPath().toOSString() + (editor.isDirty() ? "*" : "");
				}
				return "";
			}

		};

		private final IPropertyListener editorListener = (Object source, int propId) -> {
			if (propId == IEditorPart.PROP_DIRTY) {
				fileLabel.refresh();
			}
		};

		Validator validator = new Validator() {

			{
				dependsOn(editorInput);
				dependsOn(appNameAnnotationModel);
				dependsOn(errorsInYaml);
				dependsOn(applicationNames);
				dependsOn(selectedAppName);
			}

			@Override
			protected ValidationResult compute() {
				ValidationResult result = ValidationResult.OK;

				if (editorInput.getValue() == null) {
					result = ValidationResult.error(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED);
				}

				if (result.isOk()) {
					AppNameAnnotationModel appNamesModel = appNameAnnotationModel.getValue();
					if (appNamesModel == null) {
						result = ValidationResult.error(NO_SUPPORT_TO_DETERMINE_APP_NAMES);
					}
					if (result.isOk()) {
						String appName = getDeployedAppName();
						if (applicationNames.getValue().isEmpty()) {
							result = ValidationResult.error(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED);
						} else {
							if (errorsInYaml.getValue().booleanValue()) {
								result = ValidationResult.error(MANIFEST_YAML_ERRORS);
							} else {
								String selectedAnnotation = selectedAppName.getValue();
								if (appName == null) {
									if (selectedAnnotation == null) {
										result = ValidationResult.error(APPLICATION_NAME_NOT_SELECTED);
									}
								} else {
									if (selectedAnnotation == null || !appName.equals(selectedAnnotation)) {
										result = ValidationResult.error(MessageFormat.format(
												MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME,
												appName));
									}
								}
							}
						}
					}
				}

				if (result.isOk()) {
					result = ValidationResult.info(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM);
				}

				return result;
			}

		};

		private IFile tempFile;

		{
			onDispose((d) -> {
				if (tempFile != null && tempFile.exists()) {
					try {
						tempFile.delete(true, new NullProgressMonitor());
					} catch (CoreException e) {
						Log.log(e);
					}
				}
				editor.removePropertyListener(editorListener);
				validator.dispose();
				fileLabel.dispose();
			});
		}

		FileDeploymentPropertiesDialogModel(String fixedAppName) {
			super(fixedAppName);
			editor.addPropertyListener(editorListener);
		}

		public void init(IFile tempFile) {
			this.tempFile = tempFile;
			if (getManifest() == null) {
				// No manifest file? Generate dumb empty manifest YAML to get proper Manifest LS
				// based reconciler, CA etc
				try {
					generateTempManifestFile(tempFile, "");
					editor.init(null, new FileEditorInput(tempFile));
				} catch (CoreException e) {
					Log.log(e);
				}
			}
		}

		public IAnnotationModel getAnnotationModel() {
			return editor.getViewer().getAnnotationModel();
		}

		@Override
		String getManifestContents() {
			return document.getValue().get();
		}

		@Override
		IFile getManifest() {
			return getFile();
		}

		void reopenSameFile() {
			document.refresh();
		}

	}

	public class ManualDeploymentPropertiesDialogModel extends AbstractSubModel {

		private IFile tempFile;

		private boolean readOnly;

		Validator validator = new Validator() {

			{
				dependsOn(appNameAnnotationModel);
				dependsOn(errorsInYaml);
				dependsOn(applicationNames);
				dependsOn(selectedAppName);
			}

			@Override
			protected ValidationResult compute() {
				ValidationResult result = ValidationResult.OK;

				AppNameAnnotationModel appNamesModel = appNameAnnotationModel.getValue();
				if (appNamesModel == null) {
					result = ValidationResult.error(NO_SUPPORT_TO_DETERMINE_APP_NAMES);
				}
				if (result.isOk()) {
					String appName = getDeployedAppName();
					if (applicationNames.getValue().isEmpty()) {
						result = ValidationResult.error(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED);
					} else {
						if (errorsInYaml.getValue().booleanValue()) {
							result = ValidationResult.error(MANIFEST_YAML_ERRORS);
						} else {
							String selectedAnnotation = selectedAppName.getValue();
							if (appName == null) {
								if (selectedAnnotation == null) {
									result = ValidationResult.error(APPLICATION_NAME_NOT_SELECTED);
								}
							} else {
								if (selectedAnnotation == null || !appName.equals(selectedAnnotation)) {
									result = ValidationResult.error(MessageFormat.format(
											MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME,
											appName));
								}
							}
						}
					}
				}

				if (result.isOk()) {
					result = ValidationResult.info(readOnly ? CURRENT_GENERATED_DEPLOYMENT_MANIFEST : ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY);
				}

				return result;
			}
		};

		{
			onDispose((d) -> {
				validator.dispose();
				try {
					if (tempFile != null) {
						if (tempFile.exists()) {
							tempFile.delete(true, new NullProgressMonitor());
						}
						tempFile = null;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			});
		}


		ManualDeploymentPropertiesDialogModel(String fixedAppName, boolean readOnly) {
			super(fixedAppName);
			this.readOnly = readOnly;
		}

		public void init(IFile tempFile) {
			this.tempFile = tempFile;
			try {
				generateTempManifestFile(tempFile, generateDefaultContent());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				editor.init(null, new FileEditorInput(tempFile));
				selectedFile.setValue(tempFile);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void setText(String s) {
			if (readOnly) {
				throw new IllegalStateException("The model is read-only!");
			}
			IDocument doc = document.getValue();
			if (doc != null) {
				doc.set(s);
			}
		}

		public String getText() {
			IDocument doc = document.getValue();
			return doc == null ? null : doc.get();
		}

		public IAnnotationModel getAnnotationModel() {
			return editor.getViewer().getAnnotationModel();
		}

		@Override
		String getManifestContents() {
			return getText();
		}

		@Override
		IFile getManifest() {
			return null;
		}

	}

	private abstract class AnnotationModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {

		@Override
		public void modelChanged(IAnnotationModel model) {
			// Leave empty. AnnotationModelEvent method is the one that will be called
		}

		@Override
		abstract public void modelChanged(AnnotationModelEvent event);

	}


	final public LiveVariable<ManifestType> type = new LiveVariable<>();
	final public LiveVariable<Boolean> enableJmxSshTunnel = new LiveVariable<>();

	final private CFApplication deployedApp;

	final private CloudData cloudData;

	final IProject project;

	final private FileDeploymentPropertiesDialogModel fileModel;

	final private ManualDeploymentPropertiesDialogModel manualModel;

	private boolean isCancelled = false;

	final private Validator validator;

	final public boolean supportsSsh;

	public DeploymentPropertiesDialogModel(UserInteractions ui, CloudData cloudData, IProject project, CFApplication deployedApp, boolean supportsSsh) {
		super();
		this.supportsSsh = supportsSsh;
		this.ui = ui;
		this.deployedApp = deployedApp;
		this.cloudData = cloudData;
		this.project = project;
		this.manualModel = new ManualDeploymentPropertiesDialogModel(getDeployedAppName(), deployedApp != null);
		this.fileModel = new FileDeploymentPropertiesDialogModel(getDeployedAppName());

		this.validator = new Validator() {

			{
				dependsOn(type);
				dependsOn(fileModel.validator);
				dependsOn(manualModel.validator);
			}

			@Override
			protected ValidationResult compute() {
				if (isFileManifestType()) {
					return fileModel.validator.getValue();
				} else if (isManualManifestType()) {
					return manualModel.validator.getValue();
				} else {
					return ValidationResult.error(UNKNOWN_DEPLOYMENT_MANIFEST_TYPE_MUST_BE_EITHER_FILE_OR_MANUAL);
				}
			}

		};

		onDispose((d) -> {
			fileModel.dispose();
			manualModel.dispose();
		});

	}

	public void initFileModel() {
		fileModel.init(project.getFolder(".settings").getFile(DUMMY_FILE_MANIFEST_YML));
	}

	public void initManualModel() {
		manualModel.init(project.getFolder(".settings").getFile(DUMMY_MANUAL_MANIFEST_YML));
	}

	public CloudApplicationDeploymentProperties getDeploymentProperties() throws Exception {
		if (isCancelled) {
			throw new OperationCanceledException();
		}
		if (type.getValue() == null) {
			return null;
		}
		CloudApplicationDeploymentProperties props = null;
		switch (type.getValue()) {
		case FILE:
			props = fileModel.getDeploymentProperties();
			break;
		case MANUAL:
			props = manualModel.getDeploymentProperties();
			break;
		default:
		}
		if (props!=null) {
			Boolean enableJmx = enableJmxSshTunnel.getValue();
			props.setEnableJmxSshTunnel(enableJmx!=null && enableJmx);
		}
		return props;
	}

	public void cancelPressed() {
		fileModel.saveOrDiscardIfNeeded();
		isCancelled = true;
//		try {
//			fileModel.editor.setInput(null);
//			manualModel.editor.setInput(null);
//		} catch (CoreException e) {
//			Log.log(e);
//		}
	}

	public boolean okPressed() {
		fileModel.saveOrDiscardIfNeeded();
		isCancelled = false;
		try {
			CloudApplicationDeploymentProperties deploymentProperties = getDeploymentProperties();
//			fileModel.editor.setInput(null);
//			manualModel.editor.setInput(null);
			return deploymentProperties != null;
		} catch (Exception e) {
			fileModel.reopenSameFile();
			ui.errorPopup("Invalid YAML content", ExceptionUtil.getMessage(e));
			return false;
		}
	}

	public void setSelectedManifest(IResource manifest) {
		fileModel.selectedFile.setValue(manifest);
	}

	public void setManualManifest(String manifestText) {
		manualModel.setText(manifestText);
	}

	public void setManifestType(ManifestType type) {
		this.type.setValue(type);
	}

	public String getProjectName() {
		return project.getName();
	}

	public IProject getProject() {
		return project;
	}

	public boolean isFileManifestType() {
		return type.getValue() == ManifestType.FILE;
	}

	public boolean isManualManifestType() {
		return type.getValue() == ManifestType.MANUAL;
	}

	public IResource getSelectedManifest() {
		return fileModel.selectedFile.getValue();
	}

	public String getDeployedAppName() {
		return deployedApp == null ? null : deployedApp.getName();
	}

	public EmbeddedEditor getFileYamlEditor() {
		return fileModel.editor;
	}

	public EmbeddedEditor getManualYamlEditor() {
		return manualModel.editor;
	}

	public IDocument getManualDocument() {
		return manualModel.document.getValue();
	}

	public boolean isManualManifestReadOnly() {
		return deployedApp!=null;
	}

	public IDocument getFileDocument() {
		return fileModel.document.getValue();
	}

	public LiveExpression<FileEditorInput> getFileEditorInput() {
		return fileModel.editorInput;
	}

	public LiveExpression<String> getFileLabel() {
		return fileModel.fileLabel;
	}

	private CFApplication getDeployedApp() {
		return deployedApp;
	}

	public boolean isCanceled() {
		return isCancelled;
	}

	public Validator getValidator() {
		return validator;
	}

	public LiveExpression<AppNameAnnotationModel> getManualAppNameAnnotationModel() {
		return manualModel.appNameAnnotationModel;
	}

	public LiveExpression<AppNameAnnotationModel> getFileAppNameAnnotationModel() {
		return fileModel.appNameAnnotationModel;
	}

	public void fileYamlEditorControlCreated() {
		fileModel.editorControlCreated.setValue(true);
	}

	public void manualYamlEditorControlCreated() {
		manualModel.editorControlCreated.setValue(true);
	}

	public IAnnotationModel getManualResourceAnnotationModel() {
		return manualModel.resourceAnnotationModel.getValue();
	}

	public IAnnotationModel getFileResourceAnnotationModel() {
		return fileModel.resourceAnnotationModel.getValue();
	}

	public String getFileSelectedAppName() {
		return fileModel.selectedAppName.getValue();
	}

	public String getManualSelectedAppName() {
		return manualModel.selectedAppName.getValue();
	}

	private String generateDefaultContent() {
		CloudApplicationDeploymentProperties props = CloudApplicationDeploymentProperties.getFor(project, cloudData,
				getDeployedApp());
		Map<Object, Object> yaml = ApplicationManifestHandler.toYaml(props, cloudData);
		DumperOptions options = new DumperOptions();
		options.setExplicitStart(true);
		options.setCanonical(false);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		return new Yaml(options).dump(yaml);
	}


	private static IFile generateTempManifestFile(IFile manifestFile, String content) throws CoreException {
		if (manifestFile.exists()) {
			manifestFile.setContents(new ByteArrayInputStream(content.getBytes()), true, false, new NullProgressMonitor());
		} else {
			if (!manifestFile.getParent().exists()) {
				((IFolder)manifestFile.getParent()).create(true, true, new NullProgressMonitor());
			}
			manifestFile.create(new ByteArrayInputStream(content.getBytes()), true, new NullProgressMonitor());
		}
		return manifestFile;
	}

	public static CloudApplicationDeploymentProperties toDeploymentProperties(CloudData cloudData, String yaml, IProject project, String applicationName) throws Exception {
		List<CloudApplicationDeploymentProperties> propsList = new ApplicationManifestHandler(project, cloudData, null) {
			@Override
			protected InputStream getInputStream() throws Exception {
				return new ByteArrayInputStream(yaml.getBytes());
			}
		}.load(new NullProgressMonitor());
		/*
		 * If "Select Manifest..." action is invoked appName is not null,
		 * but we should allow for any manifest file selected for now. Hence
		 * set the applicationName var to null in that case
		 */
		CloudApplicationDeploymentProperties deploymentProperties = null;
		if (applicationName == null) {
			deploymentProperties = propsList.get(0);
		} else {
			for (CloudApplicationDeploymentProperties p : propsList) {
				if (applicationName.equals(p.getAppName())) {
					deploymentProperties = p;
					break;
				}
			}
		}
		return deploymentProperties;
	}

	public CloudApplicationDeploymentProperties getDeploymentProperties(String yaml, String appName) throws Exception {
		if (yaml == null) {
			yaml = generateDefaultContent();
		}
		return toDeploymentProperties(cloudData, yaml, project, appName);
	}

}
