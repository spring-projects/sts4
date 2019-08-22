package org.springframework.ide.vscode.boot.app.diagram;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sprotty.IDiagramServer;
import org.eclipse.sprotty.IPopupModelFactory;
import org.eclipse.sprotty.RequestPopupModelAction;
import org.eclipse.sprotty.SGraph;
import org.eclipse.sprotty.SLabel;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SModelRoot;
import org.springframework.stereotype.Component;

@Component
public class PopupModelFactory implements IPopupModelFactory {

	@Override
	public SModelRoot createPopupModel(SModelElement element, RequestPopupModelAction request, IDiagramServer server) {
		
//		SGraph graph = new SGraph();
//		graph.setId("popup");
//		List<SModelElement> children = new ArrayList<>();
//		
//		SLabel label = new SLabel();
//		label.setType("node:label");
//		label.setText("I'm a tooltip!");
//		children.add(label);
//		
//		graph.setChildren(children);
//		
//		return graph;
		return null;
	}

}
