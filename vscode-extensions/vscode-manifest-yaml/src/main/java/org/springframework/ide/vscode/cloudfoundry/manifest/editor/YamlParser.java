package org.springframework.ide.vscode.cloudfoundry.manifest.editor;

import java.io.StringReader;

import org.springframework.ide.vscode.util.IDocument;
import org.springframework.ide.vscode.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.yaml.ast.YamlFileAST;
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
