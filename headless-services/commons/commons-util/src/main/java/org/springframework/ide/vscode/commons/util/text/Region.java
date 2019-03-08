/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.util.text;

/**
 * Trivial implementation of {@link IRegion}
 * @author kdvolder
 *
 */
public class Region implements IRegion {
	
	private int ofs;
	private int len;
	
	public Region(int ofs, int len) {
		super();
		this.ofs = ofs;
		this.len = len;
	}

	@Override
	public int getOffset() {
		return ofs;
	}

	@Override
	public int getLength() {
		return len;
	}

	@Override
	public String toString() {
		return "Region [ofs=" + ofs + ", len=" + len + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + len;
		result = prime * result + ofs;
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
		Region other = (Region) obj;
		if (len != other.len)
			return false;
		if (ofs != other.ofs)
			return false;
		return true;
	}

}
