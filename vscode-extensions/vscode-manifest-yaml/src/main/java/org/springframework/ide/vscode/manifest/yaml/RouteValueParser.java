package org.springframework.ide.vscode.manifest.yaml;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFRoute;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileException;
import org.springframework.ide.vscode.commons.util.RegexpParser;
import org.springframework.ide.vscode.commons.util.ValueParseException;

public class RouteValueParser extends RegexpParser {
	
	private static final String ROUTE_REGEX = "^([\\da-z\\.-]+)(:\\d{1,4})?((\\/[\\dA-Za-z\\.-]+)*\\/?)?$";
	private static final String ROUTE_TYPE_NAME = "Route";
	private static final String ROUTE_DESCRIPTION = "HTTP or TCP application root route";
	
	private Callable<Collection<String>> domains;

	public RouteValueParser(Callable<Collection<String>> domains) {
		super(ROUTE_REGEX, ROUTE_TYPE_NAME, ROUTE_DESCRIPTION);
		this.domains = domains;
	}

	@Override
	public Object parse(String str) throws Exception {
		Matcher matcher = (Matcher) super.parse(str);
		if (matcher != null) {
			Collection<String> cloudDomains = domains == null ? Collections.emptyList() : domains.call();
			// Ensure cloud domains is empty list instead of null
			if (cloudDomains == null) {
				cloudDomains = Collections.emptyList();
			}
			CFRoute route = CFRoute.builder().from(str, cloudDomains).build();
			if (route.getDomain() == null || route.getDomain().isEmpty()) {
				throw new ValueParseException("Domain is missing.");
			}
			if ((route.getPath() != null && !route.getPath().isEmpty()) && (route.getPort() != CFRoute.NO_PORT)) {
				throw new ValueParseException(
						"Unable to determine type of route. HTTP port may have a path but no port. TCP route may have port but no path.");
			}
			if (!cloudDomains.contains(route.getDomain())) {
				String hostDomain = matcher.group(1);
				throw new ReconcileException("Unknown domain", ManifestYamlSchemaProblemsTypes.UNKNOWN_DOMAIN_PROBLEM, hostDomain.lastIndexOf(route.getDomain()), hostDomain.length());
			}
			return route;
		}
		return null;
	}
	
}
