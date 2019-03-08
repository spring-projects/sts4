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
package org.springframework.ide.vscode.concourse.github;

import java.util.Collection;

public interface GithubInfoProvider {

	/**
	 * Retrieves a list of owners that can be suggested as hints in completions of github repo urls.
	 * <p>
	 * Note: This list can not be used to determine whether a given owner exists. Github has millions of users
	 * and fetching all of them isn't really an option, therefore the owners are expected to be somehow limited
	 * based on the user credentials (i.e. returning a list that is deemed relevant to the current logged in user,
	 * rather than an exhaustive list of every org and user name on github.
	 */
	Collection<String> getOwners() throws Exception;

	/**
	 * Fetch information about the repos owned by a given user or org.
	 * <p>
	 * Should return null rather than throw an exception for the case
	 * where the owner did not exist. The caller can make use of this
	 * to determine implicitly whether a given owner is valid.
	 */
	Collection<String> getReposForOwner(String owner) throws Exception;

}
