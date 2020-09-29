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
package org.springsource.ide.eclipse.commons.frameworks.test.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * An SWTBot condition that checks whether a given IResource exists in the workspace.
 * <p> 
 * A Grails create-app command is spawned by a create grails project wizard.
 * We wait for this condition to ensure grails command that creates the
 * project is done.
 * @author Kris De Volder
 */
public class ResourceExists extends DefaultCondition {
	
	private IResource resource;

	/**
	 * Create an SWTBot condition that checks whether the resource
	 * pointed at by a given path String exists.
	 */
	public ResourceExists(String path) {
		this.resource = StsTestUtil.getResource(path);
	}

	public boolean test() throws Exception {
		try {
			boolean exists = resource.exists();
//			System.out.println("Resource '"+resource+"' exists? " + exists);
			return exists;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getFailureMessage() {
		return "The file '"+resource+"' does not exist.";
	}
}
