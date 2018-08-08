/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.kubernetes.container;

import org.springframework.util.Assert;

public class DockerImage {

	private String repository;
	private String tag;
	private String image;

	public DockerImage(String image) {
		Assert.hasText(image, "An image repository is required");
		this.image = image;
		parse(this.image);

	}

	private void parse(String image) {
		String[] split = image.split(":");
		if (split.length > 0) {
			this.repository = split[0];
			this.tag = split.length > 1 ? split[1] : null;
		}
	}

	public String getRepository() {
		return repository;
	}

	public String getTag() {
		return tag;
	}

	public String getImage() {
		return image;
	}

	@Override
	public String toString() {
		return this.image;
	}

}
