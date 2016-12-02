package org.springframework.ide.vscode.application.properties.metadata.hints;

import java.util.List;

import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;

import static org.springframework.ide.vscode.commons.util.Renderables.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class ValueHintHoverInfo {

	public static Renderable create(StsValueHint hint) {
		Builder<Renderable> builder = ImmutableList.builder();
		builder.add(bold(""+hint.getValue()));
		builder.add(paragraph(hint.getDescription()));
		return concat(builder.build());
	}

}
