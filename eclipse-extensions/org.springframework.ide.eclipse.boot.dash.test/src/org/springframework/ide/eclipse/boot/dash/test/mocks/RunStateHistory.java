/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableList;

public class RunStateHistory implements ValueListener<RunState> {

	List<RunState> history = new ArrayList<>();

	@Override
	public void gotValue(LiveExpression<RunState> exp, RunState value) {
		history.add(value);
	}

	public void assertHistory(RunState... runStates) {
		assertEquals(ImmutableList.copyOf(runStates), ImmutableList.copyOf(history));
	}

	public void assertHistoryContains(RunState... expectedStates) {
		for (RunState runState : expectedStates) {
			if (!history.contains(runState)) {
				fail("Not found: "+runState+" in "+expectedStates);
			}
		}
	}

	public void assertLast(RunState expectedState) {
		assertEquals(expectedState, history.get(history.size()-1));
	}

}
