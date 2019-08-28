package org.springframework.ide.vscode.commons.sprotty.scan;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.eclipse.sprotty.Action;
import org.eclipse.sprotty.ActionMessage;
import org.eclipse.sprotty.DefaultDiagramServer;
import org.eclipse.sprotty.IDiagramServer;
import org.eclipse.sprotty.ILayoutEngine;
import org.eclipse.sprotty.RequestModelAction;
import org.eclipse.sprotty.SGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.vscode.commons.sprotty.api.DiagramGenerator;
import org.springframework.ide.vscode.commons.sprotty.api.DiagramServerManager;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class DefaultDiagramServerManager implements DiagramServerManager {
	
	public static final SGraph EMPTY_GRAPH = new SGraph(((Consumer<SGraph>) (SGraph it) -> {
		it.setType("NONE");
		it.setId("EMPTY");
	}));
	
	private static final Logger log = LoggerFactory.getLogger(DefaultDiagramServerManager.class);

	private Cache<String, IDiagramServer> servers = CacheBuilder.newBuilder().build();

	@Autowired
	private DiagramGenerator diagramGenerator;
	
	@Autowired
	private ILayoutEngine layoutEngine;
	
	private Consumer<ActionMessage> remoteEndpoint;
	
	private IDiagramServer getDiagramServer(String clientId) {
		try {
			return servers.get(clientId, () -> {
				DefaultDiagramServer diagramServer = new DefaultDiagramServer(clientId);
				diagramServer.setRemoteEndpoint(this::sendMessageToRemoteEndpoint);
				diagramServer.setLayoutEngine(layoutEngine);
				return diagramServer;
			});
		} catch (ExecutionException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	public void setRemoteEndpoint(Consumer<ActionMessage> remoteEndpoint) {
		Assert.isNull(this.remoteEndpoint, "Can only be set once!");
		this.remoteEndpoint = remoteEndpoint;
	}
	
	private void sendMessageToRemoteEndpoint(ActionMessage message) {
		if (remoteEndpoint != null) {
			remoteEndpoint.accept(message);
		}
	}
	
	public void sendMessageToServer(ActionMessage message) {
		RequestModelAction modelRequest = null; 
		Action action = message.getAction();
		if (action instanceof RequestModelAction) {
			modelRequest = (RequestModelAction) action;
		}
		String clientId = message.getClientId();
		IDiagramServer server = getDiagramServer(clientId);
		if (server != null) {
			if (modelRequest!=null) {
				server.setModel(diagramGenerator.generateModel(clientId, modelRequest));
			}
			server.accept(message);
		}
	}
}
