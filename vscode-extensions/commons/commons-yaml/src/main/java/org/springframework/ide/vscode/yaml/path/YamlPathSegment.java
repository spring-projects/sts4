/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.yaml.path;

/**
 * A YamlPathSegment is a 'primitive' NodeNavigator operation.
 * More complex operations (i.e {@link YamlPath}) are composed as seqences
 * of 0 or more {@link YamlPathSegment}s.
 *
 * @author Kris De Volder
 */
public abstract class YamlPathSegment {

	public static enum YamlPathSegmentType {
		VAL_AT_KEY, //Go to value associate with given key in a map.
		KEY_AT_KEY, //Go to the key node associated with a given key in a map.
		VAL_AT_INDEX //Go to value associate with given index in a sequence
	}

	public static class AtIndex extends YamlPathSegment {

		private int index;

		public AtIndex(int index) {
			this.index = index;
		}

		public String toNavString() {
			return "["+index+"]";
		}

		public String toPropString() {
			return "["+index+"]";
		}

		@Override
		public YamlPathSegmentType getType() {
			return YamlPathSegmentType.VAL_AT_INDEX;
		}

		@Override
		public Integer toIndex() {
			return index;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
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
			AtIndex other = (AtIndex) obj;
			if (index != other.index)
				return false;
			return true;
		}
	}

	public static class ValAtKey extends YamlPathSegment {

		private String key;

		public ValAtKey(String key) {
			this.key = key;
		}

		@Override
		public String toNavString() {
			if (key.indexOf('.')>=0) {
				//TODO: what if key contains '[' or ']'??
				return "["+key+"]";
			}
			return "."+key;
		}

		@Override
		public String toPropString() {
			//Don't start with a '.' if trying to build a 'self contained' expression.
			return key;
		}

		@Override
		public YamlPathSegmentType getType() {
			return YamlPathSegmentType.VAL_AT_KEY;
		}

		@Override
		public Integer toIndex() {
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			ValAtKey other = (ValAtKey) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}
	}

	private static class KeyAtKey extends ValAtKey {

		public KeyAtKey(String key) {
			super(key);
		}

		@Override
		public YamlPathSegmentType getType() {
			return YamlPathSegmentType.KEY_AT_KEY;
		}

	}

	public String toString() {
		return toNavString();
	}

	public abstract String toNavString();
	public abstract String toPropString();

	public abstract Integer toIndex();
	public abstract YamlPathSegmentType getType();

	public static YamlPathSegment valueAt(String key) {
		return new ValAtKey(key);
	}
	public static YamlPathSegment valueAt(int index) {
		return new AtIndex(index);
	}
	public static YamlPathSegment keyAt(String key) {
		return new KeyAtKey(key);
	}

}
