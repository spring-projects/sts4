/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * Compare and Merge input for YAML string
 *
 * @author Alex Boyko
 *
 */
public class YamlInput implements ITypedElement, IStreamContentAccessor {

	private String name;
	private Image image;
	private String content;

	public YamlInput(String name, Image image, String content) {
		super();
		this.name = name;
		this.image = image;
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public Image getImage() {
		return image;
	}

	public String getType() {
		return "yml";
	}

	@Override
	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(content.getBytes());
	}

}
