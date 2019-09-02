/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

public class LiveConditional {

	private String condition;
	private String message;
	private String processId;
	private String processName;
	private String typeInfo;

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

	public String getTypeInfo() {
		return typeInfo;
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
		 * Type information for which a conditional is applied to.
		 * <p/>
		 *
		 * Example:
		 * <p/>
		 * For this class:
		 * <p/>
         * "@ConditionalOnClass(name="java.lang.String2")
         * public class MyConditionalComponent {
         * }"
         * <p/>
         * This is the "real" autoconfig JSON:
         * <p/>
		 * "negativeMatches": { "MyConditionalComponent": { "notMatched": [ {
		 * "condition": "OnClassCondition", "message": "@ConditionalOnClass did not find
		 * required class 'java.lang.String2'" } ], "matched": [] }
		 * <p/>
		 * In this example, "MyConditionalComponent" information in the JSON indicates the type where the conditional is being applied to.
		 * <p/>
		 * Type info can also contain method information if a conditional annotation is applied to a method. Example: MyConditionalComponent#myBean)
		 * @param typeInfo
		 * @return
		 */
		public LiveConditionalBuilder typeInfo(String typeInfo) {
			conditional.typeInfo = typeInfo;
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