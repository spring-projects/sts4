package org.springframework.ide.vscode.commons.cloudfoundry.client;

public interface CFRoute {

	int NO_PORT = -1;
	String EMPTY_ROUTE = "";
	
	static CFRouteBuilder builder() {
		return new CFRouteBuilder();
	}

	String getDomain();

	String getHost();

	String getPath();

	int getPort();

	String getRoute();

}
