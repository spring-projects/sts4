package org.springframework.ide.vscode.commons.javadoc;

import com.overzealous.remark.Remark;

public class HtmlJavadoc implements IJavadoc {
	
	private String html;
	private Remark remark;
	
	public HtmlJavadoc(String html) {
		this.html = html;
		this.remark = new Remark();
	}

	@Override
	public String raw() {
		throw new UnsupportedOperationException("Not yet implemnted");
	}

	@Override
	public String plainText() {
		throw new UnsupportedOperationException("Not yet implemnted");
	}

	@Override
	public String html() {
		return html;
	}

	@Override
	public String markdown() {
		return remark.convertFragment(html);
	}

}
