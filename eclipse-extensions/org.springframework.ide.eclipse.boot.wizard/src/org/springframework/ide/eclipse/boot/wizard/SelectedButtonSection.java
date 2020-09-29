/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.function.Supplier;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.ui.HtmlTooltip;

public class SelectedButtonSection<T> extends WizardPageSection {


	private Composite buttonComp;
	private ValueListener<Boolean> selectionListener;


	protected final CheckBoxModel<T> model;
	protected final LiveVariable<Boolean> isVisible = new LiveVariable<>(true);

	public SelectedButtonSection(IPageWithSections owner, CheckBoxModel<T> model) {
		super(owner);
		this.model = model;
	}

	@Override
	public void createContents(Composite page) {
		if (page != null && !page.isDisposed()) {
			buttonComp = new Composite(page, SWT.NONE);

			GridDataFactory.fillDefaults().grab(false, false).applyTo(buttonComp);

			createButtonArea();
			isVisible.addListener(new ValueListener<Boolean>() {
				public void gotValue(LiveExpression<Boolean> exp, Boolean reveal) {
					if (reveal != null && buttonComp != null && !buttonComp.isDisposed()) {
						buttonComp.setVisible(reveal);
						GridData data = (GridData) buttonComp.getLayoutData();
						data.exclude = !reveal;
					}
				}
			});
		}
	}

	private void createButtonArea() {
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).spacing(10, 0).applyTo(buttonComp);
		Label xButton = new Label(buttonComp, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(xButton);
		xButton.setText("X");
		xButton.setFont(getBold());
		applyDefaultEffects(xButton);

		xButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent event) {
				// Do nothing
			}

			@Override
			public void mouseDown(MouseEvent event) {
				removeSelection();
			}

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				removeSelection();
			}

		});

		xButton.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent event) {
				// Do nothing. Enter/exit events are faster than hover, so those will
				// be fired first before hover
			}

			@Override
			public void mouseExit(MouseEvent event) {
				applyDefaultEffects(xButton);
			}

			@Override
			public void mouseEnter(MouseEvent event) {
				applyHoverEffects(xButton);
			}
		});

		xButton.setToolTipText("Click to remove the dependency");

		Label label = new Label(buttonComp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER)
				.applyTo(label);

		label.setText(model.getLabel());
		Supplier<String> tooltip = model.getTooltipHtml();
		if (tooltip != null) {
			// Setup HTML tooltip and its content
			HtmlTooltip htmlTooltip = new HtmlTooltip(label);
			htmlTooltip.setMaxSize(400, 400);
			htmlTooltip.setHtml(tooltip);
		}
	}

	private Font getBold() {
		// Note: fonts from the registry do not need to be managed outside for disposal.
		return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
	}

	protected void applyHoverEffects(Label xButton) {
		if (xButton != null && !xButton.isDisposed()) {
			xButton.setForeground(getSystemColor(SWT.COLOR_RED));
		}
	}

	protected void applyDefaultEffects(Label xButton) {
		if (xButton != null && !xButton.isDisposed()) {
			xButton.setForeground(getSystemColor(SWT.COLOR_DARK_GRAY));
		}
	}

	protected Color getSystemColor(int colorCode) {
		return Display.getDefault().getSystemColor(colorCode);
	}

	protected void removeSelection() {
		model.getSelection().setValue(false);
	}

	@Override
	public void dispose() {
		if (buttonComp != null && !buttonComp.isDisposed()) {
			buttonComp.dispose();
			buttonComp = null;
		}
		if (selectionListener != null) {
			model.getSelection().removeListener(selectionListener);
		}
	}

	/**
	 * Apply filter and return whether this widget's visibility has changed as a
	 * result.
	 *
	 * @return Whether visibility of this widget changed.
	 */
	public boolean applyFilter(Filter<T> filter) {
		boolean wasVisible = isVisible.getValue();
		isVisible.setValue(filter.accept(model.getValue()));
		boolean changed = wasVisible != isVisible.getValue();
		return changed;
	}

	public boolean isVisible() {
		return isVisible.getValue();
	}

	@Override
	public String toString() {
		return "SelectedButtonSection(" + model.getLabel() + ")";
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return Validator.OK;
	}
}