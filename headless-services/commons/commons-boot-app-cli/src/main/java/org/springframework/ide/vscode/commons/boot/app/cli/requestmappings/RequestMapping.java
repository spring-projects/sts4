/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli.requestmappings;

import java.util.Set;

public interface RequestMapping {
	// String getPath(); commented... because... not used??
	String[] getSplitPath();
	String getFullyQualifiedClassName();
	String getMethodName();
	String[] getMethodParameters();
	String getMethodString();
	Set<String> getRequestMethods();
}
