/*******************************************************************************
 * Copyright (c) 2015, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.properties;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextViewerConfiguration;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.ILaunchConfigurationTabSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.base.Supplier;

/**
 * An IPageSection that contains a table-based properties editor.
 *
 * @author Kris De Volder
 * @author Alex Boyko
 */
@SuppressWarnings("restriction")
public class PropertiesEditorSection extends WizardPageSection implements ILaunchConfigurationTabSection {

	private static final String LEGACY_PROPERTIES_EDITOR_VIEWER_CONFIG_CLASS_NAME = "org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesFileSourceViewerConfiguration";
	private static final String LEGACY_BOOT_PROPS_EDITOR_PLUGIN_ID = "org.springframework.ide.eclipse.boot.properties.editor";
	private static final String BOOT_LS_PLUGIN_ID = "org.springframework.tooling.boot.ls";

	@Override
	public LiveVariable<Boolean> getDirtyState() {
		return dirtyState;
	}

	public PropertiesEditorSection(IPageWithSections owner, LiveExpression<IProject> project) {
		super(owner);
		this.project = project;
	}

	private final LiveExpression<IProject> project;
	private LiveVariable<Boolean> dirtyState = new LiveVariable<>(false);
	private String props = "";
	private LiveVariable<ValidationResult> validator = new LiveVariable<>(ValidationResult.OK);

	private EmbeddedEditor embeddedEditor;
	private IFile propertiesFile;

	private IAnnotationModelListener annotationModelListener = new IAnnotationModelListener() {
		@Override
		public void modelChanged(IAnnotationModel model) {
			boolean errorsPresent = model.getAnnotationIterator().hasNext();
			validator.setValue(errorsPresent ? ValidationResult.error("Errors present in application properties.") : ValidationResult.OK);
		}
	};

	private IPropertyListener editorDirtyStateListener = (source, propId) -> {
		if (propId == IEditorPart.PROP_DIRTY) {
			dirtyState.setValue(((ITextEditor)source).isDirty());
		}
	};

	private static boolean isLanguageServerEnabled() {
		return isBundleEnabled(BOOT_LS_PLUGIN_ID);
	}

	private static boolean isLegacyPropertyEditorEnabled() {
		return isBundleEnabled(LEGACY_BOOT_PROPS_EDITOR_PLUGIN_ID);
	}

	private static boolean isBundleEnabled(String bundleId) {
		Bundle lsBundle = Platform.getBundle(bundleId);
		return lsBundle != null && lsBundle.getState() != Bundle.INSTALLED;
	}

	@SuppressWarnings("unchecked")
	private SourceViewerConfiguration createViewerConfig(EmbeddedEditor editor, IPreferenceStore prefStore,
			IJavaProject jp) {
		if (isLanguageServerEnabled()) {
			return new ExtensionBasedTextViewerConfiguration(editor, prefStore);
		} else if (isLegacyPropertyEditorEnabled()) {
			try {
				// Avoid depending on the legacy boot properties editor plug-in directly
				Class<?> klass = Platform.getBundle(LEGACY_BOOT_PROPS_EDITOR_PLUGIN_ID)
						.loadClass(LEGACY_PROPERTIES_EDITOR_VIEWER_CONFIG_CLASS_NAME);
				Constructor<SourceViewerConfiguration> constructor = (Constructor<SourceViewerConfiguration>) klass
						.getConstructor(IColorManager.class, IPreferenceStore.class, ITextEditor.class, String.class,
								Supplier.class);
				Supplier<IJavaProject> jpSupplier = () -> jp;
				return constructor.newInstance(JavaPlugin.getDefault().getJavaTextTools().getColorManager(), prefStore,
						editor, IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING, jpSupplier);
			} catch (Exception e) {
				Log.log(e);
			}
		}
		// If all attempts fail create generic editor viewer configuration
		return new ExtensionBasedTextViewerConfiguration(editor, prefStore);
	}

	@Override
	public void createContents(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Override properties:");

		project.addListener((exp, p) -> {

			String content = getPropertiesDocContent();

			disposeEmbeddedEditor();

			IJavaProject jp = JavaCore.create(project.getValue());

			if (jp != null) {
				try {
					IFile newPropertiesFile = generateTempApplicationPropertiesFile(p, content);

					if (!newPropertiesFile.equals(propertiesFile)) {
						FileEditorInput editorInput = new FileEditorInput(newPropertiesFile);
						IPreferenceStore preferenceStore = JavaPlugin.getDefault().getCombinedPreferenceStore();
						embeddedEditor = new EmbeddedEditor((editor) -> createViewerConfig(editor, preferenceStore, jp),
								preferenceStore);
						embeddedEditor.init(null, editorInput);
						Control editorControl = embeddedEditor.createControl(parent);
						embeddedEditor.addPropertyListener(editorDirtyStateListener);
						GridDataFactory.fillDefaults().grab(true, true).applyTo(editorControl);
						watchEditorErrors();
						propertiesFile = newPropertiesFile;
					}

				} catch (Exception e) {
					Log.log(e);
				}


			} else {
				dirtyState.setValue(false);
			}

			parent.layout(true);
		});

	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		try {
			props = BootLaunchConfigurationDelegate.getRawApplicationProperties(conf);
			if (propertiesFile != null) {
				if (embeddedEditor != null) {
					embeddedEditor.getViewer().getDocument().set(props);
					embeddedEditor.doSave(new NullProgressMonitor());
					embeddedEditor.getViewer().getUndoManager().reset();
				} else {
					propertiesFile.setContents(new ByteArrayInputStream(props.getBytes()), true, false, new NullProgressMonitor());
				}
			}
			dirtyState.setValue(false);
		} catch (Exception e) {
			Log.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		if (embeddedEditor != null) {
			String content = embeddedEditor.getViewer().getDocument().get();
			BootLaunchConfigurationDelegate.setRawApplicationProperties(conf, content);
		}
		dirtyState.setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		BootLaunchConfigurationDelegate.clearProperties(conf);
	}

	private IFile generateTempApplicationPropertiesFile(IProject project, String content) throws CoreException {
		IFolder folder = project.getFolder(".settings");
		IFile propertiesFile = folder.getFile("application.properties");
		if (propertiesFile.exists()) {
			propertiesFile.setContents(new ByteArrayInputStream(content.getBytes()), true, false, new NullProgressMonitor());
		} else {
			propertiesFile.create(new ByteArrayInputStream(content.getBytes()), true, new NullProgressMonitor());
		}
		return propertiesFile;
	}

	private String getPropertiesDocContent() {
		return embeddedEditor == null ? props : embeddedEditor.getViewer().getDocument().get();
	}

	private void watchEditorErrors() {
		if (embeddedEditor != null) {
			IAnnotationModel annotationModel = embeddedEditor.getViewer().getAnnotationModel();
			if (annotationModel != null) {
				annotationModel.addAnnotationModelListener(annotationModelListener);
			}
		}
	}

	private void unwatchEditorErrors() {
		if (embeddedEditor != null) {
			IAnnotationModel annotationModel = embeddedEditor.getViewer().getAnnotationModel();
			if (annotationModel != null) {
				annotationModel.addAnnotationModelListener(annotationModelListener);
			}
		}
	}

	private void disposeTempApplicationPropertiesFile() {
		if (propertiesFile != null && propertiesFile.exists()) {
			try {
				propertiesFile.delete(true, new NullProgressMonitor());
				propertiesFile = null;
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private void disposeEmbeddedEditor() {
		unwatchEditorErrors();
		if (embeddedEditor != null) {
			embeddedEditor.removePropertyListener(editorDirtyStateListener);
			embeddedEditor.dispose();
			embeddedEditor = null;
			validator.setValue(ValidationResult.OK);
		}
		disposeTempApplicationPropertiesFile();
	}

	@Override
	public void dispose() {
		disposeEmbeddedEditor();
		super.dispose();
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

}
