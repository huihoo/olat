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

package org.olat.lms.commentandrate;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commentandrate.UserCommentsDao;
import org.olat.presentation.commentandrate.UserCommentsAndRatingsController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * User interface controller and manager factory for the comment and rating service. This is a spring prototype. Use the init() methods after getting your instance from
 * spring to configure the service for your resource. (See the interface for an code example)
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public class CommentAndRatingServiceImpl implements CommentAndRatingService {
    //
    private OLATResourceable ores;
    private String oresSubPath;
    private CommentAndRatingSecurityCallback secCallback;

    @Autowired
    UserCommentsDao userCommentsDao;
    //
    private UserCommentsManager userCommentsManager;

    private UserRatingsManager userRatingsManager;

    /**
     * [spring only]
     */
    private CommentAndRatingServiceImpl() {
        //
    }

    public void setUserCommentsManager(UserCommentsManager userCommentsManager) {
        this.userCommentsManager = userCommentsManager;
    }

    public void setUserRatingsManager(UserRatingsManager userRatingsManager) {
        this.userRatingsManager = userRatingsManager;
    }

    /**
     * boolean, boolean)
     */
    @Override
    public void init(Identity identity, OLATResourceable oRes, String oresSubP, boolean isAdmin, boolean isAnonymous) {
        CommentAndRatingSecurityCallback callback = new CommentAndRatingDefaultSecurityCallback(identity, isAdmin, isAnonymous);
        init(oRes, oresSubP, callback);
    }

    /**
     * org.olat.lms.commentandrate.CommentAndRatingSecurityCallback)
     */
    @Override
    public void init(OLATResourceable oRes, String oresSubP, CommentAndRatingSecurityCallback securityCallback) {
        if (this.ores != null) {
            throw new AssertException("Programming error - this Comment and Rating service is already used by another party. This is a spring prototype!");
        }
        this.ores = oRes;
        this.oresSubPath = oresSubP;
        this.secCallback = securityCallback;
        this.userCommentsManager.init(ores, oresSubPath, userCommentsDao);
        this.userRatingsManager.init(ores, oresSubPath, userCommentsDao);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public UserCommentsAndRatingsController createUserCommentsControllerMinimized(UserRequest ureq, WindowControl wControl) {
        if (ores == null || secCallback == null) {
            throw new AssertException("CommentAndRatingService must be initialized first, call init method");
        }
        return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, true, false, false);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public UserCommentsAndRatingsController createUserCommentsControllerExpandable(UserRequest ureq, WindowControl wControl) {
        if (ores == null || secCallback == null) {
            throw new AssertException("CommentAndRatingService must be initialized first, call init method");
        }
        return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, true, false, true);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public UserCommentsAndRatingsController createUserCommentsAndRatingControllerExpandable(UserRequest ureq, WindowControl wControl) {
        if (ores == null || secCallback == null) {
            throw new AssertException("CommentAndRatingService must be initialized first, call init method");
        }
        return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, true, true, true);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public UserCommentsAndRatingsController createUserCommentsAndRatingControllerMinimized(UserRequest ureq, WindowControl wControl) {
        if (ores == null || secCallback == null) {
            throw new AssertException("CommentAndRatingService must be initialized first, call init method");
        }
        return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, true, true, false);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public UserCommentsAndRatingsController createUserRatingsController(UserRequest ureq, WindowControl wControl) {
        if (ores == null || secCallback == null) {
            throw new AssertException("CommentAndRatingService must be initialized first, call init method");
        }
        return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, false, true, false);
    }

    /**
	 */
    @Override
    public int deleteAll() {
        if (ores == null || secCallback == null) {
            throw new AssertException("CommentAndRatingService must be initialized first, call init method");
        }
        int delCount = getUserCommentsManager().deleteAllComments();
        delCount += getUserRatingsManager().deleteAllRatings();
        return delCount;
    }

    /**
	 */
    @Override
    public int deleteAllIgnoringSubPath() {
        if (ores == null || secCallback == null) {
            throw new AssertException("CommentAndRatingService must be initialized first, call init method");
        }
        int delCount = getUserCommentsManager().deleteAllCommentsIgnoringSubPath();
        delCount += getUserRatingsManager().deleteAllRatingsIgnoringSubPath();
        return delCount;
    }

    /**
	 */
    @Override
    public UserCommentsManager getUserCommentsManager() {
        return this.userCommentsManager;
    }

    /**
	 */
    @Override
    public UserRatingsManager getUserRatingsManager() {
        return this.userRatingsManager;
    }

}
