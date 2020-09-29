/*******************************************************************************
 * Copyright (c) 2014, 2016 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Type;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategy;

/**
 * Extension of {@link RadioInfo} adds extra attribute(s) only
 * applicable to the 'type' input field.
 *
 * @author Kris De Volder
 */
public class TypeRadioInfo extends RadioInfo {

	private ImportStrategy importStrategy;
	private Type type;

	public String getAction() {
		return type.getAction();
	}

	public TypeRadioInfo(String groupName, Type type, ImportStrategy importStrategy) {
		super(groupName, importStrategy.getId(), type.isDefault());
		this.type = type;
		this.importStrategy = importStrategy;
	}

	@Override
	public String toString() {
		return "Radio(" + getGroupName()+", "+getValue()+")";
	}

	public ImportStrategy getImportStrategy() {
		return importStrategy;
	}

	@Override
	public String getUrlParamValue() {
		//We override super because the start.spring.io wizard doesn't understand the difference between
		// "GRADLE-STS" and "GRADLE-Buildship". Since our own wizard supports multipe import-strategies per
		//project type, our type IDs are distinct from the ones used by start.spring.io
		//This method returns the corresponding id to be used when passing it to start.spring.io as url
		//parameter value.
		return type.getId();
	}

}
