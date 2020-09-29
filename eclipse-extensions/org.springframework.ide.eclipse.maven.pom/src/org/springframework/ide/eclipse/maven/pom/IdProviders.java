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

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;


public class IdProviders {
	
	static final IdProvider FROM_TAG_NAME = (node) -> node.name;
	
	public static final IdProvider DEFAULT = (node) -> node.name + (node.parent == null ? "" : node.parent.children.size());
	
	public static IdProvider fromChildren(String... tagNames) {
		ImmutableSet<String> tagSet = ImmutableSet.copyOf(tagNames);
		return (node) -> {
			Map<String, String> tagToValue = new HashMap<>();
			for (DomStructureComparable child : node.children) {
				if (tagSet.contains(child.getName())) {	
					tagToValue.put(child.getName(), child.getValue());
				}
			}
			Builder<Object> builder = ImmutableList.builder();
			for (String tagName : tagNames) {
				String value = tagToValue.get(tagName);
				if (value != null) {
					builder.add(value);
				}
			}
			return builder.build();
		};
	}
	
//	static final IdProvider DEPENDENCY = (node) -> {
//		XMLNode group = null;
//		XMLNode artifact = null;
//		Object[] children = node.getChildren();
//		for (int i = 0; i < children.length && (group == null || artifact == null); i++) {
//			if (children[i] instanceof XMLNode) {
//				XMLNode n = (XMLNode) children[i];
//				if ("groupId".equals(n.getElement())) {
//					group = n;
//				} else if ("artifactId".equals(n.getElement())) {
//					artifact = n;
//				}
//			}
//		}
//		StringBuilder sb = new StringBuilder();
//		sb.append(getTextContent(group));
//		sb.append(' ');
//		sb.append(getTextContent(artifact));
//		return sb.toString();
//	};
}
