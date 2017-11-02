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
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;

/**
 * Parsers @ConditionalOn annotations from a spring boot running app's
 * autoconfig report. An example of a conditional that is parsed from a running
 * app's autoconfig report would be:
 *
 * {"TraceRepositoryAutoConfiguration#traceRepository":[{"condition":"OnBeanCondition","message":"@ConditionalOnMissingBean
 * (types: org.springframework.boot.actuate.trace.TraceRepository;
 * SearchStrategy: all) did not find any beans"}]
 *
 */
public class LiveConditionalParser {

	private final String autoConfigRecord;
	private final String appProcessName;
	private final String appProcessId;

	public LiveConditionalParser(String autoConfigRecord, String appProcessId, String appProcessName) {
		this.autoConfigRecord = autoConfigRecord;
		this.appProcessId = appProcessId;
		this.appProcessName = appProcessName;
	}

	public Optional<List<LiveConditional>> parse() {

		try {
			List<LiveConditional> allConditionals = new ArrayList<>();

			if (StringUtil.hasText(autoConfigRecord)) {
				getConditionalsFromPositiveMatches(autoConfigRecord).stream()
						.forEach(conditional -> allConditionals.add(conditional));
			}
			if (!allConditionals.isEmpty()) {
				return Optional.of(allConditionals);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Optional.empty();
	}

	private Optional<JSONObject> getPositiveMatchesJson(String autoConfigReport) {
		JSONObject autoConfigJson = new JSONObject(autoConfigReport);

		Iterator<String> keys = autoConfigJson.keys();

		while (keys.hasNext()) {
			String key = keys.next();
			if ("positiveMatches".equals(key)) {
				Object obj = autoConfigJson.get(key);
				if (obj instanceof JSONObject) {
					return Optional.of((JSONObject) obj);
				}
			}
		}

		return Optional.empty();
	}

	private List<LiveConditional> getConditionalsFromPositiveMatches(String autoconfigReport) {
		List<LiveConditional> conditions = new ArrayList<>();

		getPositiveMatchesJson(autoconfigReport).ifPresent((positiveMatches) -> {
			Iterator<String> pMKeys = positiveMatches.keys();
			while (pMKeys.hasNext()) {
				// The positive match key contains the bean method information where conditional
				// was applied to
				String positiveMatchKey = pMKeys.next();
				JSONArray matchList = (JSONArray) positiveMatches.get(positiveMatchKey);
				matchList.forEach((match) -> {
					if (match instanceof JSONObject) {
						resolveConditional(positiveMatchKey, (JSONObject) match)
								.ifPresent((condition) -> conditions.add(condition));
					}
				});
			}
		});

		return conditions;
	}

	private Optional<LiveConditional> resolveConditional(String positiveMatchKey, JSONObject conditionalJson) {
		if (conditionalJson != null) {
			String condition = (String) conditionalJson.get("condition");
			String message = (String) conditionalJson.get("message");
			// We care about the message itself as it contains the actual annotation as well
			// as the reason it matched
			if (StringUtil.hasText(message)) {
				return Optional.of(LiveConditional.builder().processId(appProcessId).processName(appProcessName)
						.condition(condition).message(message).positiveMatchKey(positiveMatchKey).build());
			}
		}
		return Optional.empty();
	}

	public static Optional<List<LiveConditional>> parse(String autoConfigRecord, String appProcessId,
			String appProcessName) {
		return new LiveConditionalParser(autoConfigRecord, appProcessId, appProcessName).parse();
	}
}
