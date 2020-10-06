package org.springframework.ide.vscode.xml.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.ide.vscode.xml.namespaces.model.NamespaceVersion;

public class NamespaceVersionTest {
	
	@Test
	public void allVersion() throws Exception {
		assertEquals("11.2.345-alpha", new NamespaceVersion("11.2.345-alpha").toString());
	}
	
	@Test
	public void majorAndMinorVersion() throws Exception {
		assertEquals("11.262.0", new NamespaceVersion("11.262").toString());
	}
	
	@Test
	public void majorVersion() throws Exception {
		assertEquals("11.0.0", new NamespaceVersion("11").toString());
	}
	
	@Test
	public void majorAndMinorAndPatchVersion() throws Exception {
		assertEquals("11.0.78", new NamespaceVersion("11.0.78").toString());
	}
	
	@Test
	public void zeroVersion() throws Exception {
		assertEquals("0.0.0", new NamespaceVersion("0").toString());
	}

}
