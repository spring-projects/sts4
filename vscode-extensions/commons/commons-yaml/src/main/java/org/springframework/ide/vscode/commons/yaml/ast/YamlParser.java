package org.springframework.ide.vscode.commons.yaml.ast;

import java.io.StringReader;

import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.yaml.snakeyaml.Yaml;

public class YamlParser implements YamlASTProvider {

	private Yaml yaml;

	public YamlParser(Yaml yaml) {
		this.yaml = yaml;
	}

	@Override
	public YamlFileAST getAST(IDocument doc) throws Exception {
		return new YamlFileAST(yaml.composeAll(new StringReader(doc.get())));
	}

}
