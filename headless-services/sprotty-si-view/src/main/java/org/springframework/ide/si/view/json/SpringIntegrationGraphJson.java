package org.springframework.ide.si.view.json;

import com.google.gson.JsonObject;

public class SpringIntegrationGraphJson extends PrettyJson {

	private JsonObject contentDescriptor;
	private SpringIntegrationNodeJson[] nodes; 
	private SpringIntegrationEdgeJson[] links;
	
	public JsonObject getContentDescriptor() {
		return contentDescriptor;
	}
	public void setContentDescriptor(JsonObject contentDescriptor) {
		this.contentDescriptor = contentDescriptor;
	}
	public SpringIntegrationNodeJson[] getNodes() {
		return nodes;
	}
	public void setNodes(SpringIntegrationNodeJson[] nodes) {
		this.nodes = nodes;
	}
	public SpringIntegrationEdgeJson[] getLinks() {
		return links;
	}
	public void setLinks(SpringIntegrationEdgeJson[] links) {
		this.links = links;
	}
}
