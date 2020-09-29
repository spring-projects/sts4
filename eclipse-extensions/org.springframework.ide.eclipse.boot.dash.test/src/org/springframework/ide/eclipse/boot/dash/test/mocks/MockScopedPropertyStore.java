/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.HashMap;
import java.util.Map;

import org.springsource.ide.eclipse.commons.core.pstore.IScopedPropertyStore;

/**
 * A memory-backed {@link IScopedPropertyStore} suitable for testing.
 *
 * @author Kris De Volder
 */
public class MockScopedPropertyStore<T> implements IScopedPropertyStore<T> {

	private Map<Key,String> store = new HashMap<>();

	@Override
	public String get(T element, String key) {
		return store.get(new Key(element, key));
	}

	@Override
	public void put(T element, String key, String value) throws Exception {
		store.put(new Key(element, key), value);
	}


	private class Key {
		public final T element;
		public final String key;
		public Key(T element, String key) {
			this.element = element;
			this.key = key;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((element == null) ? 0 : element.hashCode());
			result = prime * result + ((key == null) ? 0 : key.hashCode());
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
			Key other = (Key) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (element == null) {
				if (other.element != null)
					return false;
			} else if (!element.equals(other.element))
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}
		private MockScopedPropertyStore getOuterType() {
			return MockScopedPropertyStore.this;
		}

	}

}
