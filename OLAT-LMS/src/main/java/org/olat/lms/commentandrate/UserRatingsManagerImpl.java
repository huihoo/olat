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
package org.olat.lms.commentandrate;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commentandrate.UserCommentsDao;
import org.olat.data.commentandrate.UserRating;
import org.olat.data.commentandrate.UserRatingsDao;
import org.olat.lms.activitylogging.CoreLoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This implementation of the user rating manager is database based.
 * <P>
 * Initial Date: 01.12.2009 <br>
 * 
 * @author gnaegi
 */
public class UserRatingsManagerImpl extends UserRatingsManager {

    @Autowired
    UserRatingsDao userRatingsDao;

    /**
     * Spring
     */
    protected UserRatingsManagerImpl() {
        // nothing to do
    }

    /**
	 */
    @Override
    protected UserRatingsManager createRatingsManager(OLATResourceable ores, String subpath, UserCommentsDao userCommentsDao) {
        UserRatingsManager manager = new UserRatingsManagerImpl();
        manager.init(ores, subpath, userCommentsDao);
        return manager;
    }

    /**
	 */
    @Override
    public Float calculateRatingAverage() {
        return userRatingsDao.calculateRatingAverage(getOLATResourceable(), getOLATResourceableSubPath());
    }

    /**
	 */
    @Override
    public Long countRatings() {
        return userRatingsDao.countRatings(getOLATResourceable(), getOLATResourceableSubPath());
    }

    /**
	 */
    @Override
    public UserRating createRating(Identity creator, int ratingValue) {
        UserRating rating = userRatingsDao.createAndSaveRating(getOLATResourceable(), getOLATResourceableSubPath(), creator, Integer.valueOf(ratingValue));
        // do logging
        ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.RATING_CREATED, getClass(),
                CoreLoggingResourceable.wrap(getOLATResourceable(), OlatResourceableType.feedItem));
        return rating;
    }

    /**
	 */
    @Override
    public UserRating getRating(Identity identity) {
        return userRatingsDao.getRating(getOLATResourceable(), getOLATResourceableSubPath(), identity);
    }

    /**
	 */
    @Override
    public int deleteRating(UserRating rating) {
        return userRatingsDao.deleteRating(getOLATResourceable(), getOLATResourceableSubPath(), rating);

    }

    /**
	 */
    @Override
    public int deleteAllRatings() {
        return userRatingsDao.deleteAllRatings(getOLATResourceable(), getOLATResourceableSubPath());
    }

    /**
	 */
    @Override
    public int deleteAllRatingsIgnoringSubPath() {
        return userRatingsDao.deleteAllRatingsIgnoringSubPath(getOLATResourceable());
    }

    /**
	 */
    @Override
    public UserRating updateRating(UserRating rating, int newRatingValue) {
        rating = userRatingsDao.updateRating(getOLATResourceable(), getOLATResourceableSubPath(), rating, newRatingValue);
        // do logging
        ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.RATING_UPDATED, getClass(),
                CoreLoggingResourceable.wrap(getOLATResourceable(), OlatResourceableType.feedItem));
        return rating;
    }

}
