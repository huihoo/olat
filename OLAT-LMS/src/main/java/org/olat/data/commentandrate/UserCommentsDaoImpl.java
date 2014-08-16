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
package org.olat.data.commentandrate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description:<br>
 * This implementation of the user comments manager is database based.
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi, Christian Guretzki
 */
@Repository
public class UserCommentsDaoImpl implements UserCommentsDao {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    DB db;

    /**
     * [spring]
     */
    private UserCommentsDaoImpl() {
    }

    /**
	 */
    public Long countComments(OLATResourceable olatResourceable, String resourceableSubPath) {
        DBQuery query;
        if (resourceableSubPath == null) {
            // special query when sub path is null
            query = db.createQuery("select count(*) from UserCommentImpl where resName=:resname AND resId=:resId AND resSubPath is NULL");
        } else {
            query = db.createQuery("select count(*) from UserCommentImpl where resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
            query.setString("resSubPath", resourceableSubPath);
        }
        query.setString("resname", olatResourceable.getResourceableTypeName());
        query.setLong("resId", olatResourceable.getResourceableId());
        query.setCacheable(true);
        //
        Long count = (Long) query.list().get(0);
        return count;
    }

    /**
	 */
    public List<UserCommentsCount> countCommentsWithSubPath(OLATResourceable olatResourceable, String resourceableSubPath) {
        if (resourceableSubPath != null) {
            UserCommentsCount count = new UserCommentsCountImpl(olatResourceable, resourceableSubPath, countComments(olatResourceable, resourceableSubPath));
            return Collections.singletonList(count);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("select comment.resSubPath, count(comment.key) from ").append(UserCommentImpl.class.getName()).append(" as comment ")
                .append(" where comment.resName=:resname AND comment.resId=:resId").append(" group by comment.resSubPath");

        DBQuery query = db.createQuery(sb.toString());
        query.setString("resname", olatResourceable.getResourceableTypeName());
        query.setLong("resId", olatResourceable.getResourceableId());

        Set<String> countMap = new HashSet<String>();
        List<Object[]> counts = query.list();
        List<UserCommentsCount> countList = new ArrayList<UserCommentsCount>();
        for (Object[] count : counts) {
            Object subPath = count[0] == null ? "" : count[0];
            if (!countMap.contains(subPath)) {
                UserCommentsCount c = new UserCommentsCountImpl(olatResourceable, (String) count[0], (Long) count[1]);
                countList.add(c);
            }
        }
        return countList;
    }

    /**
	 */
    public UserComment createAndSaveComment(OLATResourceable olatResourceable, String resourceableSubPath, Identity creator, String commentText) {
        UserComment comment = new UserCommentImpl(olatResourceable, resourceableSubPath, creator, commentText);
        db.saveObject(comment);
        return comment;
    }

    /**
     * Reload the given user comment with the most recent version from the database
     * 
     * @return the reloaded user comment or NULL if the comment does not exist anymore
     */
    private UserComment reloadComment(UserComment comment) {
        try {
            return (UserComment) db.loadObject(comment);
        } catch (Exception e) {
            // Huh, most likely the given object does not exist anymore on the
            // db, probably deleted by someone else
            log.warn("Tried to reload a user comment but got an exception. Probably deleted in the meantime", e);
            return null;
        }
    }

    /**
     * org.olat.data.basesecurity.Identity, java.lang.String)
     */
    public UserComment replyTo(OLATResourceable olatResourceable, String resourceableSubPath, UserComment originalComment, Identity creator, String replyCommentText) {
        if (!isCommentOfResource(olatResourceable, resourceableSubPath, originalComment)) {
            throw new AssertException("This user comment manager is initialized for another resource than the given comment.");
        }
        // First reload parent from cache to prevent stale object or cache issues
        originalComment = reloadComment(originalComment);
        if (originalComment == null) {
            // Original comment has been deleted in the meantime. Don't create a reply
            return null;
        }
        UserCommentImpl reply = new UserCommentImpl(olatResourceable, resourceableSubPath, creator, replyCommentText);
        reply.setParent(originalComment);
        db.saveObject(reply);
        return reply;
    }

    /**
	 */
    public List<UserComment> getComments(OLATResourceable olatResourceable, String resourceableSubPath) {
        DBQuery query;
        if (resourceableSubPath == null) {
            // special query when sub path is null
            query = db.createQuery("select comment from UserCommentImpl as comment where resName=:resname AND resId=:resId AND resSubPath is NULL");
        } else {
            query = db.createQuery("select comment from UserCommentImpl as comment where resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
            query.setString("resSubPath", resourceableSubPath);
        }
        query.setString("resname", olatResourceable.getResourceableTypeName());
        query.setLong("resId", olatResourceable.getResourceableId());
        query.setCacheable(true);
        //
        List<UserComment> results = query.list();
        return results;
    }

    /**
	 */
    public UserComment updateComment(OLATResourceable olatResourceable, String resourceableSubPath, UserComment comment, String newCommentText) {
        if (!isCommentOfResource(olatResourceable, resourceableSubPath, comment)) {
            throw new AssertException("This user comment manager is initialized for another resource than the given comment.");
        }
        // First reload parent from cache to prevent stale object or cache issues
        comment = reloadComment(comment);
        if (comment == null) {
            // Original comment has been deleted in the meantime. Don't update it
            return null;
        }
        // Update DB entry
        comment.setComment(newCommentText);
        db.updateObject(comment);
        return comment;
    }

    /**
	 */
    public int deleteComment(OLATResourceable olatResourceable, String resourceableSubPath, UserComment comment, boolean deleteReplies) {
        if (!isCommentOfResource(olatResourceable, resourceableSubPath, comment)) {
            throw new AssertException("This user comment manager is initialized for another resource than the given comment.");
        }
        int counter = 0;
        // First reload parent from cache to prevent stale object or cache issues
        comment = reloadComment(comment);
        if (comment == null) {
            // Original comment has been deleted in the meantime. Don't delete it again.
            return 0;
        }
        // First deal with all direct replies
        DBQuery query = db.createQuery("select comment from UserCommentImpl as comment where parent=:parent");
        query.setEntity("parent", comment);
        List<UserComment> replies = query.list();
        if (deleteReplies) {
            // Since we have a many-to-one we first have to recursively delete
            // the replies to prevent foreign key constraints
            for (UserComment reply : replies) {
                counter += deleteComment(olatResourceable, resourceableSubPath, reply, true);
            }
        } else {
            // To not delete the replies we have to set the parent to the parent
            // of the original comment for each reply
            for (UserComment reply : replies) {
                reply.setParent(comment.getParent());
                db.updateObject(reply);
            }
        }
        // Now delete this comment and finish
        db.deleteObject(comment);
        return counter + 1;
    }

    /**
	 */
    public int deleteAllComments(OLATResourceable olatResourceable, String resourceableSubPath) {
        String query;
        Object[] values;
        Type[] types;
        // special query when sub path is null
        if (resourceableSubPath == null) {
            query = "from UserCommentImpl where resName=? AND resId=? AND resSubPath is NULL";
            values = new Object[] { olatResourceable.getResourceableTypeName(), olatResourceable.getResourceableId() };
            types = new Type[] { Hibernate.STRING, Hibernate.LONG };
        } else {
            query = "from UserCommentImpl where resName=? AND resId=? AND resSubPath=?";
            values = new Object[] { olatResourceable.getResourceableTypeName(), olatResourceable.getResourceableId(), resourceableSubPath };
            types = new Type[] { Hibernate.STRING, Hibernate.LONG, Hibernate.STRING };
        }
        return db.delete(query, values, types);
    }

    /**
	 */
    public int deleteAllCommentsIgnoringSubPath(OLATResourceable olatResourceable) {
        // Don't limit to subpath. Ignore if null or not, just delete on the resource
        String query = "from UserCommentImpl where resName=? AND resId=?";
        Object[] values = new Object[] { olatResourceable.getResourceableTypeName(), olatResourceable.getResourceableId() };
        Type[] types = new Type[] { Hibernate.STRING, Hibernate.LONG };
        return db.delete(query, values, types);
    }

    /**
     * Helper method to check if the given comment has the same resource and path as configured for this manager
     * 
     * @param originalComment
     * @return
     */
    private boolean isCommentOfResource(OLATResourceable olatResourceable, String resourceableSubPath, UserComment originalComment) {
        if (olatResourceable.getResourceableId().equals(originalComment.getResId()) && olatResourceable.getResourceableTypeName().equals(originalComment.getResName())) {
            // check on resource subpath: can be null
            if (resourceableSubPath == null) {
                return (originalComment.getResSubPath() == null);
            } else {
                return resourceableSubPath.equals(originalComment.getResSubPath());
            }
        }
        return false;
    }

}
