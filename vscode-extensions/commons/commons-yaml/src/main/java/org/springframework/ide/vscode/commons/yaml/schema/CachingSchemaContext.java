package org.springframework.ide.vscode.commons.yaml.schema;

import java.util.Set;

public abstract class CachingSchemaContext implements DynamicSchemaContext {

	private Set<String> definedProps;

	protected abstract Set<String> computeDefinedProperties();

	@Override
	final public Set<String> getDefinedProperties() {
		if (definedProps==null) {
			definedProps = computeDefinedProperties();
		}
		return definedProps;
	}
}
