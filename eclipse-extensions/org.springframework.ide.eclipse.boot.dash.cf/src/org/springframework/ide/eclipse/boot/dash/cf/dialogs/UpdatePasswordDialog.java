/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *
 *     Original from org.cloudfoundry.ide.eclipse.server.ui.internal
 *     and implemented under:
 *
 * Copyright (c) 2012, 2014 Pivotal Software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.dialogs;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials.LoginMethod;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.ButtonSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

import reactor.core.scheduler.Schedulers;

/**
 * Dialog for setting the password and "store password" flag.
 *
 * @author Terry Denney
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class UpdatePasswordDialog extends DialogWithSections {

	private static final StoreCredentialsMode[] storeCredentialChoices = EnumSet.allOf(StoreCredentialsMode.class)
			.toArray(new StoreCredentialsMode[0]);

	private PasswordDialogModel model;

	public UpdatePasswordDialog(Shell parentShell, PasswordDialogModel model) {
		super("Update Credentials", model, parentShell);
		this.model = model;
		disableOkButtonAt(Status.INFO);
	}

	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		List<WizardPageSection> sections = new ArrayList<>();

		sections.add(new CommentSection(this,
				"The password/code must match your existing target credentials in " + model.getTargetId() + "."));

		sections.add(new ChooseOneSectionCombo<>(this, "Method:", model.getMethodVar(), EnumSet.allOf(LoginMethod.class)));
		sections.add(new StringFieldSection(this, "Password:", model.getPasswordVar(), model.getPasswordValidator()).setPassword(true));
		sections.add(storeCredentialsSection(this, model.getStoreVar(), model.getStoreValidator()));
		sections.add(new ButtonSection(this, "Validate", () -> {
			model.validateCredentials().subscribeOn(Schedulers.elastic()).subscribe();
		}));

		return sections;
	}

	public static ChooseOneSectionCombo<StoreCredentialsMode> storeCredentialsSection(IPageWithSections owner,
			LiveVariable<StoreCredentialsMode> storeCreds,
			LiveExpression<ValidationResult> validator
	) {
		return new ChooseOneSectionCombo<>(owner, "Remember:", new SelectionModel<>(storeCreds, validator), storeCredentialChoices);
	}

}
