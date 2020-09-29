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
package org.springframework.ide.eclipse.editor.support.util;

/**
 * This class was moved to commons.core for more wide reuse. This old copy
 * is deprecated.
 *
 * @author Kris De Volder
 */
@Deprecated
public class FuzzyMatcher {

	/**
	 * Match given pattern with a given data. The data is considered a 'match' for the
	 * pattern if all characters in the pattern can be found in the data, in the
	 * same order but with possible 'gaps' in between.
	 * <p>
	 * The function returns 0. when the pattern doesn't match the data and a non-zero
	 * 'score' when it does. The higher the score, the better the match is considered to
	 * be.
	 */
	public static double matchScore(String pattern, String data) {
		return org.springsource.ide.eclipse.commons.core.util.FuzzyMatcher.matchScore(pattern, data);
	}
}
