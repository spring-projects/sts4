/*******************************************************************************
 * Copyright (c) 2012, 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion;

/**
 * Set of constants to be used to
 * convert legacy (pre-3.0) STS projects to 3.0 projects.
 * @author Andrew Eisenberg
 * @since 3.0.0
 */
public interface IConversionConstants {
    String AUTO_CHECK_FOR_LEGACY_STS_PROJECTS = "org.springsource.ide.eclipse.commons.frameworks.ui.legacyconversion.autocheck"; //$NON-NLS-1$
    String LEGACY_MIGRATION_ALREADY_DONE = "org.springsource.ide.eclipse.commons.frameworks.ui.legacyconversion.done"; //$NON-NLS-1$
    // comma separated list of plugins that already have run plugin-specific legacy conversion code
    String LEGACY_MIGRATION_PLUGINS = "org.springsource.ide.eclipse.commons.frameworks.ui.legacyconversion.plugins"; //$NON-NLS-1$
    
    // GRAILS
    String GRAILS_OLD_PREFERENCE_PREFIX =  "com.springsource.sts.grails"; //$NON-NLS-1$
    String GRAILS_NEW_PREFERENCE_PREFIX = "org.grails.ide.eclipse"; //$NON-NLS-1$
    
    String GRAILS_OLD_PLUGIN_NAME = GRAILS_OLD_PREFERENCE_PREFIX + ".core"; //$NON-NLS-1$
    String GRAILS_NEW_PLUGIN_NAME = GRAILS_NEW_PREFERENCE_PREFIX + ".core"; //$NON-NLS-1$
    
    String GRAILS_OLD_NATURE = GRAILS_OLD_PLUGIN_NAME + ".nature"; //$NON-NLS-1$
    String GRAILS_NEW_NATURE = GRAILS_NEW_PLUGIN_NAME + ".nature"; //$NON-NLS-1$

    String GRAILS_OLD_CONTAINER = GRAILS_OLD_PLUGIN_NAME + ".CLASSPATH_CONTAINER"; //$NON-NLS-1$
    String GRAILS_NEW_CONTAINER = GRAILS_NEW_PLUGIN_NAME + ".CLASSPATH_CONTAINER"; //$NON-NLS-1$
    
    String GRAILS_OLD_ATTRIBUTE = GRAILS_OLD_PLUGIN_NAME + ".SOURCE_FOLDER"; //$NON-NLS-1$
    String GRAILS_NEW_ATTRIBUTE = GRAILS_NEW_PLUGIN_NAME + ".SOURCE_FOLDER"; //$NON-NLS-1$
    
    String GRAILS_OLD_PERSPECTIVE_ID = "com.springsource.sts.grails.perspective"; //$NON-NLS-1$
    String GRAILS_NEW_PERSPECTIVE_ID = "org.grails.ide.eclipse.perspective"; //$NON-NLS-1$
   
    // GRADLE
    String GRADLE_OLD_PREFIX = "com.springsource.sts.gradle"; //$NON-NLS-1$
    String GRADLE_NEW_PREFIX = "org.springsource.ide.eclipse.gradle"; //$NON-NLS-1$
    String GRADLE_OLD_PLUGIN_NAME = "com.springsource.sts.gradle.core"; //$NON-NLS-1$
    String GRADLE_NEW_PLUGIN_NAME = "org.springsource.ide.eclipse.gradle.core"; //$NON-NLS-1$
    String GRADLE_OLD_NATURE = GRADLE_OLD_PLUGIN_NAME + ".nature"; //$NON-NLS-1$
    String GRADLE_NEW_NATURE = GRADLE_NEW_PLUGIN_NAME + ".nature"; //$NON-NLS-1$
    
    // ROO
    String ROO_OLD_NATURE = "com.springsource.sts.roo.core.nature"; //$NON-NLS-1$
    String ROO_NEW_NATURE = "com.springsource.sts.roo.core.nature"; //$NON-NLS-1$
    
    String ROO_OLD_PLUGIN_NAME = "com.springsource.sts.roo.core"; //$NON-NLS-1$
    String ROO_NEW_PLUGIN_NAME = "org.springframework.ide.eclipse.roo.core"; //$NON-NLS-1$
    
    String ROO_OLD_UI_NAME = "com.springsource.sts.roo.ui"; //$NON-NLS-1$
    String ROO_NEW_UI_NAME = "org.springframework.ide.eclipse.roo.ui"; //$NON-NLS-1$

    // OTHER
    String STS_OLD_CONTENT_CORE = "com.springsource.sts.content.core"; //$NON-NLS-1$
    String STS_NEW_CONTENT_CORE = "org.springsource.ide.eclipse.commons.content.core"; //$NON-NLS-1$

    String STS_OLD_CORE = "com.springsource.sts.core"; //$NON-NLS-1$
    String STS_NEW_CORE = "org.springsource.ide.eclipse.commons.core"; //$NON-NLS-1$

    String STS_OLD_IDE_UI = "com.springsource.sts.ide.ui"; //$NON-NLS-1$
    String STS_NEW_IDE_UI = "org.springsource.ide.eclipse.dashboard.ui"; //$NON-NLS-1$
        
    String[] STS_OLD_WORKSPACE_PREFS = new String[] {
            "com.springsource.sts.grails.core", //$NON-NLS-1$
            "com.springsource.sts.grails.ui", //$NON-NLS-1$
            "com.springsource.sts.grails.editor.gsp", //$NON-NLS-1$
            "com.springsource.sts.grails.explorer", //$NON-NLS-1$
            "com.springsource.sts.grails.refactoring", //$NON-NLS-1$
            "com.springsource.sts.grails.groovy.debug.core", //$NON-NLS-1$
            "com.springsource.sts.config.ui", //$NON-NLS-1$
            "com.springsource.sts.core", //$NON-NLS-1$
 //           "com.springsource.sts.gradle.core", Gradle does its own thing.
            "com.springsource.sts.groovy.debug.core",  //$NON-NLS-1$
            "com.springsource.sts.ide.metadata", //$NON-NLS-1$
            "com.springsource.sts.maven", //$NON-NLS-1$
            "com.springsource.sts.ide.osgi", //$NON-NLS-1$
            "com.springsource.sts.ide.ui", //$NON-NLS-1$
            //"com.springsource.sts.config.flow", //$NON-NLS-1$
    };
    String[] STS_NEW_WORKSPACE_PREFS = new String[] {
            "org.grails.ide.eclipse.core", //$NON-NLS-1$
            "org.grails.ide.eclipse.ui", //$NON-NLS-1$
            "org.grails.ide.eclipse.editor.gsp", //$NON-NLS-1$
            "org.grails.ide.eclipse.explorer", //$NON-NLS-1$
            "org.grails.ide.eclipse.refactoring", //$NON-NLS-1$
            "org.grails.ide.eclipse.groovy.debug.core", //$NON-NLS-1$
            "org.springframework.ide.eclipse.config.ui", //$NON-NLS-1$
            "org.springsource.ide.eclipse.commons.core", //$NON-NLS-1$
//            "org.springsource.ide.eclipse.gradle.core", //$NON-NLS-1$
            "org.grails.ide.eclipse.groovy.debug.core", //$NON-NLS-1$
            "org.springframework.ide.eclipse.metadata", //$NON-NLS-1$
            "org.springframework.ide.eclipse.maven", //$NON-NLS-1$
            "org.springframework.ide.eclipse.osgi.runtime", //$NON-NLS-1$
            "org.springsource.ide.eclipse.dashboard.ui", //$NON-NLS-1$
            //"org.springframework.ide.eclipse.config.graph" //$NON-NLS-1$
    };
    
}
