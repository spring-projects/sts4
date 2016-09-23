///*******************************************************************************
// * Copyright (c) 2015 Pivotal, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     Pivotal, Inc. - initial API and implementation
// *******************************************************************************/
//package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;
//
//import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
//import org.springframework.ide.eclipse.editor.support.util.ShellProviders;
//import org.springframework.ide.eclipse.editor.support.yaml.AbstractYamlEditor;
//
//
//public class ManifestYamlEditor extends AbstractYamlEditor {
//
//	@Override
//	protected YEditSourceViewerConfiguration createSourceViewerConfiguration() {
//		return new ManifestYamlSourceViewerConfiguration(ShellProviders.from(this));
//	}
//
//	@Override
//	protected void initializeEditor() {
//		super.initializeEditor();
////		SpringPropertiesEditorPlugin.getIndexManager().addListener(this);
////		SpringPropertiesEditorPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
//	}
//
////	@Override
////	public void changed(SpringPropertiesIndexManager info) {
////		if (sourceViewerConf!=null) {
////			sourceViewerConf.forceReconcile();
////		}
////	}
//
//	@Override
//	public void dispose() {
//		super.dispose();
////		SpringPropertiesEditorPlugin.getIndexManager().removeListener(this);
////		SpringPropertiesEditorPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
//	}
//
////	@Override
////	public void propertyChange(PropertyChangeEvent event) {
////		if (event.getProperty().startsWith(ProblemSeverityPreferencesUtil.PREFERENCE_PREFIX)) {
////			if (sourceViewerConf!=null) {
////				sourceViewerConf.forceReconcile();
////			}
////		}
////	}
//}
