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
package org.springframework.ide.vscode.commons.boot.app.cli.livebean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;

/**
 * @author Martin Lippert
 */
public class LiveBeansModel {

	interface Parser {
		LiveBeansModel parse(String json) throws Exception;
	}

	private static class Boot15Parser implements Parser {
		@Override
		public LiveBeansModel parse(String json) throws Exception {
			LiveBeansModel model = new LiveBeansModel();
			JSONArray mainArray = new JSONArray(json);
			for (int i = 0; i < mainArray.length(); i++) {
				JSONObject appContext = mainArray.getJSONObject(i);
				if (appContext == null) continue;

				JSONArray beansArray = appContext.optJSONArray("beans");
				if (beansArray == null) continue;

				for (int j = 0; j < beansArray.length(); j++) {
					JSONObject beanObject = beansArray.getJSONObject(j);
					if (beanObject == null) continue;

					LiveBean bean = parseBean(beanObject);
					if (bean != null) {
						model.add(bean);
					}
				}
			}
			return model;
		}
		private LiveBean parseBean(JSONObject beansJSON) {
			String id = beansJSON.optString("bean");
			String type = beansJSON.optString("type");
			String scope = beansJSON.optString("scope");
			String resource = beansJSON.optString("resource");

			JSONArray aliasesJSON = beansJSON.getJSONArray("aliases");
			String[] aliases = new String[aliasesJSON.length()];
			for (int i = 0; i < aliasesJSON.length(); i++) {
				aliases[i] = aliasesJSON.optString(i);
			}

			JSONArray dependenciesJSON = beansJSON.getJSONArray("dependencies");
			String[] dependencies = new String[dependenciesJSON.length()];
			for (int i = 0; i < dependenciesJSON.length(); i++) {
				dependencies[i] = dependenciesJSON.optString(i);
			}

			return new LiveBean(id, aliases, scope, type, resource, dependencies);
		}

	}

	private static class Boot20Parser implements Parser {
		@Override
		public LiveBeansModel parse(String json) throws Exception {
			LiveBeansModel model = new LiveBeansModel();
			JSONObject mainObject = new JSONObject(json);
			JSONObject beansObject = mainObject.getJSONObject("beans");
			for (String id : beansObject.keySet()) {
				JSONObject beanObject = beansObject.getJSONObject(id);
				System.out.println(beanObject.toString(3));
				LiveBean bean = parseBean(id, beanObject);
				if (bean!=null) {
					model.add(bean);
				}
			}
			return model;
		}

		private LiveBean parseBean(String id, JSONObject beansJSON) {
			String type = beansJSON.optString("type");
			String scope = beansJSON.optString("scope");
			String resource = beansJSON.optString("resource");

			JSONArray aliasesJSON = beansJSON.getJSONArray("aliases");
			String[] aliases = new String[aliasesJSON.length()];
			for (int i = 0; i < aliasesJSON.length(); i++) {
				aliases[i] = aliasesJSON.optString(i);
			}

			JSONArray dependenciesJSON = beansJSON.getJSONArray("dependencies");
			String[] dependencies = new String[dependenciesJSON.length()];
			for (int i = 0; i < dependenciesJSON.length(); i++) {
				dependencies[i] = dependenciesJSON.optString(i);
			}

			return new LiveBean(id, aliases, scope, type, resource, dependencies);
		}
	}

	private static final Parser[] PARSERS = {
			new Boot15Parser(),
			new Boot20Parser(),
	};

	public static LiveBeansModel parse(String json) {
		List<Exception> exceptions = new ArrayList<>(PARSERS.length);
		if (StringUtil.hasText(json)) {
			for (Parser parser : PARSERS) {
				try {
					LiveBeansModel model = parser.parse(json);
					if (model==null) {
						throw new NullPointerException("Parser returned a null model (it should not!)");
					}
					return model; //good!
				} catch (Exception e) {
					exceptions.add(e);
				}
			}
		}
		//Only getting here if none of the parsers worked... So if at least one parser works,
		// we won't log any exceptions.
		for (Exception e : exceptions) {
			Log.log(e);
		}
		return new LiveBeansModel(); // allways return at least an empty model.
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

	public Stream<LiveBean> getAllBeans() {
		return beansViaName.values().stream().flatMap(Collection::stream);
	}

	public boolean isEmpty() {
		return !getAllBeans().findAny().isPresent();
	}

}
