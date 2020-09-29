/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import org.eclipse.core.runtime.IStatus;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

public class AddStartersError extends ValidationResult {

	public final String details;

	private AddStartersError(String msg, String details) {
		super(IStatus.ERROR, msg);
		this.details = details;
	}

	/**
	 * Create an error from a short message and a longer detail
	 */
	public static AddStartersError from(String msg, String details) {
		return new AddStartersError(msg, details);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((details == null) ? 0 : details.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AddStartersError other = (AddStartersError) obj;
		if (details == null) {
			if (other.details != null)
				return false;
		} else if (!details.equals(other.details))
			return false;
		return true;
	}
}
