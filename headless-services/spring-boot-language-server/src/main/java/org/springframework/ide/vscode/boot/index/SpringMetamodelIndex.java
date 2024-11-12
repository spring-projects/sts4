/*******************************************************************************
 * Copyright (c) 2023, 2024 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.commons.protocol.spring.Bean;

public class SpringMetamodelIndex {
	
	private final ConcurrentMap<String, Bean[]> beansPerProject;

	public SpringMetamodelIndex() {
		beansPerProject = new ConcurrentHashMap<>();
	}
	
	public void updateBeans(String projectName, Bean[] beanDefinitions) {
		beansPerProject.put(projectName, beanDefinitions);
	}

	public void updateBeans(String projectName, String docURI, Bean[] beanDefinitions) {
		Bean[] existingBeans = beansPerProject.putIfAbsent(projectName, beanDefinitions);

		if (existingBeans != null) {

			List<Bean> beans = new ArrayList<>();
			
			// add old, unrelated beans
			for (Bean bean : existingBeans) {
				if (!bean.getLocation().getUri().equals(docURI)) {
					beans.add(bean);
				}
			}

			// add new beans for doc URI
			beans.addAll(Arrays.asList(beanDefinitions));
			
			// set new beans set
			beansPerProject.put(projectName, (Bean[]) beans.toArray(new Bean[beans.size()]));
		}
	}

	public void removeBeans(String projectName) {
		beansPerProject.remove(projectName);
	}

	public void removeBeans(String projectName, String docURI) {
		Bean[] oldBeans = beansPerProject.get(projectName);
		if (oldBeans != null) {
			List<Bean> newBeans = Arrays.stream(oldBeans)
				.filter(bean -> !bean.getLocation().getUri().equals(docURI))
				.collect(Collectors.toList());
			
			beansPerProject.put(projectName, (Bean[]) newBeans.toArray(new Bean[newBeans.size()]));
		}
	}

	public Bean[] getBeans() {
		List<Bean> result = new ArrayList<>();
		
		for (Bean[] beans : beansPerProject.values()) {
			for (Bean bean : beans) {
				result.add(bean);
			}
		}
		
		return (Bean[]) result.toArray(new Bean[result.size()]);
	}

	public Bean[] getBeansOfProject(String projectName) {
		return beansPerProject.get(projectName);
	}
	
	public Bean[] getBeansOfDocument(String docURI) {
		List<Bean> result = new ArrayList<>();
		
		for (Bean[] beans : beansPerProject.values()) {
			for (Bean bean : beans) {
				if (bean.getLocation().getUri().equals(docURI)) {
					result.add(bean);
				}
			}
		}
		
		return (Bean[]) result.toArray(new Bean[result.size()]);
	}
	
	public Bean[] getBeansWithName(String project, String name) {
		Bean[] allBeans = this.beansPerProject.get(project);
		
		if (allBeans != null) {
			return Arrays.stream(allBeans).filter(bean -> bean.getName().equals(name)).collect(Collectors.toList()).toArray(new Bean[0]);
		}
		else {
			return null;
		}
	}

	public Bean[] getMatchingBeans(String projectName, String matchType) {
		Bean[] allBeans = this.beansPerProject.get(projectName);
		
		if (allBeans != null) {
			return Arrays.stream(allBeans).filter(bean -> bean.isTypeCompatibleWith(matchType)).collect(Collectors.toList()).toArray(new Bean[0]);
		}
		else {
			return null;
		}
	}

}
