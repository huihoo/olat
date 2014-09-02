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
package org.olat.presentation.course.nodes.feed.blog;

import org.olat.lms.commons.fileresource.BlogFileResource;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.AbstractFeedCourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.nodes.feed.FeedNodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.webfeed.blog.BlogUIFactory;

/**
 * The blog course node edit controller.
 * <P>
 * Initial Date: Mar 31, 2009 <br>
 * 
 * @author gwassmann
 */
public class BlogNodeEditController extends FeedNodeEditController {

    /**
     * Constructor
     * 
     * @param courseNode
     * @param course
     * @param uce
     * @param ureq
     * @param control
     */
    public BlogNodeEditController(final AbstractFeedCourseNode courseNode, final ICourse course, final UserCourseEnvironment uce, final UserRequest ureq,
            final WindowControl control) {
        super(courseNode, course, uce, BlogUIFactory.getInstance(ureq.getLocale()), BlogFileResource.TYPE_NAME, ureq, control);
    }
}
