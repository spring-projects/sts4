/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.validation.rules;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;

import com.google.common.collect.ImmutableList;

public class BootValidationProblemType implements ProblemType {
	
	private final String id;
	private final ProblemSeverity defaultSeverity;
	
	private static final List<ProblemType> problemTypes = new ArrayList<>();
	private String label;
	private String description;
	
	public static List<ProblemType> values() {
		synchronized (problemTypes) {
			return ImmutableList.copyOf(problemTypes);
		}
	}

	public BootValidationProblemType(String id, ProblemSeverity defaultSeverity, String label, String description) {
		this.id = id;
		this.defaultSeverity = defaultSeverity;
		this.label = label;
		this.description = description;
		synchronized (problemTypes) {
			problemTypes.add(this);
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public ProblemSeverity getDefaultSeverity() {
		return defaultSeverity;
	}

	@Override
	public String toString() {
		return id;
	}

	public String getId() {
		return id;
	}

}
