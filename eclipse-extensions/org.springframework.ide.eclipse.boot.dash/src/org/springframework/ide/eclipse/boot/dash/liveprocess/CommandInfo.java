/*******************************************************************************
 * Copyright (c) 2019 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.liveprocess;

import java.util.Map;
import java.util.Map.Entry;

public class CommandInfo {

	public final String command;
	public final Map<String,String> info;

	public CommandInfo(String command, Map<String, String> info) {
		super();
		this.command = command;
		this.info = info;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("CommandInfo("+command+", {\n");
		for (Entry<String, String> e : info.entrySet()) {
			s.append("    "+e.getKey() +": "+e.getValue()+"\n");
		}
		s.append("})");
		return s.toString();
	}
}
