package org.olat.data.forum;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.CreateInfo;
import org.olat.data.commons.database.Persistable;

/**
 * Description:<br>
 * TODO: Lavinia Dumitrescu Class Description for ReadMessage
 * <P>
 * Initial Date: 14.03.2008 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public interface ReadMessage extends CreateInfo, Persistable {

    public abstract Identity getIdentity();

    public abstract Forum getForum();

    public abstract Message getMessage();

}
