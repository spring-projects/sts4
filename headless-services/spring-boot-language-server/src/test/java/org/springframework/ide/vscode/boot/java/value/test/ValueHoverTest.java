/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.value.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.ide.vscode.boot.java.value.ValueHoverProvider;

/**
 * @author Martin Lippert
 */
public class ValueHoverTest {
	
	@Test
	public void testGetPropertyFromValue() {
		ValueHoverProvider provider = new ValueHoverProvider();

		assertNull(provider.getPropertyKey("${spring}", 0));
		assertNull(provider.getPropertyKey("${spring}", 1));
		assertEquals("spring", provider.getPropertyKey("${spring}", 2));
		assertEquals("spring", provider.getPropertyKey("${spring}", 3));
		assertEquals("spring", provider.getPropertyKey("${spring}", 8));
		assertNull(provider.getPropertyKey("${spring}", 9));

		assertNull(provider.getPropertyKey("abc ${spring} and other stuff", 0));
		assertNull(provider.getPropertyKey("abc ${spring} and other stuff", 5));
		assertEquals("spring", provider.getPropertyKey("abc ${spring} and other stuff", 6));
		assertEquals("spring", provider.getPropertyKey("abc ${spring} and other stuff", 12));
		assertNull(provider.getPropertyKey("abc ${spring} and other stuff", 13));

		assertNull(provider.getPropertyKey("abc ${spring} and ${boot} other stuff", 5));
		assertEquals("spring", provider.getPropertyKey("abc ${spring} and ${boot} other stuff", 6));
		assertEquals("spring", provider.getPropertyKey("abc ${spring} and ${boot} other stuff", 12));
		assertNull(provider.getPropertyKey("abc ${spring} and ${boot} other stuff", 13));

		assertNull(provider.getPropertyKey("abc ${spring} and ${boot} other stuff", 19));
		assertEquals("boot", provider.getPropertyKey("abc ${spring} and ${boot} other stuff", 20));
		assertEquals("boot", provider.getPropertyKey("abc ${spring} and ${boot} other stuff", 24));
		assertNull(provider.getPropertyKey("abc ${spring} and ${boot} other stuff", 25));
	}
	
}
