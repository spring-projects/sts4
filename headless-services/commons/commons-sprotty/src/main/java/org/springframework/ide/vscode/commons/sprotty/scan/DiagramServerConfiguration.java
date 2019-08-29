package org.springframework.ide.vscode.commons.sprotty.scan;

import java.util.Optional;

import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.sprotty.ILayoutEngine;
import org.eclipse.sprotty.SGraph;
import org.eclipse.sprotty.layout.ElkLayoutEngine;
import org.eclipse.sprotty.layout.SprottyLayoutConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiagramServerConfiguration {
	
	@Bean
	public ILayoutEngine layoutEngine(Optional<SprottyLayoutConfigurator> configurator) {
		ElkLayoutEngine.initialize(new LayeredMetaDataProvider());
		final ElkLayoutEngine engine = new ElkLayoutEngine();
		return (root) -> engine.layout((SGraph)root, configurator.orElse(null));
	}
	
}
