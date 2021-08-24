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

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.dash.views.sections.TagContentProposalProvider;
import org.springframework.ide.eclipse.boot.dash.views.sections.TagsEditor;
import org.springframework.ide.eclipse.boot.dash.views.sections.UIUtils;

/**
 * Controls for Tags for the properties section
 *
 * @author Alex Boyko
 *
 */
public class TagsPropertyControl extends AbstractBdePropertyControl {

	private TagsEditor tags;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		page.getWidgetFactory().createLabel(composite, "Tags:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		tags = new TagsEditor(composite, SWT.SINGLE | SWT.BORDER, getStylers(), new TagContentProposalProvider(BootDashActivator.getDefault().getModel()), UIUtils.CTRL_SPACE, UIUtils.TAG_CA_AUTO_ACTIVATION_CHARS);
		tags.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		tags.addListener(new ICellEditorListener() {
			@Override
			public void applyEditorValue() {
				String str = (String) tags.getValue();
				BootDashElement element = getBootDashElement();
				if (element != null) {
					LinkedHashSet<String> tagsSet = str.isEmpty() ? new LinkedHashSet<>()
							: new LinkedHashSet<>(Arrays.asList(TagUtils.parseTags(str)));
					if (!tagsSet.equals(element.getTags())) {
						element.setTags(tagsSet);
					}
				}
			}
			@Override
			public void cancelEditor() {
				tags.setValue(getLabels().getStyledText(getBootDashElement(), BootDashColumn.TAGS).getString());
			}
			@Override
			public void editorValueChanged(boolean oldValidState, boolean newValidState) {
			}
		});
	}

	@Override
	public void refreshControl() {
		if (tags != null && !tags.getControl().isDisposed()) {
			tags.setValue(getLabels().getStyledText(getBootDashElement(), BootDashColumn.TAGS).getString());
		}
	}

}
