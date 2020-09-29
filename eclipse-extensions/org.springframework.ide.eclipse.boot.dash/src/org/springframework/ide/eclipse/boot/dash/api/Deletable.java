package org.springframework.ide.eclipse.boot.dash.api;

public interface Deletable {

	default boolean canDelete() { return true; }
	void delete() throws Exception;

}
