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

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commentandrate.UserComment;
import org.olat.data.commentandrate.UserCommentsCount;
import org.olat.data.commentandrate.UserCommentsDao;
import org.olat.lms.activitylogging.CoreLoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * This implementation of the user comments manager is database based.
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentsManagerImpl extends UserCommentsManager {

    /**
     * [spring]
     */
    private UserCommentsManagerImpl() {
        instance = this;
    }

    /**
	 */
    @Override
    protected UserCommentsManager createCommentManager(OLATResourceable ores, String subpath, UserCommentsDao userCommentsDao) {
        UserCommentsManager manager = new UserCommentsManagerImpl();
        manager.init(ores, subpath, userCommentsDao);
        return manager;
    }

    /**
	 */
    @Override
    public Long countComments() {
        return userCommentsDao.countComments(getOLATResourceable(), getOLATResourceableSubPath());
    }

    /**
	 */
    @Override
    public List<UserCommentsCount> countCommentsWithSubPath() {
        return userCommentsDao.countCommentsWithSubPath(getOLATResourceable(), getOLATResourceableSubPath());
    }

    /**
	 */
    @Override
    public UserComment createComment(Identity creator, String commentText) {
        UserComment comment = userCommentsDao.createAndSaveComment(getOLATResourceable(), getOLATResourceableSubPath(), creator, commentText);
        // do Logging
        ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.COMMENT_CREATED, getClass(),
                CoreLoggingResourceable.wrap(getOLATResourceable(), OlatResourceableType.feedItem));
        return comment;
    }

    /**
     * org.olat.data.basesecurity.Identity, java.lang.String)
     */
    @Override
    public UserComment replyTo(UserComment originalComment, Identity creator, String replyCommentText) {
        return userCommentsDao.replyTo(getOLATResourceable(), getOLATResourceableSubPath(), originalComment, creator, replyCommentText);
    }

    /**
	 */
    @Override
    public List<UserComment> getComments() {
        return userCommentsDao.getComments(getOLATResourceable(), getOLATResourceableSubPath());
    }

    /**
	 */
    @Override
    public UserComment updateComment(UserComment comment, String newCommentText) {
        return userCommentsDao.updateComment(getOLATResourceable(), getOLATResourceableSubPath(), comment, newCommentText);
    }

    /**
	 */
    @Override
    public int deleteComment(UserComment comment, boolean deleteReplies) {
        int counter = userCommentsDao.deleteComment(getOLATResourceable(), getOLATResourceableSubPath(), comment, deleteReplies);
        ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.COMMENT_DELETED, getClass(),
                CoreLoggingResourceable.wrap(getOLATResourceable(), OlatResourceableType.feedItem));
        return counter;
    }

    /**
	 */
    @Override
    public int deleteAllComments() {
        return userCommentsDao.deleteAllComments(getOLATResourceable(), getOLATResourceableSubPath());
    }

    /**
	 */
    @Override
    public int deleteAllCommentsIgnoringSubPath() {
        return userCommentsDao.deleteAllCommentsIgnoringSubPath(getOLATResourceable());
    }

}
