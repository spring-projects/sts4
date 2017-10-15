/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Martin Lippert
 */
public class LiveBeansModel {

	public static LiveBeansModel parse(String json) {
		LiveBeansModel model = new LiveBeansModel();

		try {
			JSONArray mainArray = new JSONArray(json);

			for (int i = 0; i < mainArray.length(); i++) {
				JSONObject appContext = mainArray.getJSONObject(i);
				if (appContext == null) continue;

				JSONArray beansArray = appContext.optJSONArray("beans");
				if (beansArray == null) continue;

				for (int j = 0; j < beansArray.length(); j++) {
					JSONObject beanObject = beansArray.getJSONObject(j);
					if (beanObject == null) continue;

					LiveBean bean = LiveBean.parse(beanObject);
					if (bean != null) {
						model.add(bean);
					}
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return model;
	}

	private final ConcurrentMap<String, List<LiveBean>> beansViaType;
	private final ConcurrentMap<String, List<LiveBean>> beansViaName;

	protected LiveBeansModel() {
		this.beansViaType = new ConcurrentHashMap<>();
		this.beansViaName = new ConcurrentHashMap<>();
	}

	public LiveBean[] getBeansOfType(String fullyQualifiedType) {
		List<LiveBean> result = beansViaType.get(fullyQualifiedType);
		return result != null ? result.toArray(new LiveBean[result.size()]) : new LiveBean[0];
	}

	public LiveBean[] getBeansOfName(String beanName) {
		List<LiveBean> result = beansViaName.get(beanName);
		return result != null ? result.toArray(new LiveBean[result.size()]) : new LiveBean[0];
	}

	protected void add(LiveBean bean) {
		String type = bean.getType();
		if (type != null) {
			beansViaType.computeIfAbsent(type, (t) -> new ArrayList<>()).add(bean);
		}

		String name = bean.getId();
		if (name != null) {
			beansViaName.computeIfAbsent(name, (n) -> new ArrayList<>()).add(bean);
		}
	}

}
