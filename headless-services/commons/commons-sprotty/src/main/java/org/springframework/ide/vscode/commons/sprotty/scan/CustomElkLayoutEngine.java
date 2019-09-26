package org.springframework.ide.vscode.commons.sprotty.scan;

import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.sprotty.BoundsAware;
import org.eclipse.sprotty.Layouting;
import org.eclipse.sprotty.SEdge;
import org.eclipse.sprotty.SLabel;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SNode;
import org.eclipse.sprotty.SPort;
import org.eclipse.sprotty.SShapeElement;
import org.eclipse.sprotty.layout.ElkLayoutEngine;

public class CustomElkLayoutEngine extends ElkLayoutEngine {

	/**
	 * Transform the children of a sprotty model element to their ELK graph counterparts.
	 */
	protected int processChildren(SModelElement sParent, ElkGraphElement elkParent, LayoutContext context) {
		int childrenCount = 0;
		if (sParent.getChildren() != null) {
			for (SModelElement schild : sParent.getChildren()) {
				context.parentMap.put(schild, sParent);
				ElkGraphElement elkChild = null;
				if (shouldInclude(schild, sParent, elkParent, context)) {
					if (schild instanceof SNode) {
						SNode snode = (SNode) schild;
						ElkNode elkNode = createNode(snode);
						if (elkParent instanceof ElkNode) {
							elkNode.setParent((ElkNode) elkParent);
							childrenCount++;
						}
						context.shapeMap.put(snode, elkNode);
						elkChild = elkNode;
					} else if (schild instanceof SPort) {
						SPort sport = (SPort) schild;
						ElkPort elkPort = createPort(sport);
						if (elkParent instanceof ElkNode) {
							elkPort.setParent((ElkNode) elkParent);
							childrenCount++;
						}
						context.shapeMap.put(sport, elkPort);
						elkChild = elkPort;
					} else if (schild instanceof SEdge) {
						SEdge sedge = (SEdge) schild;
						ElkEdge elkEdge = createEdge(sedge);
						// The most suitable container for the edge is determined later
						childrenCount++;
						context.edgeMap.put(sedge, elkEdge);
						elkChild = elkEdge;
					} else if (schild instanceof SLabel) {
						SLabel slabel = (SLabel) schild;
						ElkLabel elkLabel = createLabel(slabel);
						elkLabel.setParent(elkParent);
						childrenCount++;
						context.shapeMap.put(slabel, elkLabel);
						elkChild = elkLabel;
					} else if (schild instanceof SShapeElement) {
						System.out.println("wat?");
					}
				}
				int grandChildrenCount = processChildren(schild, elkChild != null ? elkChild : elkParent, context);
				childrenCount += grandChildrenCount;
				if (grandChildrenCount > 0 && sParent instanceof Layouting && schild instanceof BoundsAware) {
					handleClientLayout((BoundsAware) schild, (Layouting) sParent, elkParent, context);
				}
			}
		}
		return childrenCount;
	}

}
