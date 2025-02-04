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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.springframework.ide.vscode.commons.protocol.spring.AnnotationAttributeValue;
import org.springframework.ide.vscode.commons.protocol.spring.AnnotationMetadata;
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

			List<EventListenerIndexElement> listeners = index.getNodesOfType(EventListenerIndexElement.class);
			List<EventPublisherIndexElement> publishers = index.getNodesOfType(EventPublisherIndexElement.class);

			// when offset is inside an event listener, look for references from publishers
			Optional<EventListenerIndexElement> listenerElement = listeners.stream()
					.filter(listener -> listener.getLocation().getUri().equals(doc.getUri()))
					.filter(eventListener -> isPositionInside(position, eventListener.getLocation()))
					.findAny();

			if (listenerElement.isPresent()) {
				Set<String> eventTypes = getListenerEventTypes(listenerElement.get());
				
				List<Location> foundLocations = publishers.stream()
					.filter(publisher -> {
						if (eventTypes.contains(publisher.getEventType())) return true;
						
						for (String listenerEventType : eventTypes) {
							if (publisher.getEventTypesFromHierarchy().contains(listenerEventType)) {
								return true;
							}
						}
						return false;
					})
					.map(publisher -> publisher.getLocation())
					.toList();
				
				if (foundLocations.size() > 0) {
					return foundLocations;
				}
			}
			
			// when offset is inside an event publisher, look for references from listeners
			else {
				Optional<EventPublisherIndexElement> publisherElement = publishers.stream()
						.filter(publisher -> publisher.getLocation().getUri().equals(doc.getUri()))
						.filter(eventPublisher -> isPositionInside(position, eventPublisher.getLocation()))
						.findAny();

				if (publisherElement.isPresent()) {
					String eventType = publisherElement.get().getEventType();
					Set<String> eventTypesFromHierarchy = publisherElement.get().getEventTypesFromHierarchy();
					
					List<Location> foundLocations = listeners.stream()
						.filter(listener -> {
							Set<String> listenerEventTypes = getListenerEventTypes(listener);
							for (String listenerEventType : listenerEventTypes) {
								if (listenerEventType.equals(eventType)) {
									return true;
								}
								if (eventTypesFromHierarchy.contains(listenerEventType)) {
									return true;
								}
							}
							return false;
						})
						.map(listener -> listener.getLocation())
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
	
	private Set<String> getListenerEventTypes(EventListenerIndexElement eventListenerIndexElement) {
		AnnotationMetadata[] annotations = eventListenerIndexElement.getAnnotations();
		if (annotations != null && annotations.length > 0) {
			for (AnnotationMetadata annotationMetadata : annotations) {
				Map<String, AnnotationAttributeValue[]> attributes = annotationMetadata.getAttributes();
				if (attributes.containsKey("classes")) {
					AnnotationAttributeValue[] annotationAttributeValues = attributes.get("classes");
					return Arrays.stream(annotationAttributeValues)
							.map(attributeValue -> attributeValue.getName())
							.collect(Collectors.toSet());
				}
				else if (attributes.containsKey("value")) {
					AnnotationAttributeValue[] annotationAttributeValues = attributes.get("value");
					return Arrays.stream(annotationAttributeValues)
							.map(attributeValue -> attributeValue.getName())
							.collect(Collectors.toSet());
				}
			}
		}
		
		return Set.of(eventListenerIndexElement.getEventType());
	}

	private boolean isPositionInside(Position position, Location location) {
		boolean afterStart = position.getLine() > location.getRange().getStart().getLine()
				|| (position.getLine() == location.getRange().getStart().getLine() && position.getCharacter() >= location.getRange().getStart().getCharacter());
		
		boolean beforeEnd = position.getLine() < location.getRange().getEnd().getLine()
				|| (position.getLine() == location.getRange().getEnd().getLine() && position.getCharacter() <= location.getRange().getEnd().getCharacter());
		
		return afterStart && beforeEnd;
	}

}
