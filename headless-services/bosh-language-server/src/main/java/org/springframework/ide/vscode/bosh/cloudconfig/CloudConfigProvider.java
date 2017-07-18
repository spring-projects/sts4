package org.springframework.ide.vscode.bosh.cloudconfig;

import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

/**
 * Responsible for somehow obtaining {@link CloudConfigModel} relative to a {@link DynamicSchemaContext}
 */
public interface CloudConfigProvider {

	CloudConfigModel getCloudConfig(DynamicSchemaContext dc) throws Exception;

}
