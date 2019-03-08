/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.schema;

import org.springframework.ide.vscode.commons.util.IntegerRange;

/**
 * A 'schema' provides a toplevel type, which dictates the valid structure of a
 * YamlDocument and a {@link YTypeUtil} which provides the means to 'interpret'
 * the types.
 *
 * @author Kris De Volder
 */
public interface YamlSchema {

	default IntegerRange expectedNumberOfDocuments() { return IntegerRange.ANY; };
	YType getTopLevelType();
	YTypeUtil getTypeUtil();
	default String getName() { return getTopLevelType().toString(); }

}
