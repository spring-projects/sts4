package org.springframework.ide.si.view.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SpringIntegrationNodeJson extends PrettyJson {

	private int nodeId;
	private String name;
	private String componentType;
	private JsonObject properties;
	
	private String input;
	private String output;
	private String errors;
	
	private JsonElement sendTimers;
	private JsonElement receiveCounters;
	
	private String[] routes;
	
	public int getNodeId() {
		return nodeId;
	}
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getComponentType() {
		return componentType;
	}
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}
	public JsonObject getProperties() {
		return properties;
	}
	public void setProperties(JsonObject properties) {
		this.properties = properties;
	}
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	public String getErrors() {
		return errors;
	}
	public void setErrors(String errors) {
		this.errors = errors;
	}
	public String[] getRoutes() {
		return routes;
	}
	public void setRoutes(String[] routes) {
		this.routes = routes;
	}
	public JsonElement getSendTimers() {
		return sendTimers;
	}
	public void setSendTimers(JsonElement sendTimers) {
		this.sendTimers = sendTimers;
	}
	public JsonElement getReceiveCounters() {
		return receiveCounters;
	}
	public void setReceiveCounters(JsonElement receiveCounters) {
		this.receiveCounters = receiveCounters;
	}
}
