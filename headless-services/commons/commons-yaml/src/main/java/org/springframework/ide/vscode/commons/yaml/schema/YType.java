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

/**
 * Marker interface for objects that carry 'type information'.
 * <p>
 * It may seem odd that this interface has no actual methods. This
 * is because the methods for interpreting the types are defined
 * by an accompanying {@link YTypeUtil}.
 * <p>
 * The main reason why it works this way is to allow for 'YType' objects
 * themselves to be implemented as dumb data objects while making YTypeUtil
 * implementations define how to interpret these objects using context
 * information (e.g. types resolved from a project's classpath).
 *
 * @author Kris De Volder
 */
public interface YType {

}
