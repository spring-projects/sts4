package org.springframework.ide.vscode.commons.sprotty.scan;

import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.sprotty.IDiagramExpansionListener;
import org.eclipse.sprotty.IDiagramOpenListener;
import org.eclipse.sprotty.IDiagramSelectionListener;
import org.eclipse.sprotty.ILayoutEngine;
import org.eclipse.sprotty.IModelUpdateListener;
import org.eclipse.sprotty.SModelCloner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.commons.sprotty.elk.ElkLayoutEngine;

@Configuration
public class DiagramServerConfiguration {
	
	@Bean public IModelUpdateListener modelUpdateListener() {
		return new IModelUpdateListener.NullImpl();
	}
	
	@Bean
	public ILayoutEngine layoutEngine() {
		LayoutMetaDataService.getInstance().registerLayoutMetaDataProviders(new LayeredMetaDataProvider());
		return new ElkLayoutEngine();
	}
	
	@Bean public IDiagramSelectionListener diagramSelectionListener() {
		return new IDiagramSelectionListener.NullImpl();
	}
	
	@Bean public IDiagramExpansionListener diagramExpansionListener() {
		return new IDiagramExpansionListener.NullImpl();
	}

	@Bean public IDiagramOpenListener diagramOpenListener() {
		return new IDiagramOpenListener.NullImpl();
	}
	
	@Bean public SModelCloner modelCloner() {
		return new SModelCloner();
	}


}
