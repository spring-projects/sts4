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

import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.maven.pom.DomStructureComparable.DomType;
import org.springframework.ide.eclipse.maven.pom.XmlDocumentDiffer.Difference;
import org.springframework.ide.eclipse.maven.pom.XmlDocumentDiffer.Direction;

public class PomDocumentDiffer {

	public static XmlDocumentDiffer create(IDocument document1, IDocument document2) {
		return new XmlDocumentDiffer(document1, document2)
				.idProvider(XmlSelectors.childrenOf("properties", "project", "parent", "dependency", "repository",
						"pluginRepository", "plugin", "build"), IdProviders.FROM_TAG_NAME)
				.idProvider(XmlSelectors.tagName("dependency"), IdProviders.fromChildren("groupId", "artifactId"))
				.idProvider(XmlSelectors.tagName("repository"), IdProviders.fromChildren("url"))
				.idProvider(XmlSelectors.tagName("pluginRepository"), IdProviders.fromChildren("url"))
				.idProvider(XmlSelectors.tagName("plugin"), IdProviders.fromChildren("groupId", "artifactId"))
				.idProvider(XmlSelectors.path("dependency", "exclusions", "exclusion"),
						IdProviders.fromChildren("groupId", "artifactId"))
				.idProvider(XmlSelectors.path("dependency", "exclusions", "exclusion", "*"),
						IdProviders.FROM_TAG_NAME);
	}

	public static Predicate<Difference> differenceDirections(Direction... directions) {
		return new Predicate<Difference>() {

			@Override
			public boolean test(Difference t) {
				return Arrays.asList(directions).contains(t.direction);
			}
			
		};
	}
	
	public static Predicate<Difference> ignorePath(String... path) {
		return new Predicate<Difference>() {

			@Override
			public boolean test(Difference t) {
				if (t.leftComparable != null) {
					return !samePath(t.leftComparable);
				}
				if (t.rightComparable != null) {
					return !samePath(t.rightComparable);
				}
				return true;
			}
			
			private boolean samePath(DomStructureComparable structure) {
				for (int i = path.length - 1; i >= 0; i--) {
					if (structure == null || structure.getDomType() == DomType.ROOT) {
						return false;
					} else {
						if (path[i].equals(structure.getName())) {
							structure = structure.getParent();
						} else {
							return false;
						}
					}
				}
				return true;
			}
			
		};
	}

}
