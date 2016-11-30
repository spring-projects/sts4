package org.springframework.ide.vscode.boot.properties.hover;

import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.languageserver.util.IRegion;
import org.springframework.ide.vscode.commons.util.Renderable;

import reactor.util.function.Tuple2;

public class PropertiesHoverInfoProvider implements HoverInfoProvider {
	
	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;
	private JavaProjectFinder projectFinder;
	
	public PropertiesHoverInfoProvider(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider, JavaProjectFinder projectFinder) {
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		this.projectFinder = projectFinder;
	}

	@Override
	public Tuple2<Renderable, IRegion> getHoverInfo(IDocument document, int offset) throws Exception {
		return new PropertiesHoverCalculator(indexProvider.getIndex(document),
				typeUtilProvider.getTypeUtil(document), projectFinder.find(document), document, offset).calculate();
	}
	



}
