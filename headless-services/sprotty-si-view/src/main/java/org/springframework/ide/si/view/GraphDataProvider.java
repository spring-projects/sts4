package org.springframework.ide.si.view;

import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.ide.si.view.json.SpringIntegrationGraphJson;
import org.springframework.util.Assert;

import com.google.gson.Gson;

@FunctionalInterface
public interface GraphDataProvider {
	SpringIntegrationGraphJson getGraph() throws Exception;
	
	static GraphDataProvider fromClasspathResource(String path) {
		if (!path.startsWith("/")) {
			path = "/"+path;
		}
		URL url = GraphDataProvider.class.getResource(path);
		Assert.notNull(url, "Resource not found: "+path);
		return fromUrl(GraphDataProvider.class.getResource(path));
	}

	static GraphDataProvider fromUrl(URL resource) {
		return () -> {
			String jsonString = IOUtils.toString(resource);
			return new Gson().fromJson(jsonString, SpringIntegrationGraphJson.class);
		};
	}
}
