/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.docker.exceptions;

import java.util.regex.Pattern;

import org.springframework.ide.eclipse.boot.dash.api.AppConsole;
import org.springframework.ide.eclipse.boot.dash.console.LogType;

public class MissingBuildTagException extends DockerBuildException {

	private static final long serialVersionUID = 1L;
	
	public final Pattern[] whatWeLookedFor;

	public MissingBuildTagException(Pattern... whatWeLookedFor) {
		super("Couldn't detect the image id or tag");
		this.whatWeLookedFor = whatWeLookedFor;
	}

	@Override
	public void writeDetailedExplanation(AppConsole console) throws Exception {
		console.write("We detect the image tag or hash by matching specific regexp patterns", LogType.STDERROR);
		console.write("in the build output. But none of the patterns we look for where found.", LogType.STDERROR);
		console.write("", LogType.STDERROR);
		console.write("These are the patterns that we looked for:",  LogType.STDERROR);
		console.write("",  LogType.STDERROR);
		for (Pattern pattern : whatWeLookedFor) {
			console.write("   regexp: /"+pattern+"/",  LogType.STDERROR);
		}
	}

}
