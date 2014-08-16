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
 * DAO for user-ratings.
 * <P>
 * Initial Date: 01.12.2009 <br>
 * 
 * @author gnaegi, Christian Guretzki
 */
@Repository
public class UserRatingsDaoImpl implements UserRatingsDao {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private DB database;

    /**
     * Spring constructor.
     */
    protected UserRatingsDaoImpl() {
        // nothing to do
    }

    /**
	 */
    @Override
    public Float calculateRatingAverage(OLATResourceable olatResourceable, String resourceableSubPath) {
        DBQuery query;
        if (resourceableSubPath == null) {
            // special query when sub path is null
            query = database.createQuery("select avg(rating) from UserRatingImpl where resName=:resname AND resId=:resId AND resSubPath is NULL");
        } else {
            query = database.createQuery("select avg(rating) from UserRatingImpl where resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
            query.setString("resSubPath", resourceableSubPath);
        }
        query.setString("resname", olatResourceable.getResourceableTypeName());
        query.setLong("resId", olatResourceable.getResourceableId());
        query.setCacheable(true);
        //
        List results = query.list();
        Double average = (Double) query.list().get(0);
        // When no ratings are found, a null value is returned!
        if (average == null)
            return Float.valueOf(0);
        else
            return average.floatValue();
    }

    /**
	 */
    @Override
    public Long countRatings(OLATResourceable olatResourceable, String resourceableSubPath) {
        DBQuery query;
        if (resourceableSubPath == null) {
            // special query when sub path is null
            query = database.createQuery("select count(*) from UserRatingImpl where resName=:resname AND resId=:resId AND resSubPath is NULL");
        } else {
            query = database.createQuery("select count(*) from UserRatingImpl where resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
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
    @Override
    public UserRating createAndSaveRating(OLATResourceable olatResourceable, String resourceableSubPath, Identity creator, int ratingValue) {
        UserRating rating = new UserRatingImpl(olatResourceable, resourceableSubPath, creator, Integer.valueOf(ratingValue));
        database.saveObject(rating);
        return rating;
    }

    /**
	 */
    @Override
    public UserRating getRating(OLATResourceable olatResourceable, String resourceableSubPath, Identity identity) {
        DBQuery query;
        if (resourceableSubPath == null) {
            // special query when sub path is null
            query = database
                    .createQuery("select userRating from UserRatingImpl as userRating where creator=:creator AND resName=:resname AND resId=:resId AND resSubPath is NULL ");
        } else {
            query = database
                    .createQuery("select userRating from UserRatingImpl as userRating where creator=:creator AND resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
            query.setString("resSubPath", resourceableSubPath);
        }
        query.setString("resname", olatResourceable.getResourceableTypeName());
        query.setLong("resId", olatResourceable.getResourceableId());
        query.setEntity("creator", identity);
        query.setCacheable(true);
        //
        List<UserRating> results = query.list();
        if (results.size() == 0)
            return null;
        return results.get(0);
    }

    /**
	 */
    @Override
    public int deleteRating(OLATResourceable olatResourceable, String resourceableSubPath, UserRating rating) {
        if (!isRatingOfResource(olatResourceable, resourceableSubPath, rating)) {
            throw new AssertException("This user rating manager is initialized for another resource than the given comment.");
        }
        // First reload parent from cache to prevent stale object or cache issues
        rating = reloadRating(rating);
        if (rating == null) {
            // Original rating has been deleted in the meantime. Don't delete it again.
            return 0;
        }
        // Delete this rating and finish
        database.deleteObject(rating);
        return 1;

    }

    /**
	 */
    @Override
    public int deleteAllRatings(OLATResourceable olatResourceable, String resourceableSubPath) {
        String query;
        Object[] values;
        Type[] types;
        // special query when sub path is null
        if (resourceableSubPath == null) {
            query = "from UserRatingImpl where resName=? AND resId=? AND resSubPath is NULL";
            values = new Object[] { olatResourceable.getResourceableTypeName(), olatResourceable.getResourceableId() };
            types = new Type[] { Hibernate.STRING, Hibernate.LONG };
        } else {
            query = "from UserRatingImpl where resName=? AND resId=? AND resSubPath=?";
            values = new Object[] { olatResourceable.getResourceableTypeName(), olatResourceable.getResourceableId(), resourceableSubPath };
            types = new Type[] { Hibernate.STRING, Hibernate.LONG, Hibernate.STRING };
        }
        return database.delete(query, values, types);
    }

    /**
	 */
    @Override
    public int deleteAllRatingsIgnoringSubPath(OLATResourceable olatResourceable) {
        // Don't limit to subpath. Ignore if null or not, just delete on the resource
        String query = "from UserRatingImpl where resName=? AND resId=?";
        Object[] values = new Object[] { olatResourceable.getResourceableTypeName(), olatResourceable.getResourceableId() };
        Type[] types = new Type[] { Hibernate.STRING, Hibernate.LONG };
        return database.delete(query, values, types);
    }

    /**
     * Reload the given user rating with the most recent version from the database
     * 
     * @return the reloaded user rating or NULL if the rating does not exist anymore
     */
    private UserRating reloadRating(UserRating rating) {
        try {
            return (UserRating) database.loadObject(rating);
        } catch (Exception e) {
            // Huh, most likely the given object does not exist anymore on the
            // db, probably deleted by someone else
            log.warn("Tried to reload a user rating but got an exception. Probably deleted in the meantime", e);
            return null;
        }
    }

    @Override
    public UserRating updateRating(OLATResourceable olatResourceable, String resourceableSubPath, UserRating rating, int newRatingValue) {
        if (!isRatingOfResource(olatResourceable, resourceableSubPath, rating)) {
            throw new AssertException("This user rating manager is initialized for another resource than the given comment.");
        }
        // First reload parent from cache to prevent stale object or cache issues
        rating = reloadRating(rating);
        if (rating == null) {
            // Original rating has been deleted in the meantime. Don't update it
            return null;
        }
        // Update DB entry
        rating.setRating(newRatingValue);
        database.updateObject(rating);
        return rating;
    }

    /**
     * Helper method to check if the given commerating has the same resource and path as configured for this manager
     * 
     * @param originalRating
     * @return
     */
    private boolean isRatingOfResource(OLATResourceable olatResourceable, String resourceableSubPath, UserRating originalRating) {
        if (olatResourceable.getResourceableId().equals(originalRating.getResId()) && olatResourceable.getResourceableTypeName().equals(originalRating.getResName())) {
            // check on resource subpath: can be null
            if (resourceableSubPath == null) {
                return (originalRating.getResSubPath() == null);
            } else {
                return resourceableSubPath.equals(originalRating.getResSubPath());
            }
        }
        return false;
    }

}
