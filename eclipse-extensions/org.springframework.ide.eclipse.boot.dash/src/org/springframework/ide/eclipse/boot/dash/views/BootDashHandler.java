package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class BootDashHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BootDashActions actions = getActions();
		if (actions != null) {
			IAction action = actions.getAction(event.getCommand().getId());
			if (action != null && action.isEnabled()) {
				action.run();
			}
		}
		return null;
	}

	private BootDashActions getActions() {
		for (IWorkbenchPage page : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()) {
			IViewPart view = page.findView(BootDashTreeView.ID);
			if (view instanceof  BootDashTreeView) {
				return ((BootDashTreeView)view).getActions();
			}
		}
		return null;
	}

}
