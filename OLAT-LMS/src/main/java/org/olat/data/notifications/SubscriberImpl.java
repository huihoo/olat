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

package org.olat.data.notifications;

import java.util.Date;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.PersistentObject;

/**
 * Description: <br>
 * TODO: Felix Jost Class Description for Subscriber
 * <P>
 * Initial Date: 21.10.2004 <br>
 * 
 * @author Felix Jost
 */
public class SubscriberImpl extends PersistentObject implements Subscriber {
    // reference to the subscribed publisher
    private Publisher publisher;

    // the user this subscription belongs to
    private Identity identity;

    // when the user latest received an email concering this subscription; may be null if no email has been sent yet
    private Date latestEmailed;

    private Date lastModified;

    /**
     * for hibernate only
     */
    protected SubscriberImpl() {
        //
    }

    /**
     * @param persistedPublisher
     * @param listener
     */
    SubscriberImpl(final Publisher persistedPublisher, final Identity listener) {
        publisher = persistedPublisher;
        identity = listener;
    }

    /**
     * @return the identity
     */
    @Override
    public Identity getIdentity() {
        return identity;
    }

    /**
     * @param identity
     */
    @Override
    public void setIdentity(final Identity identity) {
        this.identity = identity;
    }

    /**
	 */
    @Override
    public Date getLatestEmailed() {
        return latestEmailed;
    }

    /**
	 */
    @Override
    public void setLatestEmailed(final Date latestEmailed) {
        this.latestEmailed = latestEmailed;
    }

    /**
     * @return the publisher
     */
    @Override
    public Publisher getPublisher() {
        return publisher;
    }

    /**
     * @param publisher
     */
    @Override
    public void setPublisher(final Publisher publisher) {
        this.publisher = publisher;
    }

    /**
	 */
    @Override
    public Date getLastModified() {
        return lastModified;
    }

    /**
	 */
    @Override
    public void setLastModified(final Date date) {
        this.lastModified = date;
    }

}
