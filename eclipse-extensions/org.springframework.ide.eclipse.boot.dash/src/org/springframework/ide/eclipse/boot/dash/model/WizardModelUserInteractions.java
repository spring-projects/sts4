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
package org.springframework.ide.eclipse.boot.dash.model;

/**
 * TODO: Should be merged with {@link UserInteractions}
 * This is just a workaround to avoid heavy refactoring to pass in the "main" user interactions
 * to wizard models
 */
public interface WizardModelUserInteractions {

	void informationPopup(String title, String message);

}
