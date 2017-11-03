/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

public class LiveConditional {

	private String condition;
	private String message;
	private String processId;
	private String processName;
	private String positiveMatchKey;

	public LiveConditional() {

	}

	public String getCondition() {
		return condition;
	}

	public String getMessage() {
		return message;
	}

	public String getProcessId() {
		return processId;
	}

	public String getProcessName() {
		return processName;
	}

	public String getPositiveMatchKey() {
		return positiveMatchKey;
	}

	public static class LiveConditionalBuilder {

		private LiveConditional conditional = new LiveConditional();

		public LiveConditionalBuilder condition(String condition) {
			conditional.condition = condition;
			return this;
		}

		public LiveConditionalBuilder message(String message) {
			conditional.message = message;
			return this;
		}

		public LiveConditionalBuilder processId(String processId) {
			conditional.processId = processId;
			return this;
		}

		public LiveConditionalBuilder processName(String processName) {
			conditional.processName = processName;
			return this;
		}

		/**
		 * This is a JSON key in "positiveMatches" element in the autoconfig report that contains information regarding
		 * the method that the conditional is applied to.
		 * @param positiveMatchKey
		 * @return
		 */
		public LiveConditionalBuilder positiveMatchKey(String positiveMatchKey) {
			conditional.positiveMatchKey = positiveMatchKey;
			return this;
		}

		public LiveConditional build() {
			return conditional;
		}

	}

	public static LiveConditionalBuilder builder() {
		return new LiveConditionalBuilder();
	}
}