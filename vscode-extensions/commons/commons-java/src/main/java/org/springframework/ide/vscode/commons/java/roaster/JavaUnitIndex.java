package org.springframework.ide.vscode.commons.java.roaster;

import java.net.URL;

import org.jboss.forge.roaster.model.JavaUnit;

public interface JavaUnitIndex {

	static final JavaUnitIndex DEFAULT = new DefaultJavaUnitIndex();
	
	JavaUnit getJavaUnit(URL url); 

}
