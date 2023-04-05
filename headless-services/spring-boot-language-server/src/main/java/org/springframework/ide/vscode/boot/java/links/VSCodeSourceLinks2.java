package org.springframework.ide.vscode.boot.java.links;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.utils.CuDeclarationUtils;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.Region;

public class VSCodeSourceLinks2 extends AbstractSourceLinks2 {

	public VSCodeSourceLinks2(CompilationUnitCache cuCache, JavaProjectFinder projectFinder) {
		super(cuCache, projectFinder);
	}

	@Override
	public Optional<String> sourceLinkForResourcePath(Path path) {
		return Optional.of(path.toUri().toASCIIString());
	}

	@Override
	protected String positionLink(CompilationUnitDeclaration cu, String fqName) {
		if (cu != null) {
			Region region = findTypeRegion(cu, fqName);
			if (region != null) {
				int line = CuDeclarationUtils.getLineNumber(cu, region.getOffset());
				int column = CuDeclarationUtils.getColumn(cu, region.getOffset());
				StringBuilder sb = new StringBuilder();
				sb.append('#');
				sb.append(line);
				sb.append(',');
				sb.append(column + 1); // 1-based columns?
				return sb.toString();
			}
		}
		return null;
	}

	@Override
	protected Optional<String> jarLinkUrl(IJavaProject project, String fqName, IJavaModuleData jarModuleData) {
		return Optional.ofNullable(JdtJavaDocumentUriProvider.uri(project, fqName)).map(uri -> uri.toASCIIString());
	}

}
