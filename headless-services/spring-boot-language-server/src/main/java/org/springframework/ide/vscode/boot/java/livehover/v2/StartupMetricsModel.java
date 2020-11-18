/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

/**
 * POJO for Startup metrics data
 * 
 * @author Alex Boyko
 *
 */
public class StartupMetricsModel {
	
	private static final String BEAN_INSTANCIATION_EVENT = "spring.beans.instantiate";
	
	private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, typeOfT, context) -> Duration.parse(json.getAsString()))
            .create();
	
	public static class StartupEvent {
		private Date startTime;
		private Date endTime;
		private Duration duration;
		private StartupStep startupStep;
		public Date getStartTime() {
			return startTime;
		}
		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}
		public Date getEndTime() {
			return endTime;
		}
		public void setEndTime(Date endTime) {
			this.endTime = endTime;
		}
		public Duration getDuration() {
			return duration;
		}
		public void setDuration(Duration duration) {
			this.duration = duration;
		}
		public StartupStep getStartupStep() {
			return startupStep;
		}
		public void setStartupStep(StartupStep startupStep) {
			this.startupStep = startupStep;
		}	
	}
	
	public static class StartupStep {
		private String name;
		private int id;
		private int parentId;
		private PropertyValuePair[] tags;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public int getParentId() {
			return parentId;
		}
		public void setParentId(int parentId) {
			this.parentId = parentId;
		}
		public PropertyValuePair[] getTags() {
			return tags;
		}
		public void setTags(PropertyValuePair[] tags) {
			this.tags = tags;
		}
		String findPropertyValue(String key) {
			for (PropertyValuePair pair : tags) {
				if (key.equals(pair.getKey())) {
					return pair.getValue();
				}
			}
			return null;
		}
	}
	
	public static class PropertyValuePair {
		private String key;
		private String value;
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public static StartupMetricsModel parse(Map<?, ?> mapContent) {
		Object timeline = mapContent.get("timeline");
		if (timeline instanceof Map) {
			Object events = ((Map<?,?>) timeline).get("events");
			if (events instanceof List) {
				String json = gson.toJson(events);
				List<StartupEvent> fromJson = gson.fromJson(json, new TypeToken<List<StartupEvent>>() {}.getType());
				return new StartupMetricsModel(fromJson);
			}
		}
		return null;
	}
	
	private List<StartupEvent> startupEvents;
	
	private Map<String, Duration> beanInstanciationTimes;
	
	public StartupMetricsModel(List<StartupEvent> startupEvents) {
		this.startupEvents = startupEvents;
		beanInstanciationTimes = createbeanInstanciationTimes();
	}
	
	private Map<String, Duration> createbeanInstanciationTimes() {
		Map<String, Duration> beanInstanciationTimes = new HashMap<>();
		for (StartupEvent event : startupEvents) {
			if (event.getStartupStep() != null && BEAN_INSTANCIATION_EVENT.equals(event.getStartupStep().getName())) {
				String beanId = event.getStartupStep().findPropertyValue("beanName");
				if (beanId != null) {
					beanInstanciationTimes.put(beanId, event.getDuration());
				}
			}
		}
		return beanInstanciationTimes;
	}
	
	public Duration getBeanInstanciationTime(String beanId) {
		return beanInstanciationTimes.get(beanId);
	}

	public List<StartupEvent> getStartupEvents() {
		return startupEvents;
	}

}
