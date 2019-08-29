package org.springframework.ide.si.view;

import java.util.ArrayList;

import org.eclipse.sprotty.IDiagramServer;
import org.eclipse.sprotty.IPopupModelFactory;
import org.eclipse.sprotty.PreRenderedElement;
import org.eclipse.sprotty.RequestPopupModelAction;
import org.eclipse.sprotty.SGraph;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SModelRoot;
import org.springframework.stereotype.Component;

@Component
public class IntegrationPopupFactory implements IPopupModelFactory {

	@Override
	public SModelRoot createPopupModel(SModelElement element, RequestPopupModelAction request, IDiagramServer server) {
		SGraph popup = new SGraph();
		popup.setType("html");
		popup.setId("popup");
		popup.setCanvasBounds(request.getBounds());
		
		ArrayList<SModelElement> children = new ArrayList<>();
		popup.setChildren(children);
		
		PreRenderedElement content = new PreRenderedElement();
		children.add(content);
		content.setId("popup-title");
		content.setCode("<div class=\"sprotty-popup-title\">Hello World!</div>");
		
		return popup;
	}
	
}
