package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.util.version.VersionParser;

public class BootPropertyTesterTest {

	@Test
	public void getVersionFromJarName() {
		//Example: (old format) spring-boot-starter-web-1.2.3.RELEASE.jar
		//         (new format) spring-boot-starter-web-2.4.0.jar
		//         (new format) spring-boot-starter-web-2.4.0-SNAPSHOT.jar

		assertVersion("1.2.3.RELEASE", "spring-boot-starter-web-1.2.3.RELEASE.jar");
		assertVersion("2.4.0", "spring-boot-starter-web-2.4.0.jar");
		assertVersion("2.4.0-SNAPSHOT", "spring-boot-starter-web-2.4.0-SNAPSHOT.jar");
	}

	private void assertVersion(String versionStr, String jarName) {
		String actualVersionStr = BootPropertyTester.getVersionFromJarName(jarName);
		assertEquals(versionStr, actualVersionStr);
		assertNotNull(VersionParser.DEFAULT.parse(actualVersionStr));
	}
}
