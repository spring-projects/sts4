/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.remote;

public interface AppDataSummarizer<T> {

	/**
	 * Used to fetch data from the current node. This should only
	 * return the data that actually originiates at the node and not
	 * include data sumarised from its children.
	 */
	T getHere(GenericRemoteAppElement app);

	/**
	 * Used to fetch summary data from node. The summary accounts for
	 * the data from the node itself as well as the summarized data from
	 * all its children.
	 * <p>
	 * THe typical implementation should call a method on the {@link GenericRemoteAppElement}
	 * that fetches the current value of a LiveExpression that computes the
	 * summary data.
	 */
	T getSummary(GenericRemoteAppElement element);

	/**
	 * Combines information from 2 data sources to create a 'summary'
	 * of both datas.
	 * <p>
	 * Default implementation just keeps the first 'real' data
	 * and ignores the rest.
	 */
	T merge(T d1 , T d2);

	/**
	 * Defines the element that represents 'no data'. A typical value
	 * would be 'null' or '0'.
	 */
	default T zero() {
		return null;
	}

}
