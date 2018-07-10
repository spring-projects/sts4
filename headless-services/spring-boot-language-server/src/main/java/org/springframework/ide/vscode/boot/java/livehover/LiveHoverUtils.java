/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.SpringResource;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Renderables;
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

	public static String showBeanWithResource(BootJavaLanguageServerComponents server, LiveBean bean, String indentStr, IJavaProject project) {
		String newline = "  \n"+indentStr; //Note: the double space before newline makes markdown see it as a real line break

		String type = bean.getType(true);

		StringBuilder buf = new StringBuilder("Bean: ");
		buf.append(bean.getId());
		SourceLinks sourceLinks = SourceLinkFactory.createSourceLinks(server);
		if (type != null) {
			// Try creating a URL link to open source for the type
			buf.append(newline);
			buf.append("Type: ");
			Optional<String> url = sourceLinks.sourceLinkUrlForFQName(project, type);
			if (url.isPresent()) {
				buf.append(Renderables.link(type, url.get()).toMarkdown());
			} else {
				buf.append("`" + type + "`");
			}
		}
		String resource = bean.getResource();
		if (StringUtil.hasText(resource)) {
			buf.append(newline);
			buf.append("Resource: ");
			buf.append(showResource(sourceLinks, resource, project));
		}
		return buf.toString();
	}

	public static String showResource(SourceLinks sourceLinks, String resource, IJavaProject project) {
		return new SpringResource(sourceLinks, resource, project).toMarkdown();
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

	public static String niceAppName(SpringBootApp app) {
		return niceAppName(app.getProcessID(), app.getProcessName());
	}

	public static String niceAppName(String processId, String processName) {
		return "Process [PID="+processId+", name=`"+processName+"`]";
	}


}
