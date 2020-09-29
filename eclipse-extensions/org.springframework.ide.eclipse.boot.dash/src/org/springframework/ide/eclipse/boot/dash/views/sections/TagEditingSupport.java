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
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;
import org.springframework.ide.eclipse.boot.dash.model.Taggable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;

/**
 * Support for editing tags with the text cell editor
 *
 * @author Alex Boyko
 *
 */
public class TagEditingSupport extends EditingSupport {

	private StyledTextCellEditor editor;

	public TagEditingSupport(TableViewer viewer, LiveExpression<BootDashElement> selection, Stylers stylers) {
		super(viewer);
		this.editor = new TagsCellEditor(viewer.getTable(), stylers);
	}

	public TagEditingSupport(TableViewer viewer, LiveExpression<BootDashElement> selection, BootDashViewModel model, Stylers stylers) {
		super(viewer);
		IContentProposalProvider proposalProvider = new TagContentProposalProvider(model);
		this.editor = new TagsCellEditor(viewer.getTable(), stylers, proposalProvider, UIUtils.CTRL_SPACE,
				UIUtils.TAG_CA_AUTO_ACTIVATION_CHARS);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return element instanceof Taggable;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof Taggable) {
			return TagUtils.toString(((Taggable)element).getTags());
		} else {
			return null;
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof Taggable && value instanceof String) {
			String str = (String) value;
			Taggable taggable = (Taggable) element;
			if (str.isEmpty()) {
				taggable.setTags(null);
			} else {
				taggable.setTags(new LinkedHashSet<>(Arrays.asList(TagUtils.parseTags(str))));
			}
		}
	}

}
