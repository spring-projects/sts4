package org.springframework.ide.vscode.commons.sprotty.elk;

import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.sprotty.ILayoutEngine;
import org.eclipse.sprotty.SModelRoot;

public class ElkLayoutEngine implements ILayoutEngine {

	@Override
	public void layout(SModelRoot root) {
		ElkUtils utils = new ElkUtils(root);
		ElkNode graph = utils.graph;
		new RecursiveGraphLayoutEngine().layout(graph, new BasicProgressMonitor());
		utils.applyLayout();
	}

}
