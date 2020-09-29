/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

/**
 * Controls for Run State for the properties section
 *
 * @author Alex Boyko
 *
 */
public class RunStatePropertyControl extends AbstractBdePropertyControl {

	private static final long ANIMATION_FRAME = 100;

	private CLabel runState;
	private ImageAnimation animation = null;
	private RunState previousRunState = null;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		page.getWidgetFactory().createLabel(composite, "State:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		runState = page.getWidgetFactory().createCLabel(composite, ""); //$NON-NLS-1$
		runState.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		runState.setBottomMargin(0);
		runState.setLeftMargin(0);
		runState.setRightMargin(0);
		runState.setTopMargin(0);
	}

	@Override
	public void refreshControl() {
		if (runState != null && !runState.isDisposed()) {
			BootDashElement bde = getBootDashElement();
			runState.setText(getLabels().getStyledText(bde, BootDashColumn.RUN_STATE_ICN).getString());

			if (bde == null) {
				runState.setImage(null);
			} else {
				if (previousRunState != bde.getRunState()) {
					previousRunState = bde.getRunState();
					final Image[] images = getLabels().getImageAnimation(bde, BootDashColumn.RUN_STATE_ICN);
					if (animation != null) {
						animation.stop();
						animation = null;
					}
					if (images == null || images.length == 0) {
						runState.setImage(null);
					} else if (images.length == 1) {
						runState.setImage(images[0]);
					} else {
						animation = new ImageAnimation(images, ANIMATION_FRAME) {
							@Override
							protected void setFrame(Image image) {
								runState.setImage(image);
							}
						};
						animation.start();
					}
				}
			}
		}
	}

	@Override
	public void dispose() {
		if (animation != null) {
			animation.stop();
		}
		super.dispose();
	}

}
