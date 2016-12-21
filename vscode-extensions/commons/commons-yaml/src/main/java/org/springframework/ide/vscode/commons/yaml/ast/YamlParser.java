package org.springframework.ide.vscode.commons.yaml.ast;

import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.yaml.snakeyaml.Yaml;

import javolution.io.CharSequenceReader;

public class YamlParser implements YamlASTProvider {

	private Yaml yaml;

	public YamlParser(Yaml yaml) {
		this.yaml = yaml;
	}

	@Override
	public YamlFileAST getAST(IDocument doc) throws Exception {
		CharSequenceReader reader = new CharSequenceReader();
		reader.setInput(doc.get());
		return new YamlFileAST(doc, yaml.composeAll(reader));
	}

}
