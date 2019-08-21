package org.springframework.ide.vscode.boot.app.diagram;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.sprotty.Dimension;
import org.eclipse.sprotty.IDiagramServer;
import org.eclipse.sprotty.Point;
import org.eclipse.sprotty.SCompartment;
import org.eclipse.sprotty.SEdge;
import org.eclipse.sprotty.SGraph;
import org.eclipse.sprotty.SLabel;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SModelRoot;
import org.eclipse.sprotty.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class LiveBeanDiagramModel implements InitializingBean {
		
	private static final Logger log = LoggerFactory.getLogger(LiveBeanDiagramModel.class);

	@Autowired
	private IDiagramServer diagramServer;
	
	@Autowired
	private RunningAppProvider runningAppProvider;
	
	@Autowired
	@Qualifier("taskScheduler")
	private TaskScheduler taskScheduler;


	@Override
	public void afterPropertiesSet() throws Exception {
		taskScheduler.scheduleAtFixedRate(() -> diagramServer.setModel(generateModel()), Duration.ofSeconds(15));
	}
	
	private SModelRoot generateModel() {
		try {
			Collection<SpringBootApp> apps = runningAppProvider.getAllRunningSpringApps();
			if (!apps.isEmpty()) {
				return toSprottyGraph(apps.iterator().next());
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
		return SGraph.EMPTY_ROOT;
	}
	
	private SModelRoot toSprottyGraph(SpringBootApp app) throws Exception {
		SModelRoot graph = new SModelRoot();
		graph.setId(app.getProcessName());
		graph.setType("graph");
		
		graph.setChildren(new ArrayList<>());
		List<SModelElement> graphChildren = graph.getChildren();
		
		LiveBeansModel beansModel = app.getBeans();
		for (String targetBeanId : beansModel.getBeanNames()) {
			for (LiveBean bean : beansModel.getBeansOfName(targetBeanId)) {
				graphChildren.add(createBean(bean.getId(), bean.getShortName(), new Point(), new Dimension()));
			}
			for (LiveBean sourceBean : beansModel.getBeansDependingOn(targetBeanId)) {
				graphChildren.add(createEdge(sourceBean.getId() + " " + targetBeanId, sourceBean.getId(), targetBeanId));
			}
		}

		return graph;
	}
	
	private static SNode createBean(String id, String labelText, Point location, Dimension size) {
		SNode node = new SNode();
	    node.setId(id);
	    node.setType("node:bean");
	    node.setLayout("vbox");
	    node.setPosition(new Point(Math.random() * 1024, Math.random() * 768));
	    node.setSize(new Dimension(80, 80));
	    node.setChildren(new ArrayList<>());
	    
	    SCompartment compartment = new SCompartment();
	    compartment.setId(id + "-comp");
	    compartment.setType("compartment");
	    compartment.setLayout("hbox");
	    compartment.setChildren(new ArrayList<>());
	    
	    SLabel label = new SLabel();
	    label.setId(id + "-lanbel");
	    label.setType("node:label");
	    label.setText(labelText);
	    
	    compartment.getChildren().add(label);
	    node.getChildren().add(compartment);
	    
	    return node;
	}
	
	private static SEdge createEdge(String id, String sourceId, String targetId) {
		SEdge edge = new SEdge();
		edge.setId(id);
		edge.setType("edge:straight");
		edge.setSourceId(sourceId);
		edge.setTargetId(targetId);
		return edge;
	}


}
