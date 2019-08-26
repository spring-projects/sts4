package org.springframework.ide.vscode.commons.sprotty.scan;

import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.sprotty.ILayoutEngine;
import org.eclipse.sprotty.layout.ElkLayoutEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiagramServerConfiguration {
	
	@Bean
	public ILayoutEngine layoutEngine() {
		ElkLayoutEngine.initialize(new LayeredMetaDataProvider());
		return new ElkLayoutEngine();
	}
	
}
