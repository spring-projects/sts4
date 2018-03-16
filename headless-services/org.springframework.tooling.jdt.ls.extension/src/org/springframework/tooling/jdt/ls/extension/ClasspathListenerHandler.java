package org.springframework.tooling.jdt.ls.extension;

import static org.springframework.tooling.jdt.ls.extension.Logger.log;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaClientConnection;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.springframework.tooling.jdt.ls.extension.ClasspathListenerManager.ClasspathListener;

@SuppressWarnings("restriction")
public class ClasspathListenerHandler implements IDelegateCommandHandler {

	static class MyClasspathListener implements ClasspathListener {
		
		private ClasspathListenerManager manager = null;
		private List<String> subscribers = new ArrayList<>(1);

		public synchronized void subscribe(String callbackCommandId) {
			Logger.log("subscribing to classpath changes: "+callbackCommandId);
			if (manager==null) {
				this.manager = new ClasspathListenerManager(this);
			}
			subscribers.add(callbackCommandId);
			Logger.log("subsribers = "+subscribers);
		}

		@Override
		public void classpathChanged(IJavaProject jp) {
			log("Classpath changed "+jp.getElementName());
			String project = jp.getProject().getLocationURI().toString();
			boolean deleted = !jp.exists();
			JavaClientConnection conn = JavaLanguageServerPlugin.getInstance().getClientConnection();
			for (String callbackCommandId : subscribers) {
				conn.executeCommand(callbackCommandId, project, deleted);
			}
		}

		public synchronized void unsubscribe(String callbackCommandId) {
			Logger.log("unsubscribing from classpath changes: "+callbackCommandId);
			if (subscribers!=null) {
				subscribers.remove(callbackCommandId);
				if (subscribers.isEmpty()) {
					subscribers = null;
					if (manager!=null) {
						manager.dispose();
						manager = null;
					}
				}
			}
			Logger.log("subsribers = "+subscribers);
		}
	}
	
	private static MyClasspathListener classpathListener = new MyClasspathListener();

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		log("ClasspathListenerHandler executeCommand "+commandId+ ", "+arguments);
		switch (commandId) {
		case "sts.java.addClasspathListener":
			return addClasspathListener((String)arguments.get(0));
		case "sts.java.removeClasspathListener":
			return removeClasspathListener((String)arguments.get(0));
		default:
			throw new IllegalArgumentException("Unknown command id: "+commandId);
		}
	}

	private Object removeClasspathListener(String callbackCommandId) {
		log("ClasspathListenerHandler addClasspathListener "+callbackCommandId);
		classpathListener.unsubscribe(callbackCommandId);
		log("ClasspathListenerHandler addClasspathListener "+callbackCommandId+ " => OK");
		return "ok";
	}

	private Object addClasspathListener(String callbackCommandId) {
		log("ClasspathListenerHandler addClasspathListener "+callbackCommandId);
		classpathListener.subscribe(callbackCommandId);
		log("ClasspathListenerHandler addClasspathListener "+callbackCommandId+ " => OK");
		return "ok";
	}

}
