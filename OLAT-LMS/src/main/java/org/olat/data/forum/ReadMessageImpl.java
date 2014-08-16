package org.olat.data.forum;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.PersistentObject;

public class ReadMessageImpl extends PersistentObject implements ReadMessage {

    private Identity identity;
    private Message message;
    private Forum forum;

    ReadMessageImpl() {
        // default constructor
    }

    @Override
    public Forum getForum() {
        return forum;
    }

    public void setForum(final Forum forum) {
        this.forum = forum;
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(final Identity identity) {
        this.identity = identity;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    public void setMessage(final Message message) {
        this.message = message;
    }

}
