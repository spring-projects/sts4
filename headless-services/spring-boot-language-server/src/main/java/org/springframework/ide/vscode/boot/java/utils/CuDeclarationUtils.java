package org.springframework.ide.vscode.boot.java.utils;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

public class CuDeclarationUtils {
	
	public static int getLineNumber(CompilationUnitDeclaration cu, int offset) {
		int insertionIndex = Arrays.binarySearch(cu.compilationResult.lineSeparatorPositions, offset);
		if (insertionIndex < 0) {
			return -insertionIndex;
		}
		// start lines from 1.
		return insertionIndex + 1;
	}
	
	public static int getColumn(CompilationUnitDeclaration cu, int offset) {
		int line = getLineNumber(cu, offset);
		// line start from 1 hence -1 and -1 for getting end of previous line
		return line == 0 ? offset : offset - cu.compilationResult.lineSeparatorPositions[line - 2] - 1; // -1 at end because offset is for the line separator on the previous line
	}

}
