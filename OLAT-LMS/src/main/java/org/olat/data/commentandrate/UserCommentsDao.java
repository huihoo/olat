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
package org.olat.data.commentandrate;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * DAO class User comments
 * 
 * <P>
 * Initial Date: 05.07.2011 <br>
 * 
 * @author cg
 */
public interface UserCommentsDao {

    /**
	 */
    public abstract Long countComments(OLATResourceable olatResourceable, String resourceableSubPath);

    /**
	 */
    public abstract List<UserCommentsCount> countCommentsWithSubPath(OLATResourceable olatResourceable, String resourceableSubPath);

    /**
	 */
    public abstract UserComment createAndSaveComment(OLATResourceable olatResourceable, String resourceableSubPath, Identity creator, String commentText);

    /**
     * org.olat.data.basesecurity.Identity, java.lang.String)
     */
    public abstract UserComment replyTo(OLATResourceable olatResourceable, String resourceableSubPath, UserComment originalComment, Identity creator,
            String replyCommentText);

    /**
	 */
    public abstract List<UserComment> getComments(OLATResourceable olatResourceable, String resourceableSubPath);

    /**
	 */
    public abstract UserComment updateComment(OLATResourceable olatResourceable, String resourceableSubPath, UserComment comment, String newCommentText);

    /**
	 */
    public abstract int deleteComment(OLATResourceable olatResourceable, String resourceableSubPath, UserComment comment, boolean deleteReplies);

    /**
	 */
    public abstract int deleteAllComments(OLATResourceable olatResourceable, String resourceableSubPath);

    /**
	 */
    public abstract int deleteAllCommentsIgnoringSubPath(OLATResourceable olatResourceable);

}
