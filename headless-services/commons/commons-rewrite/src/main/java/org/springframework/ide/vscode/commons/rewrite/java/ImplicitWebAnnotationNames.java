package org.springframework.ide.vscode.commons.rewrite.java;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.Range;

public class ImplicitWebAnnotationNames extends org.openrewrite.java.spring.ImplicitWebAnnotationNames implements RangeScopedRecipe {
	
	private Range range;

	@Override
	public String getDisplayName() {
		return "Removes implicit web annotation names.";
	}

	@Override
	public String getDescription() {
		return "Removes implicit web annotation names. The method parameter automatically binds to the Web annotation name when it is the same.";
	}

	@Override
	public void setRange(Range range) {
		this.range = range;
	}
	
	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new RangeScopedJavaIsoVisitor<ExecutionContext>(range) {
			
			@Override
			public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
				J.Annotation a = super.visitAnnotation(annotation, ctx);
				
				if (a != null && a.getArguments() != null) {
					a = a.withArguments(ListUtils.map(a.getArguments(), arg -> {
						Cursor varDeclarationCursor = getCursor().getParentOrThrow();
						J.VariableDeclarations.NamedVariable namedVariable = varDeclarationCursor.<J.VariableDeclarations>getValue().getVariables().get(0);
						if (arg instanceof J.Assignment) {
							J.Assignment assignment = (J.Assignment) arg;
							if (assignment.getVariable() instanceof J.Identifier && assignment.getAssignment() instanceof J.Literal) {
								J.Literal annotationValue = (J.Literal) assignment.getAssignment();
								J.Identifier identifier = (J.Identifier) ((J.Assignment) arg).getVariable();
								if ("value".equals(identifier.getSimpleName())) {
									if(removeArg(namedVariable, annotationValue)) {
										return null;
									}
								}
							}
						} else if (arg instanceof J.Literal) {
							if(removeArg(namedVariable, (J.Literal) arg)) {
								return null;
							}
						}
						return arg;
					}));
				}
				return a != null ? a : annotation;
			}
		};
			
	}
	
	private boolean removeArg(J.VariableDeclarations.NamedVariable namedVariable, J.Literal annotationValue) {
        Object value = annotationValue.getValue();
        assert value != null;
        return namedVariable.getSimpleName().equals(value);
    }
}
