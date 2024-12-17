package org.springframework.ide.vscode.commons.rewrite.java;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.SearchResult;

public class JavaFileChecker extends Recipe {
    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Checks if source is a Java file";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Checks if source is a Java file.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {
                if (cu.getSourcePath() != null && cu.getSourcePath().getFileName().toString().endsWith(".java")) {
                    return SearchResult.found(cu);
                }
                return cu;
            }
        };
    }
}