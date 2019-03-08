/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.protocol;

public class ProgressParams {

	/**
	 * An id representing the enitity for which progress messages are to be shown.
	 */
	private String id;

	/**
	 * Updates the current statusMsg associated with a given the id. If null, then the message
	 * is cleared.
	 */
	private String statusMsg;


	public ProgressParams() {
	}

	public ProgressParams(String id, String statusMsg) {
		super();
		this.id = id;
		this.statusMsg = statusMsg;
	}

	@Override
	public String toString() {
		return "ProgressParams [id=" + id + ", statusMsg=" + statusMsg + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((statusMsg == null) ? 0 : statusMsg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProgressParams other = (ProgressParams) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (statusMsg == null) {
			if (other.statusMsg != null)
				return false;
		} else if (!statusMsg.equals(other.statusMsg))
			return false;
		return true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}

}
