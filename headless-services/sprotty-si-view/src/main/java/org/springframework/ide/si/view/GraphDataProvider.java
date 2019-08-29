package org.springframework.ide.si.view;

import org.springframework.ide.si.view.json.SpringIntegrationGraph;

public interface GraphDataProvider {
	SpringIntegrationGraph getGraph() throws Exception;
}
