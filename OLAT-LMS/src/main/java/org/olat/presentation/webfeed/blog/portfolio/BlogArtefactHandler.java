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

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.filter.Filter;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.BlogArtefact;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.artefacthandler.EPAbstractHandler;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.Item;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.logging.log4j.LoggerHelper;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * Artefact handler for blog entry
 * <P>
 * Initial Date: 3 déc. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BlogArtefactHandler extends EPAbstractHandler<BlogArtefact> {

    private static final Logger log = LoggerHelper.getLogger();

    protected BlogArtefactHandler() {
    }

    @Override
    public String getType() {
        return BlogArtefact.TYPE;
    }

    @Override
    public String getIcon(final AbstractArtefact artefact) {
        return "o_blog_icon";
    }

    @Override
    public BlogArtefact createArtefact() {
        return new BlogArtefact();
    }

    /**
	 */
    @Override
    public void prefillArtefactAccordingToSource(final AbstractArtefact artefact, final Object source) {
        super.prefillArtefactAccordingToSource(artefact, source);
        if (source instanceof Feed) {
            final Feed feed = (Feed) source;
            final String subPath = getItemUUID(artefact.getBusinessPath());
            for (final Item item : feed.getItems()) {
                if (subPath.equals(item.getGuid())) {
                    prefillBlogArtefact(artefact, feed, item);
                }
            }
            artefact.setSignature(70);
        }
    }

    private void prefillBlogArtefact(final AbstractArtefact artefact, final Feed feed, final Item item) {
        final VFSContainer itemContainer = FeedManager.getInstance().getItemContainer(item, feed);
        artefact.setFileSourceContainer(itemContainer);
        artefact.setTitle(item.getTitle());
        artefact.setDescription(item.getDescription());

        final VFSLeaf itemXml = (VFSLeaf) itemContainer.resolve(BlogArtefact.BLOG_FILE_NAME);
        if (itemXml != null) {
            final InputStream in = itemXml.getInputStream();
            final String xml = FileUtils.load(in, "UTF-8");
            artefact.setFulltextContent(xml);
            FileUtils.closeSafely(in);
        }
    }

    @Override
    public Controller createDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final boolean readOnlyMode) {
        final BlogArtefactDetailsController ctrl = new BlogArtefactDetailsController(ureq, wControl, (BlogArtefact) artefact, readOnlyMode);
        return ctrl;
    }

    @Override
    protected void getContent(final AbstractArtefact artefact, final StringBuilder sb, final SearchResourceContext context, final EPFrontendManager ePFManager) {
        final String content = ePFManager.getArtefactFullTextContent(artefact);
        if (content != null) {
            try {
                final XStream xstream = XStreamHelper.createXStreamInstance();
                xstream.alias("item", Item.class);
                final Item item = (Item) xstream.fromXML(content);

                final String mapperBaseURL = "";
                final Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(mapperBaseURL);
                sb.append(mediaUrlFilter.filter(item.getDescription())).append(" ").append(mediaUrlFilter.filter(item.getContent()));
            } catch (final Exception e) {
                log.warn("Cannot read an artefact of type blog while idnexing", e);
            }
        }
    }

    private String getItemUUID(final String businessPath) {
        final int start = businessPath.lastIndexOf("item=");
        final int stop = businessPath.lastIndexOf(":0]");
        if (start < stop && start > 0 && stop > 0) {
            return businessPath.substring(start + 5, stop);
        } else {
            return null;
        }
    }
}
