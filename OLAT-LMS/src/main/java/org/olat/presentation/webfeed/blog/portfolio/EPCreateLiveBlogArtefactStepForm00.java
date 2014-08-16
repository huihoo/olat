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
package org.olat.presentation.webfeed.blog.portfolio;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.LiveBlogArtefact;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.portfolio.artefacts.collect.EPCollectStepForm00;

/**
 * Description:<br>
 * Only retrieve the title and description to apply them to the blog.
 * <P>
 * Initial Date: 9 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http.//www.frentix.com
 */
public class EPCreateLiveBlogArtefactStepForm00 extends EPCollectStepForm00 {

    private final LiveBlogArtefact blogArtefact;

    public EPCreateLiveBlogArtefactStepForm00(final UserRequest ureq, final WindowControl wControl, final Form rootForm, final StepsRunContext runContext,
            final int layout, final String customLayoutPageName, final AbstractArtefact artefact) {
        super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName, artefact);

        blogArtefact = (LiveBlogArtefact) artefact;
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        // set title and description
        super.formOK(ureq);
        // copy title and description to the blog

        final Feed feed = FeedManager.getInstance().getFeedLight(blogArtefact.getBusinessPath());
        feed.setAuthor(blogArtefact.getAuthor().getName());
        feed.setTitle(blogArtefact.getTitle());
        feed.setDescription(blogArtefact.getDescription());
        FeedManager.getInstance().updateFeedMetadata(feed);
    }
}
