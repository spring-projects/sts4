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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors;

import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameter;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ParameterKind;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist.JavaContentAssistUIAdapter;


/**
 * @author Nieraj Singh
 */
public class ParameterEditorFactory {

	public static IParameterEditor getParameterEditor(
			ICommandParameter parameter, boolean showLabel) {
		if (parameter == null) {
			return null;
		}
		ParameterKind kind = parameter.getParameterDescriptor()
				.getParameterKind();

		switch (kind) {
		case BASE:
			return new BaseParameterEditor(parameter, showLabel);
		case COMBO:
			return new ComboParameterEditor(parameter, showLabel);
		}
		return null;
	}

	public static IParameterEditor getBooleanParameterEditor(
			ICommandParameter parameter) {
		ParameterKind kind = parameter.getParameterDescriptor()
				.getParameterKind();
		if (kind == ParameterKind.BOOLEAN) {
			return new BooleanParameterEditor(parameter);
		}

		return null;
	}

	public static IParameterEditor getJavaParameterEditor(
			ICommandParameter parameter, JavaContentAssistUIAdapter adapter,
			boolean showLabel) {
		if (parameter == null
				|| parameter.getParameterDescriptor().getParameterKind() != ParameterKind.JAVA_TYPE) {
			return null;
		}
		return new JavaParameterEditor(parameter, adapter, showLabel);
	}

}
