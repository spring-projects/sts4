/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.events;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.handlers.ReferenceProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class EventReferenceProvider implements ReferenceProvider {

	private static final Logger log = LoggerFactory.getLogger(EventReferenceProvider.class);

	private final SpringMetamodelIndex index;

	public EventReferenceProvider(SpringMetamodelIndex index) {
		this.index = index;
	}

	@Override
	public List<? extends Location> provideReferences(CancelChecker cancelToken, IJavaProject project, ASTNode node,
			Annotation annotation, ITypeBinding type, int offset) {
		return null;
	}

	@Override
	public List<? extends Location> provideReferences(CancelChecker cancelToken, IJavaProject project, TextDocument doc, ASTNode node, int offset) {
		try {
			Position position = doc.toPosition(offset);

			Bean[] beans = index.getBeans();

			// when offset is inside an event listener, find the respective event type
			Optional<String> listenerEventType = Arrays.stream(beans)
					.filter(bean -> bean.getLocation().getUri().equals(doc.getUri()))
					.flatMap(bean -> bean.getChildren().stream())
					.filter(element -> element instanceof EventListenerIndexElement)
					.map(element -> (EventListenerIndexElement) element)
					.filter(eventListener -> isPositionInside(position, eventListener.getLocation()))
					.map(eventListener -> eventListener.getEventType())
					.findAny();

			if (listenerEventType.isPresent()) {
				// use the listener event type to look for publishers for that type
				String eventType = listenerEventType.get();
				
				List<Location> foundLocations = Arrays.stream(beans)
					.flatMap(bean -> bean.getChildren().stream())
					.filter(element -> element instanceof EventPublisherIndexElement)
					.map(element -> (EventPublisherIndexElement) element)
					.filter(publisher -> publisher.getEventType().equals(eventType) || publisher.getEventTypesFromHierarchy().contains(eventType))
					.map(publisher -> publisher.getLocation())
					.toList();
				
				if (foundLocations.size() > 0) {
					return foundLocations;
				}
			}
			
			// when offset is inside an event publisher, find the respective event type
			else {
				Optional<String> publisherEventType = Arrays.stream(beans)
						.filter(bean -> bean.getLocation().getUri().equals(doc.getUri()))
						.flatMap(bean -> bean.getChildren().stream())
						.filter(element -> element instanceof EventPublisherIndexElement)
						.map(element -> (EventPublisherIndexElement) element)
						.filter(eventListener -> isPositionInside(position, eventListener.getLocation()))
						.map(eventListener -> eventListener.getEventType())
						.findAny();

				if (publisherEventType.isPresent()) {
					// use the listener event type to look for publishers for that type
					String eventType = publisherEventType.get();
					
					List<Location> foundLocations = Arrays.stream(beans)
						.flatMap(bean -> bean.getChildren().stream())
						.filter(element -> element instanceof EventListenerIndexElement)
						.map(element -> (EventListenerIndexElement) element)
						.filter(listener -> listener.getEventType().equals(eventType))
						.map(listener-> listener.getLocation())
						.toList();
					
					if (foundLocations.size() > 0) {
						return foundLocations;
					}
				}

			}

		} catch (BadLocationException e) {
			log.error("", e);
		}
		return null;
	}
	
	private boolean isPositionInside(Position position, Location location) {
		boolean afterStart = position.getLine() > location.getRange().getStart().getLine()
				|| (position.getLine() == location.getRange().getStart().getLine() && position.getCharacter() >= location.getRange().getStart().getCharacter());
		
		boolean beforeEnd = position.getLine() < location.getRange().getEnd().getLine()
				|| (position.getLine() == location.getRange().getEnd().getLine() && position.getCharacter() <= location.getRange().getEnd().getCharacter());
		
		return afterStart && beforeEnd;
	}

}
