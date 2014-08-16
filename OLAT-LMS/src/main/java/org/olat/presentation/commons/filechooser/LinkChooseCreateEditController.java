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

package org.olat.presentation.commons.filechooser;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.presentation.framework.common.htmleditor.WysiwygFactory;
import org.olat.presentation.framework.common.linkchooser.CustomLinkTreeModel;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Controller to create link chooser (select link to file or select internal link e.g. link to course node).
 * 
 * @author Christian Guretzki
 */
public class LinkChooseCreateEditController extends FileChooseCreateEditController {

    private final CustomLinkTreeModel customLinkTreeModel;

    /**
	 * 
	 */
    public LinkChooseCreateEditController(final UserRequest ureq, final WindowControl wControl, final String chosenFile, final Boolean allowRelativeLinks,
            final VFSContainer rootContainer, final String target, final String fieldSetLegend, final CustomLinkTreeModel customLinkTreeModel) {
        super(ureq, wControl, chosenFile, allowRelativeLinks, rootContainer, target, fieldSetLegend);
        this.customLinkTreeModel = customLinkTreeModel;
    }

    /**
	 * 
	 */
    public LinkChooseCreateEditController(final UserRequest ureq, final WindowControl wControl, final String chosenFile, final Boolean allowRelativeLinks,
            final VFSContainer rootContainer, final CustomLinkTreeModel customLinkTreeModel) {
        super(ureq, wControl, chosenFile, allowRelativeLinks, rootContainer);
        this.customLinkTreeModel = customLinkTreeModel;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        super.doDispose();
    }

    /**
     * Creates a Controller with internal-link support.
     * 
     * org.olat.presentation.framework.control.WindowControl, org.olat.data.commons.vfs.VFSContainer, java.lang.String)
     */
    @Override
    protected Controller createWysiwygController(final UserRequest ureq, final WindowControl windowControl, final VFSContainer rootContainer, final String chosenFile) {
        return WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, windowControl, rootContainer, chosenFile, true, customLinkTreeModel);
    }

}
