package org.springframework.ide.vscode.commons.java;

public interface IVoidType extends IJavaType {
	
	static IVoidType DEFAULT = new IVoidType() {

		@Override
		public String name() {
			return "V";
		}
		
	};

}
