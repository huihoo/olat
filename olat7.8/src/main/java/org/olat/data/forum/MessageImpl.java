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

package org.olat.data.forum;

import java.util.Date;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.PersistentObject;

/**
 * @author Felix Jost
 */

public class MessageImpl extends PersistentObject implements Message {

    private String title;
    private String body;
    private Message parent;
    private Message threadtop;
    private Forum forum;

    private Identity creator = null;
    private Identity modifier = null;
    private int statusCode;
    private Date lastModified;
    private Integer numOfCharacters;
    private Integer numOfWords;

    /**
     * Default construcor
     */
    public MessageImpl() {
        // nothing to do
    }

    /**
     * @return
     */
    @Override
    public String getBody() {
        return body;
    }

    /**
     * @return
     */
    @Override
    public Identity getCreator() {
        return creator;
    }

    /**
     * @return
     */
    @Override
    public Forum getForum() {
        return forum;
    }

    /**
     * @return
     */
    @Override
    public Identity getModifier() {
        return modifier;
    }

    /**
     * @return
     */
    @Override
    public Message getParent() {
        return parent;
    }

    /**
     * @return
     */
    @Override
    public Message getThreadtop() {
        return threadtop;
    }

    /**
     * @return
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @param string
     */
    @Override
    public void setBody(final String string) {
        body = string;
    }

    /**
     * @param identity
     */
    @Override
    public void setCreator(final Identity identity) {
        creator = identity;
    }

    /**
     * @param forum
     */
    @Override
    public void setForum(final Forum forum) {
        this.forum = forum;
    }

    /**
     * @param identity
     */
    @Override
    public void setModifier(final Identity identity) {
        modifier = identity;
    }

    /**
     * @param message
     */
    @Override
    public void setParent(final Message message) {
        parent = message;
    }

    /**
     * @param message
     */
    @Override
    public void setThreadtop(final Message message) {
        threadtop = message;
    }

    /**
     * @param string
     */
    @Override
    public void setTitle(final String string) {
        title = string;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
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

    @Override
    public Integer getNumOfCharacters() {
        return numOfCharacters;
    }

    @Override
    public void setNumOfCharacters(final Integer numOfCharacters) {
        this.numOfCharacters = numOfCharacters;
    }

    @Override
    public Integer getNumOfWords() {
        return numOfWords;
    }

    @Override
    public void setNumOfWords(final Integer numOfWords) {
        this.numOfWords = numOfWords;
    }

    @Override
    public int compareTo(final Message arg0) {
        // threadtop always is on top!
        if (arg0.getParent() == null) {
            return 1;
        }
        if (getCreationDate().after(arg0.getCreationDate())) {
            return 1;
        }
        return 0;
    }

}
