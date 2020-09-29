/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

public class GeneratedResource implements ITypedElement, IStreamContentAccessor {

	private String name;
	private Image image;
	private String content;

	public GeneratedResource(String name, Image image, String content) {
		super();
		this.name = name;
		this.image = image;
		this.content = content;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public String getType() {
		return "xml";
	}

	@Override
	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(content.getBytes());
	}
}