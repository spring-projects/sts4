/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		//Disabled by default for now because of bug introduced in VSCode 1.12:
		//https://github.com/Microsoft/vscode/issues/26096
		return false;
	}
	
	YamlCompletionEngineOptions DEFAULT = new YamlCompletionEngineOptions() {};
	YamlCompletionEngineOptions TEST_DEFAULT = new YamlCompletionEngineOptions() {
		@Override public boolean includeDeindentedProposals() { return true; }
	};
}