package org.springframework.ide.vscode.boot.java.copilot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassNameExtractor {

	private String[] patternStrings = { "(?<=\\bclass\\s)\\w+", "(?<=\\binterface\\s)\\w+", "(?<=\\b@interface\\s)\\w+",
			"(?<=\\benum\\s)\\w+" };

	private List<Pattern> patterns = new ArrayList<>();

	public ClassNameExtractor() {
		for (String patternString : patternStrings) {
			patterns.add(Pattern.compile(patternString));
		}
	}

	public Optional<String> extractClassName(String code) {
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(code);
			if (matcher.find()) {
				return Optional.of(matcher.group());
			}
		}
		return Optional.empty();
	}

}