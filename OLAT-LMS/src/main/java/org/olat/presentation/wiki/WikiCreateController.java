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

package org.olat.presentation.wiki;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.wiki.WikiManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: guido Class Description for WikiCreateController
 * <P>
 * Initial Date: May 5, 2006 <br>
 * 
 * @author guido
 */
public class WikiCreateController extends DefaultController implements IAddController {

    private FileResource newWikiResource;

    public WikiCreateController(final RepositoryAddCallback addCallback, final UserRequest ureq, final WindowControl wControl) {
        super(wControl);
        if (addCallback != null) {
            newWikiResource = WikiManager.getInstance().createWiki();
            final Translator trans = PackageUtil.createPackageTranslator(IAddController.class, ureq.getLocale());
            addCallback.setDisplayName(trans.translate(newWikiResource.getResourceableTypeName()));
            addCallback.setResourceable(newWikiResource);
            addCallback.setResourceName(WikiManager.WIKI_RESOURCE_FOLDER_NAME);
            addCallback.finished(ureq);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do here
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do here
    }

    /**
	 */
    @Override
    public Component getTransactionComponent() {
        // no GUI additional workflow for WIKI creation
        return null;
    }

    /**
	 */
    @Override
    public boolean transactionFinishBeforeCreate() {
        return true;
    }

    /**
	 */
    @Override
    public void repositoryEntryCreated(final RepositoryEntry re) {
        //
    }

    /**
	 */
    @Override
    public void transactionAborted() {
        FileResourceManager.getInstance().deleteFileResource(newWikiResource);
    }

}
