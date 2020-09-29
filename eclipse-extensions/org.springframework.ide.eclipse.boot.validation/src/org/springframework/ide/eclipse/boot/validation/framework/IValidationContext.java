/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.validation.framework;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;

public interface IValidationContext {

	void problem(IResource cu, ProblemType problemId, String msg, int offset, int len);

}
