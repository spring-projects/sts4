package org.springframework.ide.si.view;

import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.sprotty.layout.SprottyLayoutConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntegrationConfiguration {
	
	@Bean
	public SprottyLayoutConfigurator integrationLayoutConfigurator() {
		SprottyLayoutConfigurator configurator = new SprottyLayoutConfigurator();
		
		IPropertyHolder holder = configurator.configureByType(SIDiagramGenerator.TYPE_INTEGRATION_GRAPH);
		holder.setProperty(LayeredOptions.SPACING_COMPONENT_COMPONENT, 80.0);
		holder.setProperty(LayeredOptions.SPACING_NODE_NODE_BETWEEN_LAYERS, 80.0);
		
		holder = configurator.configureByType("output-port");
		holder.setProperty(LayeredOptions.PORT_SIDE, PortSide.EAST);
		
		holder = configurator.configureByType("input-port");
		holder.setProperty(LayeredOptions.PORT_SIDE, PortSide.WEST);
		
		holder = configurator.configureByType("error-port");
		holder.setProperty(LayeredOptions.PORT_SIDE, PortSide.SOUTH);
		
		holder = configurator.configureByType("node:channel");
		holder.setProperty(LayeredOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_SIDE);

		holder = configurator.configureByType("node:integration_node");
		holder.setProperty(LayeredOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_SIDE);
		
		return configurator;
	}

}
