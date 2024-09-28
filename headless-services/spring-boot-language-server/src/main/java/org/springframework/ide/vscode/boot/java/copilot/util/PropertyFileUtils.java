package org.springframework.ide.vscode.boot.java.copilot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public final class PropertyFileUtils {

	private PropertyFileUtils() {
	}

	public static Properties getPropertyFile() {
		File homeDir = new File(System.getProperty("user.home"));
		File propertyFile = new File(homeDir, ".openai");
		Properties props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(propertyFile);
			props.load(in);
			return props;
		}
		catch (IOException ex) {
			throw new SpringCliException("Could not load property file ~/.openai", ex);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
	}

	public static Properties mergeProperties(Properties... properties) {
		Properties mergedProperties = new Properties();
		for (Properties property : properties) {
			mergedProperties.putAll(property);
		}
		return mergedProperties;
	}

}