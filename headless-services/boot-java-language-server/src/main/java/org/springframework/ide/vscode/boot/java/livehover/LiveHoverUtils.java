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

import org.springframework.ide.vscode.boot.java.utils.SpringResource;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.StringUtil;

public class LiveHoverUtils {

	public static String showBean(LiveBean bean) {
		StringBuilder buf = new StringBuilder("Bean [id: " + bean.getId());
		String type = bean.getType(true);
		if (type != null) {
			buf.append(", type: `" + type + "`");
		}
		buf.append(']');
		return buf.toString();
	}

	public static String showBeanWithResource(LiveBean bean, String indentStr, IJavaProject project) {
		String newline = "  \n"+indentStr; //Note: the double space before newline makes markdown see it as a real line break
		StringBuilder buf = new StringBuilder("Bean: ");
		buf.append(bean.getId());
		String type = bean.getType(true);
		if (type != null) {
			buf.append(newline);
			buf.append("Type: `" + type + "`");
		}
		String resource = bean.getResource();
		if (StringUtil.hasText(resource)) {
			buf.append(newline);
			buf.append("Resource: ");
			buf.append(showResource(resource, project));
		}
		return buf.toString();
	}

	public static String showResource(String resource, IJavaProject project) {
		return new SpringResource(resource, project).toMarkdown();
	}

	public static String niceAppName(SpringBootApp app) {
		return niceAppName(app.getProcessID() ,app.getProcessName());
	}

	public static String niceAppName(String processId, String processName) {
		return "Process [PID=" + processId + ", name=`" + processName + "`]";
	}

	public static boolean hasRelevantBeans(SpringBootApp app, LiveBean definedBean) {
		return findRelevantBeans(app, definedBean).findAny().isPresent();
	}

	public static Stream<LiveBean> findRelevantBeans(SpringBootApp app, LiveBean definedBean) {
		LiveBeansModel beansModel = app.getBeans();
		if (beansModel != null) {
			Stream<LiveBean> relevantBeans = beansModel.getBeansOfName(definedBean.getId()).stream();
			String type = definedBean.getType();
			if (type != null) {
				relevantBeans = relevantBeans.filter(bean -> type.equals(bean.getType(true)));
			}
			return relevantBeans;
		}
		return Stream.empty();
	}

}
