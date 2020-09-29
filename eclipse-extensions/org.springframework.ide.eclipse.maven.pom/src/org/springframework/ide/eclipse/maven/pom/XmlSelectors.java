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
package org.springframework.ide.eclipse.maven.pom;

import java.util.function.Predicate;

import org.springframework.ide.eclipse.maven.pom.DomStructureComparable.Builder;

import com.google.common.collect.ImmutableSet;

public class XmlSelectors {
	
	public static Predicate<DomStructureComparable.Builder> childrenOf(String... _tagNames) {
		ImmutableSet<String> tagNames = ImmutableSet.copyOf(_tagNames);
		return new Predicate<DomStructureComparable.Builder>() {

			@Override
			public boolean test(DomStructureComparable.Builder n) {
				return n.parent != null && tagNames.contains(n.parent.name);
			}
			
			@Override
			public String toString() {
				return "childrenOf(" + tagNames + ")";
			}
		};
	}

	public static Predicate<Builder> tagName(String tagName) {
		return new Predicate<DomStructureComparable.Builder>() {

			@Override
			public boolean test(Builder n) {
				return tagName.equals(n.name);
			}
			
			@Override
			public String toString() {
				return "tagName(" + tagName + ")";
			}
		};
	}

	public static Predicate<Builder> grandParent(String tagName) {
		return ancestor(2, tagName);
	}

	public static Predicate<Builder> ancestor(int level, String tagName) {
		return new Predicate<DomStructureComparable.Builder>() {

			@Override
			public boolean test(Builder n) {
				Builder ancestor = getAncestor(level, n);
				return ancestor != null && tagName.equals(ancestor.name);
			}
			
			private Builder getAncestor(int level, Builder n) {
				if (level == 0) {
					return n;
				} else {
					return n == null ? null : getAncestor(level - 1, n.parent);
				}
			}

			@Override
			public String toString() {
				return "ancestor(" + level + ", "+ tagName + ")";
			}
		};
	}

	public static Predicate<Builder> path(String... tagNames) {
		return new Predicate<Builder>() {

			@Override
			public boolean test(Builder t) {
				int i = tagNames.length - 1;
				Builder current = t; 
				while (i >= 0) {
					String expectTag = tagNames[i];
					if (current == null || !match(expectTag, current.name)) {
						return false;
					}
					i--;
					current = current.parent;
				}
				return true;
			}
			
			private boolean match(String expectTag, String name) {
				return "*".equals(expectTag) || expectTag.equals(name);
			}

			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder();
				sb.append("path(");
				boolean first = true;
				for (String t : tagNames) {
					if (!first) {
						sb.append(" -> ");
					}
					sb.append(t);
					first = false;
				}
				sb.append(")");
				return sb.toString();
			}
		};
	}

}
