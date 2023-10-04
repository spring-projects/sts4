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
package org.springframework.ide.vscode.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class VersionTests {
	
    @Test
    void testVersionCalculation1() throws Exception {
        Version version = Version.parse("2.7.5");
        assertEquals(2, version.getMajor());
        assertEquals(7, version.getMinor());
        assertEquals(5, version.getPatch());
        assertNull(version.getQualifier());

        version = Version.parse("3.0.0-SNAPSHOT");
        assertEquals(3, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals(version.getQualifier(), "SNAPSHOT");


        version = Version.parse("2.6.14-RC2");
        assertEquals(2, version.getMajor());
        assertEquals(6, version.getMinor());
        assertEquals(14, version.getPatch());
        assertEquals(version.getQualifier(), "RC2");
    }

    @Test
    void testVersionCalculation2() throws Exception {
        Version version = Version.parse("2.7");
        assertEquals(2, version.getMajor());
        assertEquals(7, version.getMinor());
        assertEquals(0, version.getPatch());
        assertNull(version.getQualifier());

        version = Version.parse("2");
        assertEquals(2, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getPatch());
        assertNull(version.getQualifier());
    }


}
