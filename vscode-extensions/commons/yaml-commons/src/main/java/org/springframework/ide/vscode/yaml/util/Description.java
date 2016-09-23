package org.springframework.ide.vscode.yaml.util;

public abstract class Description {
	
	public abstract void renderAsText(StringBuilder buf);
	
	public void renderAsHtml(StringBuilder buf) {
		throw new UnsupportedOperationException("Rendering as html not supported");
	}
	
	public static Description text(String text) {
		return new Description() {
			@Override
			public void renderAsText(StringBuilder buf) {
				buf.append(text);
			}
		};
	}

	public static Description italic(Description d) {
		//Not really supported, we just ignore italic and display as is
		return d;
	}

	public String toText() {
		StringBuilder buf = new StringBuilder();
		renderAsText(buf);
		return buf.toString();
	}

}
