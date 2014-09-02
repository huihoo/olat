package org.olat.data.commons.database;

/**
 * This Listener can be added to a DB instance in order to be notified about commit/rollbacks
 * <P>
 * Initial Date: 19.08.2008 <br>
 * 
 * @author Stefan
 * @deprecated
 */
public interface ITransactionListener {

    public void handleCommit(DB db);

    public void handleRollback(DB db);

}
