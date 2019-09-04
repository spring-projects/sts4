package org.springframework.tooling.eclipse.si.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class LiveGraphView extends ViewPart {
	
	private Browser browser;

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setUrl("http://localhost:8877");
	}

	@Override
	public void setFocus() {
		if (!browser.isFocusControl()) {
			browser.setFocus();
		}
	}

}
