package org.springframework.ide.eclipse.boot.validation.framework;

import org.eclipse.core.resources.IMarker;

/**
 * Markers related with Spring validation problems.
 * <p>
 * This interface declares constants only; it is not intended to be implemented
 * or extended.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IValidationProblemMarker {

	int SEVERITY_WARNING = IMarker.SEVERITY_WARNING;

	int SEVERITY_ERROR = IMarker.SEVERITY_ERROR;
	
	/**
	 * @since 2.0.2
	 */
	int SEVERITY_INFO = IMarker.SEVERITY_INFO;
	
	/**
	 * @since 2.3.1
	 */
	int SEVERITY_UNKOWN = -1;

	/**
	 * Rule ID marker attribute (value <code>"ruleId"</code>).
	 */
	String RULE_ID = "ruleId";

	/**
	 * Error ID marker attribute (value <code>"errorId"</code>).
	 */
	String ERROR_ID = "errorId";
}
