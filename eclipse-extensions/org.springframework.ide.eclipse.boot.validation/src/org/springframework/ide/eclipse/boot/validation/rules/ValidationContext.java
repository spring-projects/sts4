package org.springframework.ide.eclipse.boot.validation.rules;

import org.springframework.ide.eclipse.boot.validation.framework.CompilationUnitElement;
import org.springframework.ide.eclipse.boot.validation.framework.IModelElement;

public interface ValidationContext {

	void problem(IModelElement element, String problemId, String msg, int offset, int len);

}
