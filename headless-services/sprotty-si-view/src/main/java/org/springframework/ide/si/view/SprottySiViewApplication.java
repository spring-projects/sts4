package org.springframework.ide.si.view;

import java.util.EnumSet;

import org.eclipse.elk.alg.layered.options.FixedAlignment;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.options.Alignment;
import org.eclipse.elk.core.options.EdgeRouting;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.sprotty.layout.SprottyLayoutConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SprottySiViewApplication {
	
	@Bean
	GraphDataProvider graphDataProvider() {
//		return GraphDataProvider.fromClasspathResource("/sample.json");
		return GraphDataProvider.fromUrlOption("target");
	}

	@Bean
	public SprottyLayoutConfigurator integrationLayoutConfigurator() {
		SprottyLayoutConfigurator configurator = new SprottyLayoutConfigurator();
		
		IPropertyHolder holder = configurator.configureByType(SIDiagramGenerator.TYPE_INTEGRATION_GRAPH);
		holder.setProperty(LayeredOptions.SPACING_COMPONENT_COMPONENT, 60.0);
		holder.setProperty(LayeredOptions.SPACING_NODE_NODE_BETWEEN_LAYERS, 60.0);
		holder.setProperty(LayeredOptions.NODE_PLACEMENT_BK_FIXED_ALIGNMENT, FixedAlignment.BALANCED);
		holder.setProperty(LayeredOptions.EDGE_ROUTING, EdgeRouting.POLYLINE);

		holder = configurator.configureByType("output-port");
		holder.setProperty(LayeredOptions.PORT_SIDE, PortSide.EAST);
		
		holder = configurator.configureByType("input-port");
		holder.setProperty(LayeredOptions.PORT_SIDE, PortSide.WEST);
		
		holder = configurator.configureByType("error-port");
		holder.setProperty(LayeredOptions.PORT_SIDE, PortSide.SOUTH);
		
		holder = configurator.configureByType("node:channel");
		holder.setProperty(LayeredOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_SIDE);

		holder = configurator.configureByType("node:integration");
		holder.setProperty(LayeredOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_SIDE);
		holder.setProperty(LayeredOptions.NODE_LABELS_PLACEMENT, NodeLabelPlacement.outsideBottomCenter());
		holder.setProperty(LayeredOptions.NODE_LABELS_PADDING, new ElkPadding(5.0));

		holder = configurator.configureByType("node:label");
		holder.setProperty(LayeredOptions.NODE_LABELS_PLACEMENT, NodeLabelPlacement.outsideBottomCenter());

		return configurator;
	}

	public static void main(String[] args) {
		SpringApplication.run(SprottySiViewApplication.class, args);
	}

}
