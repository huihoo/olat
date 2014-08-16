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

import org.olat.data.basesecurity.Identity;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * DAO class for user-rating.
 * 
 * <P>
 * Initial Date: 06.07.2011 <br>
 * 
 * @author guretzki
 */
public interface UserRatingsDao {

    /**
     * @param olatResourceable
     * @param resourceableSubPath
     * @return The average of ratings for the configured resource. 0 if no ratings are available.
     */
    public abstract Float calculateRatingAverage(OLATResourceable olatResourceable, String resourceableSubPath);

    /**
     * @param olatResourceable
     * @param resourceableSubPath
     * @return The number of ratings for the configured resource. 0 if no ratings are available.
     */
    public abstract Long countRatings(OLATResourceable olatResourceable, String resourceableSubPath);

    /**
     * Create and save a new rating for the configured resource
     * 
     * @param olatResourceable
     * @param resourceableSubPath
     * @param creator
     *            The user who is rating
     * @param ratingValue
     *            The rating
     * @return
     */
    public abstract UserRating createAndSaveRating(OLATResourceable olatResourceable, String resourceableSubPath, Identity creator, int ratingValue);

    /**
     * Get the rating for the configured user, olat-resourceable and sub-path.
     * 
     * @param olatResourceable
     * @param resourceableSubPath
     * @param identity
     * @return The users rating or NULL
     */
    public abstract UserRating getRating(OLATResourceable olatResourceable, String resourceableSubPath, Identity identity);

    /**
     * Delete a rating
     * 
     * @param olatResourceable
     *            delete rating for this olatResourceable
     * @param resourceableSubPath
     *            delete rating for this resourceableSubPath
     * @param rating
     * @return int number of deleted ratings
     */
    public abstract int deleteRating(OLATResourceable olatResourceable, String resourceableSubPath, UserRating rating);

    /**
     * Delete all ratings for the configured resource and sub path
     * 
     * @param olatResourceable
     *            delete rating for this olatResourceable
     * @param resourceableSubPath
     *            delete rating for this resourceableSubPath
     * @return the number of deleted comments
     */
    public abstract int deleteAllRatings(OLATResourceable olatResourceable, String resourceableSubPath);

    /**
     * Delete all ratingsfor the configured resource while ignoring the sub path. Use this to delete all ratings e.g. from a blog for all blog posts in one query
     * 
     * @param olatResourceable
     *            delete rating for this olatResourceable
     * @return
     */
    public abstract int deleteAllRatingsIgnoringSubPath(OLATResourceable olatResourceable);

    /**
     * Update a rating. This will first reload the comment object and then update this new object to reduce stale object issues. Make sure you replace your object in your
     * datamodel with the returned user comment object.
     * 
     * @param rating
     *            The rating which should be updated
     * @param rewRatingValue
     *            The updated rating value
     * @return the updated rating object. Might be a different object than the rating given as attribute or NULL if the rating has been deleted in the meantime and could
     *         not be updated at all.
     */
    public abstract UserRating updateRating(OLATResourceable olatResourceable, String resourceableSubPath, UserRating rating, int newRatingValue);

}
