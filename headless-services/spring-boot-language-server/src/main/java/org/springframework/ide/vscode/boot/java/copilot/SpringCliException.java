package org.springframework.ide.vscode.boot.java.copilot;

@SuppressWarnings("serial")
public class SpringCliException extends RuntimeException {

	/**
	 * Instantiates a new {@code UpException}.
	 * @param message the message
	 */
	public SpringCliException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new {@code UpException}.
	 * @param message the message
	 * @param cause the cause
	 */
	public SpringCliException(String message, Throwable cause) {
		super(message, cause);
	}

}