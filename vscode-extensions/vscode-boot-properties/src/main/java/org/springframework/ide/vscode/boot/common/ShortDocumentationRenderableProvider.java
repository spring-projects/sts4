package org.springframework.ide.vscode.boot.common;

import static org.springframework.ide.vscode.commons.util.Renderables.concat;
import static org.springframework.ide.vscode.commons.util.Renderables.lineBreak;

import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Renderable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class ShortDocumentationRenderableProvider extends PropertyRenderableProvider {

	public ShortDocumentationRenderableProvider(IJavaProject project, PropertyInfo data) {
		super(project, data);
	}

	@Override
	public Renderable getRenderable() {
		Builder<Renderable> renderableBuilder = ImmutableList.builder();

		Renderable description = getDescription();
		if (description!=null) {
			descriptionRenderable(renderableBuilder, description);
		}
		
		String deflt = formatDefaultValue(getDefaultValue());
		if (deflt!=null) {
			if (description != null) {
				renderableBuilder.add(lineBreak());
			}
			defaultValueRenderable(renderableBuilder, deflt);
		}
		
		if (isDeprecated()) {
			if (description != null) {
				renderableBuilder.add(lineBreak());
			}
			depreactionRenderable(renderableBuilder);
		}		
		
		
		ImmutableList<Renderable> pieces = renderableBuilder.build();
		return pieces.isEmpty() ? null : concat(pieces);
	}
	
	

}
