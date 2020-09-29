// COPIED from spring-ide org.springframework.ide.eclipse.ui.SpringUIUtils
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
package org.springsource.ide.eclipse.commons.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.springsource.ide.eclipse.commons.internal.ui.UiPlugin;


/**
 * This is a collection of UI-related helper methods.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public final class SpringUIUtils {

	/**
	 * Returns the standard display to be used. The method first checks, if the
	 * thread calling this method has an associated display. If so, this display
	 * is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/**
	 * Returns a button with the given label and selection listener.
	 */
	public static Button createButton(Composite parent, String labelText, SelectionListener listener) {
		return createButton(parent, labelText, listener, 0, true);
	}

	/**
	 * Returns a button with the given label, indentation, enablement and
	 * selection listener.
	 */
	public static Button createButton(Composite parent, String labelText, SelectionListener listener, int indentation,
			boolean enabled) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(labelText);
		button.addSelectionListener(listener);
		button.setEnabled(enabled);

		FontMetrics fontMetrics = getFontMetrics(button);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
		gd.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		gd.horizontalIndent = indentation;
		button.setLayoutData(gd);
		return button;
	}

	/**
	 * Returns a check box with the given label.
	 */
	public static Button createCheckBox(Composite parent, String labelText) {
		Button button = new Button(parent, SWT.CHECK);
		button.setFont(parent.getFont());
		button.setText(labelText);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		return button;
	}

	/**
	 * Returns a text field with the given label.
	 */
	public static Text createTextField(Composite parent, String labelText) {
		return createTextField(parent, labelText, 0, 0, 0);
	}

	/**
	 * Returns a text field with the given label and horizontal indentation.
	 */
	public static Text createTextField(Composite parent, String labelText, int indentation) {
		return createTextField(parent, labelText, indentation, 0, 0);
	}

	/**
	 * Returns a text field with the given label, horizontal indentation and
	 * width hint.
	 */
	public static Text createTextField(Composite parent, String labelText, int indentation, int textWidth) {
		return createTextField(parent, labelText, indentation, textWidth, 0);
	}

	/**
	 * Returns a text field with the given label, horizontal indentation and
	 * label and text width hint.
	 */
	public static Text createTextField(Composite parent, String labelText, int indentation, int textWidth,
			int labelWidth) {
		Composite textArea = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		textArea.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		textArea.setLayoutData(gd);

		Label label = new Label(textArea, SWT.NONE);
		label.setText(labelText);
		label.setFont(parent.getFont());
		if (labelWidth > 0) {
			gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gd.widthHint = labelWidth;
			label.setLayoutData(gd);
		}

		Text text = new Text(textArea, SWT.BORDER | SWT.SINGLE);
		text.setFont(parent.getFont());
		if (textWidth > 0) {
			gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gd.widthHint = textWidth;
		}
		else {
			gd = new GridData(GridData.FILL_HORIZONTAL);
		}
		text.setLayoutData(gd);
		return text;
	}

	/**
	 * Returns the font metrics for given control.
	 */
	public static FontMetrics getFontMetrics(Control control) {
		FontMetrics fontMetrics = null;
		GC gc = new GC(control);
		try {
			gc.setFont(control.getFont());
			fontMetrics = gc.getFontMetrics();
		}
		finally {
			gc.dispose();
		}
		return fontMetrics;
	}

	/**
	 * Displays specified preferences or property page and returns
	 * <code>true</code> if <code>PreferenceDialog.OK</code> was selected.
	 */
	public static boolean showPreferenceDialog(String propertyPageId, IProject project, Map<String, Object> data) {
		PropertyPageManager pageManager = new PropertyPageManager();
		PropertyPageContributorManager.getManager().contribute(pageManager, project);
		IPreferenceNode targetNode = pageManager.find("org.springsource.ide.eclipse.commons.projectPropertyPage/org.springframework.ide.eclipse.ui.validationPropertyPage");
		if (targetNode != null) {
			return openPreferenceNode(propertyPageId, targetNode, "Properties for " + project.getName(), project, data);
		}
		return false;
	}

	private static boolean openPreferenceNode(final String propertyPageId, final IPreferenceNode targetNode,
			final String title, Object element, Map<String, Object> data) {

		PreferenceManager manager = new PreferenceManager();
		manager.addToRoot(targetNode);
		final PropertyDialog dialog = new PropertyDialog(UiPlugin.getActiveWorkbenchShell(), manager,
				new StructuredSelection(element));

		if (propertyPageId != null) {
			dialog.setSelectedNode(propertyPageId);
		}
		if (data != null) {
			dialog.setPageData(data);
		}

		final boolean[] result = new boolean[] { false };
		BusyIndicator.showWhile(getStandardDisplay(), new Runnable() {
			public void run() {
				dialog.create();
				dialog.setMessage(targetNode.getLabelText());
				dialog.getShell().setText(title);
				result[0] = (dialog.open() == Window.OK);
			}
		});
		return result[0];
	}

	public static IEditorPart getActiveEditor() {
		IWorkbenchWindow window = UiPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				return page.getActiveEditor();
			}
		}
		return null;
	}

	/**
	 * Returns the <code>ITextEditor</code> instance for given
	 * <code>IEditorPart</code> or <code>null</code> for any non text editor.
	 */
	public static ITextEditor getTextEditor(IEditorPart part) {
		if (part instanceof ITextEditor) {
			return (ITextEditor) part;
		}
		return part.getAdapter(ITextEditor.class);
	}

	public static void revealInEditor(IEditorPart editor, int line) {
		ITextEditor textEditor = getTextEditor(editor);
		if (textEditor != null && line > 0) {
			IDocumentProvider provider = textEditor.getDocumentProvider();
			IEditorInput input = editor.getEditorInput();
			try {
				provider.connect(input);
			}
			catch (CoreException e) {
				return;
			}
			IDocument document = provider.getDocument(input);
			try {
				IRegion lineRegion = document.getLineInformation(line - 1);
				textEditor.selectAndReveal(lineRegion.getOffset(), lineRegion.getLength());
			}
			catch (BadLocationException e) {
				// ignore if specified line is not available in document
			}
			provider.disconnect(input);
		}
	}

	public static IEditorPart openInEditor(IFile file, int line) {
		return openInEditor(file, line, true);
	}

	/**
	 * Opens given file in associated editor and go to specified line (if > 0).
	 */
	public static IEditorPart openInEditor(IFile file, int line, boolean activate) {
		IEditorPart editor = null;
		IWorkbenchPage page = UiPlugin.getActiveWorkbenchPage();
		try {
			if (line > 0) {
				editor = IDE.openEditor(page, file, activate);
				ITextEditor textEditor = null;
				if (editor instanceof ITextEditor) {
					textEditor = (ITextEditor) editor;
				}
				textEditor = ((IAdaptable) editor).getAdapter(ITextEditor.class);
				if (textEditor != null) {
					IDocument document = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
					try {
						int start = document.getLineOffset(line - 1);
						textEditor.selectAndReveal(start, 0);
						page.activate(editor);
					}
					catch (BadLocationException x) {
						// ignore
					}
				}
				else {
					IMarker marker = file.createMarker(IMarker.TEXT);
					marker.setAttribute(IMarker.LINE_NUMBER, line);
					editor = IDE.openEditor(page, marker, activate);
					marker.delete();
				}
			}
			else {
				editor = IDE.openEditor(page, file, activate);
			}
		}
		catch (CoreException e) {
			openError("Open Editor Problems", e.getMessage(), e);
		}
		return editor;
	}

	public static IEditorPart openInEditor(IEditorInput input, String editorId) {
		return openInEditor(input, editorId, true);
	}

	public static IEditorPart openInEditor(IEditorInput input, String editorId, boolean activate) {
		IWorkbenchPage page = UiPlugin.getActiveWorkbenchPage();
		try {
			return page.openEditor(input, editorId, activate);
		}
		catch (PartInitException e) {
			openError("Open Editor Problems", e.getMessage(), e);
		}
		return null;
	}

	public static IEditorPart openInEditor(IJavaElement element) {
		try {
			IEditorPart editor = JavaUI.openInEditor(element);
			if (editor != null) {
				JavaUI.revealInEditor(editor, element);
			}
			return editor;
		}
		catch (PartInitException e) {
			openError("Open Editor Problems", e.getMessage(), e);
		}
		catch (JavaModelException e) {
			openError("Open Editor Problems", e.getMessage(), e);
		}
		return null;
	}

	public static int getCaretOffset(ITextEditor editor) {
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			return ((ITextSelection) selection).getOffset();
		}
		return -1;
	}

	public static String getSelectedText(ITextEditor editor) {
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			return ((ITextSelection) selection).getText().trim();
		}
		return null;
	}

	public static IProgressMonitor getStatusLineProgressMonitor() {
		IWorkbenchPage wbPage = UiPlugin.getActiveWorkbenchPage();
		if (wbPage != null) {
			IEditorPart editor = wbPage.getActiveEditor();
			if (editor != null) {
				IActionBars bars = editor.getEditorSite().getActionBars();
				return bars.getStatusLineManager().getProgressMonitor();
			}
		}
		return null;
	}

	/**
	 * Open an error style dialog for a given <code>CoreException</code> by
	 * including any extra information from a nested <code>CoreException</code>.
	 */
	public static void openError(String title, String message, CoreException exception) {
		Shell shell = UiPlugin.getActiveWorkbenchShell();

		// Check for a nested CoreException
		CoreException nestedException = null;
		IStatus status = exception.getStatus();
		if (status != null && status.getException() instanceof CoreException) {
			nestedException = (CoreException) status.getException();
		}
		if (nestedException != null) {
			// Open an error dialog and include the extra
			// status information from the nested CoreException
			ErrorDialog.openError(shell, title, message, nestedException.getStatus());
		}
		else {
			// Open a regular error dialog since there is no
			// extra information to display
			MessageDialog.openError(shell, title, message);
		}
	}

	public static IFile getFile(IStructuredDocument document) {
		if (document != null) {
			try {
				IStructuredModel model = StructuredModelManager.getModelManager().getModelForRead(document);
				IFile resource = null;
				try {
					String baselocation = model.getBaseLocation();
					if (baselocation != null) {
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						IPath filePath = new Path(baselocation);
						if (filePath.segmentCount() > 0) {
							resource = root.getFile(filePath);
						}
					}
				}
				finally {
					if (model != null) {
						model.releaseFromRead();
					}
				}
				return resource;
			}
			catch (Exception e) {
				// Sometime WTP fails for no good reason
			}
		}
		// fall back
		IEditorPart editor = SpringUIUtils.getActiveEditor();
		if (editor != null && editor.getEditorInput() instanceof IFileEditorInput) {
			return ((IFileEditorInput) editor.getEditorInput()).getFile();
		}
		return null;
	}

	public static void openUrl(String location) {
		openUrl(location, 0);
	}

	private static void openUrl(String location, int customFlags) {
		try {
			URL url = null;

			if (location != null) {
				url = new URL(location);
			}
			if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL) {
				try {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					support.getExternalBrowser().openURL(url);
				}
				catch (Exception e) {
				}
			}
			else {
				IWebBrowser browser = null;
				int flags = customFlags;
				if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
					flags |= IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}
				else {
					flags |= IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				String generatedId = UiPlugin.PLUGIN_ID + "-" + Calendar.getInstance().getTimeInMillis();
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags, generatedId, null, null);
				browser.openURL(url);
			}
		}
		catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Browser init error",
					"Browser could not be initiated");
		}
		catch (MalformedURLException e) {
		}
	}

}
