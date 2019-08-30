package org.springframework.ide.si.view;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.eclipse.sprotty.IDiagramServer;
import org.eclipse.sprotty.IPopupModelFactory;
import org.eclipse.sprotty.PreRenderedElement;
import org.eclipse.sprotty.RequestPopupModelAction;
import org.eclipse.sprotty.SGraph;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SModelRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.si.view.json.SpringIntegrationNodeJson;
import org.springframework.ide.vscode.commons.util.HtmlBuffer;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class SpringIntegrationPopupFactory implements IPopupModelFactory {
	
	@Autowired SIDiagramGenerator siDiagram;

	@Override
	public SModelRoot createPopupModel(SModelElement element, RequestPopupModelAction request, IDiagramServer server) {
		SpringIntegrationNodeJson nodeData = siDiagram.getNodeData(request.getElementId());
		if (nodeData!=null) {
			SGraph popup = new SGraph();
			popup.setType("html");
			popup.setId("popup");
			popup.setCanvasBounds(request.getBounds());
			
			ArrayList<SModelElement> children = new ArrayList<>();
			popup.setChildren(children);
	
			addHtmlChild(children, "popup-title", createTitle(nodeData));
			addHtmlChild(children, "popup-body", createBody(nodeData));
			
			return popup;
		}
		return SModelRoot.EMPTY_ROOT;
	}

	private void addHtmlChild(ArrayList<SModelElement> children, String id, HtmlBuffer html) {
		PreRenderedElement content = new PreRenderedElement();
		content.setId(id);
		content.setCode(html.toString());
		children.add(content);
	}

	protected HtmlBuffer createTitle(SpringIntegrationNodeJson nodeData) {
		HtmlBuffer html = new HtmlBuffer();
		html.bold("Full Json Data");
		return html;
	}
	protected HtmlBuffer createBody(SpringIntegrationNodeJson _nodeData) {
		JsonObject nodeData = (JsonObject) new Gson().toJsonTree(_nodeData);
		HtmlBuffer html = new HtmlBuffer();
		
//		html.raw("<table width="100%">");
//		html.raw("<tr>");
//		html.raw("<td>a</td>");
//		html.raw("<td>b</td>");
//		html.raw("</tr>");
//		html.raw("<tr>");
//		html.raw("<td>cc</td>");
//		html.raw("<td>de</td>");
//		html.raw("</tr>");
//		html.raw("</table>");
		
		html.raw("<table width=\"100%\">");
		for (Entry<String, JsonElement> e : nodeData.entrySet()) {
			String key = e.getKey();
			dumpProperty(html, key, nodeData.get(key));
		}
		html.raw("</table>");
		return html;
	}

	private void dumpProperty(HtmlBuffer html, String key, JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive()) {
			String value = jsonElement.getAsString();
			html.raw("<tr>");
				html.raw("<td>");
					html.text(key);
				html.raw("</td>");
				html.raw("<td>");
					html.text(value);
				html.raw("</td>");
			html.raw("</tr>");
		} else if (jsonElement.isJsonArray()) {
			JsonArray array = (JsonArray) jsonElement;
			for (int i = 0; i < array.size(); i++) {
				dumpProperty(html, key+"["+i+"]", array.get(i));
			}
		} else if (jsonElement.isJsonObject()) {
			for (Entry<String, JsonElement> e : jsonElement.getAsJsonObject().entrySet()) {
				dumpProperty(html, key+"."+e.getKey(), e.getValue());
			}
		}
	}
}
