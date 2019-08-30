package org.springframework.ide.si.view.json;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
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
	public static SpringIntegrationGraphJson readFrom(URL resource) throws IOException {
		String jsonString = IOUtils.toString(resource);
		return new Gson().fromJson(jsonString, SpringIntegrationGraphJson.class);
	}
}
