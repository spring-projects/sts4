/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.view;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialogModel;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InFileSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InProjectSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InWorkspaceSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.favourites.FavouritesPreference;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

public class SpringSymbolsViewModel {
	
	public final LiveVariable<IResource> currentResource = new LiveVariable<>();
	public final LiveExpression<IProject> currentProject = currentResource.apply(r -> r==null ? null : r.getProject()); 
	public final GotoSymbolDialogModel gotoSymbols = new GotoSymbolDialogModel(null, 
			InWorkspaceSymbolsProvider.createFor(currentProject::getValue),
			InProjectSymbolsProvider.createFor(currentProject),
			InFileSymbolsProvider.createFor(currentResource)
	)
	.setFavourites(FavouritesPreference.INSTANCE);
	{	
		gotoSymbols.unfilteredSymbols.dependsOn(currentProject);
	}
}
