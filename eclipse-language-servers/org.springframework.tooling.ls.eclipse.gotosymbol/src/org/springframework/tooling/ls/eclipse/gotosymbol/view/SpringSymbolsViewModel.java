package org.springframework.tooling.ls.eclipse.gotosymbol.view;

import org.eclipse.core.resources.IProject;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialogModel;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InWorkspaceSymbolsProvider;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

public class SpringSymbolsViewModel {

	public final LiveVariable<IProject> currentProject = new LiveVariable<>();
	public final GotoSymbolDialogModel gotoSymbols = new GotoSymbolDialogModel(null, InWorkspaceSymbolsProvider.createFor(currentProject::getValue));
	
}
