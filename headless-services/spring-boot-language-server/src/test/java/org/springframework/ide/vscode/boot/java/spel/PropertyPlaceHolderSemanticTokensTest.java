package org.springframework.ide.vscode.boot.java.spel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;

public class PropertyPlaceHolderSemanticTokensTest {

	private PropertyPlaceHolderSemanticTokens provider = new PropertyPlaceHolderSemanticTokens(Optional.of(Assertions::fail));
	
	@Test
	void propertyWithDefault() {
		List<SemanticTokenData> tokens = provider.computeTokens("server.port:5673");
		assertThat(tokens.size()).isEqualTo(3);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 11, "property", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(11, 12, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(12, 16, "string", new String[0]));
	}
	
	@Test
	void propertyOnly() {
		List<SemanticTokenData> tokens = provider.computeTokens("server.port");
		assertThat(tokens.size()).isEqualTo(1);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 11, "property", new String[0]));
	}

	@Test
	void propertyWithEmptyDefaulValue() {
		List<SemanticTokenData> tokens = provider.computeTokens("server.port:");
		assertThat(tokens.size()).isEqualTo(3);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 11, "property", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(11, 12, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(12, 12, "string", new String[0]));
	}

	@Test
	void error_1() {
		provider = new PropertyPlaceHolderSemanticTokens(Optional.empty());
		List<SemanticTokenData> tokens = provider.computeTokens("server.:");
		assertThat(tokens.size()).isEqualTo(3);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "property", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(6, 7, "property", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
	}

	@Test
	void error_2() {
		provider = new PropertyPlaceHolderSemanticTokens(Optional.empty());
		List<SemanticTokenData> tokens = provider.computeTokens("server.");
		assertThat(tokens.size()).isEqualTo(2);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "property", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(6, 7, "property", new String[0]));
	}
}
