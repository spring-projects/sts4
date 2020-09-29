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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class IdProviderRegistry {
	
	static class Registration {
		Predicate<DomStructureComparable.Builder> selector;
		IdProvider provider;
		public Registration(Predicate<DomStructureComparable.Builder> selector, IdProvider provider) {
			super();
			this.selector = selector;
			this.provider = provider;
		}
		@Override
		public String toString() {
			return "[" + selector + " -> " + provider + "]\n";
		}
	}
	
	private List<Registration> registry = new ArrayList<>();
	
	public void add(Predicate<DomStructureComparable.Builder> selector, IdProvider provider) {
		registry.add(new Registration(selector, provider));
	}

	@Override
	public String toString() {
		return registry.toString();
	}

	/**
	 * 
	 * @param partialNode not fully initialized
	 * @return
	 */
	public Object getId(DomStructureComparable.Builder partialNode) {
		for (Registration registration : registry) {
			if (registration.selector.test(partialNode)) {
				return registration.provider.id(partialNode);
			}
		};
		return IdProviders.DEFAULT.id(partialNode);
	}
	
}
