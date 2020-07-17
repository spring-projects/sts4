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
import org.eclipse.ui.IWorkbenchWindow;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveCounter;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialogModel;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InFileSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InProjectSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InWorkspaceSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.SelectionTracker;
import org.springframework.tooling.ls.eclipse.gotosymbol.favourites.FavouritesPreference;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class SpringSymbolsViewModel {

	public final SelectionTracker currentSelection;
	public final GotoSymbolDialogModel gotoSymbols;
	public final LiveCounter refreshButton = new LiveCounter();
	
	public SpringSymbolsViewModel(IWorkbenchWindow wbw) {
		currentSelection = SelectionTracker.getInstance(wbw);
		LiveExpression<IResource> currentResource = currentSelection.currentResource();
		LiveExpression<IProject> currentProject = currentSelection.currentProject;
		gotoSymbols = new GotoSymbolDialogModel(null, 
				InWorkspaceSymbolsProvider.createFor(currentProject::getValue),
				InProjectSymbolsProvider.createFor(currentProject),
				InFileSymbolsProvider.createFor(currentSelection.getDocumentData())
		)
		.setFavourites(FavouritesPreference.INSTANCE);
		{	
			gotoSymbols.unfilteredSymbols.dependsOn(currentResource);
			gotoSymbols.unfilteredSymbols.dependsOn(currentProject);
			gotoSymbols.unfilteredSymbols.dependsOn(refreshButton);
		}
	}
}
