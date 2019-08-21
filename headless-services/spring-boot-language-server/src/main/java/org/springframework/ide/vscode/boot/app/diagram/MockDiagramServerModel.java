package org.springframework.ide.vscode.boot.app.diagram;

import java.util.ArrayList;

import org.eclipse.sprotty.Dimension;
import org.eclipse.sprotty.IDiagramServer;
import org.eclipse.sprotty.Point;
import org.eclipse.sprotty.SCompartment;
import org.eclipse.sprotty.SEdge;
import org.eclipse.sprotty.SLabel;
import org.eclipse.sprotty.SModelRoot;
import org.eclipse.sprotty.SNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import net.bytebuddy.utility.RandomString;


@Component
public class MockDiagramServerModel implements InitializingBean {
	
	@Autowired
	IDiagramServer diagramServer;
	
	@Autowired
	@Qualifier("taskScheduler")
	private TaskScheduler taskScheduler;

	@Override
	public void afterPropertiesSet() throws Exception {
//		taskScheduler.scheduleAtFixedRate(() -> diagramServer.setModel(generateModel()), Duration.ofSeconds(5));
		diagramServer.setModel(generateModel(10));
	}
	
	public static SModelRoot generateModel(int nodesNum) {
		SModelRoot graph = new SModelRoot();
		graph.setId("graph");
		graph.setType("graph");
		
		SNode node0 = createBean("node0", "main", new Point(100, 100), new Dimension(120, 40));
		
		graph.setChildren(new ArrayList<>());
		graph.getChildren().add(node0);

		for (int i = 1; i < nodesNum; i++) {
			SNode node = createBean("node" + i, RandomString.make(((int) Math.round(Math.random()* 10 + 1))), new Point(Math.random() * 1024, Math.random() * 768), new Dimension(120, 40));	
			SEdge edge = createEdge("edge-" + i, node0.getId(), node.getId());
			graph.getChildren().add(edge);
			graph.getChildren().add(node);
			
		}
		
		return graph;
	}
	
	private static SNode createBean(String id, String labelText, Point location, Dimension size) {
		SNode node = new SNode();
	    node.setId(id);
	    node.setType("node:bean");
	    node.setLayout("vbox");
	    node.setPosition(new Point(Math.random() * 1024, Math.random() * 768));
	    node.setSize(new Dimension(80, 80));
	    node.setChildren(new ArrayList<>());
	    
	    SCompartment compartment = new SCompartment();
	    compartment.setId(id + "-comp");
	    compartment.setType("compartment");
	    compartment.setLayout("hbox");
	    compartment.setChildren(new ArrayList<>());
	    
	    SLabel label = new SLabel();
	    label.setId(id + "-lanbel");
	    label.setType("node:label");
	    label.setText(labelText);
	    
	    compartment.getChildren().add(label);
	    node.getChildren().add(compartment);
	    
	    return node;
	}
	
	private static SEdge createEdge(String id, String sourceId, String targetId) {
		SEdge edge = new SEdge();
		edge.setId(id);
		edge.setType("edge:straight");
		edge.setSourceId(sourceId);
		edge.setTargetId(targetId);
		return edge;
	}

}
