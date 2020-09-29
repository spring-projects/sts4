package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteAppElement;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;

import com.google.common.collect.ImmutableSet;

public class DebugPortPropertyControl extends AbstractBdePropertyControl {

	private Label ports;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		page.getWidgetFactory().createLabel(composite, "Debug Port:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		ports = page.getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		ports.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
	}

	@Override
	public void refreshControl() {
		if (ports != null && !ports.isDisposed()) {
			BootDashElement element = getBootDashElement();
			String portStr = "";
			if (element instanceof GenericRemoteAppElement) {
				GenericRemoteAppElement app = (GenericRemoteAppElement) element;
				ImmutableSet<Integer> ports = app.getDebugPortSummary();
				portStr = StringUtil.collectionToCommaDelimitedString(ports);
			}
			ports.setText(portStr);
		}
	}
}
