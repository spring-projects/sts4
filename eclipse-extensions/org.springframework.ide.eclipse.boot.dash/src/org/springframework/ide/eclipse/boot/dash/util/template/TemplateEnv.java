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
package org.springframework.ide.eclipse.boot.dash.util.template;

/**
 * An implementation of this interface provides a means to 'resolve' variable names
 * used in rendering a {@link Template}.
 *
 * @author Kris De Volder
 */
public interface TemplateEnv {
	String getTemplateVar(char name);
}
