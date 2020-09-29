/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.console;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import reactor.core.Disposable;

public class ApplicationLogConsole extends MessageConsole implements IPropertyChangeListener, IApplicationLogConsole {

	private Map<LogType, IOConsoleOutputStream> activeStreams = new HashMap<>();

	private Disposable logStreamingToken;

	public ApplicationLogConsole(String name, String type, ImageDescriptor icon) {
		super(name, type, icon, true);
	}

	public synchronized void setLogStreamingToken(Disposable logStreamingToken) {
		if (this.logStreamingToken != null) {
			this.logStreamingToken.dispose();
		}
		this.logStreamingToken = logStreamingToken;
	}

	public synchronized Disposable getLogStreamingToken() {
		return this.logStreamingToken;
	}

	public synchronized void writeLog(LogMessage log) {
		if (log == null) {
			return;
		}
		final LogType logType = log.getType();
		writeApplicationLog(log.getMessage(), logType);
	}

	/**
	 *
	 * @param message
	 * @param type
	 * @return true if successfully wrote to stream. False otherwise
	 */
	public synchronized void writeApplicationLog(String message, LogType type) {
		if (message != null) {
			IOConsoleOutputStream stream = getStream(type);

			try {
				if (stream != null && !stream.isClosed()) {
					message = format(message);
					stream.write(message);
				}
			} catch (IOException e) {
				Log.log(e);
			}
		}
	}

	protected static String format(String message) {
		if (message.contains("\n") || message.contains("\r")) {
			return message;
		}
		return message + '\n';
	}

	/**
	 * Closes the console AND removes it from the console manager in Eclipse.
	 *  <p>
	 * IMPORTANT: When the  console is removed from the console manager by close,
	 * it ALSO destroys the console, so it is NOT necessary
	 * to call destroy on the console after it is closed.
	 * <p>
	 * Once a console is closed, it CANNOT be used again.
	 */
	public synchronized void close() {
		setLogStreamingToken(null);

		for (IOConsoleOutputStream outputStream : activeStreams.values()) {
			if (!outputStream.isClosed()) {
				try {
					outputStream.close();
				} catch (IOException e) {
					Log.log(e);
				}
			}
		}
		activeStreams.clear();
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		// IMPORTANT: by removing from the console manager, the console is  ALSO  automatically
		// destroyed.  See javadoc for org.eclipse.ui.console.AbstractConsole.destroy()
		// See also PT 170262394
		manager.removeConsoles(new IConsole[] { this });
	}

	protected synchronized IOConsoleOutputStream getStream(final LogType logType) {

		IOConsoleOutputStream stream = activeStreams.get(logType);
		// If the console is no longer managed by the Eclipse console manager,
		// do NOT
		// write to the stream to avoid exceptions
		if (!isStillManaged() || (stream != null && stream.isClosed())) {
			return null;
		}
		if (stream == null) {
			stream = newOutputStream();

			final IOConsoleOutputStream toConfig = stream;

			// Setting colour must be done in UI thread
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					toConfig.setColor(Display.getDefault().getSystemColor(logType.getDisplayColour()));
				}
			});

			activeStreams.put(logType, stream);
		}
		return stream;
	}

	protected synchronized boolean isStillManaged() {
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();

		IConsole[] activeConsoles = manager.getConsoles();
		if (activeConsoles != null) {
			for (IConsole console : activeConsoles) {
				if (console.getName().equals(this.getName())) {
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public void onMessage(LogMessage log) {
		writeLog(log);
	}

	@Override
	public void onComplete() {
		// Leave open for tail
	}

	@Override
	public void onError(Throwable exception) {
		writeApplicationLog(exception.getMessage(), LogType.STDERROR);
	}

	@Override
	protected void init() {
		super.init();
		JFaceResources.getFontRegistry().addListener(this);
	}

	@Override
	protected void dispose() {
		JFaceResources.getFontRegistry().removeListener(this);
		super.dispose();
	}

	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getProperty();
		if (property.equals(IDebugUIConstants.PREF_CONSOLE_FONT)) {
			setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
		}
	}

}
