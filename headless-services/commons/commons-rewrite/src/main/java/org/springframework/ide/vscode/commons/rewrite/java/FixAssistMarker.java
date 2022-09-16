package org.springframework.ide.vscode.commons.rewrite.java;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Range;

public class FixAssistMarker implements Marker {
	
	private UUID id;
	
	private Range scope;
	
	private String recipeId;
	
	private Map<String, Object> parameters = Collections.emptyMap();
	
	public FixAssistMarker(UUID id) {
		super();
		this.id = id;
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
	
	public FixAssistMarker withScope(Range scope) {
		this.scope = scope;
		return this;
	}

	public Range getScope() {
		return scope;
	}
	
	public FixAssistMarker withRecipeId(String recipeId) {
		this.recipeId = recipeId;
		return this;
	}

	public String getRecipeId() {
		return recipeId;
	}
	
	public FixAssistMarker withParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
		return this;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
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
		return Objects.equals(id, other.id);
	}

}
