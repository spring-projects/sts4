/*******************************************************************************
 * Copyright (c) 2012, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parsing of Boot 1.x live beans. Differences between Boot 1.x and other Boot versions should be handled by protected API that can be overridden
 * by more recent Boot version subclasses.
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class LiveBeansJsonParser {

	private final TypeLookup typeLookup;

	private final String jsonInput;

	private Map<String, LiveBean> beansMap;

	private Map<String, LiveBeansContext> contextMap;

	private Map<String, LiveBeansResource> resourceMap;

	public LiveBeansJsonParser(TypeLookup typeLookup, String jsonInput) {
		this.jsonInput = jsonInput;
		this.typeLookup = typeLookup;
	}

	private void groupByResource() {
		for (LiveBean bean : beansMap.values()) {
			String resource = bean.getResource();
			if (resourceMap.containsKey(resource)) {
				LiveBeansResource group = resourceMap.get(resource);
				group.addElement(bean);
			}
			else {
				LiveBeansResource group = new LiveBeansResource(resource, bean.getTypeLookup());
				group.addElement(bean);
				resourceMap.put(resource, group);
			}
		}
	}
	
	protected JSONArray extractContextsJson(String json) throws JSONException {
		// JSON structure is an array of context descriptions, each containing
		// an array of beans		
		return new JSONArray(json);
	}

	public LiveBeansModel parse() throws JSONException {
		beansMap = new LinkedHashMap<String, LiveBean>();
		contextMap = new LinkedHashMap<String, LiveBeansContext>();
		resourceMap = new LinkedHashMap<String, LiveBeansResource>();

		JSONArray contextsArray = extractContextsJson(jsonInput);
		
		parseContexts(contextsArray);
		populateContextDependencies(contextsArray);
		groupByResource();

		LiveBeansModel model = new LiveBeansModel(typeLookup);
		model.addBeans(beansMap.values());
		model.addContexts(contextMap.values());
		model.addResources(resourceMap.values());
		return model;
	}

	private void parseBeans(LiveBeansContext context, JSONArray beansArray) throws JSONException {
		// construct LiveBeans
		for (int i = 0; i < beansArray.length(); i++) {
			JSONObject beanJson = beansArray.getJSONObject(i);
			if (beanJson != null && beanJson.has(LiveBean.ATTR_BEAN)) {
				LiveBean bean = parseBean(beanJson, context);
				bean.addAttribute(LiveBeansContext.ATTR_CONTEXT, context.getLabel());
				context.addElement(bean);
				beansMap.put(bean.getId(), bean);
			}
		}
	}
	
	protected LiveBean parseBean(JSONObject beanJson, LiveBeansContext context) throws JSONException {
		LiveBean bean = new LiveBean(typeLookup, beanJson.getString(LiveBean.ATTR_BEAN));
		if (beanJson.has(LiveBean.ATTR_SCOPE)) {
			bean.addAttribute(LiveBean.ATTR_SCOPE, beanJson.getString(LiveBean.ATTR_SCOPE));
		}
		if (beanJson.has(LiveBean.ATTR_TYPE)) {
			bean.addAttribute(LiveBean.ATTR_TYPE, beanJson.getString(LiveBean.ATTR_TYPE));
		}
		if (beanJson.has(LiveBean.ATTR_RESOURCE)) {
			bean.addAttribute(LiveBean.ATTR_RESOURCE, beanJson.getString(LiveBean.ATTR_RESOURCE));
		}
		if (typeLookup != null && typeLookup.getApplicationName() != null) {
			bean.addAttribute(LiveBean.ATTR_APPLICATION, typeLookup.getApplicationName());
		}
		return bean;
	}
	
	/**
	 * IMPORTANT: "beans" structure in the context JSON DIFFERS between Boot 1.x and later Boot versions. It is important
	 * to ALWAYS extract beans from a context using this method (for example, when creating the live beans initially, or separately, when
	 * populating the dependencies for those live beans), as this method may be overridden for other boot versions
	 * @param contextJson
	 * @return
	 */
	protected JSONArray extractBeans(JSONObject contextJson) {
		// Boot 1.x version
		return contextJson.optJSONArray(LiveBeansContext.ATTR_BEANS);
	}
	
	protected String getContextId(JSONObject contextJson) throws JSONException {
		return contextJson.getString(LiveBeansContext.ATTR_CONTEXT);
	}
	
	protected LiveBeansContext parseContext(JSONObject contextJson) throws JSONException {
		LiveBeansContext context = new LiveBeansContext(getContextId(contextJson));
		JSONArray beansArray = extractBeans(contextJson);
		if (beansArray != null) {
			parseBeans(context, beansArray);
		}
		return context;
	}

	private void parseContexts(JSONArray contextsArray) throws JSONException {
		// construct LiveBeansContexts
		for (int i = 0; i < contextsArray.length(); i++) {
			JSONObject contextJson = contextsArray.optJSONObject(i);
			if (contextJson != null) {
				LiveBeansContext context = parseContext(contextJson);
				contextMap.put(context.getLabel(), context);
			}
		}
	}

	private void populateBeanDependencies(JSONArray beansArray) throws JSONException {
		// populate LiveBean dependencies
		for (int i = 0; i < beansArray.length(); i++) {
			JSONObject beanJson = beansArray.optJSONObject(i);
			if (beanJson != null && beanJson.has(LiveBean.ATTR_BEAN)) {
				LiveBean bean = beansMap.get(beanJson.getString(LiveBean.ATTR_BEAN));
				JSONArray dependencies = beanJson.optJSONArray(LiveBean.ATTR_DEPENDENCIES);
				if (dependencies != null) {
					for (int j = 0; j < dependencies.length(); j++) {
						String dependency = dependencies.getString(j);
						LiveBean dependencyBean = beansMap.get(dependency);
						if (dependencyBean != null) {
							bean.addDependency(dependencyBean);
						}
						else {
							LiveBean dependentBean = new LiveBean(typeLookup, dependency, true);
							if (typeLookup != null && typeLookup.getApplicationName() != null) {
								dependentBean.addAttribute(LiveBean.ATTR_APPLICATION, typeLookup.getApplicationName());
							}
							bean.addDependency(dependentBean);
						}
					}
				}
			}
		}
	}

	private void populateContextDependencies(JSONArray contextsArray) throws JSONException {
		// populate LiveBeanContext dependencies
		for (int i = 0; i < contextsArray.length(); i++) {
			JSONObject contextJson = contextsArray.optJSONObject(i);
			if (contextJson != null) {
				LiveBeansContext context = contextMap.get(getContextId(contextJson));
				if (!contextJson.isNull(LiveBeansContext.ATTR_PARENT)) {
					String parent = contextJson.getString(LiveBeansContext.ATTR_PARENT);
					LiveBeansContext parentContext = contextMap.get(parent);
					if (parentContext != null) {
						context.setParent(parentContext);
					}
				}

				// PT 164156323 - Extracting beans differs between Boot 1.x and Boot 2.x.
				// Dependencies was not being populated for Boot 2.x because of a bug
				// where, when populating the dependencies, beans were being extracted using Boot 1.x structure
				// even for Boot 2.x. The way to fix this is to delegate to the method below which is
				// overridden for Boot 2.x and handles the Boot 2.x case correctly
				JSONArray beansArray = extractBeans(contextJson); // correct way
				if (beansArray != null) {
					populateBeanDependencies(beansArray);
				}
			}
		}
	}

}
