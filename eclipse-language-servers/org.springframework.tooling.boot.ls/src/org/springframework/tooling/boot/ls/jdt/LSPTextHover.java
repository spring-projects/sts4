/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - initial implementation
 *  Lucas Bullen (Red Hat Inc.) - Bug 508458 - Add support for codelens
 *  Angelo Zerr <angelo.zerr@gmail.com> - Bug 525602 - LSBasedHover must check if LS have codelens capability
 *  Lucas Bullen (Red Hat Inc.) - [Bug 517428] Requests sent before initialization
 *  Kris De Volder - copied from lsp4e see: https://www.pivotaltracker.com/story/show/167763955
 *******************************************************************************/
package org.springframework.tooling.boot.ls.jdt;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerPlugin;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.mylyn.wikitext.markdown.MarkdownLanguage;
import org.eclipse.mylyn.wikitext.parser.MarkupParser;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;

/**
 * LSP implementation of {@link org.eclipse.jface.text.ITextHover}
 *
 */
public class LSPTextHover implements ITextHover, ITextHoverExtension {

	private static final String HEAD = "<head>"; //$NON-NLS-1$


	private static final MarkupParser MARKDOWN_PARSER = new MarkupParser(new MarkdownLanguage());


	private IRegion lastRegion;
	private ITextViewer lastViewer;
	private CompletableFuture<List<Hover>> request;

	public LSPTextHover() {
		// nothing to init yet, comment requested by sonar
	}

	public static String styleHtml(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		// put CSS styling to match Eclipse style
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		Color foreground = colorRegistry.get("org.eclipse.ui.workbench.HOVER_FOREGROUND"); //$NON-NLS-1$
		Color background = colorRegistry.get("org.eclipse.ui.workbench.HOVER_BACKGROUND"); //$NON-NLS-1$
		String style = "<style TYPE='text/css'>html { " + //$NON-NLS-1$
				"font-family: " + JFaceResources.getDefaultFontDescriptor().getFontData()[0].getName() + "; " + //$NON-NLS-1$ //$NON-NLS-2$
				"font-size: " + Integer.toString(JFaceResources.getDefaultFontDescriptor().getFontData()[0].getHeight()) //$NON-NLS-1$
				+ "pt; " + //$NON-NLS-1$
				(background != null ? "background-color: " + toHTMLrgb(background.getRGB()) + "; " : "") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				(foreground != null ? "color: " + toHTMLrgb(foreground.getRGB()) + "; " : "") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				" }</style>"; //$NON-NLS-1$

		int headIndex = html.indexOf(HEAD);
		StringBuilder builder = new StringBuilder(html.length() + style.length());
		builder.append(html.substring(0, headIndex + HEAD.length()));
		builder.append(style);
		builder.append(html.substring(headIndex + HEAD.length()));
		return builder.toString();
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (textViewer == null || hoverRegion == null) {
			return null;
		}
		if (this.request == null || !textViewer.equals(this.lastViewer) || !hoverRegion.equals(this.lastRegion)) {
			initiateHoverRequest(textViewer, hoverRegion.getOffset());
		}
		try {
			String result = request.get(500, TimeUnit.MILLISECONDS).stream()
				.filter(Objects::nonNull)
				.map(LSPTextHover::getHoverString)
				.filter(Objects::nonNull)
				.collect(Collectors.joining("\n\n")) //$NON-NLS-1$
				.trim();
			if (!result.isEmpty()) {
				return styleHtml(MARKDOWN_PARSER.parseToHtml(result));
			}
		} catch (ExecutionException | TimeoutException e) {
			LanguageServerPlugin.logError(e);
		} catch (InterruptedException e) {
			LanguageServerPlugin.logError(e);
			Thread.currentThread().interrupt();
		}
		return null;
	}

	protected static @Nullable String getHoverString(@NonNull Hover hover) {
		Either<List<Either<String, MarkedString>>, MarkupContent> hoverContent = hover.getContents();
		if (hoverContent.isLeft()) {
			List<Either<String, MarkedString>> contents = hoverContent.getLeft();
			if (contents == null || contents.isEmpty()) {
				return null;
			}
			return contents.stream().map(content -> {
				if (content.isLeft()) {
					return content.getLeft();
				} else if (content.isRight()) {
					MarkedString markedString = content.getRight();
					// TODO this won't work fully until markup parser will support syntax
					// highlighting but will help display
					// strings with language tags, e.g. without it things after <?php tag aren't
					// displayed
					if (markedString.getLanguage() != null && !markedString.getLanguage().isEmpty()) {
						return String.format("```%s%n%s%n```", markedString.getLanguage(), markedString.getValue()); //$NON-NLS-1$
					} else {
						return markedString.getValue();
					}
				} else {
					return ""; //$NON-NLS-1$
				}
			}).filter(((Predicate<String>) String::isEmpty).negate()).collect(Collectors.joining("\n\n")); //$NON-NLS-1$ )
		} else {
			return hoverContent.getRight().getValue();
		}
	}

	private static @NonNull String toHTMLrgb(RGB rgb) {
		StringBuilder builder = new StringBuilder(7);
		builder.append('#');
		appendAsHexString(builder, rgb.red);
		appendAsHexString(builder, rgb.green);
		appendAsHexString(builder, rgb.blue);
		return builder.toString();
	}

	private static void appendAsHexString(StringBuilder buffer, int intValue) {
		String hexValue= Integer.toHexString(intValue);
		if (hexValue.length() == 1) {
			buffer.append('0');
		}
		buffer.append(hexValue);
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		if (textViewer == null) {
			return null;
		}
		if (this.request == null || this.lastRegion == null || !textViewer.equals(this.lastViewer)
				|| offset < this.lastRegion.getOffset() || offset > lastRegion.getOffset() + lastRegion.getLength()) {
			initiateHoverRequest(textViewer, offset);
		}
		try {
			final IDocument document = textViewer.getDocument();
			boolean[] oneHoverAtLeast = new boolean[] { false };
			int[] regionStartOffset = new int[] { 0 };
			int[] regionEndOffset = new int[] { document.getLength() };
			this.request.get(500, TimeUnit.MILLISECONDS).stream()
				.filter(Objects::nonNull)
				.map(Hover::getRange)
				.filter(Objects::nonNull)
				.forEach(range -> {
					try {
							regionStartOffset[0] = Math.max(regionStartOffset[0],
									LSPEclipseUtils.toOffset(range.getStart(), document));
							regionEndOffset[0] = Math.min(regionEndOffset[0],
									LSPEclipseUtils.toOffset(range.getEnd(), document));
						oneHoverAtLeast[0] = true;
					} catch (BadLocationException e) {
						LanguageServerPlugin.logError(e);
					}
				});
			if (oneHoverAtLeast[0]) {
				this.lastRegion = new Region(regionStartOffset[0], regionEndOffset[0] - regionStartOffset[0]);
				return this.lastRegion;
			}
		} catch (ExecutionException | TimeoutException e1) {
			LanguageServerPlugin.logError(e1);
		} catch (InterruptedException e1) {
			LanguageServerPlugin.logError(e1);
			Thread.currentThread().interrupt();
		}
		this.lastRegion = new Region(offset, 0);
		return this.lastRegion;
	}

	/**
	 * Initialize hover requests with hover (if available) and codelens (if
	 * available).
	 *
	 * @param viewer
	 *            the text viewer.
	 * @param offset
	 *            the hovered offset.
	 */
	private void initiateHoverRequest(@NonNull ITextViewer viewer, int offset) {
		final IDocument document = viewer.getDocument();
		this.lastViewer = viewer;
		this.request = LanguageServiceAccessor
			.getLanguageServers(document, capabilities -> Boolean.TRUE.equals(capabilities.getHoverProvider()))
				.thenApplyAsync(languageServers -> // Async is very important here, otherwise the LS Client thread is in
													// deadlock and doesn't read bytes from LS
				languageServers.stream()
					.map(languageServer -> {
						try {
								return languageServer.getTextDocumentService()
										.hover(LSPEclipseUtils.toTextDocumentPosistionParams(offset, document)).get();
						} catch (ExecutionException | BadLocationException e) {
							LanguageServerPlugin.logError(e);
							return null;
						} catch (InterruptedException e) {
							LanguageServerPlugin.logError(e);
							Thread.currentThread().interrupt();
							return null;
						}
					}).filter(Objects::nonNull).collect(Collectors.toList()));
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new AbstractReusableInformationControlCreator() {
			@Override
			protected IInformationControl doCreateInformationControl(Shell parent) {
				if (BrowserInformationControl.isAvailable(parent)) {
					return new FocusableBrowserInformationControl(parent);
				} else {
					return new DefaultInformationControl(parent);
				}
			}
		};
	}
}
