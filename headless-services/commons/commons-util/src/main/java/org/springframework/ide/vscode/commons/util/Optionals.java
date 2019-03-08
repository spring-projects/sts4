/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.util.Optional;

import com.google.common.base.Supplier;

public class Optionals {

	@SafeVarargs
	public static <T> Optional<T> tryInOrder(Supplier<Optional<T>>... optionals) {
		for (Supplier<Optional<T>> supplier : optionals) {
			Optional<T> opt = supplier.get();
			if (opt.isPresent()) {
				return opt;
			}
		}
		return Optional.empty();
	}
}
