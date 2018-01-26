/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.java.ls;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageServerProcessStreamConnector.MissingToolsJarException;

public class MissingToolsJarWarning {

	private static boolean missingToolsJarWarningIssued = false;
	
	public static void show(MissingToolsJarException e) {
		Display.getDefault().asyncExec(() -> {
			
			//Don't warn more than once per Eclipse session.
			if (!missingToolsJarWarningIssued) {
				missingToolsJarWarningIssued = true;
				
				StringBuilder lookedIn = new StringBuilder();
				for (File file : e.lookedIn) {
					lookedIn.append("  ");
					lookedIn.append(file);
					lookedIn.append("\n");
				}
				
				MessageDialog.openWarning(null, "Missing 'tools.jar'", 
						"Could not find 'tools.jar' in the active JRE.\n\n"+
						"Spring Boot Live hovers will not work without it.\n\n" +
						"The JRE you are running Eclipse with is:\n  "+e.javaHome+"\n\n" +
						"Where we looked for 'tools.jar':\n" +
						lookedIn
				);
			}
		});
	}



}
