/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.boot.ide.properties.servers;

import org.dadacoalition.yedit.editor.ColorManager;
import org.dadacoalition.yedit.editor.YEditDamageRepairer;
import org.dadacoalition.yedit.editor.scanner.YAMLScanner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

/**
 * @author Martin Lippert
 */
public class SpringYMLPresentationReconciler extends PresentationReconciler {
	
	public SpringYMLPresentationReconciler() {
		
		ColorManager colorManager = new ColorManager();
		ITokenScanner yamlScanner = new YAMLScanner( colorManager );
		DefaultDamagerRepairer dr = new YEditDamageRepairer(yamlScanner);

		this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);			
	}

}
