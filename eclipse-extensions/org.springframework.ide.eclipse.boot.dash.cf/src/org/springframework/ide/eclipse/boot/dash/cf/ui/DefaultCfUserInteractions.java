package org.springframework.ide.eclipse.boot.dash.cf.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudData;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.ManifestDiffDialog;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.YamlGraphDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.CustomizeAppsManagerURLDialog;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.CustomizeAppsManagerURLDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialog;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.DeploymentPropertiesDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.UpdatePasswordDialog;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlFileInput;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlInput;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel.Result;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.UIContext;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class DefaultCfUserInteractions implements CfUserInteractions {

	private final SimpleDIContext context;

	public DefaultCfUserInteractions(SimpleDIContext context) {
		this.context = context;
	}

	private Shell getShell() {
		return context.getBean(UIContext.class).getShell();
	}

	@Override
	public void openPasswordDialog(PasswordDialogModel model) {
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				new UpdatePasswordDialog(getShell(), model).open();
			}
		});
	}

	@Override
	public void openEditAppsManagerURLDialog(CustomizeAppsManagerURLDialogModel model) {
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				new CustomizeAppsManagerURLDialog(model, getShell()).open();
			}
		});
	}

	@Override
	public CloudApplicationDeploymentProperties promptApplicationDeploymentProperties(DeploymentPropertiesDialogModel model)
			throws Exception {
		final Shell shell = getShell();

		if (shell != null) {
			model.initFileModel();
			model.initManualModel();
			shell.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					new DeploymentPropertiesDialog(shell, model).open();
				}
			});
		}

		return model.getDeploymentProperties();
	}

	@Override
	public ManifestDiffDialogModel.Result confirmReplaceApp(String title, CloudData cloudData, IFile manifestFile, CloudApplicationDeploymentProperties deploymentProperties) throws  Exception {
		final Exception[] error = new Exception[1];
		final Result[] result = new Result[1];
		getShell().getDisplay().syncExec(() -> {
			try {
				result[0] = confirmReplaceApp(title, cloudData, manifestFile, deploymentProperties, new NullProgressMonitor());
			} catch (Exception e) {
				error[0] = e;
			}
		});

		if (error[0] != null) {
			throw error[0];
		}
		return result[0];
	}

	private ManifestDiffDialogModel.Result confirmReplaceApp(String title, CloudData cloudData, IFile manifestFile, CloudApplicationDeploymentProperties existingAppDeploymentProperties, IProgressMonitor monitor) throws Exception {


		Result result = confirmReplaceAppWithManifest(title, cloudData, manifestFile,
				existingAppDeploymentProperties, monitor);
		if (result == null) {
			String message = "Replace content of the existing Cloud application? Existing deployment properties including bound services will be retained.";
			if (MessageDialog.openConfirm(getShell(), title, message)) {
				// Not ideal, but using "Forget Manifest" to indicate to use existing Cloud Foundry app deployment properties
				return Result.FORGET_MANIFEST;
			} else {
				return Result.CANCELED;
			}
		} else {
			return result;
		}
	}

	private ManifestDiffDialogModel.Result confirmReplaceAppWithManifest(String title, CloudData cloudData, IFile manifestFile, CloudApplicationDeploymentProperties existingAppDeploymentProperties, IProgressMonitor monitor) throws Exception {

		if (manifestFile != null && manifestFile.isAccessible()) {


			String yamlContents = IOUtil.toString(manifestFile.getContents());

			YamlGraphDeploymentProperties newDeploymentProperties = new YamlGraphDeploymentProperties(yamlContents, existingAppDeploymentProperties.getAppName(), cloudData);
			TextEdit edit = null;
			String errorMessage = null;
			try {
				MultiTextEdit me = newDeploymentProperties.getDifferences(existingAppDeploymentProperties);
				edit = me != null && me.hasChildren() ? me : null;
			} catch (MalformedTreeException e) {
				errorMessage  = "Failed to create text differences between local manifest file and deployment properties on CF.";
			} catch (Throwable t) {
				errorMessage = "Failed to parse local manifest file YAML contents.";
			}
			if (errorMessage != null) {
				throw ExceptionUtil.coreException(errorMessage);
			}

			if (edit != null) {
				final IDocument doc = new Document(yamlContents);
				edit.apply(doc);

				final YamlFileInput left = new YamlFileInput(manifestFile,
						BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CLOUD_ICON));
				final YamlInput right = new YamlInput("Existing application in Cloud Foundry",
						BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CLOUD_ICON),
						doc.get());

				CompareConfiguration config = new CompareConfiguration();
				config.setLeftLabel(left.getName());
				config.setLeftImage(left.getImage());
				config.setRightLabel(right.getName());
				config.setRightImage(right.getImage());

				final CompareEditorInput input = new CompareEditorInput(config) {
					@Override
					protected Object prepareInput(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						if (hasDeletedService(newDeploymentProperties, existingAppDeploymentProperties)) {
							setMessage("WARNING: If using the manifest file, existing service bindings for the application that are not listed in the manifest will be removed.");
						}
						return new DiffNode(left, right);
					}

				};
				input.setTitle("Replacing existing application");

				input.run(monitor);

				// TODO: At the moment, this dialogue offers limited functionality to just compare manifest with existing app. Eventually
				// we want to have a "full feature" compare editor that allows users to forget manifest
				// or make changes
				config.setLeftEditable(false);

				ManifestDiffDialogModel model = new ManifestDiffDialogModel(input);

				int val = new ManifestDiffDialog(getShell(), model, title).open();
				return ManifestDiffDialog.getResultForCode(val);
			}
		}
		return null;
	}

	private boolean hasDeletedService(YamlGraphDeploymentProperties newDeployment, CloudApplicationDeploymentProperties oldDeployment) {
		return !newDeployment.getServices().containsAll(oldDeployment.getServices());
	}

	@Override
	public Result openManifestDiffDialog(ManifestDiffDialogModel model) throws CoreException {
		LiveVariable<Integer> resultCode = new LiveVariable<>();
		LiveVariable<Throwable> error = new LiveVariable<>();
		getShell().getDisplay().syncExec(() -> {
			try {
				resultCode.setValue(new ManifestDiffDialog(getShell(), model).open());
			} catch (Exception e) {
				error.setValue(e);
			}
		});
		if (error.getValue()!=null) {
			throw ExceptionUtil.coreException(error.getValue());
		} else {
			return ManifestDiffDialog.getResultForCode(resultCode.getValue());
		}
	}


}
