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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.data.commons.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jmx.StatisticsService;
import org.hibernate.stat.Statistics;
import org.hibernate.type.Type;
import org.olat.data.commons.database.exception.DBRuntimeException;
import org.olat.system.commons.Settings;
import org.olat.system.commons.configuration.Destroyable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.testutils.codepoints.server.Codepoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A <b>DB </b> is a central place to get a Hibernate Session. It acts as a facade to the database, transactions and Queries. The hibernateSession is lazy loaded per
 * thread.
 * 
 * @author Andreas Ch. Kapp
 * @author Christian Guretzki
 * @deprecated
 */
public class DBImpl implements DB, Destroyable {

    private static final Logger log = LoggerHelper.getLogger();

    private static final int MAX_DB_ACCESS_COUNT = 500;
    private static DBImpl INSTANCE;
    private SessionFactory sessionFactory = null;

    private final ThreadLocal<ThreadLocalData> data = new ThreadLocal<ThreadLocalData>();
    @Autowired
    private MBeanServer mBeanServer;
    // Max value for commit-counter, values over this limit will be logged.
    private static int maxCommitCounter = 10;

    /**
     * [used by spring]
     */
    private DBImpl() {
        INSTANCE = this;
    }

    /**
     * A <b>ThreadLocalData</b> is used as a central place to store data on a per thread basis.
     * 
     * @author Andreas CH. Kapp
     * @author Christian Guretzki
     */
    private class ThreadLocalData {
        private DBManager manager;
        private boolean initialized = false;
        // count number of db access in beginTransaction, used to log warn 'to many db access in one transaction'
        private int accessCounter = 0;
        // count number of commit in db-session, used to log warn 'Call more than one commit in a db-session'
        private int commitCounter = 0;

        // transaction listeners
        private Set<ITransactionListener> transactionListeners_ = null;

        private ThreadLocalData() {
            // don't let any other class instantiate ThreadLocalData.
        }

        /**
         * @return true if initialized.
         */
        protected boolean isInitialized() {
            return initialized;
        }

        protected DBManager getManager() {
            return manager;
        }

        protected void setInitialized(final boolean b) {
            initialized = b;
        }

        protected void setManager(final DBManager manager) {
            this.manager = manager;
        }

        protected void incrementAccessCounter() {
            this.accessCounter++;
        }

        protected int getAccessCounter() {
            return this.accessCounter;
        }

        protected void resetAccessCounter() {
            this.accessCounter = 0;
        }

        protected void incrementCommitCounter() {
            this.commitCounter++;
        }

        protected int getCommitCounter() {
            return this.commitCounter;
        }

        protected void resetCommitCounter() {
            this.commitCounter = 0;
        }

        protected void addTransactionListener(final ITransactionListener txListener) {
            if (transactionListeners_ == null) {
                transactionListeners_ = new HashSet<ITransactionListener>();
            }
            transactionListeners_.add(txListener);
        }

        protected void removeTransactionListener(final ITransactionListener txListener) {
            if (transactionListeners_ == null) {
                // can't remove then - never mind
                return;
            }
            transactionListeners_.remove(txListener);
        }

        protected void handleCommit(final DB db) {
            if (transactionListeners_ == null) {
                // nobody to be notified
                return;
            }
            for (final Iterator<ITransactionListener> it = transactionListeners_.iterator(); it.hasNext();) {
                final ITransactionListener listener = it.next();
                try {
                    listener.handleCommit(db);
                } catch (final Exception e) {
                    log.warn("ITransactionListener threw exception in handleCommit:", e);
                }
            }
        }

        protected void handleRollback(final DB db) {
            if (transactionListeners_ == null) {
                // nobody to be notified
                return;
            }
            for (final Iterator<ITransactionListener> it = transactionListeners_.iterator(); it.hasNext();) {
                final ITransactionListener listener = it.next();
                try {
                    listener.handleRollback(db);
                } catch (final Exception e) {
                    log.warn("ITransactionListener threw exception in hanldeRollback:", e);
                }
            }
        }
    }

    private void setData(final ThreadLocalData data) {
        this.data.set(data);
    }

    private ThreadLocalData getData() {
        ThreadLocalData tld = data.get();
        if (tld == null) {
            tld = new ThreadLocalData();
            setData(tld);
        }
        return tld;
    }

    protected DBSession getDBSession() {
        final DBManager dbm = getData().getManager();
        if (log.isDebugEnabled() && dbm == null) {
            log.debug("DB manager ist null.", null);
        }
        return (dbm == null) ? null : dbm.getDbSession();
    }

    protected void setManager(final DBManager manager) {
        getData().setManager(manager);
    }

    DBManager getDBManager() {
        return getData().getManager();
    }

    boolean isConnectionOpen() {
        if ((getData().getManager() == null) || (getData().getManager().getDbSession() == null)) {
            return false;
        }
        return getData().getManager().getDbSession().isOpen();
    }

    DBTransaction getTransaction() {
        if ((getData().getManager() == null) || (getData().getManager().getDbSession() == null)) {
            return null;
        }
        return getData().getManager().getDbSession().getTransaction();
    }

    private void createSession() {
        final DBSession dbs = getDBSession();
        if (dbs == null) {
            if (log.isDebugEnabled()) {
                log.debug("createSession start...", null);
            }
            Session session = null;
            Codepoint.codepoint(DBImpl.class, "initializeSession");
            if (log.isDebugEnabled()) {
                log.debug("initializeSession", null);
            }
            try {
                session = sessionFactory.openSession();
            } catch (final HibernateException e) {
                log.error("could not open database session!", e);
            }
            setManager(new DBManager(session));
            getData().resetAccessCounter();
            getData().resetCommitCounter();
        } else if (!dbs.isOpen()) {
            Session session = null;
            try {
                session = sessionFactory.openSession();
            } catch (final HibernateException e) {
                log.error("could not open database session!", e);
            }
            setManager(new DBManager(session));
            getData().resetAccessCounter();
            getData().resetCommitCounter();
        }
        setInitialized(true);
        if (log.isDebugEnabled()) {
            log.debug("createSession end...", null);
        }
    }

    /**
     * Close the database session.
     */
    @Override
    public void closeSession() {
        getData().resetAccessCounter();
        // Note: closeSession() now also checks if the connection is open at all
        // in OLAT-4318 a situation is described where commit() fails and closeSession()
        // is not called at all. that was due to a call to commit() with a session
        // that was closed underneath by hibernate (not noticed by DBImpl).
        // in order to be robust for any similar situation, we check if the
        // connection is open, otherwise we shouldn't worry about doing any commit/rollback anyway
        if (isConnectionOpen() && hasTransaction() && getTransaction().isInTransaction() && !getTransaction().isRolledBack()) {
            if (Settings.isJUnitTest()) {
                if (log.isDebugEnabled()) {
                    log.debug("Call commit", null);
                }
                getTransaction().commit();
                getData().handleCommit(this);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Call commit", null);
                }
                throw new AssertException("Close db-session with un-committed transaction");
            }
        }
        final DBSession s = getDBSession();
        if (s != null) {
            Codepoint.codepoint(DBImpl.class, "closeSession");
            s.close();
            // OLAT-3652 related: on closeSession also set the transaction to null
            s.clearTransaction();
        }

        data.remove();
    }

    @Override
    public void cleanUpSession() {
        if (data.get() == null) {
            return;
        }
        closeSession();
    }

    /**
     * access database via spring and only in data layer!
     * 
     * @return
     */
    @Deprecated
    protected static DBImpl getInstance() {
        return getInstance(true);
    }

    /**
     * Get the DB instance. Initialisation is performed if flag is true.
     * 
     * @param initialize
     * @return the DB instance.
     */
    protected static DBImpl getInstance(final boolean initialize) {

        // OLAT-3621: paranoia check for error state: we need to catch errors at the earliest point possible. OLAT-3621 has a suspected situation
        // where an earlier transaction failed and didn't clean up nicely. To check this, we introduce error checking in getInstance here
        final DBTransaction transaction = INSTANCE.getTransaction();
        if (transaction != null) {
            // Filter Exception form async TaskExecutorThread, there are exception allowed
            if (transaction.isError() && !Thread.currentThread().getName().equals("TaskExecutorThread")) {
                INSTANCE.log.warn("getInstance: Transaction (still?) in Error state: " + transaction.getError(),
                        new Exception("DBImpl begin transaction)", transaction.getError()));
            }
        }

        // if module is not active we return a non-initialized instance and take
        // care that
        // the only cleanup-calls to db.closeSession do nothing
        if (initialize) {
            INSTANCE.createSession();
        }
        return INSTANCE;
    }

    /**
     * Get db instance without checking transaction state
     * 
     * @return
     */
    protected static DBImpl getInstanceForClosing() {
        return INSTANCE;
    }

    /**
     * @return true if tread is initialized.
     */
    boolean threadLocalsInitialized() {
        return getData().isInitialized();
    }

    private void setInitialized(final boolean initialized) {
        getData().setInitialized(initialized);

    }

    boolean isInitialized() {
        return getData().isInitialized();
    }

    /**
     * Call this to begin a transaction .
     * 
     * @param logObject
     *            TODO
     */
    private void beginTransaction(final Object logObject) {
        // OLAT-3621: paranoia check for error state: we need to catch errors at the earliest point possible. OLAT-3621 has a suspected situation
        // where an earlier transaction failed and didn't clean up nicely. To check this, we introduce error checking in getInstance here
        final DBTransaction transaction = INSTANCE.getTransaction();
        if (transaction != null) {
            // Filter Exception form async TaskExecutorThread, there are exception allowed
            if (transaction.isError() && !Thread.currentThread().getName().equals("TaskExecutorThread")) {
                INSTANCE.log.warn("beginTransaction: Transaction (still?) in Error state: " + transaction.getError(), new Exception("DBImpl begin transaction)",
                        transaction.getError()));
            }
        }
        createSession();

        // TODO: 07.01.2009/cg ONLY FOR DEBUGGING 'too many db access in one transaction'
        // increment only non-cachable query
        if (logObject instanceof String) {
            String query = (String) logObject;
            query = query.trim();
            if (!query
                    .startsWith("select count(poi) from org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi, org.olat.data.basesecurity.PolicyImpl as poi,")
                    && !query.startsWith("select count(grp) from org.olat.data.group.BusinessGroupImpl as grp")
                    && !query.startsWith("select count(sgmsi) from  org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi")) {
                // it is no of cached queries
                getData().incrementAccessCounter();
            }
        } else {
            getData().incrementAccessCounter();
        }

        if (getData().getAccessCounter() > MAX_DB_ACCESS_COUNT) {
            log.warn(
                    "beginTransaction bulk-change, too many db access for one transaction, could be a performance problem (add closeSession/createSession in loop) logObject="
                            + logObject, null);
            getData().resetAccessCounter();
        }
        if (log.isDebugEnabled()) {
            log.debug("beginTransaction TEST getDBSession()=" + getDBSession(), null);
        }

        if (!hasTransaction()) {
            // if no transaction exists, start a new one.
            getDBSession().beginDbTransaction();
            if (log.isDebugEnabled()) {
                log.debug("No transaction exists, start a new one.", null);
            }
        } else if (getTransaction() != null && getTransaction().isRolledBack()) {
            log.error("Call beginTransaction but transaction is already rollbacked", null);
        }
        if (log.isDebugEnabled()) {
            log.debug("beginTransaction TEST hasTransaction()=" + hasTransaction(), null);
        }
    }

    private boolean contains(final Object object) {
        return getDBManager().contains(object);
    }

    /**
     * Create a DBQuery
     * 
     * @param query
     * @return DBQuery
     */
    @Override
    public DBQuery createQuery(final String query) {
        beginTransaction(query);
        return getDBManager().createQuery(query);
    }

    /**
     * Delete an object.
     * 
     * @param object
     */
    @Override
    public void deleteObject(final Object object) {
        beginTransaction(object);
        getDBManager().deleteObject(getTransaction(), object);
    }

    /**
     * Deletion query.
     * 
     * @param query
     * @param value
     * @param type
     * @return nr of deleted rows
     */
    @Override
    public int delete(final String query, final Object value, final Type type) {
        beginTransaction(query);
        return getDBManager().delete(getTransaction(), query, value, type);
    }

    /**
     * Deletion query.
     * 
     * @param query
     * @param values
     * @param types
     * @return nr of deleted rows
     */
    @Override
    public int delete(final String query, final Object[] values, final Type[] types) {
        beginTransaction(query);
        return getDBManager().delete(getTransaction(), query, values, types);
    }

    /**
     * Find objects based on query
     * 
     * @param query
     * @param value
     * @param type
     * @return List of results.
     */
    @Override
    public List find(final String query, final Object value, final Type type) {
        beginTransaction(query);
        return getDBManager().find(getTransaction(), query, value, type);
    }

    /**
     * Find objects based on query
     * 
     * @param query
     * @param values
     * @param types
     * @return List of results.
     */
    @Override
    public List find(final String query, final Object[] values, final Type[] types) {
        beginTransaction(query);
        return getDBManager().find(getTransaction(), query, values, types);
    }

    /**
     * Find objects based on query
     * 
     * @param query
     * @return List of results.
     */
    @Override
    public List find(final String query) {
        beginTransaction(query);
        return getDBManager().find(getTransaction(), query);
    }

    /**
     * Find an object.
     * 
     * @param theClass
     * @param key
     * @return Object, if any found. Null, if non exist.
     */
    @Override
    public Object findObject(final Class theClass, final Long key) {
        beginTransaction(key);
        return getDBManager().findObject(theClass, key);
    }

    /**
     * Load an object.
     * 
     * @param theClass
     * @param key
     * @return Object.
     */
    @Override
    public Object loadObject(final Class theClass, final Long key) {
        beginTransaction(key);
        return getDBManager().loadObject(getTransaction(), theClass, key);
    }

    /**
     * Save an object.
     * 
     * @param object
     */
    @Override
    public void saveObject(final Object object) {
        beginTransaction(object);
        getDBManager().saveObject(getTransaction(), object);
    }

    /**
     * Update an object.
     * 
     * @param object
     */
    @Override
    public void updateObject(final Object object) {
        beginTransaction(object);
        getDBManager().updateObject(getTransaction(), object);
    }

    /**
     * Get any errors from a previous DB call.
     * 
     * @return Exception, if any.
     */
    public Exception getError() {
        if (hasTransaction()) {
            return getTransaction().getError();
        } else {
            return getDBManager().getLastError();
        }
    }

    /**
     * @return True if any errors occured in the previous DB call.
     */
    @Override
    public boolean isError() {
        if (hasTransaction()) {
            return getTransaction().isError();
        } else {
            return getDBManager() == null ? false : getDBManager().isError();
        }

    }

    boolean hasTransaction() {
        return null == getTransaction() ? false : getTransaction().isInTransaction();
    }

    /**
     * @return a JDBC java.sql.Connection.
     */
    protected Connection getConnection() {
        return getData().getManager().getConnection();
    }

    /**
     * see DB.loadObject(Persistable persistable, boolean forceReloadFromDB)
     * 
     * @param persistable
     * @return the loaded object
     */
    @Override
    public Persistable loadObject(final Persistable persistable) {
        return loadObject(persistable, false);
    }

    /**
     * loads an object if needed. this makes sense if you have an object which had been generated in a previous hibernate session AND you need to access a Set or a
     * attribute which was defined as a proxy.
     * 
     * @param persistable
     *            the object which needs to be reloaded
     * @param forceReloadFromDB
     *            if true, force a reload from the db (e.g. to catch up to an object commited by another thread which is still in this thread's session cache
     * @return the loaded Object
     */
    @Override
    public Persistable loadObject(final Persistable persistable, final boolean forceReloadFromDB) {
        if (persistable == null) {
            throw new AssertException("persistable must not be null");
        }
        beginTransaction(persistable);
        final Persistable ret;
        final Class theClass = persistable.getClass();
        if (forceReloadFromDB) {
            // we want to reload it from the database.
            // there are 3 scenarios possible:
            // a) the object is not yet in the hibernate cache
            // b) the object is in the hibernate cache
            // c) the object is detached and there is an object with the same id in the hibernate cache

            if (contains(persistable)) {
                // case b - then we can use evict and load
                getDBManager().evict(persistable);
                return (Persistable) loadObject(theClass, persistable.getKey());
            } else {
                // case a or c - unfortunatelly we can't distinguish these two cases
                // and session.refresh(Object) doesn't work.
                // the only scenario that works is load/evict/load
                final Persistable attachedObj = (Persistable) loadObject(theClass, persistable.getKey());
                getDBManager().evict(attachedObj);
                return (Persistable) loadObject(theClass, persistable.getKey());
            }
        } else if (!contains(persistable)) {
            // forceReloadFromDB is false - hence it is OK to take it from the cache if it would be there
            // now this object directly is not in the cache, but it's possible that the object is detached
            // and there is an object with the same id in the hibernate cache.
            // therefore the following loadObject can either return it from the cache or load it from the DB
            return (Persistable) loadObject(theClass, persistable.getKey());
        } else {
            // nothing to do, return the same object
            return persistable;
        }
    }

    @Override
    public void commitAndCloseSession() {
        try {
            if (needsCommit()) {
                commit();
            }
        } finally {
            try {
                // double check: is the transaction still open? if yes, is it not rolled-back? if yes, do a rollback now!
                if (isConnectionOpen() && hasTransaction() && getTransaction().isInTransaction() && !getTransaction().isRolledBack()) {
                    log.error("commitAndCloseSession: commit seems to have failed, transaction still open. Doing a rollback!", new Exception("commitAndCloseSession"));
                    rollback();
                }
            } finally {
                closeSession();
            }
        }
    }

    @Override
    public void rollbackAndCloseSession() {
        try {
            rollback();
        } finally {
            closeSession();
        }
    }

    /**
     * Call this to commit a transaction opened by beginTransaction().
     */
    @Override
    public void commit() {
        if (log.isDebugEnabled()) {
            log.debug("commit start...", null);
        }
        try {
            if (isConnectionOpen() && hasTransaction() && getTransaction().isInTransaction()) {
                if (log.isDebugEnabled()) {
                    log.debug("has Transaction and is in Transaction => commit", null);
                }
                getData().incrementCommitCounter();
                if (log.isDebugEnabled()) {
                    if ((maxCommitCounter != 0) && (getData().getCommitCounter() > maxCommitCounter)) {
                        log.info("Call too many commit in a db-session, commitCounter=" + getData().getCommitCounter() + "; could be a performance problem", null);
                    }
                }
                getTransaction().commit();
                getData().handleCommit(this);
                if (log.isDebugEnabled()) {
                    log.debug("Commit DONE hasTransaction()=" + hasTransaction(), null);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Call commit without starting transaction", null);
                }
            }
        } catch (final Error er) {
            log.error("Uncaught Error in DBImpl.commit.", er);
            throw er;
        } catch (final Exception e) {
            // Filter Exception form async TaskExecutorThread, there are exception allowed
            if (!Thread.currentThread().getName().equals("TaskExecutorThread")) {
                log.warn("Caught Exception in DBImpl.commit.", e);
            }
            // Error when trying to commit
            try {
                if (hasTransaction() && !getTransaction().isRolledBack()) {
                    getTransaction().rollback();
                    getData().handleRollback(this);
                }
            } catch (final Error er) {
                log.error("Uncaught Error in DBImpl.commit.catch(Exception).", er);
                throw er;
            } catch (final Exception ex) {
                log.warn("Could not rollback transaction after commit!", ex);
                throw new DBRuntimeException("rollback after commit failed", e);
            }
            throw new DBRuntimeException("commit failed, rollback transaction", e);
        }
    }

    /**
     * Call this to rollback current changes.
     */
    @Override
    public void rollback() {
        if (log.isDebugEnabled()) {
            log.debug("rollback start...", null);
        }
        try {
            // see closeSession() and OLAT-4318: more robustness with commit/rollback/close, therefore
            // we check if the connection is open at this stage at all
            if (isConnectionOpen() && hasTransaction() && getTransaction().isInTransaction()) {
                if (log.isDebugEnabled()) {
                    log.debug("Call rollback", null);
                }
                getTransaction().rollback();
                getData().handleRollback(this);
            }
        } catch (final Exception ex) {
            log.warn("Could not rollback transaction!", ex);
            throw new DBRuntimeException("rollback failed", ex);
        }
    }

    /**
     * Statistics must be enabled first, when you want to use it.
     * 
     * @return Return Hibernates statistics object.
     */
    @Override
    public Statistics getStatistics() {
        return sessionFactory.getStatistics();
    }

    /**
     * Register StatisticsService as MBean for JMX support.
     * 
     * @param mySessionFactory
     */
    private void registerStatisticsServiceAsMBean(final SessionFactory mySessionFactory) {
        if (mySessionFactory == null) {
            throw new AssertException("Can not register StatisticsService as MBean, SessionFactory is null");
        }
        try {
            final Hashtable<String, String> tb = new Hashtable<String, String>();
            tb.put("type", "statistics");
            tb.put("sessionFactory", "HibernateStatistics");
            final ObjectName on = new ObjectName("org.olat.data.persistance", tb);
            final StatisticsService stats = new StatisticsService();
            stats.setSessionFactory(mySessionFactory);
            mBeanServer.registerMBean(stats, on);
        } catch (final MalformedObjectNameException e) {
            log.warn("JMX-Error : Can not register as MBean, MalformedObjectNameException=", e);
        } catch (final InstanceAlreadyExistsException e) {
            log.warn("JMX-Error : Can not register as MBean, InstanceAlreadyExistsException=", e);
        } catch (final MBeanRegistrationException e) {
            log.warn("JMX-Error : Can not register as MBean, MBeanRegistrationException=", e);
        } catch (final NotCompliantMBeanException e) {
            log.warn("JMX-Error : Can not register as MBean, NotCompliantMBeanException=", e);
        } catch (final NoSuchBeanDefinitionException e) {
            log.warn("JMX-Error : Can not register as MBean, NoSuchBeanDefinitionException=", e);
        }
    }

    /**
	 */
    @Override
    public void intermediateCommit() {
        this.commit();
        getData().handleCommit(this);
        this.closeSession();
    }

    @Override
    public void addTransactionListener(final ITransactionListener listener) {
        getData().addTransactionListener(listener);
    }

    @Override
    public void removeTransactionListener(final ITransactionListener listener) {
        getData().removeTransactionListener(listener);
    }

    /**
	 */
    public boolean needsCommit() {
        // see closeSession() and OLAT-4318: more robustness with commit/rollback/close, therefore
        // we check if the connection is open at this stage at all
        return isConnectionOpen() && hasTransaction() && !getTransaction().isRolledBack() && getTransaction().isInTransaction();
    }

    /**
     * [used by spring]
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void destroy() {
        // clean up registered drivers to prevent messages like
        // The web application [/olat] registered the JBDC driver [org.hsqldb.jdbc.JDBCDriver] but failed to unregister...
        final Enumeration<Driver> registeredDrivers = DriverManager.getDrivers();
        while (registeredDrivers.hasMoreElements()) {
            try {
                DriverManager.deregisterDriver(registeredDrivers.nextElement());
            } catch (final SQLException e) {
                log.error("Could not unregister database driver.", e);
            }
        }
    }
}
