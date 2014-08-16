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
package org.olat.lms.webfeed;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Facade class from refactoring of DB Bad Smells - intermediate commit moved here
 * 
 * <P>
 * Initial Date: 14.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class FeedMediaEBL {

    @Autowired
    private FeedManager feedManager;

    /**
     * facade method makes intermediate commit before creation of feed file
     * 
     * @param feed
     * @param identity
     * @param path
     * @return concrete MediaResource (Feed File)
     */
    public MediaResource createFeedFile(final OLATResourceable feed, final Identity identity, final Path path, final Translator translator) {
        doIntermediateCommit();
        return feedManager.createFeedFile(feed, identity, path.getCourseId(), path.getNodeId(), translator);
    }

    /**
     * facade method makes intermediate commit before creation of Item Media file
     * 
     * @param feed
     * @param path
     * @return concrete MediaResource (Item Media File)
     */
    public MediaResource createItemMediaFile(final OLATResourceable feed, final Path path) {
        doIntermediateCommit();
        return feedManager.createItemMediaFile(feed, path.getItemId(), path.getItemFileName());
    }

    /**
     * facade method makes intermediate commit before creation of Feed Media file
     * 
     * @param feed
     * @param path
     * @return concrete MediaResource (Feed Media File)
     */
    public MediaResource createFeedMediaFile(final OLATResourceable feed, final Path path) {
        doIntermediateCommit();
        return feedManager.createFeedMediaFile(feed, path.getIconFileName());
    }

    private void doIntermediateCommit() {
        // OLAT-5243 related: deliverFile can last arbitrary long, which can cause the open db connection to timeout and cause errors,
        // hence we need to do an intermediateCommit here
        DBFactory.getInstance().intermediateCommit();
    }

}
