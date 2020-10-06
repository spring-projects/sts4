package org.springframework.ide.vscode.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.services.IXMLValidationService;
import org.eclipse.lemminx.services.extensions.commands.IXMLCommandService;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

class JavaProjectCache implements IJavaProjectProvider {
	
	private static Logger LOGGER = Logger.getLogger(JavaProjectCache.class.getName());
	
	private class JavaProjectData implements IJavaProjectData {

		private String name;
		private String uri;
		private Classpath classpath;
		
		public JavaProjectData(String name, String uri, Classpath classpath) {
			super();
			this.name = name;
			this.uri = uri;
			this.classpath = classpath;
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getUri() {
			return uri;
		}

		@Override
		public Classpath getClasspath() {
			return classpath;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((classpath == null) ? 0 : classpath.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((uri == null) ? 0 : uri.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JavaProjectData other = (JavaProjectData) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (classpath == null) {
				if (other.classpath != null)
					return false;
			} else if (!classpath.equals(other.classpath))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (uri == null) {
				if (other.uri != null)
					return false;
			} else if (!uri.equals(other.uri))
				return false;
			return true;
		}

		private JavaProjectCache getEnclosingInstance() {
			return JavaProjectCache.this;
		}
		
	}
	
	private static final String JAVA_EXECUTE_WORKSPACE_COMMAND = "java.execute.workspaceCommand";

	private static final Gson gson = new Gson();

	private static final long CLASSPATH_TIMEOUT = 30_000;
	
	private final Map<String, IJavaProjectData> projectsCache = new ConcurrentHashMap<>();
	private final String callbackCommandId;
	
	private List<Consumer<IJavaProjectData>> listeners = new ArrayList<>();

	private final IXMLCommandService commandService;

	private final IXMLDocumentProvider documentProvider;

	private final IXMLValidationService validationService;
	
	public JavaProjectCache(IXMLCommandService commandService, IXMLDocumentProvider documentProvider, IXMLValidationService validationService) {
		this.callbackCommandId = UUID.randomUUID().toString();
		this.commandService = commandService;
		this.documentProvider = documentProvider;
		this.validationService = validationService;
	}

	void start() {
		// Register handler for the classpath change callback command
		commandService.registerCommand(callbackCommandId, (params, cancelChecker) -> handleClasspathChanged(params));
		
		// Register classpath listener by executing the command below
		final ExecuteCommandParams execCmdParams = new ExecuteCommandParams(JAVA_EXECUTE_WORKSPACE_COMMAND, Arrays.asList("sts.java.addClasspathListener", callbackCommandId, true));

		// Keep trying to register classpath listener until success or timeout
		new Thread() {
			@Override
			public void run() {
				Object result = null;
				long startTime = System.currentTimeMillis();
				while (result == null && System.currentTimeMillis() - startTime < CLASSPATH_TIMEOUT) {
					CompletableFuture<Object> clientCommand = commandService.executeClientCommand(execCmdParams);
					try {
						result = clientCommand.get(1000, TimeUnit.MILLISECONDS);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						clientCommand.cancel(true);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// ignore
						}
					}
				}
			}
		}.start();
		
	}
	
	private String handleClasspathChanged(ExecuteCommandParams callbackParams) {
		List<Object> args = callbackParams.getArguments();
		//Args are deserialized as com.google.gson.JsonElements.
		List<String> projectUris = new ArrayList<>();
		if (((JsonElement) args.get(0)).isJsonArray()) {
			// If events are batched... then they will arrive as a array of arrays.
			for (Object arg : args) {
				JsonArray event = (JsonArray) arg;
				String projectUri = event.get(0).getAsString();
				String name = event.get(1).getAsString();
				boolean deleted = event.get(2).getAsBoolean();
				Classpath classpath = gson.fromJson((JsonElement)event.get(3), Classpath.class);
				
				projectUris.add(projectUri);
				updateProject(name, projectUri, classpath, deleted);
			}
		} else {
			//Still support non-batched events for backwards compatibility with clients
			// that don't provide batched event support (e.g. IDEA client may only adopt this
			// later, or not adopt it at all).
			String projectUri = ((JsonElement) args.get(0)).getAsString();
			String name = ((JsonElement) args.get(1)).getAsString();
			boolean deleted = ((JsonElement)args.get(2)).getAsBoolean();
			Classpath classpath = gson.fromJson((JsonElement)args.get(3), Classpath.class);
			
			projectUris.add(projectUri);
			updateProject(name, projectUri, classpath, deleted);
		}
		
		documentProvider.getAllDocuments().stream()
			.filter(dm -> projectUris.stream().filter(uri -> dm.getTextDocument().getUri().startsWith(uri)).findFirst().isPresent())
			.forEach(dm -> validationService.validate(dm));
		
		return "done";
	}
	
	void stop() {
		ExecuteCommandParams execCmdParams = new ExecuteCommandParams(JAVA_EXECUTE_WORKSPACE_COMMAND, Arrays.asList("sts.java.removeClasspathListener", callbackCommandId));
		commandService.executeClientCommand(execCmdParams);
		commandService.unregisterCommand(callbackCommandId);
	}
	
	private void updateProject(String name, String projectUri, Classpath classpath, boolean deleted) {
		JavaProjectData project = new JavaProjectData(name, projectUri, classpath);
		if (deleted) {
			this.projectsCache.remove(project.getName());
		} else {
			this.projectsCache.put(name, project);
		}
		for (Consumer<IJavaProjectData> l : listeners) {
			try {
				l.accept(project);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e, null);
			}
		}
	}

	@Override
	public Collection<IJavaProjectData> all() {
		return projectsCache.values();
	}

	@Override
	public IJavaProjectData get(String name) {
		return projectsCache.get(name);
	}

	@Override
	public void addListener(Consumer<IJavaProjectData> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Consumer<IJavaProjectData> listener) {
		listeners.remove(listener);
	}

}
