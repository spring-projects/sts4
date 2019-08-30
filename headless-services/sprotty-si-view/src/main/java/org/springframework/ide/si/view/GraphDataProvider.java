package org.springframework.ide.si.view;

import java.net.URL;

import org.eclipse.sprotty.RequestModelAction;
import org.springframework.ide.si.view.json.SpringIntegrationGraphJson;
import org.springframework.util.Assert;

@FunctionalInterface
public interface GraphDataProvider {
	SpringIntegrationGraphJson getGraph(RequestModelAction modelRequest) throws Exception;
	
	static GraphDataProvider fromClasspathResource(String path) {
		if (!path.startsWith("/")) {
			path = "/"+path;
		}
		URL url = GraphDataProvider.class.getResource(path);
		Assert.notNull(url, "Resource not found: "+path);
		return fromUrl(GraphDataProvider.class.getResource(path));
	}

	static GraphDataProvider fromUrl(URL resource) {
		return (RequestModelAction modelRequest) -> {
			return SpringIntegrationGraphJson.readFrom(resource);
		};
	}

	static GraphDataProvider fromUrlOption(String propName) {
		return (RequestModelAction modelRequest) -> {
			String urlStr = modelRequest.getOptions().get(propName);
			Assert.hasText(urlStr, "Url must be provided in modelRequest.options."+propName);
			URL url = new URL(modelRequest.getOptions().get(propName));
			return SpringIntegrationGraphJson.readFrom(url);
		};
	}
}
