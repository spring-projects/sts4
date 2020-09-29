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

import java.util.Collection;

import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

import com.google.common.collect.ImmutableSet;

/**
 * Provides a {@link MultiSelection} and some convenience methods to change
 * and get the selected elements in test code.
 *
 * @author Kris De Volder
 */
public class MockMultiSelection<T> {

	private final MultiSelection<T> readableSelection;
	private final LiveSetVariable<T> writableSelection = new LiveSetVariable<>(AsyncMode.SYNC);

	public MockMultiSelection(Class<T> klass) {
		readableSelection = new MultiSelection<>(klass, writableSelection);
	}

	@SuppressWarnings("unchecked")
	public void setElements(T... newElements) {
		setElements(ImmutableSet.copyOf(newElements));
	}

	public void setElements(Collection<T> newElements) {
		writableSelection.replaceAll(newElements);
	}

	public MultiSelection<T> forReading() {
		return readableSelection;
	}

	public boolean isEmpty() {
		return readableSelection.getValue().isEmpty();
	}

	public ImmutableSet<T> getElements() {
		return readableSelection.getElements().getValues();
	}

}
