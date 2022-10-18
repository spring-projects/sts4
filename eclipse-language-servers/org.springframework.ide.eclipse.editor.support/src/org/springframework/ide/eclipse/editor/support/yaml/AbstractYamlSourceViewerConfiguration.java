/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Provider;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.dadacoalition.yedit.template.YEditCompletionProcessor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.editor.support.EditorSupportActivator;
import org.springframework.ide.eclipse.editor.support.ForceableReconciler;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.completions.ProposalProcessor;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoTextHover;
import org.springframework.ide.eclipse.editor.support.reconcile.DefaultQuickfixContext;
import org.springframework.ide.eclipse.editor.support.reconcile.QuickfixContext;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblemAnnotationHover;
import org.springframework.ide.eclipse.editor.support.util.DefaultUserInteractions;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * @author Kris De Volder
 */
public abstract class AbstractYamlSourceViewerConfiguration extends YEditSourceViewerConfiguration {

	private static final Set<String> ANNOTIONS_SHOWN_IN_TEXT = new HashSet<>();
	static {
		ANNOTIONS_SHOWN_IN_TEXT.add("org.eclipse.jdt.ui.warning");
		ANNOTIONS_SHOWN_IN_TEXT.add("org.eclipse.jdt.ui.error");
	}
	private static final Set<String> ANNOTIONS_SHOWN_IN_OVERVIEW_BAR = ANNOTIONS_SHOWN_IN_TEXT;

	//TODO: the ANNOTIONS_SHOWN_IN_TEXT and ANNOTIONS_SHOWN_IN_OVERVIEW_BAR should be replaced with
	// properly using preferences. An example of how to set this up can be found in the code
	// of the Java properties file editor. Roughly these things need to happen:
	//   1) use methods like 'isShownIntext' and 'isShownInOverviewRuler' which are defined in
	//     our super class.
	//   2) initialize the super class with a preference store (simialr to how java properties file does it)
	//   3) To be able to do 2) it is necessary to add a constructor to YEditSourceViewerConfiguration which
	//      accepts preference store and passes it to its super class. So this requires a patch to
	//      YEdit source code.



	private Provider<Shell> shellProvider;
	private final String DIALOG_SETTINGS_KEY = this.getClass().getName();
	private final YamlASTProvider astProvider = new YamlASTProvider(new Yaml(new SafeConstructor()));
	private YamlCompletionEngine completionEngine;
	protected ForceableReconciler fReconciler;

	public AbstractYamlSourceViewerConfiguration(Provider<Shell> shellProvider) {
		this.shellProvider = shellProvider;
	}

	protected final IDialogSettings getDialogSettings() {
		IDialogSettings dialogSettings = getPluginDialogSettings();
		IDialogSettings existing = dialogSettings.getSection(DIALOG_SETTINGS_KEY);
		if (existing!=null) {
			return existing;
		}
		IDialogSettings created = dialogSettings.addNewSection(DIALOG_SETTINGS_KEY);
		Point defaultPopupSize = getDefaultPopupSize();
		if (defaultPopupSize!=null) {
			int suggestW = defaultPopupSize.x;
			int suggestH = defaultPopupSize.y;
			created.put(ContentAssistant.STORE_SIZE_X, suggestW);
			created.put(ContentAssistant.STORE_SIZE_Y, suggestH);
		}
		return created;
	}

	protected Point getDefaultPopupSize() {
		return null;
	}

	protected abstract IDialogSettings getPluginDialogSettings();

	@Override
	public final IContentAssistant getContentAssistant(ISourceViewer viewer) {
		IContentAssistant _a = super_getContentAssistant(viewer);

		if (_a instanceof ContentAssistant) {
			ContentAssistant a = (ContentAssistant)_a;
			//IContentAssistProcessor processor = assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
			//if (processor!=null) {
			//TODO: don't overwrite existing processor but wrap it so
			// we combine our proposals with existing propopals
			//}

		    a.setInformationControlCreator(getInformationControlCreator(viewer));
		    a.enableColoredLabels(true);
		    a.enablePrefixCompletion(false);
		    a.enableAutoInsert(true);
		    a.enableAutoActivation(true);
			a.setRestoreCompletionProposalSize(getDialogSettings());
			ProposalProcessor processor = new ProposalProcessor(getCompletionEngine(viewer));
			a.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
			a.setSorter(CompletionFactory.SORTER);
		}
		return _a;
	}

	private IContentAssistant super_getContentAssistant(ISourceViewer sourceViewer) {
		//Copied from superclass's getContentAssistant... then modifed to make ContentAssistant
		// asynchronous.
		ContentAssistant ca;
		try {
			//Use reflection to call the constructor because it only exists in Eclipse 4.7.
			Constructor<ContentAssistant> constructor = ContentAssistant.class.getConstructor(boolean.class);
			ca = constructor.newInstance(true);
		} catch (Exception e) {
			ca = new ContentAssistant();
		}

		IContentAssistProcessor cap = new YEditCompletionProcessor();
		ca.setContentAssistProcessor(cap, IDocument.DEFAULT_CONTENT_TYPE);
		ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		ca.enableAutoInsert(true);

		return ca;
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE) && ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK==stateMask) {
			ITextHover delegate = getTextAnnotationHover(sourceViewer);
			try {
				HoverInfoProvider hoverProvider = getHoverProvider(sourceViewer);
				if (hoverProvider!=null) {
					return new HoverInfoTextHover(sourceViewer, getHoverProvider(sourceViewer), delegate);
				}
			} catch (Exception e) {
				EditorSupportActivator.log(e);
			}
			return delegate;
		} else {
			return super.getTextHover(sourceViewer, contentType, stateMask);
		}
	}

	public ICompletionEngine getCompletionEngine(ISourceViewer viewer) {
		if (completionEngine==null) {
			completionEngine = new YamlCompletionEngine(getStructureProvider(), getAssistContextProvider(viewer));
		}
		return completionEngine;
	}

	protected HoverInfoProvider getHoverProvider(ISourceViewer viewer) {
		return new YamlHoverInfoProvider(getAstProvider(), getStructureProvider(), getAssistContextProvider(viewer));
	}

	protected final YamlASTProvider getAstProvider() {
		return astProvider;
	}

	@Override
	public final IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fReconciler==null) {
			fReconciler = createReconciler(sourceViewer);
		}
		return fReconciler;
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover() {
			@Override
			protected boolean isIncluded(Annotation annotation) {
				return ANNOTIONS_SHOWN_IN_OVERVIEW_BAR.contains(annotation.getType());
			}
		};
	}

	protected ITextHover getTextAnnotationHover(ISourceViewer sourceViewer) {
		return new ReconcileProblemAnnotationHover(sourceViewer, getQuickfixContext(sourceViewer));
	}

	protected Shell getShell() {
		return shellProvider.get();
	}

	protected final QuickfixContext getQuickfixContext(ISourceViewer sourceViewer) {
		return new DefaultQuickfixContext(
				getPluginId(),
				getPreferencesStore(),
				sourceViewer,
				new DefaultUserInteractions(getShell())
		);
	}

	protected abstract String getPluginId();
	protected abstract IPreferenceStore getPreferencesStore();
	protected abstract YamlStructureProvider getStructureProvider();
	protected abstract YamlAssistContextProvider getAssistContextProvider(ISourceViewer viewer);

	protected IReconcilingStrategy createReconcilerStrategy(ISourceViewer sourceViewer) {
		return null;
	}

	protected ForceableReconciler createReconciler(ISourceViewer sourceViewer) {
		//TODO: aplication.properties|yaml editors are overriding this. That should not be necessary.
		IReconcilingStrategy strategy = createReconcilerStrategy(sourceViewer);
		if (strategy!=null) {
			ForceableReconciler reconciler = new ForceableReconciler(strategy);
			reconciler.setDelay(500);
			return reconciler;
		}
		return null;
	}
}
