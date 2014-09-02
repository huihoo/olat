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
package org.olat.presentation.webfeed.podcast;

import java.util.Locale;

import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.AbstractFeedCourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.Item;
import org.olat.presentation.course.nodes.feed.podcast.PodcastNodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.presentation.webfeed.FeedMainController;
import org.olat.presentation.webfeed.FeedUIFactory;

/**
 * UI factory for podcast controllers.
 * <P>
 * Initial Date: Jun 8, 2009 <br>
 * 
 * @author gwassmann
 */
public class PodcastUIFactory extends FeedUIFactory {

    private Translator translator;

    public PodcastUIFactory() {
        super();
    }

    private PodcastUIFactory(final Locale locale) {
        super();
        setTranslator(locale);
    }

    // TODO:GW comments (or refactor?)
    public static PodcastUIFactory getInstance(final Locale locale) {
        return new PodcastUIFactory(locale);
    }

    @Override
    public Translator getTranslator() {
        return translator;
    }

    /**
	 */
    @Override
    public void setTranslator(final Locale locale) {
        final Translator fallbackTans = PackageUtil.createPackageTranslator(FeedMainController.class, locale);
        translator = PackageUtil.createPackageTranslator(PodcastUIFactory.class, locale, fallbackTans);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public IAddController createAddController(final RepositoryAddCallback addCallback, final UserRequest ureq, final WindowControl wControl) {
        return new CreatePodcastController(addCallback, ureq, wControl);
    }

    /**
	 */
    @Override
    public VelocityContainer createInfoVelocityContainer(final BasicController controller) {
        return new VelocityContainer(VC_INFO_NAME, this.getClass(), VC_INFO_NAME, translator, controller);
    }

    /**
	 */
    @Override
    public VelocityContainer createItemsVelocityContainer(final BasicController controller) {
        return new VelocityContainer(VC_ITEMS_NAME, this.getClass(), "episodes", translator, controller);
    }

    /**
	 */
    @Override
    public VelocityContainer createItemVelocityContainer(final BasicController controller) {
        return new VelocityContainer(VC_ITEM_NAME, this.getClass(), "episode", translator, controller);
    }

    /**
	 */
    @Override
    public VelocityContainer createRightColumnVelocityContainer(final BasicController controller) {
        return new VelocityContainer(VC_RIGHT_NAME, this.getClass(), VC_RIGHT_NAME, translator, controller);
    }

    /**
     * org.olat.data.webfeed.Item, org.olat.data.webfeed.Feed)
     */
    @Override
    public FormBasicController createItemFormController(final UserRequest ureq, final WindowControl wControl, final Item item, final Feed feed) {
        return new EpisodeFormController(ureq, wControl, item, feed, getTranslator());
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public TabbableController createNodeEditController(final AbstractFeedCourseNode courseNode, final ICourse course, final UserCourseEnvironment uce,
            final UserRequest ureq, final WindowControl control) {
        return new PodcastNodeEditController(courseNode, course, uce, ureq, control);
    }
}
