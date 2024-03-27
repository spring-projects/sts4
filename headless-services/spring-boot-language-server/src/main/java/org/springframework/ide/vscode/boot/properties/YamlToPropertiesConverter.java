/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.properties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

class YamlToPropertiesConverter {

	private final Properties properties;


	public YamlToPropertiesConverter(Map<String, ?> yaml) {
		this.properties = new Properties() {

			private static final long serialVersionUID = 1L;

			private LinkedHashMap<Object, Object> delegate = new LinkedHashMap<>();

			@Override
			public synchronized Object put(Object key, Object value) {
				delegate.put(key, value);
				return super.put(key, value);
			}

			@Override
			public Set<Map.Entry<Object, Object>> entrySet() {
				return delegate.entrySet();
			}

		};

		for (Map.Entry<String, ?> e : yaml.entrySet()) {
			readProperties(e.getValue(), e.getKey());
		}
	}

	private void readPropertiesFromYamlMap(Map<String, ?> map, String prefix) {
		for (Map.Entry<String, ?> e : map.entrySet()) {
			readProperties(e.getValue(), "%s.%s".formatted(prefix, e.getKey()));
		}
	}

	private void readPropertiesFromYamlList(List<?> l, String prefix) {
		for (int i = 0; i < l.size(); i++) {
			readProperties(l.get(i), "%s[%d]".formatted(prefix, i));
		}
	}

	@SuppressWarnings("unchecked")
	private void readProperties(Object o, String prefix) {
		if (o instanceof Map) {
			readPropertiesFromYamlMap((Map<String, ?>) o, prefix);
		} else if ( o instanceof List) {
			readPropertiesFromYamlList((List<?>) o, prefix);
		} else {
			properties.put(prefix, o.toString());
		}
	}

	public Properties getProperties() {
		return properties;
	}

}
