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
package org.springframework.ide.eclipse.boot.dash.cf.debug;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.launch.ExistingBootProjectSelectionValidator;
import org.springframework.ide.eclipse.boot.launch.SelectProjectLaunchTabModel;
import org.springframework.ide.eclipse.boot.launch.devtools.DevtoolsEnabledValidator;
import org.springframework.ide.eclipse.boot.launch.devtools.StringFieldLaunchTabModel;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

/**
 * Model for the 'main type' selection widgetry on a launchconfiguration tab.
 * <p>
 * Contains the 'logic' for the UI except for the widgets themselves.
 * Can be unit tested without having to instantiate launch configuration dialogs
 * etc.
 *
 * @author Kris De Volder
 */
public class SshDebugLaunchUIModel {

	public final SelectProjectLaunchTabModel project;
	public final SelectRunTargetLaunchTabModel cfTarget;
	public final StringFieldLaunchTabModel appName;
	private BootDashViewModel context;

	public SshDebugLaunchUIModel(BootDashViewModel context) {
		this.context = context;
		project = createProjectSelectionModel();
		cfTarget = createCfTargetModel(context);
		appName = createAppNameModel();
	}

	private SelectRunTargetLaunchTabModel createCfTargetModel(BootDashViewModel context) {
		LiveVariable<CloudFoundryRunTarget> variable = new LiveVariable<>();
		LiveExpression<ValidationResult> validator = Validator.notNull(variable, "No Target selected");
		return new SelectRunTargetLaunchTabModel(variable, validator, context);
	}

	private StringFieldLaunchTabModel createAppNameModel() {
		StringFieldModel field = new StringFieldModel("App Name", "");
		field.validator(appNameValidator(project.selection, cfTarget.selection, field.getVariable()));
		return new StringFieldLaunchTabModel(field, SshDebugLaunchConfigurationDelegate.APP_NAME);
	}


	private LiveExpression<ValidationResult> appNameValidator(
			final LiveExpression<IProject> projectSelection,
			final LiveExpression<CloudFoundryRunTarget> targetSelection,
			final LiveExpression<String> appNameSelection
	) {
		return new Validator() {
			{
				dependsOn(projectSelection);
				dependsOn(appNameSelection);
				dependsOn(targetSelection);
			}
			protected ValidationResult compute() {
				CloudFoundryRunTarget target = targetSelection.getValue();
				if (target!=null) {
					String name = appNameSelection.getValue();
					if (!StringUtil.hasText(name)) {
						return ValidationResult.error("App Name not set");
					}
					BootDashModel section = context.getSectionByTargetId(target.getId());
					if (section instanceof CloudFoundryBootDashModel) {
						CloudFoundryBootDashModel cfSection = (CloudFoundryBootDashModel) section;
						CloudAppDashElement app = cfSection.getApplication(name);
						if (app==null) {
							return ValidationResult.error("There is no app '"+name+"' in '"+target+"'");
						}
						IProject project = projectSelection.getValue();
						IProject appProject = app.getProject();
						if (appProject==null) {
							//  we don't know what project goes with selected app so give the user
							// the benefit of the doubdt
							// => no error!
						} else if (project==null) {
							// not this validator's problem. (project validator should check this)
							// => no error
						} else {
							if (!project.equals(appProject)) {
								return ValidationResult.error("The app '"+name+"' is bound to a different project ("+appProject.getName()+")");
							}
						}
					}
				}
				return ValidationResult.OK;
			}
		};
	}

	private SelectProjectLaunchTabModel createProjectSelectionModel() {
		LiveVariable<IProject> project = new LiveVariable<>();
		CompositeValidator validator = new CompositeValidator();
		validator.addChild(new ExistingBootProjectSelectionValidator(project));
		validator.addChild(new DevtoolsEnabledValidator(project));
		return new SelectProjectLaunchTabModel(project, validator);
	}


}
