package org.springframework.ide.vscode.commons.sprotty.scan;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.sprotty.ActionMessage;
import org.eclipse.sprotty.IDiagramServer;
import org.eclipse.sprotty.server.json.ActionTypeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
public class DiagramWebsocketServer implements WebSocketConfigurer, InitializingBean {
		
	private static final Logger log = LoggerFactory.getLogger(DiagramWebsocketServer.class);
	
	private Set<WebSocketSession> ws_sessions = new HashSet<>();

	private Gson gson;
	
	@Autowired
	private IDiagramServer diagramServer;
	
	private void initializeGson() {
		if (gson == null) {
			GsonBuilder builder = new GsonBuilder();
			ActionTypeAdapter.configureGson(builder);
			gson = builder.create();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initializeGson();
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(WsMessageHandler(), "/websocket")
		.setAllowedOrigins("*")
		.withSockJS();
	}
	
	private final String END_MESSAGE = "@end";
	/**
	 * WebSocketHandler which receives messages from a websocket and forwards them to a
	 * spring-cloud-stream.
	 */
	@Bean
	public WebSocketHandler WsMessageHandler() {
		return new TextWebSocketHandler() {
			
			private StringBuilder buff = new StringBuilder();

			@Override
			public void afterConnectionEstablished(WebSocketSession session) throws Exception {
				synchronized (ws_sessions) {
					ws_sessions.add(session);
				}
				log.info("Websocket connection OPENED in: "+this);
				log.info("Number of active sessions = {}", ws_sessions.size());
				diagramServer.setRemoteEndpoint(message -> {
					sendMessage(gson.toJson(message, ActionMessage.class));
				});
			}

			@Override
			protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
				String payload = message.getPayload();
				log.info(payload);
				try {
					if (END_MESSAGE.equals(payload)) {
						ActionMessage actionMessage = gson.fromJson(buff.toString(), ActionMessage.class);
						buff = new StringBuilder();
						diagramServer.accept(actionMessage);
					} else {
						buff.append(payload);
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}

			@Override
			public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
				log.info("Websocket connection CLOSED in: "+this);
				synchronized (ws_sessions) {
					ws_sessions.remove(session);
				}
				log.info("Number of active sessions = {}", ws_sessions.size());
			}
			
			@Override
			public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
				log.error("Websocket trasnport error: ", exception);
				synchronized (ws_sessions) {
					ws_sessions.remove(session);
				}
			}
			
			
		};
	}
	
	private void sendMessage(String msg) {
		synchronized (ws_sessions) {
			for (WebSocketSession ws : ws_sessions) {
				try {
					if (ws.isOpen()) {
						log.info("Sent: {}", msg);
						ws.sendMessage(new TextMessage(msg));
					}
				} catch (Exception e) {
					log.error("Error forwarding message to ws session", e);
				}
			}
		}
	}
	
}
