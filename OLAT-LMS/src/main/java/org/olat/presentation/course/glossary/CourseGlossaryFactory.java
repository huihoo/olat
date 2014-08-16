/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.course.glossary;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.presentation.glossary.GlossaryMainController;

/**
 * Description: <br>
 * Factory methods to create the glossary wrapper, the run and edit controller
 * <p>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class CourseGlossaryFactory {

    /**
     * The glossary wrapper enables the glossary in the given component. Meaning, within the component the glossary terms are highlighted. The controller hides itself,
     * the user won't see anything besides the glossary terms.
     * 
     * @param ureq
     * @param wControl
     * @param tmComponent
     *            the component to which the glossary should be applied
     * @param courseConfig
     *            use the glossary configuration from the given course configuration
     */
    public static GlossaryMarkupItemController createGlossaryMarkupWrapper(final UserRequest ureq, final WindowControl wControl, final Component tmComponent,
            final CourseConfig courseConfig) {
        if (courseConfig.hasGlossary()) {
            final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(courseConfig.getGlossarySoftKey(), false);
            if (repoEntry == null) {
                // seems to be removed
                return null;
            }
            final VFSContainer glossaryFolder = GlossaryManager.getInstance().getGlossaryRootFolder(repoEntry.getOlatResource());
            final String glossaryId = repoEntry.getOlatResource().getResourceableId().toString();
            return new GlossaryMarkupItemController(ureq, wControl, tmComponent, glossaryFolder, glossaryId);
        }
        return null;
    }

    /**
     * Creates the key for the GUI preferences where the users glossary display settings are stored
     * 
     * @param course
     * @return
     */
    public static String createGuiPrefsKey(final ICourse course) {
        return "glossary.enabled.course." + course.getResourceableId();
    }

    /**
     * The glossarymaincontroller allows browsing in the glossary. A flag enables the edit mode.
     * 
     * @param windowControl
     * @param ureq
     * @param courseConfig
     *            use the glossary configuration from the given course configuration
     * @param hasGlossaryEditRights
     * @return
     */
    public static GlossaryMainController createCourseGlossaryMainRunController(final WindowControl lwControl, final UserRequest lureq, final CourseConfig cc,
            final boolean allowGlossaryEditing) {
        if (cc.hasGlossary()) {
            final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(cc.getGlossarySoftKey(), false);
            if (repoEntry == null) {
                // seems to be removed
                return null;
            }
            final VFSContainer glossaryFolder = GlossaryManager.getInstance().getGlossaryRootFolder(repoEntry.getOlatResource());
            return new GlossaryMainController(lwControl, lureq, glossaryFolder, repoEntry.getOlatResource(), allowGlossaryEditing);
        }
        return null;
    }

}
