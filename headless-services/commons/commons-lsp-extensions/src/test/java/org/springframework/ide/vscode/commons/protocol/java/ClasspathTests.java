/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.commons.Version;

public class ClasspathTests {
	
    @Test
    void testDependencyVersionCalculation2() throws Exception {
        Version version = Classpath.getDependencyVersion("spring-boot-1.2.3.jar");
        assertEquals(1, version.getMajor(), 1);
        assertEquals(2, version.getMinor(), 2);
        assertEquals(3, version.getPatch());
        assertNull(version.getQualifier());

        version = Classpath.getDependencyVersion("spring-boot-1.2.3-RELEASE.jar");
        assertEquals(version.getMajor(), 1);
        assertEquals(version.getMinor(), 2);
        assertEquals(version.getPatch(), 3);
        assertEquals(version.getQualifier(), "RELEASE");

        version = Classpath.getDependencyVersion("spring-boot-1.2.3.RELEASE.jar");
        assertEquals(1, version.getMajor(), 1);
        assertEquals(2, version.getMinor(), 2);
        assertEquals(3, version.getPatch());
        assertEquals("RELEASE", version.getQualifier());

        version = Classpath.getDependencyVersion("spring-boot-1.2.3.BUILD-SNAPSHOT.jar");
        assertEquals(1, version.getMajor(), 1);
        assertEquals(2, version.getMinor(), 2);
        assertEquals(3, version.getPatch());
        assertEquals("BUILD-SNAPSHOT", version.getQualifier());

        version = Classpath.getDependencyVersion("spring-boot-actuator-1.2.3.BUILD-SNAPSHOT.jar");
        assertEquals(1, version.getMajor(), 1);
        assertEquals(2, version.getMinor(), 2);
        assertEquals(3, version.getPatch());
        assertEquals("BUILD-SNAPSHOT", version.getQualifier());
    }


}
