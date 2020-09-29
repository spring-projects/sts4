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
 * A template is some kind of pattern containing variables. It can be rendered into
 * text by substituting the variables values.
 *
 * @author Kris De Volder
 */
public interface Template {
	/**
	 * Render this template into text, looking up variables from the given {@link TemplateEnv}
	 */
	String render(TemplateEnv env);
}
