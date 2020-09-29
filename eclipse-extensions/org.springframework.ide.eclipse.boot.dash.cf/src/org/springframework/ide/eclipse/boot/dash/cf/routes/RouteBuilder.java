/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.routes;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFDomainStatus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

/**
 * This class implements the logics of translating from the multitude of attributes in a
 * `manifest.mf` like 'random-route', 'no-host', 'domain', 'domains', etc. into concrete
 * 'route bindings' that can be passed of to a lower-level cf client operation such as `mapRoute`
 * to bind a route to an app.
 * <p>
 * This process is somewhat complex because of the multitude of attributes, and their interactions,
 * as well as different interpretations of some attributes depending on the target domain being a
 * TCP vs HTTP domain.
 *
 * @author Kris De Volder
 */
public class RouteBuilder {

	private Map<String, CFCloudDomain> domainsByName = new LinkedHashMap<>();
	private String defaultDomain;

	public RouteBuilder(Collection<CFCloudDomain> domains) {
		ImmutableMap.Builder<String, CFCloudDomain> builder = ImmutableMap.builder();
		for (CFCloudDomain d : domains) {
			builder.put(d.getName(), d);
		}
		domainsByName = builder.build();
	}

	/**
	 * Builds the list of route bindings from a parsed manifest.yml's attributes.
	 * <p>
	 * The routes builder makes implicit assumptions that the manifest doesn't have
	 * conflicting attributes (e.g. `host` and `random-route`).
	 * <p>
	 * If conflicting attributes *are* present then the routes builder will ignore
	 * some of the conflicting attributes in a somewhat arbitrary way. So it will still
	 * compute a list of route bindings rather than raise an exception.
	 * <p>
	 * A similar approach is taken for 'routes' definitions that can't be mapped onto
	 * the list of known domains in a valid way. These routes will simply be silently
	 * ignored.
	 */
	public List<RouteBinding> buildRoutes(RouteAttributes manifest) {
		if (manifest.isNoRoute()) {
			return ImmutableList.of();
		}
		if (manifest.isRandomRoute()) {
			return buildRandomRoutes(manifest);
		}
		if (manifest.getRoutes()!=null) {
			return buildRoutesFromUris(manifest);
		}
		return buildRoutesFromHostsAndDomains(manifest);
	}

	private List<RouteBinding> buildRoutesFromHostsAndDomains(RouteAttributes manifest) {
		if (manifest.isNoHost()) {
			List<String> domains = getDomains(manifest);
			ImmutableList.Builder<RouteBinding> routes = ImmutableList.builder();
			for (String domain : domains) {
				RouteBinding route = new RouteBinding();
				route.setDomain(domain);
				routes.add(route);
			}
			return routes.build();
		} else {
			List<String> hosts = getHosts(manifest);
			List<String> domains = getDomains(manifest);
			ImmutableList.Builder<RouteBinding> routes = ImmutableList.builder();
			for (String host : hosts) {
				for (String domain : domains) {
					RouteBinding route = new RouteBinding();
					route.setHost(host);
					route.setDomain(domain);
					routes.add(route);
				}
			}
			return routes.build();
		}
	}

	protected List<RouteBinding> buildRoutesFromUris(RouteAttributes manifest) {
		Builder<RouteBinding> builder = ImmutableList.builder();
		for (String desiredUri : manifest.getRoutes()) {
			RouteBinding route = buildRouteFromUri(desiredUri, manifest);
			if (route!=null) {
				builder.add(route);
			}
		}
		return builder.build();
	}

	protected List<RouteBinding> buildRandomRoutes(RouteAttributes manifest) {
		Builder<RouteBinding> builder = ImmutableList.builder();
		List<String> domains = getDomains(manifest);
		for (String domain : domains) {
			RouteBinding route = new RouteBinding();
			route.setDomain(domain);
			if (isTcpDomain(domain)) {
				route.setPort(Randomized.random());
			} else {
				route.setHost(Randomized.random());
			}
			builder.add(route);
		}
		return builder.build();
	}

	private boolean isTcpDomain(String domainName) {
		CFCloudDomain d = domainsByName.get(domainName);
		if (d!=null) {
			return d.getType()==CFDomainType.TCP;
		}
		return false;
	}

	private List<String> getHosts(RouteAttributes manifest) {
		Set<String> builder = new LinkedHashSet<>();
		String host = manifest.getHost();
		if (host!=null) {
			builder.add(host);
		}
		List<String> hosts = manifest.getHosts();
		if (hosts!=null) {
			builder.addAll(hosts);
		}
		if (builder.isEmpty()) {
			String appName = manifest.getAppName();
			if (appName!=null) {
				builder.add(appName);
			}
		}
		return ImmutableList.copyOf(builder);
	}

	private List<String> getDomains(RouteAttributes manifest) {
		Set<String> domains = new LinkedHashSet<>();
		List<String> ds = manifest.getDomains();
		if (ds!=null) {
			domains.addAll(ds);
		}
		String d = manifest.getDomain();
		if (d!=null) {
			domains.add(d);
		}
		if (domains.isEmpty()) {
			//This assumes 'getDomains' is only called in context where we actually
			// want the domains from the 'domain' or 'domains' attributes. So, not
			// for example, in a manifest with a 'routes' or a 'no-routes'.
			//In these contexts, of the list of domains is empty, we generally want
			// to fallback to using the defaultDomain.
			String dflt = getDefaultDomain();
			if (dflt!=null) {
				domains.add(dflt);
			}
		}
		return ImmutableList.copyOf(domains);
	}

	/**
	 * Create a RouteBinding from a 'target' uri. Such a routebinding is always specific
	 * and doesn't contain randomized components (i.e. no random host/port).
	 */
	private RouteBinding buildRouteFromUri(String _uri, RouteAttributes args) {
		ParsedUri uri = new ParsedUri(_uri);
		CFCloudDomain bestDomain = domainsByName.values().stream()
			.filter(domain -> matches(domain, uri))
			.max((d1, d2) -> Integer.compare(d1.getName().length(), d2.getName().length()))
			.orElse(null);
		if (bestDomain!=null) {
			RouteBinding route = new RouteBinding();
			route.setDomain(bestDomain.getName());
			route.setHost(bestDomain.splitHost(uri.getHostAndDomain()));
			route.setPath(uri.getPath());
			route.setPort(uri.getPort());
			return route;
		}
		return null;
	}

	public String getDefaultDomain() {
		if (defaultDomain==null) {
			defaultDomain = getDomains().stream()
					.filter(d -> d.getStatus()==CFDomainStatus.SHARED && d.getType()==CFDomainType.HTTP)
					.findFirst()
					.map(d -> d.getName())
					.orElse(null);
		}
		return defaultDomain;
	}

	private Collection<CFCloudDomain> getDomains() {
		return domainsByName.values();
	}

	/**
	 * Determines whether a given domain can be used to construct a route for a given
	 * target uri. This check is strictly based on the names of the domain and target uri,
	 * type of the domain/uri doesn't play into it (so, for example there are no 'smarts' here
	 * to detect whether or not a domain's type is compatible with the uri type).
	 */
	private boolean matches(CFCloudDomain domainData, ParsedUri uri) {
		String domain = domainData.getName();
		String hostAndDomain = uri.getHostAndDomain();
		if (!hostAndDomain.endsWith(domain)) {
			return false;
		}
		if (domain.length()==hostAndDomain.length()) {
			//The uri matches domain precisely
			return true;
		} else if (hostAndDomain.charAt(hostAndDomain.length()-domain.length()-1)=='.') {
			//The uri matches as ${host}.${domain}
			return true;
		}
		return false;
	}
}
