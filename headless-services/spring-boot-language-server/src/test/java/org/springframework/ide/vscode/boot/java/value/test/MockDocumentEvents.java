package org.springframework.ide.vscode.boot.java.value.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.ide.vscode.commons.languageserver.util.DocumentEventListenerManager;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocumentSaveChange;

public class MockDocumentEvents implements DocumentEventListenerManager {

	List<Consumer<TextDocumentSaveChange>> saveHandlers = new ArrayList<Consumer<TextDocumentSaveChange>>();

	@Override
	public void onDidSave(Consumer<TextDocumentSaveChange> h) {
		saveHandlers.add(h);
	}

	public void fire(TextDocumentSaveChange e) {
		saveHandlers.forEach(h -> {
			h.accept(e);
		});
	}

}
