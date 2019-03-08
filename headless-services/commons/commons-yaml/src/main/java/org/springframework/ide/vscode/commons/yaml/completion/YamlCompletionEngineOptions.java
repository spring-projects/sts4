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
package org.springframework.ide.vscode.commons.yaml.completion;

public interface YamlCompletionEngineOptions {
	/**
	 * Whether the completion engine includes 'less indented' proposals (i.e. proposals
	 * that aren't valid at the current CA position, but are valid if we delete
	 * some spaces in front of the cursor first.
	 */
	default boolean includeDeindentedProposals() {
		return true;
	}

	YamlCompletionEngineOptions DEFAULT = new YamlCompletionEngineOptions() {};
}