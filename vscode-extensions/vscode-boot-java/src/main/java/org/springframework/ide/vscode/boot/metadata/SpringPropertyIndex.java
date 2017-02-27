/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata;

import java.util.Collection;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.util.FuzzyMap;

public class SpringPropertyIndex extends FuzzyMap<ConfigurationMetadataProperty> {
	
	public SpringPropertyIndex(IClasspath projectPath) {
		if (projectPath!=null) {
			PropertiesLoader loader = new PropertiesLoader();
			ConfigurationMetadataRepository metadata = loader.load(projectPath);
			Collection<ConfigurationMetadataProperty> allEntries = metadata.getAllProperties().values();
			for (ConfigurationMetadataProperty item : allEntries) {
				add(item);
			}
		}
	}

	/**
	 * Dumps out 'test data' based on the current contents of the index. This is not meant to be
	 * used in 'production' code. The idea is to call this method during development to dump a
	 * 'snapshot' of the index onto System.out. The data is printed in a forma so that it can be easily
	 * pasted/used into JUNit testing code.
	 */
//	public void dumpAsTestData() {
//		List<Match<PropertyInfo>> allData = this.find("");
//		for (Match<PropertyInfo> match : allData) {
//			PropertyInfo d = match.data;
//			System.out.println("data("
//					+dumpString(d.getId())+", "
//					+dumpString(d.getType())+", "
//					+dumpString(d.getDefaultValue())+", "
//					+dumpString(d.getDescription()) +");"
//			);
//			for (PropertySource source : d.getSources()) {
//				String st = source.getSourceType();
//				String sm = source.getSourceMethod();
//				if (sm!=null) {
//					System.out.println(d.getId() +" from: "+st+"::"+sm);
//				}
//			}
//		}
//	}

//	private String dumpString(Object v) {
//		if (v==null) {
//			return "null";
//		}
//		return dumpString(""+v);
//	}

	private String dumpString(String s) {
		if (s==null) {
			return "null";
		} else {
			StringBuilder buf = new StringBuilder("\"");
			for (char c : s.toCharArray()) {
				switch (c) {
				case '\r':
					buf.append("\\r");
					break;
				case '\n':
					buf.append("\\n");
					break;
				case '\\':
					buf.append("\\\\");
					break;
				case '\"':
					buf.append("\\\"");
					break;
				default:
					buf.append(c);
					break;
				}
			}
			buf.append("\"");
			return buf.toString();
		}
	}

	@Override
	protected String getKey(ConfigurationMetadataProperty entry) {
		return entry.getId();
	}
	
	/**
	 * Find the longest known property that is a prefix of the given name. Here prefix does not mean
	 * 'string prefix' but a prefix in the sense of treating '.' as a kind of separators. So
	 * 'prefix' is not allowed to end in the middle of a 'segment'.
	 */
//	public static PropertyInfo findLongestValidProperty(FuzzyMap<PropertyInfo> index, String name) {
//		int bracketPos = name.indexOf('[');
//		int endPos = bracketPos>=0?bracketPos:name.length();
//		PropertyInfo prop = null;
//		String prefix = null;
//		while (endPos>0 && prop==null) {
//			prefix = name.substring(0, endPos);
//			String canonicalPrefix = StringUtil.camelCaseToHyphens(prefix);
//			prop = index.get(canonicalPrefix);
//			if (prop==null) {
//				endPos = name.lastIndexOf('.', endPos-1);
//			}
//		}
//		if (prop!=null) {
//			//We should meet caller's expectation that matched properties returned by this method
//			// match the names exactly even if we found them using relaxed name matching.
//			return prop.withId(prefix);
//		}
//		return null;
//	}

}
