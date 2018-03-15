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
			if (manager==null) {
				this.manager = new ClasspathListenerManager(this);
			}
			subscribers.add(callbackCommandId);
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
	}
	
	private static MyClasspathListener classpathListener = new MyClasspathListener();

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		log("ClasspathListenerHandler executeCommand "+commandId+ ", "+arguments);
		if (commandId.equals("sts.java.addClasspathListener")) {
			return addClasspathListener((String)arguments.get(0));
		}
		throw new IllegalArgumentException("Unknown command id: "+commandId);
	}

	private Object addClasspathListener(String callbackCommandId) {
		log("ClasspathListenerHandler addClasspathListener "+callbackCommandId);
		classpathListener.subscribe(callbackCommandId);
		log("ClasspathListenerHandler addClasspathListener "+callbackCommandId+ " => OK");
		return "ok";
	}

}
