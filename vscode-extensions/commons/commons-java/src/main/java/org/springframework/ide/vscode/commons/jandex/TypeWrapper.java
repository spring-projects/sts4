package org.springframework.ide.vscode.commons.jandex;

class TypeWrapper<T> {
	
	private T type;
	
	TypeWrapper(T type) {
		this.type = type;
	}
	
	T getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeWrapper) {
			return type.equals(((TypeWrapper<T>)obj).type);
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return type.toString();
	}

}
