/*******************************************************************************
 * Copyright (c) 2023 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol;

public record LiveProcessLoggersSummary(String processType, String processKey, String processName, String processID,
		String packageName, String effectiveLevel, String configuredLevel) {

}
