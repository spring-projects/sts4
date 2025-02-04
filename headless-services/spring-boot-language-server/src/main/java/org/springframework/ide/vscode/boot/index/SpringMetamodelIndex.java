/*******************************************************************************
 * Copyright (c) 2023, 2025 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.index;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.DocumentElement;
import org.springframework.ide.vscode.commons.protocol.spring.ProjectElement;
import org.springframework.ide.vscode.commons.protocol.spring.SpringIndexElement;

public class SpringMetamodelIndex {
	
	private final ConcurrentMap<String, ProjectElement> projectRootElements;

	public SpringMetamodelIndex() {
		projectRootElements = new ConcurrentHashMap<>();
	}
	
	public void updateBeans(String projectName, Bean[] beanDefinitions) {
		ProjectElement projectRoot = new ProjectElement(projectName);
		
		Map<String, DocumentElement> documents = new HashMap<>();
		for (Bean bean : beanDefinitions) {
			String docURI = bean.getLocation() != null ? bean.getLocation().getUri() : null;
			
			if (docURI != null) {
				
				DocumentElement document = documents.computeIfAbsent(docURI, uri -> {
					DocumentElement newDocument = new DocumentElement(uri);
					projectRoot.addChild(newDocument);
					return newDocument;
				});

				document.addChild(bean);
			}
			else {
				projectRoot.addChild(bean);
			}
		}
		
		projectRootElements.put(projectName, projectRoot);
	}

	public void updateElements(String projectName, String docURI, SpringIndexElement[] beanDefinitions) {
		ProjectElement project = this.projectRootElements.computeIfAbsent(projectName, name -> new ProjectElement(name));
		project.removeDocument(docURI);
		
		DocumentElement document = new DocumentElement(docURI);
		for (SpringIndexElement bean : beanDefinitions) {
			document.addChild(bean);
		}
		
		project.addChild(document);
	}

	public void removeProject(String projectName) {
		projectRootElements.remove(projectName);
	}

	public void removeElements(String projectName, String docURI) {
		ProjectElement project = projectRootElements.get(projectName);
		if (project != null) {
			project.removeDocument(docURI);
		}
	}
	
	public DocumentElement getDocument(String docURI) {
		ArrayDeque<SpringIndexElement> elementsToVisit = new ArrayDeque<>();
		elementsToVisit.addAll(this.projectRootElements.values());
		
		while (!elementsToVisit.isEmpty()) {
			SpringIndexElement element = elementsToVisit.pop();

			if (element instanceof DocumentElement doc && doc.getDocURI().equals(docURI)) {
				return doc;
			}

			elementsToVisit.addAll(element.getChildren());
		}
		
		return null;
	}

	public <T extends SpringIndexElement> List<T> getNodesOfType(Class<T> type) {
		List<T> result = new ArrayList<>();
		
		ArrayDeque<SpringIndexElement> elementsToVisit = new ArrayDeque<>();
		elementsToVisit.addAll(this.projectRootElements.values());
		
		while (!elementsToVisit.isEmpty()) {
			SpringIndexElement element = elementsToVisit.pop();

			if (type.isInstance(element)) {
				result.add(type.cast(element));
			}
			
			elementsToVisit.addAll(element.getChildren());
		}
		
		return result;
	}

	public Bean[] getBeans() {
		List<Bean> result = new ArrayList<>();
		
		ArrayDeque<SpringIndexElement> elementsToVisit = new ArrayDeque<>();
		elementsToVisit.addAll(this.projectRootElements.values());
		
		while (!elementsToVisit.isEmpty()) {
			SpringIndexElement element = elementsToVisit.pop();

			if (element instanceof Bean bean) {
				result.add(bean);
			}
			
			elementsToVisit.addAll(element.getChildren());
		}
		
		return (Bean[]) result.toArray(new Bean[result.size()]);
	}

	public Bean[] getBeansOfProject(String projectName) {
		List<Bean> result = new ArrayList<>();
		
		ProjectElement project = this.projectRootElements.get(projectName);
		if (project != null) {
			ArrayDeque<SpringIndexElement> elementsToVisit = new ArrayDeque<>();
			elementsToVisit.push(project);
		
			while (!elementsToVisit.isEmpty()) {
				SpringIndexElement element = elementsToVisit.pop();

				if (element instanceof Bean bean) {
					result.add(bean);
				}

				elementsToVisit.addAll(element.getChildren());
			}
		}
		
		return (Bean[]) result.toArray(new Bean[result.size()]);
	}
	
	public Bean[] getBeansOfDocument(String docURI) {
		List<Bean> result = new ArrayList<>();
		
		ArrayDeque<SpringIndexElement> elementsToVisit = new ArrayDeque<>();
		elementsToVisit.addAll(this.projectRootElements.values());
		
		while (!elementsToVisit.isEmpty()) {
			SpringIndexElement element = elementsToVisit.pop();

			if (element instanceof Bean bean) {
				result.add(bean);
			}
			
			if (element instanceof DocumentElement doc) {
				if (doc.getDocURI().equals(docURI)) {
					elementsToVisit.addAll(doc.getChildren());
				}
				// else do not look into other document structures
			}
			else {
				elementsToVisit.addAll(element.getChildren());
			}
		}
		
		return (Bean[]) result.toArray(new Bean[result.size()]);
	}
	
	public Bean[] getBeansWithName(String projectName, String name) {
		List<Bean> result = new ArrayList<>();

		ProjectElement project = this.projectRootElements.get(projectName);
		if (project != null) {
			ArrayDeque<SpringIndexElement> elementsToVisit = new ArrayDeque<>();
			elementsToVisit.push(project);

			while (!elementsToVisit.isEmpty()) {
				SpringIndexElement element = elementsToVisit.pop();

				if (element instanceof Bean bean && bean.getName().equals(name)) {
					result.add(bean);
				}

				elementsToVisit.addAll(element.getChildren());
			}
		}

		return (Bean[]) result.toArray(new Bean[result.size()]);
	}

	public Bean[] getBeansWithType(String projectName, String type) {
		List<Bean> result = new ArrayList<>();

		ProjectElement project = this.projectRootElements.get(projectName);
		if (project != null) {
			ArrayDeque<SpringIndexElement> elementsToVisit = new ArrayDeque<>();
			elementsToVisit.push(project);

			while (!elementsToVisit.isEmpty()) {
				SpringIndexElement element = elementsToVisit.pop();

				if (element instanceof Bean bean && bean.getType().equals(type)) {
					result.add(bean);
				}

				elementsToVisit.addAll(element.getChildren());
			}
		}

		return (Bean[]) result.toArray(new Bean[result.size()]);
	}

	public Bean[] getMatchingBeans(String projectName, String matchType) {
		List<Bean> result = new ArrayList<>();

		ProjectElement project = this.projectRootElements.get(projectName);
		if (project != null) {
			ArrayDeque<SpringIndexElement> elementsToVisit = new ArrayDeque<>();
			elementsToVisit.push(project);

			while (!elementsToVisit.isEmpty()) {
				SpringIndexElement element = elementsToVisit.pop();

				if (element instanceof Bean bean && bean.isTypeCompatibleWith(matchType)) {
					result.add(bean);
				}

				elementsToVisit.addAll(element.getChildren());
			}
		}

		return (Bean[]) result.toArray(new Bean[result.size()]);
	}

}
