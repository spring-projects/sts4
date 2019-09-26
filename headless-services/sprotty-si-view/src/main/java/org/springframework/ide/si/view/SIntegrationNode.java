package org.springframework.ide.si.view;

import java.util.ArrayList;

import org.eclipse.sprotty.SLabel;
import org.eclipse.sprotty.SNode;
import org.springframework.ide.si.view.json.SpringIntegrationNodeJson;

public class SIntegrationNode extends SNode {

	private String componentType;
	
	{
		setType("node:integration");
		setChildren(new ArrayList<>());
	}

	public SIntegrationNode(SpringIntegrationNodeJson json) {
		this.setComponentType(json.getComponentType());
		this.getChildren().add(new SLabel(l -> {
			l.setType("node:label");
			l.setText(json.getName());
		}));
		this.setId(""+json.getNodeId());
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}
	
	@Override
	public void setLayout(String layout) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getLayout() {
		return null;
	}
}
