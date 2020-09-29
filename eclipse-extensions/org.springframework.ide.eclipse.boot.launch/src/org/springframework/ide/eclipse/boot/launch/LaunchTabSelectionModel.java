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
package org.springframework.ide.eclipse.boot.launch;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.launch.util.ILaunchConfigurationTabModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * Abstract base clase for a {@link SelectionModel} that also implements {@link ILaunchConfigurationTabModel}
 * <p>
 * Note that this class manages the 'dirtyState' for the model but only partially. I.e. the state gets set
 * to true when the model contents changes, but it is up to the subclass to set it to false whenever
 * the model state is synched with {@link ILaunchConfigurationWorkingCopy} state.
 *
 * @author Kris De Volder
 */
public abstract class LaunchTabSelectionModel<T> extends SelectionModel<T> implements ILaunchConfigurationTabModel {

	private LiveVariable<Boolean> dirtyState = new LiveVariable<Boolean>(false);

	public LaunchTabSelectionModel(final LiveVariable<T> selection, LiveExpression<ValidationResult> validator) {
		super(selection, validator);
		selection.addListener(new ValueListener<T>() {
			@Override
			public void gotValue(LiveExpression<T> exp, T value) {
				getDirtyState().setValue(true);
			}
		});
	}

	public LaunchTabSelectionModel(SelectionModel<T> selection) {
		this(selection.selection, selection.validator);
	}

	@Override
	public final LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

	@Override
	public final LiveVariable<Boolean> getDirtyState() {
		return dirtyState;
	}

}
