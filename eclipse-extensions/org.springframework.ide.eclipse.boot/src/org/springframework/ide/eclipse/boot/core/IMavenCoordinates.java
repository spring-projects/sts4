/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

/**
 * An instance of this interface represents the typical 'Maven coordinates' for an
 * artifact, consisting of a groupId, artifactId and version.
 *
 * @author Kris De Volder
 */
public interface IMavenCoordinates {

	String getGroupId();

	String getArtifactId();

	String getClassifier();

	String getVersion();

}
