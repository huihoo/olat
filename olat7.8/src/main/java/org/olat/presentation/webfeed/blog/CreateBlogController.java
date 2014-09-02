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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.webfeed.blog;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.webfeed.FeedManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * Controller that handles the creation of a new podcast resource.
 * <P>
 * Initial Date: Mar 18, 2009 <br>
 * 
 * @author gwassmann
 */
public class CreateBlogController extends DefaultController implements IAddController {
    private OLATResourceable feedResource;

    /**
     * Constructor
     * 
     * @param addCallback
     * @param ureq
     * @param wControl
     */
    protected CreateBlogController(final RepositoryAddCallback addCallback, final UserRequest ureq, final WindowControl wControl) {
        super(wControl);
        if (addCallback != null) {
            final FeedManager manager = FeedManager.getInstance();
            // Create a new podcast feed resource
            feedResource = manager.createBlogResource();
            final Translator trans = new PackageTranslator("org.olat.presentation.repository", ureq.getLocale());
            addCallback.setDisplayName(trans.translate(feedResource.getResourceableTypeName()));
            addCallback.setResourceable(feedResource);
            addCallback.setResourceName(manager.getFeedKind(feedResource));
            addCallback.finished(ureq);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Nothing to dispose
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // Nothing to catch
    }

    /**
	 */
    @Override
    public Component getTransactionComponent() {
        // No additional workflow for feed creation
        return null;
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    public void repositoryEntryCreated(final RepositoryEntry re) {
        // Nothing to do here, but thanks for asking.
    }

    /**
	 */
    @Override
    public void transactionAborted() {
        FeedManager.getInstance().delete(feedResource);
    }

    /**
	 */
    @Override
    public boolean transactionFinishBeforeCreate() {
        // Don't finish before creation (?!)
        return true;
    }

}
