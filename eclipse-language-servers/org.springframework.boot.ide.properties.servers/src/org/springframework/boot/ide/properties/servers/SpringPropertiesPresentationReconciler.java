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

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertyValueScanner;
import org.eclipse.jdt.internal.ui.text.SingleTokenJavaScanner;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringPropertiesPresentationReconciler extends PresentationReconciler {
	
	public SpringPropertiesPresentationReconciler() {
		
		IColorManager colorManager = JavaPlugin.getDefault().getJavaTextTools().getColorManager();
		IPreferenceStore store= JavaPlugin.getDefault().getCombinedPreferenceStore();
		
		ITokenScanner propertyKeyScanner= new SingleTokenJavaScanner(colorManager, store, PreferenceConstants.PROPERTIES_FILE_COLORING_KEY);
		ITokenScanner propertyValueScanner= new PropertyValueScanner(colorManager, store);
		ITokenScanner propertyCommentScanner= new SingleTokenJavaScanner(colorManager, store, PreferenceConstants.PROPERTIES_FILE_COLORING_COMMENT);

		this.setDocumentPartitioning(IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING);

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(propertyKeyScanner);
		this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr= new DefaultDamagerRepairer(propertyCommentScanner);
		this.setDamager(dr, IPropertiesFilePartitions.COMMENT);
		this.setRepairer(dr, IPropertiesFilePartitions.COMMENT);

		dr= new DefaultDamagerRepairer(propertyValueScanner);
		this.setDamager(dr, IPropertiesFilePartitions.PROPERTY_VALUE);
		this.setRepairer(dr, IPropertiesFilePartitions.PROPERTY_VALUE);

	}

}
