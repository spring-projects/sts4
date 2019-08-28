package org.springframework.ide.si.view.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SpringIntegrationGraph extends PrettyJson {

	private JsonObject contentDescriptor;
	private SpringIntegrationNode[] nodes; 
	private SpringIntegrationEdge[] links;
	
	public JsonObject getContentDescriptor() {
		return contentDescriptor;
	}
	public void setContentDescriptor(JsonObject contentDescriptor) {
		this.contentDescriptor = contentDescriptor;
	}
	public SpringIntegrationNode[] getNodes() {
		return nodes;
	}
	public void setNodes(SpringIntegrationNode[] nodes) {
		this.nodes = nodes;
	}
	public SpringIntegrationEdge[] getLinks() {
		return links;
	}
	public void setLinks(SpringIntegrationEdge[] links) {
		this.links = links;
	}
}
