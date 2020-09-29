/*******************************************************************************
 * Copyright (c) 2016 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.dialogs.EditTemplateDialogModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;

/**
 * Model of the dialog opened by {@link CustmomizeTargetLabelAction}. Has all the functionality
 * of the dialog except for the widgets themselves.
 *
 * @author Kris De Volder
 */
public final class CustomizeTargetLabelDialogModel {

	//Note: there is no representation in this model of the 'cancel' button that exists in the
	// real dialog. When cancel is clicked basically... nothing happens and the dialog just closes,
	// there is no call to the model and this is fine as there is literally nothing for the model to
	// do.

	//Why not a 'real' class rather than this method that creates an anonymous class?
	// Because the real class has troubles initializing itself because of the call to 'getDefaultValue'
	// in the super constructor (it is called before the 'type' field is initialized!
	//This static method is almost functionally equivalent but doesn't have that problem because
	// the local variables exist before the class instance is constructed.
	public static EditTemplateDialogModel create(final BootDashModel section ) {
		final RunTargetType type = section.getRunTarget().getType();
		return new EditTemplateDialogModel() {

			{
				template.setValue(section.getNameTemplate());
			}

			@Override
			public String getTitle() {
				String type = section.getRunTarget().getType().getName();
				return "Customize Labels for "+type+" Target(s)";
			}

			@Override
			public void performOk() throws Exception {
				if (applyToAll.getValue()) {
					section.getRunTarget().getType().setNameTemplate(template.getValue());
					//To *really* apply the template to *all* targets of a given type, we must make sure
					// that the targets do not override the value individually:
					for (BootDashModel model : section.getViewModel().getSectionModels().getValue()) {
						if (model.getRunTarget().getType().equals(type)) {
							model.setNameTemplate(null);
							model.notifyModelStateChanged();
						}
					}
				} else {
					section.setNameTemplate(template.getValue());
					section.notifyModelStateChanged();
				}
			}

			@Override
			public String getHelpText() {
				return type.getTemplateHelpText();
			}

			@Override
			public String getDefaultValue() {
				return type.getDefaultNameTemplate();
			}

			@Override
			public String getApplyToAllLabel() {
				return "Apply to all "+type.getName()+" targets";
			}

			@Override
			public boolean getApplyToAllDefault() {
				//'apply to all' is enabled by default, unless there is at least one applicable model which already
				// has an individually customized label.
				for (BootDashModel section : section.getViewModel().getSectionModels().getValue()) {
					if (
							section.getRunTarget().getType().equals(type) &&
							section.hasCustomNameTemplate()
					) {
						return false;
					}
				}
				return true;
			}
		};
	}


}