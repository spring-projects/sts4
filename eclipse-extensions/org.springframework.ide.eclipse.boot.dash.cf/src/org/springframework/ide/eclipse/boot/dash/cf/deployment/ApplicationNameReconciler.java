/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.yaml.snakeyaml.Yaml;

/**
 * Reconciler for Application names in CF deployment manifest YAML
 *
 * @author Alex Boyko
 *
 */
public class ApplicationNameReconciler extends Reconciler {

	private AppNameReconcilingStrategy strategy;

	public ApplicationNameReconciler() {
		super();
		YamlASTProvider parser = new YamlASTProvider(new Yaml());
		strategy= new AppNameReconcilingStrategy(parser);
		this.setReconcilingStrategy(strategy, IDocument.DEFAULT_CONTENT_TYPE);
	}

	@Override
	public void install(ITextViewer textViewer) {
		super.install(textViewer);
		if (textViewer instanceof ISourceViewer) {
			strategy.install((ISourceViewer)textViewer);
		}
	}

	@Override
	public void uninstall() {
		super.uninstall();
		strategy.uninstall();
	}

}
