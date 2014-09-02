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

package org.olat.presentation.glossary;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Repository workflow to create new glossary item.
 * <P>
 * Initial Date: Dec 04 2006 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class CreateNewGlossaryController extends DefaultController implements IAddController {
    private FileResource newFileResource;

    /**
     * Constructor for the create new glossary workflow
     * 
     * @param addCallback
     * @param ureq
     * @param wControl
     */
    public CreateNewGlossaryController(final RepositoryAddCallback addCallback, final UserRequest ureq, final WindowControl wControl) {
        super(wControl);
        if (addCallback != null) {
            newFileResource = GlossaryManager.getInstance().createGlossary();
            final Translator trnsltr = new PackageTranslator("org.olat.presentation.repository", ureq.getLocale());
            addCallback.setDisplayName(trnsltr.translate(newFileResource.getResourceableTypeName()));
            addCallback.setResourceable(newFileResource);
            addCallback.setResourceName("-");
            addCallback.finished(ureq);
        }
    }

    /**
	 */
    @Override
    public Component getTransactionComponent() {
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
    public void transactionAborted() {
        // File resource already created. Cleanup file resource on abort.
        if (newFileResource != null) {
            GlossaryManager.getInstance().deleteGlossary(newFileResource);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to process here.
    }

    /**
	 */
    @Override
    public void repositoryEntryCreated(final RepositoryEntry re) {
        return;
    } // nothing to do here.

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
