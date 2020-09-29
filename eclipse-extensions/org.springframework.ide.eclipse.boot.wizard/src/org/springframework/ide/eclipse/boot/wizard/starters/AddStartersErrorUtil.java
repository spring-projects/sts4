/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class AddStartersErrorUtil {

	public static ValidationResult getError(String shortMessage, Throwable e) {

		AddStartersError result = getError(shortMessage, shortMessage, e);

		// This is some general unidentifiable error, so notify the loading validator
		// for now that an error was encountered
		return result;
	}

	public static AddStartersError getError(String shortMessage, String details, Throwable e) {
		String exceptionMsg = ExceptionUtil.getMessage(e);

		StringBuffer detailsBuffer = new StringBuffer(details);
		if (exceptionMsg != null) {
			detailsBuffer.append('\n');
			detailsBuffer.append('\n');
			detailsBuffer.append("Error Details:");
			detailsBuffer.append('\n');
			detailsBuffer.append(exceptionMsg);
		}
		return AddStartersError.from(shortMessage, detailsBuffer.toString());
	}
}
