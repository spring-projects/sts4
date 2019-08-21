package org.springframework.ide.vscode.commons.sprotty.elk;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.elk.alg.layered.options.ContentAlignment;
import org.eclipse.elk.alg.layered.options.FixedAlignment;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.alg.layered.options.NodePlacementStrategy;
import org.eclipse.elk.core.options.Alignment;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.graph.ElkBendPoint;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.sprotty.Point;
import org.eclipse.sprotty.SEdge;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SModelRoot;
import org.eclipse.sprotty.SNode;
import org.eclipse.sprotty.SShapeElement;
import org.springframework.util.Assert;

public class ElkUtils {
	
	private static final IProperty<SModelElement> SPROTTY_ELEMENT = new Property<>("sprotty-element");
	public final ElkNode graph;
	private Map<String, ElkNode> nodes = new HashMap<>();;

	public ElkUtils(SModelRoot root) {
		graph = ElkGraphUtil.createGraph();
		
//		graph.setProperty(LayeredOptions.DIRECTION, Direction.DOWN);
//		graph.setProperty(LayeredOptions.CONTENT_ALIGNMENT, EnumSet.of(ContentAlignment.H_CENTER));
//		graph.setProperty(LayeredOptions.ALIGNMENT, Alignment.RIGHT);
		
//		graph.setProperty(LayeredOptions.NODE_PLACEMENT_STRATEGY, NodePlacementStrategy.SIMPLE);
		graph.setProperty(LayeredOptions.NODE_PLACEMENT_BK_FIXED_ALIGNMENT, FixedAlignment.BALANCED);
		
		List<SEdge> edges = new ArrayList<>();
		for (SModelElement child : root.getChildren()) {
			System.out.println(child.getClass());
			if (child instanceof SNode) {
				nodes.put(child.getId(), toElk((SNode)child, graph));
			} else if (child instanceof SEdge) {
				edges.add((SEdge)child);
			} else {
				throw new IllegalArgumentException("Unsupported graph layout element");
			}
		}
		
		for (SEdge edge : edges) {
			toElk(edge);
		}
	}

	private ElkEdge toElk(SEdge child) {
		ElkNode source = nodes.get(child.getSourceId());
		ElkNode target = nodes.get(child.getTargetId());
		Assert.isTrue(source != null);
		Assert.isTrue(target != null);
		ElkEdge edge = ElkGraphUtil.createSimpleEdge(source, target);
		edge.setProperty(SPROTTY_ELEMENT, child);
		return edge;
	}

	private ElkNode toElk(SNode child, ElkNode parent) {
		ElkNode node = ElkGraphUtil.createNode(parent);
		node.setX(child.getPosition().getX());
		node.setY(child.getPosition().getY());
		node.setWidth(child.getSize().getWidth());
		node.setHeight(child.getSize().getHeight());
		node.setIdentifier(child.getId());
		node.setProperty(SPROTTY_ELEMENT, child);
//		node.setProperty(LayeredOptions.ALIGNMENT, Alignment.CENTER);
		return node;
	}
	
	public void applyLayout() {
		for (Entry<String, ElkNode> entry : nodes.entrySet()) {
			ElkNode elkNode = entry.getValue();
			SModelElement element = elkNode.getProperty(SPROTTY_ELEMENT);
			if (element instanceof SShapeElement) {
				SShapeElement shape = (SShapeElement) element;
				shape.setPosition(new Point(elkNode.getX(), elkNode.getY()));
			}
		}
		
		for (ElkEdge elkEdge : graph.getContainedEdges()) {
			SModelElement element = elkEdge.getProperty(SPROTTY_ELEMENT);
			if (element instanceof SEdge) {
				SEdge edge = (SEdge) element;
				List<Point> bendpoints = new ArrayList<>();
				for (ElkEdgeSection section : elkEdge.getSections()) {
					bendpoints.add(new Point(section.getStartX(), section.getStartY()));
					for (ElkBendPoint elkBend : section.getBendPoints()) {
						Point bendpoint = new Point(elkBend.getX(), elkBend.getY());
						bendpoints.add(bendpoint);
					}
					bendpoints.add(new Point(section.getEndX(), section.getEndY()));
				}
				edge.setRoutingPoints(bendpoints);
			}
		}
	}
	
}
