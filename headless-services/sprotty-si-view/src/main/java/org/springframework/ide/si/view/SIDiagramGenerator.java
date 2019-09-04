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
import org.springframework.ide.si.view.json.SpringIntegrationEdgeJson;
import org.springframework.ide.si.view.json.SpringIntegrationGraphJson;
import org.springframework.ide.si.view.json.SpringIntegrationNodeJson;
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

	private static final double INTEGRATION_NODE_VERTICAL_PADDING = 20.0;

	static final String TYPE_INTEGRATION_GRAPH = "graph";

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

	Map<String, SpringIntegrationNodeJson> nodesById = new HashMap<>();

	/**
	 * Fetch node data for given node id. This data is retrieved from the
	 * model produced the last time 'generateModel' was called.
	 */
	public synchronized SpringIntegrationNodeJson getNodeData(String nodeId) {
		return nodesById.get(nodeId);
	}

	@Override
	public synchronized SModelRoot generateModel(String clientId, RequestModelAction modelRequest) {
		nodesById.clear();
		try {
			SpringIntegrationGraphJson jsonData = graphDataProvider.getGraph(modelRequest);
			return toSprottyGraph(jsonData);
		} catch (Exception e) {
			log.error("", e);
			return SModelRoot.EMPTY_ROOT;
		}
	}

	private SGraph toSprottyGraph(SpringIntegrationGraphJson json) throws Exception {
		SGraph graph = new SGraph();
		graph.setId("root");
		graph.setType(TYPE_INTEGRATION_GRAPH);

		List<SModelElement> children = new ArrayList<>();
		graph.setChildren(children);

		for (SpringIntegrationNodeJson node : json.getNodes()) {
			String id = node.getNodeId()+"";
			String name = node.getName();
			Assert.isLegal(!nodesById.containsKey(id));
			nodesById.put(id, node);
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
		for (SpringIntegrationEdgeJson link : json.getLinks()) {
			String id = "l"+(linkId++);
			String sourceId = link.getFrom()+"";
			String targetId = link.getTo()+"";

			SEdge e = createEdge(id, sourceId, targetId, getEdgeLabel(nodesById.get(sourceId)), link.getType());
			children.add(e);
		}

		return graph;
	}

	private String getEdgeLabel(SpringIntegrationNodeJson springIntegrationNode) {
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
		node.setSize(new Dimension(80, 80));
		node.setChildren(new ArrayList<>());
		centerNode(node);

		{
			SLabel label = new SLabel();
			label.setId(id + "-label");
			label.setType("node:label");
			label.setText(labelText);
			centerNode(label);
			node.getChildren().add(label);
		}

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
		layoutOptions.setMinWidth(MIN_WIDTH);
		layoutOptions.setPaddingBottom(INTEGRATION_NODE_VERTICAL_PADDING);
		layoutOptions.setPaddingTop(20.0);
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
		//		layoutOptions.setMinHeight(CHANNEL_HEIGHT);
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
