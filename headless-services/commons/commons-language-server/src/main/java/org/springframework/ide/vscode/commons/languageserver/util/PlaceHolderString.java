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
package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.Map;

import org.springframework.ide.vscode.commons.util.CollectionUtil;
import org.springframework.ide.vscode.commons.util.text.IRegion;

import com.google.common.collect.ImmutableMap;

/**
 * Represents a string with placeholder inside. Provides methods to retrieve
 * the location of a placeholder given its 'id'.
 */
public class PlaceHolderString {

	public static class PlaceHolder {
		public final Object id; // object used to identify the placeholder
		public final IRegion location;

		public PlaceHolder(Object id, IRegion location) {
			super();
			this.id = id;
			this.location = location;
		}

		public int getOffset() {
			return location.getOffset();
		}

		public int getEnd() {
			return location.getOffset() + location.getLength();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
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
			PlaceHolder other = (PlaceHolder) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

	}

	private final ImmutableMap<Object, PlaceHolder> placeHolders;
	private final String string;

	public PlaceHolderString(Map<Object, PlaceHolder> placeHolders, String string) {
		super();
		this.placeHolders = ImmutableMap.copyOf(placeHolders);
		this.string = string;
	}


	@Override
	public String toString() {
		if (placeHolders.size()==1) {
			PlaceHolder placeHolder = CollectionUtil.getAny(placeHolders.values());
			if (string.length()==placeHolder.getEnd()) {
				return string.substring(0, placeHolder.getOffset());
			}
		}
		return string;
	}

	public PlaceHolder getPlaceHolder(Object id) {
		return placeHolders.get(id);
	}

}
