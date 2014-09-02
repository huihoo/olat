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

import org.olat.data.commons.filter.Filter;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.LiveBlogArtefact;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.BlogFileResource;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.artefacthandler.EPAbstractHandler;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.FeedItemDocument;
import org.olat.lms.search.document.OlatDocument;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.FeedResourceSecurityCallback;
import org.olat.lms.webfeed.FeedSecurityCallback;
import org.olat.lms.webfeed.Item;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.webfeed.FeedMainController;
import org.olat.presentation.webfeed.blog.BlogUIFactory;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;

/**
 * Description:<br>
 * The handler for the life blog artefact
 * <P>
 * Initial Date: 8 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class LiveBlogArtefactHandler extends EPAbstractHandler<LiveBlogArtefact> {

    public static final String LIVEBLOG = "[LiveBlog:";

    private FeedManager manager;

    protected LiveBlogArtefactHandler() {
    }

    @Override
    public String getType() {
        return LiveBlogArtefact.TYPE;
    }

    @Override
    public String getIcon(final AbstractArtefact artefact) {
        return "o_blog_icon";
    }

    @Override
    public LiveBlogArtefact createArtefact() {
        final LiveBlogArtefact artefact = new LiveBlogArtefact();
        manager = FeedManager.getInstance();
        final OLATResourceable ores = manager.createBlogResource();
        artefact.setBusinessPath(LIVEBLOG + ores.getResourceableId() + "]");
        return artefact;
    }

    @Override
    public void prefillArtefactAccordingToSource(final AbstractArtefact artefact, final Object source) {
        super.prefillArtefactAccordingToSource(artefact, source);
    }

    @Override
    public Controller createDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final boolean readOnlyMode) {
        final FeedSecurityCallback callback = new FeedResourceSecurityCallback(false, false);
        final String businessPath = artefact.getBusinessPath();
        final Long resid = Long.parseLong(businessPath.substring(10, businessPath.length() - 1));
        final OLATResource ores = OLATResourceManager.getInstance().findResourceable(resid, BlogFileResource.TYPE_NAME);
        final FeedMainController detailsController = BlogUIFactory.getInstance(ureq.getLocale()).createMainController(ores, ureq, wControl, callback);
        return detailsController;
    }

    /**
	 */
    @Override
    public boolean isProvidingSpecialMapViewController() {
        return true;
    }

    /**
	 */
    @Override
    public Controller getSpecialMapViewController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact) {
        final boolean isOwner = ureq.getIdentity().equalsByPersistableKey(artefact.getAuthor());
        final FeedSecurityCallback callback = new FeedResourceSecurityCallback(ureq.getUserSession().getRoles().isOLATAdmin(), isOwner);
        final String businessPath = artefact.getBusinessPath();
        final Long resid = Long.parseLong(businessPath.substring(10, businessPath.length() - 1));
        final OLATResource ores = OLATResourceManager.getInstance().findResourceable(resid, BlogFileResource.TYPE_NAME);
        return BlogUIFactory.getInstance(ureq.getLocale()).createMainController(ores, ureq, wControl, callback);
    }

    @Override
    protected void getContent(final AbstractArtefact artefact, final StringBuilder sb, final SearchResourceContext context, final EPFrontendManager ePFManager) {
        final String businessPath = artefact.getBusinessPath();
        if (StringHelper.containsNonWhitespace(businessPath)) {
            manager = FeedManager.getInstance();
            final String oresId = businessPath.substring(LIVEBLOG.length(), businessPath.length() - 1);
            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(BlogFileResource.TYPE_NAME, Long.parseLong(oresId));
            final Feed feed = manager.getFeed(ores);

            final DummyFilter filter = new DummyFilter();
            for (final Item item : feed.getPublishedItems()) {
                final OlatDocument itemDoc = new FeedItemDocument(item, context, filter);
                final String content = itemDoc.getContent();
                sb.append(content);
            }
        }
    }

    public class DummyFilter implements Filter {

        @Override
        public String filter(final String original) {
            return original;
        }

    }
}
