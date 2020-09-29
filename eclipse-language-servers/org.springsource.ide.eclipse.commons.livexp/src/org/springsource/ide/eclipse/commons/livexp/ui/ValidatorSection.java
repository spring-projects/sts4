package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

/**
 * A page section that has no UI widgetry but conains just
 * some validation logic.
 * 
 * @author Kris De Volder
 */
public class ValidatorSection extends WizardPageSection {

	public ValidatorSection(LiveExpression<ValidationResult> validator, IPageWithSections owner) {
		super(owner);
		this.validator = validator;
	}

	private LiveExpression<ValidationResult> validator;

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return this.validator;
	}

	@Override
	public void createContents(Composite page) {
		//no widgets so nothing to do
	}

}
