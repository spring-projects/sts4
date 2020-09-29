/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.Failable;
import org.springframework.ide.eclipse.boot.dash.model.MissingLiveInfoMessages;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

public abstract class LiveDataPropertiesSection<T> extends AbstractBdePropertiesSection {

	protected Failable<T> data = Failable.error(MissingLiveInfoMessages.NOT_YET_COMPUTED);

	protected TabbedPropertySheetPage page;
	private Composite composite;
	private Composite missingInfo;
	private Browser browser;
	private StackLayout layout;

	private Control dataControl;

	private static final LocationListener HYPER_LINK_LISTENER = new LocationListener() {

		@Override
		public void changing(LocationEvent event) {
			if (!"about:blank".equals(event.location)) { //$NON-NLS-1$
				UiUtil.openUrl(event.location);
				event.doit = false;
			}
		}

		@Override
		public void changed(LocationEvent event) {
			// comment requested by sonar
		}
	};

	final public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		this.page = aTabbedPropertySheetPage;
		composite = getWidgetFactory().createComposite(parent, SWT.NONE);

		// Layout variant to have owner composite size to be equal the client area size of the next upper level ScrolledComposite
		composite.setLayout(layout = new SectionStackLayout());

		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;

		missingInfo = getWidgetFactory().createComposite(composite, SWT.NONE);
		missingInfo.setLayout(new FillLayout());

		browser = new Browser(missingInfo, SWT.NONE);
		browser.addLocationListener(HYPER_LINK_LISTENER);

		this.dataControl = createSectionDataControls(composite);
	}

	@Override
	public void dispose() {
		try {
			if (browser != null && !browser.isDisposed()) {
				browser.removeLocationListener(HYPER_LINK_LISTENER);
				browser = null;
			}
		} catch (SWTException e) {
			//See: https://www.pivotaltracker.com/story/show/174560730
			//Despite the 'isDisposed' check we still sometimes get a 'widget is disposed' exception.
			//Not sure why... but it should be harmless to ignore this.
		}
		super.dispose();
	}

	public void refresh() {
		this.data = fetchData();

		if (data.hasFailed()) {
			layout.topControl = this.missingInfo;
			String newText = this.data.getErrorMessage().toHtml();
			if (browser != null && !browser.isDisposed() && !newText.equals(browser.getText())) {
				browser.setText(newText);
			}
		} else {
			layout.topControl = this.dataControl;
		}

		refreshDataControls();

		SectionStackLayout.reflow(page);
	}

	protected abstract Control createSectionDataControls(Composite composite);

	protected abstract void refreshDataControls();

	protected abstract Failable<T> fetchData();

}
