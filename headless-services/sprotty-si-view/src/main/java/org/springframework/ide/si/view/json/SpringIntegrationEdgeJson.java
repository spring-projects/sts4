package org.springframework.ide.si.view.json;

public class SpringIntegrationEdgeJson extends PrettyJson {
	
	private int from;
	private int to;
	private String type;
	
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
