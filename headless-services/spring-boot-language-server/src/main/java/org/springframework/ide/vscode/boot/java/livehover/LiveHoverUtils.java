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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.SpringResource;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.java.IJavaProject;
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
		buf.append('`');
		buf.append(bean.getId());
		buf.append('`');
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

	public static String showBeanInline(BootJavaLanguageServerComponents server, IJavaProject project, LiveBean bean) {
		String id = bean.getId();
		String type = bean.getType(true);
		StringBuilder sb = new StringBuilder();
		sb.append('`');
		sb.append(id);
		sb.append('`');
		String displayId = sb.toString();
		SourceLinks sourceLinks = SourceLinkFactory.createSourceLinks(server);
		if (type != null) {
			Optional<String> url = sourceLinks.sourceLinkUrlForFQName(project, type);
			if (url.isPresent()) {
				return Renderables.link(displayId, url.get()).toMarkdown();
			}
		}
		return displayId;
	}

	public static boolean doBeansFitInline(Collection<LiveBean> beans, int maxLength, String delimiter) {
		int length = 0;
		for (LiveBean bean : beans) {
			if (length != 0) {
				length += delimiter.length();
			}
			length += bean.getId().length();
			if (length > maxLength) {
				return false;
			}
		}
		return true;
	}

	public static String showBeanIdAndTypeInline(BootJavaLanguageServerComponents server, IJavaProject project, LiveBean bean) {
		String id = bean.getId();
		String type = bean.getType(true);
		SourceLinks sourceLinks = SourceLinkFactory.createSourceLinks(server);
		String displayType = type;
		if (type != null) {
			int lastDotIdx = type.lastIndexOf('.');
			if (lastDotIdx >= 0 && lastDotIdx < type.length() - 1) {
				displayType = "`" + type.substring(lastDotIdx + 1) + "`";
			}
			Optional<String> url = sourceLinks.sourceLinkUrlForFQName(project, type);
			if (url.isPresent()) {
				displayType = Renderables.link(displayType, url.get()).toMarkdown();
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append('`');
		sb.append(id);
		sb.append('`');
		if (displayType != null) {
			sb.append(' ');
			sb.append(displayType);
		}
		return sb.toString();
	}


	public static String showResource(SourceLinks sourceLinks, String resource, IJavaProject project) {
		return new SpringResource(sourceLinks, resource, project).toMarkdown();
	}

	public static boolean hasRelevantBeans(SpringBootApp app, LiveBean definedBean) {
		return findRelevantBeans(app, definedBean).stream().findAny().isPresent();
	}

	public static List<LiveBean> findRelevantBeans(SpringBootApp app, LiveBean definedBean) {
		LiveBeansModel beansModel = app.getBeans();
		if (beansModel != null) {
			List<LiveBean> relevantBeans = beansModel.getBeansOfName(definedBean.getId());
			String type = definedBean.getType();
			if (type != null) {
				// TODO: check if we should check for bean type rather than id that we build ourselves based on type
//				if (relevantBeans.isEmpty()) {
//					relevantBeans = beansModel.getBeansOfType(type);
//				} else {
					relevantBeans = relevantBeans.stream().filter(bean -> type.equals(bean.getType(true))).collect(Collectors.toList());
//				}
			}
			return relevantBeans;
		}
		return Collections.emptyList();
	}

	public static String niceAppName(SpringBootApp app) {
		try {
			return niceAppName(app.getProcessID(), app.getProcessName());
		} catch (Exception e) {
			e.printStackTrace();
			return app.toString();
		}
	}

	public static String niceAppName(String processId, String processName) {
		return "Process [PID="+processId+", name=`"+processName+"`]";
	}


}
