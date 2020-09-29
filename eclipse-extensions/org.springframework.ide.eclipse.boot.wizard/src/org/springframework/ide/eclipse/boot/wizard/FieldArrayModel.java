/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;

/**
 * An array of FieldModels of the same type.
 */
public class FieldArrayModel<T> implements Iterable<FieldModel<T>> {

	private final ArrayList<FieldModel<T>> fields;

	public FieldArrayModel(FieldModel<T>... initialFields) {
		fields = new ArrayList<FieldModel<T>>(Arrays.asList(initialFields));
	}

	public FieldModel<T> getField(String name) {
		for (FieldModel<T> f : fields) {
			if (name.equals(f.getName())) {
				return f;
			}
		}
		return null;
	}

	//@Override
	public Iterator<FieldModel<T>> iterator() {
		return fields.iterator();
	}

	public void add(FieldModel<T> f) {
		fields.add(f);
	}

}
