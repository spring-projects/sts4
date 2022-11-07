/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openrewrite.marker.Marker;

public class FixAssistMarker implements Marker {
	
	private UUID id;
	
	private String descriptorId;
	
	private List<FixDescriptor> fixes = new ArrayList<>();
	
	private String label;
	
	public FixAssistMarker(UUID id, String descriptorId) {
		super();
		this.id = id;
		this.descriptorId = descriptorId;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FixAssistMarker withId(UUID id) {
		this.id = id;
		return this;
	}
	
	public String getDescriptorId() {
		return descriptorId;
	}
	
	public FixAssistMarker withFix(FixDescriptor f) {
		fixes.add(f);
		return this;
	}
	
	public FixAssistMarker withFixes(FixDescriptor... fixes) {
		this.fixes.addAll(Arrays.asList(fixes));
		return this;
	}

	public List<FixDescriptor> getFixes() {
		return fixes;
	}
	
	public FixAssistMarker withLabel(String label) {
		this.label = label;
		return this;
	}
	
	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		return Objects.hash(descriptorId, fixes, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FixAssistMarker other = (FixAssistMarker) obj;
		return Objects.equals(descriptorId, other.descriptorId) && Objects.equals(fixes, other.fixes)
				&& Objects.equals(id, other.id);
	}
	

}
