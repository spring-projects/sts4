/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Basic implementation of File Observer interface
 * 
 * @author Alex Boyko
 *
 */
public class BasicFileObserver implements FileObserver {
	
	protected ConcurrentHashMap<String, ImmutablePair<List<PathMatcher>, Consumer<String>>> createRegistry = new ConcurrentHashMap<>(); 
	protected ConcurrentHashMap<String, ImmutablePair<List<PathMatcher>, Consumer<String>>> deleteRegistry = new ConcurrentHashMap<>(); 
	protected ConcurrentHashMap<String, ImmutablePair<List<PathMatcher>, Consumer<String>>> changeRegistry = new ConcurrentHashMap<>(); 
	
	@Override
	public String onFileCreated(List<String> globPattern, Consumer<String> handler) {
		return registerFileListener(createRegistry, globPattern, handler);
	}

	@Override
	public String onFileChanged(List<String> globPattern, Consumer<String> handler) {
		return registerFileListener(changeRegistry, globPattern, handler);
	}

	@Override
	public String onFileDeleted(List<String> globPattern, Consumer<String> handler) {
		return registerFileListener(deleteRegistry, globPattern, handler);
	}

	@Override
	public boolean unsubscribe(String subscriptionId) {
		if (createRegistry.remove(subscriptionId) != null) {
			return true;
		}
		if (changeRegistry.remove(subscriptionId) != null) {
			return true;
		}
		if (deleteRegistry.remove(subscriptionId) != null) {
			return true;
		}
		return false;
	}
	
	private static String registerFileListener(Map<String, ImmutablePair<List<PathMatcher>, Consumer<String>>> registry, List<String> globPattern, Consumer<String> handler) {
		String subscriptionId = UUID.randomUUID().toString();
		registry.put(subscriptionId, ImmutablePair.of(globPattern.stream().map(g -> FileSystems.getDefault().getPathMatcher("glob:" + g)).collect(Collectors.toList()), handler));
		return subscriptionId;
	}
	
	final public void notifyFileCreated(String uri) {
		notify(createRegistry, uri);
	}
	
	final public void notifyFileChanged(String uri) {
		notify(changeRegistry, uri);
	}
	
	final public void notifyFileDeleted(String uri) {
		notify(deleteRegistry, uri);
	}
	
	private static void notify(Map<String, ImmutablePair<List<PathMatcher>, Consumer<String>>> registry, String uri) {
		Path path = Paths.get(URI.create(uri));
		registry.values().stream()
			.filter(pair -> pair.left.stream()
					.filter(matcher -> matcher.matches(path))
					.findFirst()
					.isPresent())
			.forEach(pair -> pair.right.accept(uri));
	}

}
