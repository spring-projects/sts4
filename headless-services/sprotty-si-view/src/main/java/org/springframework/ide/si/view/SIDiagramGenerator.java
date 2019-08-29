package org.springframework.ide.si.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sprotty.Dimension;
import org.eclipse.sprotty.EdgePlacement;
import org.eclipse.sprotty.EdgePlacement.Side;
import org.eclipse.sprotty.LayoutOptions;
import org.eclipse.sprotty.Point;
import org.eclipse.sprotty.RequestModelAction;
import org.eclipse.sprotty.SCompartment;
import org.eclipse.sprotty.SEdge;
import org.eclipse.sprotty.SGraph;
import org.eclipse.sprotty.SLabel;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SModelRoot;
import org.eclipse.sprotty.SNode;
import org.eclipse.sprotty.SPort;
import org.eclipse.sprotty.SShapeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.si.view.json.SpringIntegrationEdge;
import org.springframework.ide.si.view.json.SpringIntegrationGraph;
import org.springframework.ide.si.view.json.SpringIntegrationNode;
import org.springframework.ide.vscode.commons.sprotty.api.DiagramGenerator;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class SIDiagramGenerator implements DiagramGenerator {

	static final String TYPE_INTEGRATION_GRAPH = "graph";

	private static final double CHANNEL_HEIGHT = 40D;

	private static final double NODE_MIN_HEIGHT = 80D;

	private static final double MIN_WIDTH = 120D;

	private static final Logger log = LoggerFactory.getLogger(SIDiagramGenerator.class);

	private static final ImmutableSet<String> channelTypes = ImmutableSet.of(
			"channel",
			"publish-subscribe-channel"
	);
	
	private String labelProperty = "stats.sendCount";
	
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
		graph.setType(TYPE_INTEGRATION_GRAPH);
		
		List<SModelElement> children = new ArrayList<>();
		graph.setChildren(children);
		
		Map<String, SpringIntegrationNode> nodeIds = new HashMap<>();
		
		for (SpringIntegrationNode node : json.getNodes()) {
			String id = node.getNodeId()+"";
			String name = node.getName();
			Assert.isLegal(!nodeIds.containsKey(id));
			nodeIds.put(id, node);
			String type = visualType(node.getComponentType());
			switch (type) {
			case "channel":
				children.add(createChannelNode(id, name));
				break;
			default:
				children.add(createIntegrationNode(id, name));
			}
		}

		int linkId = 0;
		for (SpringIntegrationEdge link : json.getLinks()) {
			String id = "l"+(linkId++);
			String sourceId = link.getFrom()+"";
			String targetId = link.getTo()+"";
			
			
			SEdge e = createEdge(id, sourceId, targetId, getEdgeLabel(nodeIds.get(sourceId)), link.getType());
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
	
	private String getEdgeLabel(SpringIntegrationNode springIntegrationNode) {
		JsonElement json = gson.toJsonTree(springIntegrationNode);
		for (String prop : labelProperty.split("\\.")) {
			if (json instanceof JsonObject) {
				JsonObject obj = (JsonObject)json;
				if (obj.has(prop)) {
					json = obj.get(prop);
				} else {
					return null;
				}
			} else {
				return null;
			}
		};
		return json.getAsString();
	}

	private static SNode createNode(String id, String labelText, String type) {
		SNode node = new SNode();
	    node.setId(id);
	    node.setType("node:"+type);
	    node.setLayout("vbox");
	    node.setPosition(new Point(Math.random() * 1024, Math.random() * 768));
	    node.setSize(new Dimension(80, 80));
	    node.setChildren(new ArrayList<>());
	    centerNode(node);
	    
	    SCompartment compartment = new SCompartment();
	    compartment.setId(id + "-comp");
	    compartment.setType("compartment");
	    compartment.setLayout("vbox");
	    compartment.setChildren(new ArrayList<>());
	    centerNode(compartment);
	    
	    SLabel label = new SLabel();
	    label.setId(id + "-lanbel");
	    label.setType("node:label");
	    label.setText(labelText);
	    centerNode(label);
	    
	    compartment.getChildren().add(label);

	    label = new SLabel();
	    label.setId(id + "-lanbel1");
	    label.setType("node:label");
	    label.setText("s");
	    centerNode(label);
	    
	    compartment.getChildren().add(label);

	    node.getChildren().add(compartment);
	    
	    SPort outputPort = new SPort();
	    outputPort.setType("output-port");
	    outputPort.setId("output-port-" + id);
	    outputPort.setSize(new Dimension(10,10));	    
	    node.getChildren().add(outputPort);
	    
	    SPort inputPort = new SPort();
	    inputPort.setType("input-port");
	    inputPort.setId("input-port-" + id);
	    inputPort.setSize(new Dimension(10,10));
	    node.getChildren().add(inputPort);
	    	    
	    return node;
	}
	
	private static void centerNode(SShapeElement shape) {
		LayoutOptions options = shape.getLayoutOptions();
		if (options == null) {
			options = new LayoutOptions();
			shape.setLayoutOptions(options);
		}
		options.setHAlign("center");
		options.setVAlign("center");
	}
	
	private static SNode createIntegrationNode(String id, String labelText) {
		SNode node = createNode(id, labelText, "integration_node");
		LayoutOptions layoutOptions = new LayoutOptions();
		layoutOptions.setMinHeight(NODE_MIN_HEIGHT);
		layoutOptions.setMinWidth(MIN_WIDTH);
		node.setLayoutOptions(layoutOptions);
		
		SPort errorPort = new SPort();
		errorPort.setType("error-port");
		errorPort.setId("error-port-" + id);
		errorPort.setSize(new Dimension(10, 10));
		node.getChildren().add(errorPort);

		return node;
	}
	
	private static SNode createChannelNode(String id, String labelText) {
		SNode node = createNode(id, labelText, "channel");
		LayoutOptions layoutOptions = new LayoutOptions();
		layoutOptions.setMinHeight(CHANNEL_HEIGHT);
		layoutOptions.setMinWidth(MIN_WIDTH);
		node.setLayoutOptions(layoutOptions);
		
		return node;
	}
	
	private static String visualType(String type) {
		if (channelTypes.contains(type)) {
			return "channel";
		} else {
			return "integration_node";
		}
	}

	private static SEdge createEdge(String id, String sourceId, String targetId, String labelStr, String linkType) {
		SEdge edge = new SEdge();
		edge.setId(id);
		edge.setType("edge:straight");
		if ("error".equals(linkType)) {
			edge.setSourceId("error-port-" + sourceId);
		} else {
			edge.setSourceId("output-port-" + sourceId);
		}
		edge.setTargetId("input-port-" + targetId);
		
		if (StringUtil.hasText(labelStr)) {
			SLabel label = new SLabel();
			label.setType("node:label");
			EdgePlacement edgePlacement = new EdgePlacement();
			edgePlacement.setRotate(true);
			edgePlacement.setPosition(0.0);
			edgePlacement.setSide(Side.right);
			label.setEdgePlacement(edgePlacement);
			label.setId(id + "-label1");
			label.setText(labelStr);
			
			label.setPosition(new Point(20,30));
			ArrayList<SModelElement> children = new ArrayList<>();
			children.add(label);
			edge.setChildren(children);
		}
		
		return edge;
	}


}
