/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.github.auth;

import java.net.URLConnection;

/**
 * Accesses github rest apis without any credentials.
 * Severe rate limits will be in effect, but for some use cases that may be ok.
 *
 * @author Kris De Volder
 */
public class NullCredentials extends Credentials {

	@Override
	public void apply(URLConnection conn) {
		//No credentials so nothing to apply
	}

}
