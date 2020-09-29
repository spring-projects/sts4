/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.tests.util.swtbot;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;

import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.finders.PathGenerator;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.utils.TreePath;
import org.eclipse.ui.forms.widgets.Section;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * @author Leo Dos Santos
 */
public class InSection<T extends Widget> extends AbstractMatcher<T> {

	@Factory
	public static <T extends Widget> Matcher<T> inSection(Matcher<?> matcher) {
		return new InSection<T>(matcher);
	}

	@Factory
	public static <T extends Widget> Matcher<T> inSection(String labelText) {
		return new InSection<T>(labelText);
	}

	private final Matcher<?> matcher;

	InSection(Matcher<?> matcher) {
		this.matcher = matcher;
	}

	InSection(String labelText) {
		matcher = withMnemonic(labelText);
	}

	public void describeTo(Description description) {
		description.appendText("in section (").appendDescriptionOf(matcher).appendText(")"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected boolean doMatch(Object item) {
		Widget previousWidget = SWTUtils.previousWidget((Widget) item);
		TreePath path = new PathGenerator().getPath((Widget) item);
		int segmentCount = path.getSegmentCount();
		for (int i = 1; i < segmentCount; i++) {
			previousWidget = (Widget) path.getSegment(segmentCount - i - 1);
			if ((previousWidget instanceof Section) && matcher.matches(previousWidget)) {
				return true;
			}
		}
		return false;
	}

}
