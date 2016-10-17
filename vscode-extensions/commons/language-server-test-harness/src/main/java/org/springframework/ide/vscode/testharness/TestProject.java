package org.springframework.ide.vscode.testharness;

import java.nio.file.Path;

public interface TestProject {

	Path getPath();

	IType findType(String string);

}
