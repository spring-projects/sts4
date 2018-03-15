package org.springframework.tooling.jdt.ls.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaClientConnection;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.handlers.JDTLanguageServer;
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
			String project = jp.getProject().getLocationURI().toString();
			boolean deleted = !jp.exists();
			JavaClientConnection conn = JavaLanguageServerPlugin.getInstance().getClientConnection();
			for (String callbackCommandId : subscribers) {
				conn.executeCommand(callbackCommandId, Arrays.asList(project, deleted));
			}
		}
	}
	
	private static MyClasspathListener classpathListener = new MyClasspathListener();

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		if (commandId.equals("sts.java.addClasspathListener")) {
			return addClasspathListener((String)arguments.get(0));
		}
		return null;
	}

	private Object addClasspathListener(String callbackCommandId) {
		classpathListener.subscribe(callbackCommandId);
		return "ok";
	}

}
