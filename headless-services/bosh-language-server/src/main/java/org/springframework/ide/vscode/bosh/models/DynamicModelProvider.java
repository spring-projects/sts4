package org.springframework.ide.vscode.bosh.models;

import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

/**
 * Responsible for somehow obtaining {@link CloudConfigModel} relative to a {@link DynamicSchemaContext}
 */
public interface DynamicModelProvider<T> {
	T getModel(DynamicSchemaContext dc) throws Exception;
}
