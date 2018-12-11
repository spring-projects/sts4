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
package org.springframework.tooling.boot.ls.jdt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.lsp4e.operations.hover.LSBasedHover;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringBootJavaHoverProvider extends JavadocHover {
	
	private LSBasedHover lsBasedHover;

	public SpringBootJavaHoverProvider() {
		super();
		lsBasedHover = new LSBasedHover();
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		// Launch javadoc hover computation in async fashion
		CompletableFuture<JavadocBrowserInformationControlInput> javadocHoverFuture = CompletableFuture.supplyAsync(
				() -> (JavadocBrowserInformationControlInput) super.getHoverInfo2(textViewer, hoverRegion));
		String content = this.lsBasedHover.getHoverInfo(textViewer, hoverRegion);
		if (content != null && !content.isEmpty()) {
			IJavaElement javaElement = null;
			JavadocBrowserInformationControlInput previous = null;
			int leadingImageWidth = 0;
			JavadocBrowserInformationControlInput input;
			String html = "";
			try {
				input = javadocHoverFuture.get(500, TimeUnit.MILLISECONDS);
				if (input != null) {
					previous = (JavadocBrowserInformationControlInput) input.getPrevious();
					javaElement = input.getElement();
					leadingImageWidth = input.getLeadingImageWidth();
					html = input.getHtml();
				}
			} catch (InterruptedException e) {
				html = noJavadocMessage("Javadoc unavailable.");
			} catch (ExecutionException e) {
				html = noJavadocMessage("Javadoc unavailable. Failed to obtain it.");
			} catch (TimeoutException e) {
				html = noJavadocMessage("Javadoc unavailable. Took too long to obtain it.");
			}
			content = content + html;
			return new JavadocBrowserInformationControlInput(previous, javaElement, content, leadingImageWidth);
		} else {
			javadocHoverFuture.cancel(true);
		}
		return null;
	}
	
	private String noJavadocMessage(String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("<h4>");
		sb.append(message);
		sb.append("</h4>");
		return sb.toString();
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return this.lsBasedHover.getHoverRegion(textViewer, offset);
	}
	
}
