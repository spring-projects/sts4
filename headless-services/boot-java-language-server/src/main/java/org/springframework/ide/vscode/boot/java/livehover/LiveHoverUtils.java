/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover;

import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;

public class LiveHoverUtils {

	public static String showBean(LiveBean bean) {
		return "Bean [id: " + bean.getId() + ", type: `" + bean.getType() + "`]";
	}

	public static String niceAppName(SpringBootApp app) {
		return "Process [PID=" + app.getProcessID() + ", name=`" + app.getProcessName() + "`]";
	}

	public static boolean hasRelevantBeans(SpringBootApp app, LiveBean definedBean) {
		return findRelevantBeans(app, definedBean).findAny().isPresent();
	}

	public static Stream<LiveBean> findRelevantBeans(SpringBootApp app, LiveBean definedBean) {
		return app.getBeans().getBeansOfName(definedBean.getId()).stream()
				.filter(bean -> definedBean.getType().equals(bean.getType()));
	}

}
