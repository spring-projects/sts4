/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * @author Martin Lippert
 */
public class RecordingElementChangedListener implements BootDashModel.ElementStateListener {

	private List<RunState> recordedRunStates = new ArrayList<>();

	@Override
	public void stateChanged(BootDashElement e) {
		recordedRunStates.add(e.getRunState());
	}

	public List<RunState> getRecordedRunStates() {
		return recordedRunStates;
	}

}
