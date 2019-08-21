package org.springframework.ide.vscode.boot.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.sprotty.Point;
import org.eclipse.sprotty.SModelRoot;
import org.junit.Test;
import org.springframework.ide.vscode.boot.app.diagram.MockDiagramServerModel;
import org.springframework.ide.vscode.commons.sprotty.elk.ElkUtils;


public class LayoutTest {

	@Test
	public void testCreateGraph() throws Exception {
		SModelRoot modelRoot = MockDiagramServerModel.generateModel(10);
		ElkNode graph = new ElkUtils(modelRoot).graph;
		assertNotNull(graph);
		assertEquals(10, graph.getChildren().size());
		assertEquals(9, graph.getContainedEdges().size());
	}
	
	@Test
	public void testLayout() throws Exception {
		LayoutMetaDataService.getInstance().registerLayoutMetaDataProviders(new LayeredMetaDataProvider());
		
		SModelRoot modelRoot = MockDiagramServerModel.generateModel(10);
		ElkNode graph = new ElkUtils(modelRoot).graph;
		
		Map<String, Point> locations = new HashMap<>();
		
		for (ElkNode child : graph.getChildren()) {
			locations.put(child.getIdentifier(), new Point(child.getX(), child.getY()));
		}
		
		new RecursiveGraphLayoutEngine().layout(graph, new BasicProgressMonitor());
		
		for (ElkNode child : graph.getChildren()) {
			assertNotEquals(locations.get(child.getIdentifier()), new Point(child.getX(), child.getY()));
		}

	}

}
