/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.IElementStateListenerExtension;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.springsource.ide.eclipse.commons.internal.ui.UiPlugin;

/**
 * Embedded editor that can be nested inside SWT controls such as dialogs
 *
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class EmbeddedEditor implements ITextEditor {

	@FunctionalInterface
	public interface ViewerConfigurationFactory {
		SourceViewerConfiguration create(EmbeddedEditor editor);
	}

	private static final NullEditorInput NULL_EDITOR_INPUT = new NullEditorInput();

	private class EditorActionHandler extends AbstractHandler {

		private final String actionId;
		private final int operationId;

		public EditorActionHandler(String actionId, int operationId) {
			super();
			this.actionId = actionId;
			this.operationId = operationId;
		}

		public String getActionId() {
			return actionId;
		}

		@Override
		public Object execute(ExecutionEvent arg0) throws ExecutionException {
			viewer.doOperation(operationId);
			return null;
		}
	}

	private final EditorActionHandler[] handlers = new EditorActionHandler[] {
			new EditorActionHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS,
					SourceViewer.CONTENTASSIST_PROPOSALS),
			new EditorActionHandler(IWorkbenchCommandConstants.EDIT_UNDO, SourceViewer.UNDO),
			new EditorActionHandler(IWorkbenchCommandConstants.EDIT_REDO, SourceViewer.REDO)
	};

	private final boolean withHandlers;

	private final boolean withDecorations;

	private final ViewerConfigurationFactory viewerConfigFactory;

	private ProjectionViewer viewer;

	private SourceViewerDecorationSupport decorationSupport;

	private final List<IHandlerActivation> activations = new ArrayList<>();

	private final IPreferenceStore prefStore;

	private IEditorInput editorInput;

	private IDocumentProvider docProvider;

	private IVerticalRuler verticalRuler;

	private final ListenerList<IPropertyListener> propertyChangeListeners = new ListenerList<>();

	private final IElementStateListener elementStateListener = new ElementStateListener();

	private String context;

	private DefaultMarkerAnnotationAccess fileMarkerAnnotationAccess;

	public EmbeddedEditor(ViewerConfigurationFactory viewerConfigFactory, IPreferenceStore prefStore, boolean withHandlers, boolean withDecorations) {
		this.viewerConfigFactory = viewerConfigFactory;
		this.prefStore = prefStore;
		this.withHandlers = withHandlers;
		this.withDecorations = withDecorations;
	}

	public EmbeddedEditor(ViewerConfigurationFactory viewerConfigFactory, IPreferenceStore prefStore) {
		this(viewerConfigFactory, prefStore, true, true);
	}

	public Control createControl(Composite parent) throws CoreException {
		verticalRuler = new CompositeRuler();
		fileMarkerAnnotationAccess = new DefaultMarkerAnnotationAccess();
		OverviewRuler overviewRuler = new OverviewRuler(fileMarkerAnnotationAccess, 10, getSharedColors());
		viewer = new ProjectionViewer(parent, verticalRuler, overviewRuler, true,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);

		viewer.configure(viewerConfigFactory.create(this));

		if (withDecorations) {
			decorationSupport = new SourceViewerDecorationSupport(viewer, overviewRuler,
					fileMarkerAnnotationAccess, getSharedColors());

			for (AnnotationPreference preference : new MarkerAnnotationPreferences()
					.getAnnotationPreferences()) {
				decorationSupport.setAnnotationPreference(preference);
			}
			decorationSupport.install(prefStore);
		}

		if (withHandlers) {
			activateHandlers();
		}

		initViewer();

		return viewer.getControl();
	}

	public ProjectionViewer getViewer() {
		return viewer;
	}

	public DefaultMarkerAnnotationAccess getAnnotationAccess() {
		return fileMarkerAnnotationAccess;
	}

	public ISharedTextColors getSharedColors() {
		return EditorsPlugin.getDefault().getSharedTextColors();
	}

	private void activateHandlers() {
		IHandlerService service = PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
		if (service != null) {
			for (EditorActionHandler handler : handlers) {
				activations.add(service.activateHandler(handler.getActionId(), handler));
			}
		}
	}

	private void deactivateHandlers() {
		IHandlerService service = PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
		if (service != null && activations != null) {
			for (IHandlerActivation activation : activations) {
				service.deactivateHandler(activation);
			}
			activations.clear();
		}
	}

	@Override
	public IEditorInput getEditorInput() {
		return editorInput == null ? NULL_EDITOR_INPUT : editorInput;
	}

	@Override
	public IEditorSite getEditorSite() {
		return null;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		try {
			setInput(input);
		} catch (CoreException e) {
			throw new PartInitException(e.getStatus());
		}
	}

	public void setInput(IEditorInput editorInput) throws CoreException {
		if (editorInput == null) {
			editorInput = NULL_EDITOR_INPUT;
		}
		IEditorInput oldInput = this.editorInput;
		if (oldInput != null) {
			getDocumentProvider().disconnect(oldInput);
		}

		this.editorInput = editorInput;

		updateDocumentProvider(editorInput);

		IDocumentProvider provider = getDocumentProvider();
		if (provider == null) {
			IStatus s = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK,
					"No document provider for input " + editorInput, null);
			throw new CoreException(s);
		}

		provider.connect(editorInput);

		if (viewer != null) {
			initViewer();
		}

		firePropertyChange(IEditorPart.PROP_INPUT);
	}

	private void initViewer() {
		IDocumentProvider provider = getDocumentProvider();
		IDocument document = provider.getDocument(editorInput);
		IAnnotationModel annotationModel = provider.getAnnotationModel(editorInput);
		viewer.setDocument(document, annotationModel);
		viewer.setEditable(this.isEditable());
	}

	private void updateDocumentProvider(IEditorInput editorInput) {

		if (docProvider != null) {
			docProvider.removeElementStateListener(elementStateListener);
		}

		docProvider = editorInput == NULL_EDITOR_INPUT ? NULL_DOCUMENT_PROVIDER : DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);

		if (docProvider != null) {
			docProvider.addElementStateListener(elementStateListener);
		}

	}

	private void firePropertyChange(int propId) {
		propertyChangeListeners.forEach(l -> l.propertyChanged(this, propId));
	}

	@Override
	public void addPropertyListener(IPropertyListener listener) {
		propertyChangeListeners.add(listener);
	}

	@Override
	public void createPartControl(Composite parent) {
	}

	@Override
	public void dispose() {
		if (withHandlers) {
			deactivateHandlers();
		}
		if (editorInput != null) {
			getDocumentProvider().disconnect(editorInput);
			editorInput = null;
		}
		if (decorationSupport != null) {
			decorationSupport.dispose();
		}
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getControl().dispose();
		}
	}

	@Override
	public IWorkbenchPartSite getSite() {
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public Image getTitleImage() {
		return null;
	}

	@Override
	public String getTitleToolTip() {
		return null;
	}

	@Override
	public void removePropertyListener(IPropertyListener listener) {
		propertyChangeListeners.remove(listener);
	}

	@Override
	public void setFocus() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> required) {

		if (IVerticalRulerInfo.class.equals(required)) {
			if (verticalRuler != null) {
				return (T) verticalRuler;
			}
		}

		if (ITextOperationTarget.class.equals(required)) {
			return (viewer == null ? null : (T) viewer.getTextOperationTarget());
		}

		if (IRewriteTarget.class.equals(required)) {
			if (viewer instanceof ITextViewerExtension) {
				ITextViewerExtension extension= viewer;
				return (T) extension.getRewriteTarget();
			}
			return null;
		}

		if (Control.class.equals(required)) {
			return viewer != null ? (T) viewer.getTextWidget() : null;
		}

		if (ITextViewer.class.isAssignableFrom(required)) {
			return (viewer == null ? null : (T) viewer);
		}

		return null;
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		IDocumentProvider p= getDocumentProvider();
		if (p == null) {
			return;
		}

		if (p.isDeleted(getEditorInput())) {

			if (isSaveAsAllowed()) {
				// No Save As
			}

		} else {
			IDocumentProvider provider= getDocumentProvider();
			if (provider == null) {
				return;
			}

			try {
				provider.aboutToChange(getEditorInput());
				IEditorInput input= getEditorInput();
				provider.saveDocument(progressMonitor, input, getDocumentProvider().getDocument(input), false);

			} catch (CoreException e) {
				UiPlugin.log(e);
			} finally {
				provider.changed(getEditorInput());
			}
		}
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		IDocumentProvider p= getDocumentProvider();
		return p == null ? false : p.canSaveDocument(editorInput);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	@Override
	public IDocumentProvider getDocumentProvider() {
		return docProvider;
	}

	@Override
	public void close(boolean save) {
	}

	@Override
	public boolean isEditable() {
		return editorInput != NULL_EDITOR_INPUT;
	}

	@Override
	public void doRevertToSaved() {
	}

	@Override
	public void setAction(String actionID, IAction action) {
	}

	@Override
	public IAction getAction(String actionId) {
		return null;
	}

	@Override
	public void setActionActivationCode(String actionId, char activationCharacter, int activationKeyCode,
			int activationStateMask) {
	}

	@Override
	public void removeActionActivationCode(String actionId) {
	}

	@Override
	public boolean showsHighlightRangeOnly() {
		return false;
	}

	@Override
	public void showHighlightRangeOnly(boolean showHighlightRangeOnly) {
	}

	@Override
	public void setHighlightRange(int offset, int length, boolean moveCursor) {
	}

	@Override
	public IRegion getHighlightRange() {
		return null;
	}

	@Override
	public void resetHighlightRange() {
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	@Override
	public void selectAndReveal(int offset, int length) {
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

//	public void onPreCreateControl(Runnable preCreateControlRunnable) {
//		this.preCreateControlRunnable = preCreateControlRunnable;
//	}
//
//	public void onPostCreateControl(Runnable postCreateControlRunnable) {
//		this.postCreateControlRunnable  = postCreateControlRunnable;
//	}

	private static final Document NULL_DOCUMENT = new Document("");

	private static final IDocumentProvider NULL_DOCUMENT_PROVIDER = new IDocumentProvider() {

		@Override
		public void connect(Object element) throws CoreException {
		}

		@Override
		public void disconnect(Object element) {
		}

		@Override
		public IDocument getDocument(Object element) {
			return NULL_DOCUMENT;
		}

		@Override
		public void resetDocument(Object element) throws CoreException {
		}

		@Override
		public void saveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
				throws CoreException {
		}

		@Override
		public long getModificationStamp(Object element) {
			return 0;
		}

		@Override
		public long getSynchronizationStamp(Object element) {
			return 0;
		}

		@Override
		public boolean isDeleted(Object element) {
			return false;
		}

		@Override
		public boolean mustSaveDocument(Object element) {
			return false;
		}

		@Override
		public boolean canSaveDocument(Object element) {
			return false;
		}

		@Override
		public IAnnotationModel getAnnotationModel(Object element) {
			return null;
		}

		@Override
		public void aboutToChange(Object element) {
		}

		@Override
		public void changed(Object element) {
		}

		@Override
		public void addElementStateListener(IElementStateListener listener) {
		}

		@Override
		public void removeElementStateListener(IElementStateListener listener) {
		}

	};

	class ElementStateListener implements IElementStateListener, IElementStateListenerExtension {

		@Override
		public void elementStateValidationChanged(final Object element, final boolean isStateValidated) {
		}

		@Override
		public void elementDirtyStateChanged(Object element, boolean isDirty) {
			if (element != null && element.equals(getEditorInput())) {
				Runnable r = () -> {
					firePropertyChange(PROP_DIRTY);
				};
				execute(r, false);
			}
		}

		@Override
		public void elementContentAboutToBeReplaced(Object element) {
		}

		@Override
		public void elementContentReplaced(Object element) {
			if (element != null && element.equals(getEditorInput())) {
				Runnable r = () -> {
					firePropertyChange(PROP_DIRTY);
				};
				execute(r, false);
			}
		}

		@Override
		public void elementDeleted(Object deletedElement) {
			if (deletedElement != null && deletedElement.equals(getEditorInput())) {
				Runnable r = () -> {
					try {
						setInput(NULL_EDITOR_INPUT);
					} catch (CoreException e) {
						UiPlugin.log(e);
					}
				};
				execute(r, false);
			}
		}

		@Override
		public void elementMoved(final Object originalElement, final Object movedElement) {
		}

		@Override
		public void elementStateChanging(Object element) {
		}

		@Override
		public void elementStateChangeFailed(Object element) {
		}

		private void execute(Runnable runnable, boolean postAsync) {
			if (postAsync || Display.getCurrent() == null) {
				if (viewer != null && !viewer.getControl().isDisposed()) {
					Display display = viewer.getControl().getDisplay();
					display.asyncExec(runnable);
				}
			} else {
				runnable.run();
			}
		}

	}

}
