package org.springframework.ide.si.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sprotty.Dimension;
import org.eclipse.sprotty.Point;
import org.eclipse.sprotty.RequestModelAction;
import org.eclipse.sprotty.SCompartment;
import org.eclipse.sprotty.SEdge;
import org.eclipse.sprotty.SGraph;
import org.eclipse.sprotty.SLabel;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SModelRoot;
import org.eclipse.sprotty.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.si.view.json.SpringIntegrationEdge;
import org.springframework.ide.si.view.json.SpringIntegrationGraph;
import org.springframework.ide.si.view.json.SpringIntegrationNode;
import org.springframework.ide.vscode.commons.sprotty.api.DiagramGenerator;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class SIDiagramGenerator implements DiagramGenerator {

	private static final Logger log = LoggerFactory.getLogger(SIDiagramGenerator.class);

	private static final ImmutableSet<String> channelTypes = ImmutableSet.of(
			"channel",
			"publish-subscribe-channel"
	);
	
	@Autowired
	GraphDataProvider graphDataProvider;
	
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	@Override
	public SModelRoot generateModel(String clientId, RequestModelAction modelRequest) {
		try {
			SpringIntegrationGraph jsonData = graphDataProvider.getGraph();
			return toSprottyGraph(jsonData);
		} catch (Exception e) {
			log.error("", e);
			return SModelRoot.EMPTY_ROOT;
		}
	}

	private SGraph toSprottyGraph(SpringIntegrationGraph json) throws Exception {
		SGraph graph = new SGraph();
		graph.setId("root");
		graph.setType("graph");
		
		List<SModelElement> children = new ArrayList<>();
		graph.setChildren(children);
		
		Set<String> nodeIds = new HashSet<>();
		
		for (SpringIntegrationNode node : json.getNodes()) {
			String id = node.getNodeId()+"";
			String name = node.getName();
			Assert.isLegal(nodeIds.add(id));
			SNode n = createNode(id, name, node.getComponentType());
			children.add(n);
		}

		int linkId = 0;
		for (SpringIntegrationEdge link : json.getLinks()) {
			String id = "l"+(linkId++);
			String sourceId = link.getFrom()+"";
			ensureNode(nodeIds, children, sourceId);
			String targetId = link.getTo()+"";
			ensureNode(nodeIds, children, targetId);
			SEdge e = createEdge(id, sourceId, targetId);
			children.add(e);
		}
//		
//		LiveBeansModel beansModel = app.getBeans();
//		for (String targetBeanId : beansModel.getBeanNames()) {
//			for (LiveBean bean : beansModel.getBeansOfName(targetBeanId)) {
//				graphChildren.add(createBean(bean.getId(), bean.getShortName(), new Point(), new Dimension()));
//			}
//			for (LiveBean sourceBean : beansModel.getBeansDependingOn(targetBeanId)) {
//				graphChildren.add(createEdge(sourceBean.getId() + " " + targetBeanId, sourceBean.getId(), targetBeanId));
//			}
//		}

		return graph;
	}
	
	private void ensureNode(Set<String> nodeIds, List<SModelElement> children, String sourceId) {
		if (!nodeIds.contains(sourceId)) {
			nodeIds.add(sourceId);
			children.add(createNode(sourceId, "?"+sourceId, "unknown"));
		}
	}

	private static SNode createNode(String id, String labelText, String type) {
		SNode node = new SNode();
	    node.setId(id);
	    node.setType("node:"+visualType(type));
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
	
	private static String visualType(String type) {
		if (channelTypes.contains(type)) {
			return "channel";
		} else {
			return "bean";
		}
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
