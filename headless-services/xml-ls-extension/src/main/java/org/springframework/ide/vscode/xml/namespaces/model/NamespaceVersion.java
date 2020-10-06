package org.springframework.ide.vscode.xml.namespaces.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamespaceVersion implements Comparable<NamespaceVersion> {

	private static final String MINIMUM_VERSION_STRING = "0";

	private static final Pattern versionPattern = Pattern.compile("([0-9]\\d*)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:-([a-zA-Z0-9]+))?");
	
	public static final NamespaceVersion MINIMUM_VERSION = new NamespaceVersion(MINIMUM_VERSION_STRING);

	private final int major;
	private final int minor;
	private final int patch;
	private final String qualifier;

	public NamespaceVersion(String v) {
		Matcher matcher = versionPattern.matcher(v);

		if (matcher.matches()) {			
			qualifier = matcher.groupCount() > 3 ? (matcher.group(4) == null ? "" : matcher.group(4)) : "";
			patch = matcher.groupCount() > 2 ? (matcher.group(3) == null ? 0 : Integer.valueOf(matcher.group(3))) : 0;
			minor = matcher.groupCount() > 1 ? (matcher.group(2) == null ? 0 : Integer.valueOf(matcher.group(2))) : 0;
			major = matcher.groupCount() > 0 ? (matcher.group(1) == null ? 0 : Integer.valueOf(matcher.group(1))) : 0;
		} else {
			major = 0;
			minor = 0;
			patch = 0;
			qualifier = "";
		}
	}

	public int compareTo(NamespaceVersion v2) {
		if (major == v2.major) {
			if (minor == v2.minor) {
				if (patch == v2.patch) {
					return qualifier.compareTo(v2.qualifier);
				} else {
					return patch - v2.patch;
				}
			} else {
				return minor - v2.minor;
			}
		} else {
			return major - v2.major;
		}
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}

	public String getQualifier() {
		return qualifier;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(major);
		sb.append('.');
		sb.append(minor);
		sb.append('.');
		sb.append(patch);
		if (!qualifier.isEmpty()) {
			sb.append('-');
			sb.append(qualifier);
		}
		return sb.toString();
	}
	
	
}