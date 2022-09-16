package org.springframework.tooling.boot.ls;

import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.tooling.boot.ls.prefs.FileListEditor;

public class RewritePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BootLanguageServerPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	protected void createFieldEditors() {
		Composite fieldEditorParent = getFieldEditorParent();
		
		addField(new BooleanFieldEditor(Constants.PREF_REWRITE_RECONCILE, "Experimental reconciling for Java source based on Rewrite project", fieldEditorParent));
		
		addField(new FileListEditor(Constants.PREF_REWRITE_RECIPES_SCAN_FILES, "JAR and YAML files to scan for Recipes", "Select JARs and YAML files:", fieldEditorParent, List.of("jar", "yml", "yaml")));
		
		addField(new PathEditor(Constants.PREF_REWRITE_RECIPES_SCAN_DIRS, "Directories to scan for Recipes", "Select directory to scan for Recipes", fieldEditorParent));

	}


}
