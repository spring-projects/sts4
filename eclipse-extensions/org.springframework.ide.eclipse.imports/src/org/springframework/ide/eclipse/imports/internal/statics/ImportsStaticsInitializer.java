/*******************************************************************************
 * Copyright (c) 2016 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.imports.internal.statics;

import org.eclipse.ui.IStartup;

public class ImportsStaticsInitializer implements IStartup {

	@Override
	public void earlyStartup() {
		SpringStaticImportFavourites importFavourites = new SpringStaticImportFavourites(
				StaticImportCatalogue.DEFAULT_CATALOGUE);
		importFavourites.asynchLoad();
	}

}
