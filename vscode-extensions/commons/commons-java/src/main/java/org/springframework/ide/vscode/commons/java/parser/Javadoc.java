package org.springframework.ide.vscode.commons.java.parser;

import org.springframework.ide.vscode.commons.java.IJavadoc;

import com.github.javaparser.ast.comments.JavadocComment;

final class Javadoc implements IJavadoc {
	
	private JavadocComment javadocComment;
	
	Javadoc(JavadocComment javadocComment) {
		this.javadocComment = javadocComment;
	}

	@Override
	public String raw() {
		return javadocComment.getContent();
	}

	@Override
	public String plainText() {
		throw new UnsupportedOperationException("Not yet implemnted");
	}

	@Override
	public String html() {
		throw new UnsupportedOperationException("Not yet implemnted");
	}

	@Override
	public String markdown() {
		throw new UnsupportedOperationException("Not yet implemnted");
	}

}
