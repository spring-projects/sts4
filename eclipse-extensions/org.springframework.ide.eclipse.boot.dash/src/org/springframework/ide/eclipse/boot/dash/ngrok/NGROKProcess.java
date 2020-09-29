/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.ngrok;

import java.io.File;
import java.io.FileWriter;

import org.eclipse.jdt.launching.SocketUtil;

/**
 * @author Martin Lippert
 */
public class NGROKProcess {

	private final Process process;
	private final String apiURL;

	public static NGROKProcess startNGROK(String path, String proto, String addr) throws Exception {
		FileWriter fileWriter = null;

		try {
			int freePort = SocketUtil.findFreePort();
			String webAddr = "localhost:" + freePort;

			File ngrokConfigFile = File.createTempFile("ngrok-config", "yml");
			ngrokConfigFile.deleteOnExit();

			fileWriter = new FileWriter(ngrokConfigFile);
			fileWriter.write("web_addr: " + webAddr + "\n");
			fileWriter.close();

			String configFileArg = "-config=" + ngrokConfigFile.getAbsolutePath();

			ProcessBuilder processBuilder = new ProcessBuilder(path, proto, configFileArg, addr);
			Process process = processBuilder.start();

			return new NGROKProcess(process, "http://" + webAddr);
		}
		finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}

	public NGROKProcess(Process process, String apiURL) {
		this.process = process;
		this.apiURL = apiURL;
	}

	public String getApiURL() {
		return apiURL;
	}

	public void terminate() {
		if (this.process != null) {
			this.process.destroy();
		}
	}

}
