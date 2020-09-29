/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.core.resources.IProject;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElementsFilterBoxModel;
import org.springframework.ide.eclipse.boot.dash.model.LocalBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;
import org.springframework.ide.eclipse.boot.dash.test.AbstractLaunchConfigurationsDashElementTest.TestElement;
import org.springframework.ide.eclipse.boot.dash.test.mocks.Mocks;
import org.springframework.ide.eclipse.boot.dash.util.LaunchConfRunStateTracker;

/**
 * Light-weight mockito-based tests for tags.
 *
 * @author Alex Boyko
 *
 */
public class BootDashElementTagsTests extends Mocks {

	private static TestElement createElement(String name, String... tags) {
//		LaunchConfDashElementFactory childFactory = mock(LaunchConfDashElementFactory.class);
//		BootProjectDashElementFactory factory = mock(BootProjectDashElementFactory.class);
//		IScopedPropertyStore<IProject> projectProperties = new MockScopedPropertyStore<IProject>();
		IProject project = mockProject(name, true);
		LocalBootDashModel model = mock(LocalBootDashModel.class);
		LaunchConfRunStateTracker tracker = mock(LaunchConfRunStateTracker.class);
		when(model.getLaunchConfRunStateTracker()).thenReturn(tracker);
		TestElement element = spy(new TestElement(name, project, model));
		when(element.getTags()).thenReturn(new LinkedHashSet<String>(Arrays.asList(tags)));
		return element;
	}

	@Test
	public void testTagParsing_1() throws Exception {
		assertArrayEquals(new String[] {"xd", "spring"}, TagUtils.parseTags("xd,spring"));
	}

	@Test
	public void testTagParsing_2() throws Exception {
		assertArrayEquals(new String[0], TagUtils.parseTags(""));
	}

	@Test
	public void testTagParsing_3() throws Exception {
		assertArrayEquals(new String[0], TagUtils.parseTags("  , ,,  , \t, ,    ,,,\n\t, "));
	}

	@Test
	public void testTagParsing_4() throws Exception {
		assertArrayEquals(new String[] {"spring"}, TagUtils.parseTags("  spring  \t  "));
	}

	@Test
	public void testTagParsing_5() throws Exception {
		assertArrayEquals(new String[] {"spring", "spring"}, TagUtils.parseTags("spring  ,\t  spring ,"));
	}

	@Test
	public void testTagParsing_6() throws Exception {
		assertArrayEquals(new String[] {"spring"}, TagUtils.parseTags(", ,,, spring"));
	}

	@Test
	public void defaultValues_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();

		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring", "xd"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring source", "xd"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void empty_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("");

		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring source"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void basicSearchTerm_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("spring");

		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring source"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void basicSearchTermWithWhiteSpace_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("  \t  , ,,, spring \t  \n  ");

		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring source"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void basicSearchTag_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("spring,");

		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void basicSearchTagWithWhiteSpace_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("  \t  , ,,, spring \t ,  \n  ");

		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void multipleSearchTags_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("spring,xd,");

		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void mixedCaseSearchTags_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("sPriNg,xD,");

		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"Xd", "sprinG"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spRing source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xD", "spring, source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void multipleSearchTagsWithWhiteSpace_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("  \t  , ,,, spring \n\t, ,   ,,,, ,  \t \n, xd \n \t ,   \t\n, ,,,,    ,  ,");

		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void combineSearchTagsAndTerm_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("xd,spring");

		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring", "xd"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring source", "xd"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void combineSearchTagsAndTermWithWhiteSpace_FilterTest() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("  \t  , ,,, xd \n\t, ,   ,,,, ,  \t \n, spring");

		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring", "xd"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "springsource"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"spring source", "xd"})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "spring, source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {"xd", "source"})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("t1", new String[] {})));
	}

	@Test
	public void filterMatchesElementName() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("foo");

		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("demo-foo", new String[] {})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("demo-foo-bar", new String[] {})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("foo", new String[] {})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("demo", new String[] {})));
	}

	@Test
	public void starWildcardInSearchTerm() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("foo*bar");

		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("something-foo-more-bar", new String[] {})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("demo-bar", new String[] {})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("foobar", new String[] {})));
	}

	@Test
	public void questionmarkWildcardInSearchTerm() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("foo?bar");

		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("something-foo-bar", new String[] {})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("something-foo-more-bar", new String[] {})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("foobar", new String[] {})));
	}

	@Test
	public void wildcardInTag() throws Exception {
		BootDashElementsFilterBoxModel filterBoxModel = new BootDashElementsFilterBoxModel();
		filterBoxModel.getText().setValue("foo*bar,");

		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("something-foo-bar", new String[] {})));
		assertFalse(filterBoxModel.getFilter().getValue().accept(createElement("something-foo-more-bar", new String[] {})));
		assertTrue(filterBoxModel.getFilter().getValue().accept(createElement("foobar", new String[] {})));
	}

}
