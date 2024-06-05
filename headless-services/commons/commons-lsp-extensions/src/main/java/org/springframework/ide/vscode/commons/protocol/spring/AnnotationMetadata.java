/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.spring;

import java.util.Map;

/**
 * @author Martin Lippert
 */
public class AnnotationMetadata {
	
	private final String annotationType;
	private final boolean isMetaAnnotation;
	private final Map<String, String[]> attributes;
	
	public AnnotationMetadata(String annotationType, boolean isMetaAnnotation, Map<String, String[]> attributes) {
		this.annotationType = annotationType;
		this.isMetaAnnotation = isMetaAnnotation;
		this.attributes = attributes;
	}
	
	public String getAnnotationType() {
		return annotationType;
	}

	public boolean isMetaAnnotation() {
		return isMetaAnnotation;
	}

	public Map<String, String[]> getAttributes() {
		return attributes;
	}

}
