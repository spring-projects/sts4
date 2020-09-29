/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.legacyconversion;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion.IConversionConstants;
import org.springsource.ide.eclipse.commons.frameworks.ui.FrameworkUIActivator;

/**
 * Migrates legacy perspectives to new perspectives.
 * 
 * Actually, we're having a bit of trouble removing the persective from the Perspective bar in the UI, but
 * this ensures that the new perspective is opened if necessary.
 * @author Andrew Eisenberg
 * @since 3.0.0
 */
public class PerspectiveMigrator {
    public IStatus migratePerspective(String oldPerspectiveId, String newPerspectiveId, IProgressMonitor monitor) {
        try {
            monitor = SubMonitor.convert(monitor, "Migrating legacy perspectives", 3);
            IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();
            monitor.worked(1);
            IWorkbenchPage page = null;
            try {
                page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            } catch (NullPointerException e) {
                // something wasn't initialized...ignore
            }
            if (page != null) {
                if (page.getPerspective()  == null || page.getPerspective().getId().equals(oldPerspectiveId)) {
                    IPerspectiveDescriptor newPerspective = registry.findPerspectiveWithId(newPerspectiveId);
                    page.setPerspective(newPerspective);
                    IPerspectiveDescriptor oldPerspective = registry.findPerspectiveWithId(oldPerspectiveId);
                    monitor.worked(1);
                    if (oldPerspective != null) {
                        page.closePerspective(oldPerspective, false, false);
                        registry.deletePerspective(oldPerspective);
                        
                    }
                }
                // Actually, there is no mechanism to close this kind of perspective
                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=381473
                if (page instanceof WorkbenchPage) {
                    try {
                        Method closePerspectiveMethod = WorkbenchPage.class.getDeclaredMethod("closePerspective", IPerspectiveDescriptor.class, String.class, boolean.class, boolean.class);
                        closePerspectiveMethod.invoke(page, null, IConversionConstants.GRAILS_OLD_PERSPECTIVE_ID, true, false);
                    } catch (Exception e) {
                        // this method doesn't exist on e37. OK to ignore
//                        FrameworkUIActivator.getDefault().getLog().log(new Status(IStatus.INFO, FrameworkUIActivator.PLUGIN_ID, "Cannot use reflection to close legacy perspective on Eclipse 3.7.", e));
                    }
                }
            }
            monitor.worked(1);
            monitor.done();
            return new Status(IStatus.OK, FrameworkUIActivator.PLUGIN_ID, "Migrate legacy perspectives.");
        } catch (Exception e) {
            return new Status(IStatus.ERROR, FrameworkUIActivator.PLUGIN_ID, "Problem migrating legacy perspectives.", e);
        }
    }
}
