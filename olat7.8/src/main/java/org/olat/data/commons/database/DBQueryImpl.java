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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.SecurityGroupMembershipImpl;
import org.olat.data.commons.database.exception.DBRuntimeException;
import org.olat.data.lifecycle.LifeCycleEntry;
import org.olat.data.resource.OLATResourceImpl;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * A <b>DBQueryImpl</b> is a wrapper around a Hibernate Query object.
 * 
 * @author Andreas Ch. Kapp
 * @deprecated
 */
public class DBQueryImpl implements DBQuery {

    private static final Logger log = LoggerHelper.getLogger();

    private Query query = null;
    public final static Map<String, SimpleProbe> listTableStatsMap_ = new HashMap<String, SimpleProbe>();
    public final static Set<String> registeredTables_ = new HashSet<String>();

    static {
        registeredTables_.add(SecurityGroupMembershipImpl.class.getName());
        registeredTables_.add("org.olat.data.group.area.BGAreaImpl");
        registeredTables_.add("org.olat.data.group.BusinessGroupImpl");
        registeredTables_.add(OLATResourceImpl.class.getName());
        registeredTables_.add(LifeCycleEntry.class.getName());
    }

    /**
     * Default construcotr.
     * 
     * @param q
     */
    public DBQueryImpl(final Query q) {
        query = q;
    }

    /**
	 */
    @Override
    public DBQuery setLong(final String string, final long value) {
        query.setLong(string, value);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setString(final String string, final String value) {
        query.setString(string, value);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setTime(final String name, final Date date) {
        query.setTime(name, date);
        return this;
    }

    /**
	 */
    @Override
    public int executeUpdate(final FlushMode nullOrFlushMode) {
        if (nullOrFlushMode != null) {
            query.setFlushMode(nullOrFlushMode);
        }
        return query.executeUpdate();
    }

    /**
	 */
    @Override
    public <T> List<T> list() {
        Codepoint.codepoint(getClass(), "list-entry");
        final long startTime = System.currentTimeMillis();
        try {
            long start = 0;
            if (log.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }
            final List li = query.list();
            if (log.isDebugEnabled()) {
                final long time = (System.currentTimeMillis() - start);
                log.debug("list dbquery (time " + time + ") query " + getQueryString());
            }
            String queryString = query.getQueryString().trim();
            String queryStringToLowerCase = queryString.toLowerCase();
            if (queryStringToLowerCase.startsWith("from ")) {
                queryString = queryString.substring(5).trim();
                queryStringToLowerCase = queryString.toLowerCase();
            } else if (queryStringToLowerCase.startsWith("select ") && (queryStringToLowerCase.contains(" from "))) {
                queryString = queryString.substring(queryStringToLowerCase.indexOf(" from ") + 6).trim();
                queryStringToLowerCase = queryString.toLowerCase();
            } else {
                queryString = null;
            }
            if (queryString != null) {
                final long endTime = System.currentTimeMillis();
                final long diff = endTime - startTime;
                final int wherePos = queryStringToLowerCase.indexOf(" where ");
                if (wherePos != -1) {
                    queryString = queryString.substring(0, wherePos);
                }
                queryString = queryString.trim();
                final StringTokenizer st = new StringTokenizer(queryString, ",");
                while (st.hasMoreTokens()) {
                    String aTable = st.nextToken();
                    aTable = aTable.trim();
                    final int spacePos = aTable.toLowerCase().indexOf(" ");
                    if (spacePos != -1) {
                        aTable = aTable.substring(0, spacePos);
                    }
                    aTable = aTable.trim();
                    SimpleProbe probe = listTableStatsMap_.get(aTable);
                    if (probe == null) {
                        probe = new SimpleProbe();
                        listTableStatsMap_.put(aTable, probe);
                    }
                    probe.addMeasurement(diff);
                    if (!registeredTables_.contains(aTable)) {
                        aTable = "THEREST";
                        probe = listTableStatsMap_.get(aTable);
                        if (probe == null) {
                            probe = new SimpleProbe();
                            listTableStatsMap_.put(aTable, probe);
                        }
                        probe.addMeasurement(diff);
                    }
                    // System.out.println(" A TABLE: "+aTable+" stats: "+probe);
                }
            }
            return li;
        } catch (final HibernateException he) {
            final String msg = "Error in list()";
            throw new DBRuntimeException(msg, he);
        } finally {
            Codepoint.codepoint(getClass(), "list-exit", query);
        }
    }

    /**
	 */
    @Override
    public String[] getNamedParameters() {
        try {
            return query.getNamedParameters();
        } catch (final HibernateException e) {
            throw new DBRuntimeException("GetNamedParameters failed. ", e);
        }
    }

    /**
	 */
    @Override
    public String getQueryString() {
        return query.getQueryString();
    }

    /**
	 */
    @Override
    public Type[] getReturnTypes() {

        try {
            return query.getReturnTypes();
        } catch (final HibernateException e) {
            throw new DBRuntimeException("GetReturnTypes failed. ", e);
        }
    }

    /**
     * @return iterator
     */
    public Iterator iterate() {
        try {
            return query.iterate();
        } catch (final HibernateException e) {
            throw new DBRuntimeException("Iterate failed. ", e);
        }
    }

    /*
     * public ScrollableResults scroll() { try { return query.scroll(); } catch (HibernateException e) { throw new DBRuntimeException("Scroll failed. ", e); } }
     */

    /**
	 */
    @Override
    public DBQuery setBigDecimal(final int position, final BigDecimal number) {
        query.setBigDecimal(position, number);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setBigDecimal(final String name, final BigDecimal number) {
        query.setBigDecimal(name, number);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setBinary(final int position, final byte[] val) {
        query.setBinary(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setBinary(final String name, final byte[] val) {
        query.setBinary(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setBoolean(final int position, final boolean val) {
        query.setBoolean(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setBoolean(final String name, final boolean val) {
        query.setBoolean(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setByte(final int position, final byte val) {
        query.setByte(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setByte(final String name, final byte val) {
        query.setByte(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setCacheable(final boolean cacheable) {
        query.setCacheable(cacheable);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setCacheRegion(final String cacheRegion) {
        query.setCacheRegion(cacheRegion);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setCalendar(final int position, final Calendar calendar) {
        query.setCalendar(position, calendar);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setCalendar(final String name, final Calendar calendar) {
        query.setCalendar(name, calendar);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setCalendarDate(final int position, final Calendar calendar) {
        query.setCalendarDate(position, calendar);
        return this;

    }

    /**
	 */
    @Override
    public DBQuery setCalendarDate(final String name, final Calendar calendar) {
        query.setCalendarDate(name, calendar);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setCharacter(final int position, final char val) {
        query.setCharacter(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setCharacter(final String name, final char val) {
        query.setCharacter(name, val);
        return this;

    }

    /**
	 */
    @Override
    public DBQuery setDate(final int position, final Date date) {
        query.setDate(position, date);
        return this;

    }

    /**
	 */
    @Override
    public DBQuery setDate(final String name, final Date date) {
        query.setDate(name, date);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setDouble(final int position, final double val) {
        query.setDouble(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setDouble(final String name, final double val) {
        query.setDouble(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setEntity(final int position, final Object val) {
        query.setEntity(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setEntity(final String name, final Object val) {
        query.setEntity(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setFirstResult(final int firstResult) {
        query.setFirstResult(firstResult);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setFloat(final int position, final float val) {
        query.setFloat(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setFloat(final String name, final float val) {
        query.setFloat(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setInteger(final int position, final int val) {
        query.setInteger(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setInteger(final String name, final int val) {
        query.setInteger(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setLocale(final int position, final Locale locale) {
        query.setLocale(position, locale);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setLocale(final String name, final Locale locale) {
        query.setLocale(name, locale);
        return this;
    }

    /**
	 */
    @Override
    public void setLockMode(final String alias, final LockMode lockMode) {
        query.setLockMode(alias, lockMode);

    }

    /**
	 */
    @Override
    public DBQuery setLong(final int position, final long val) {
        query.setLong(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setMaxResults(final int maxResults) {
        query.setMaxResults(maxResults);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setParameter(final int position, final Object val, final Type type) {
        query.setParameter(position, val, type);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setParameter(final int position, final Object val) {
        try {
            query.setParameter(position, val);
        } catch (final HibernateException e) {
            throw new DBRuntimeException("DBQuery error. ", e);
        }
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setParameter(final String name, final Object val, final Type type) {
        query.setParameter(name, val, type);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setParameter(final String name, final Object val) {
        try {
            query.setParameter(name, val);
        } catch (final HibernateException e) {
            throw new DBRuntimeException("DBQuery error. ", e);
        }
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setParameterList(final String name, final Collection vals, final Type type) {
        try {
            query.setParameterList(name, vals, type);
        } catch (final HibernateException e) {
            throw new DBRuntimeException("DBQuery error. ", e);
        }
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setParameterList(final String name, final Collection vals) {
        try {
            query.setParameterList(name, vals);
        } catch (final HibernateException e) {
            throw new DBRuntimeException("DBQuery error. ", e);
        }
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setParameterList(final String name, final Object[] vals, final Type type) {
        try {
            query.setParameterList(name, vals, type);
        } catch (final HibernateException e) {
            throw new DBRuntimeException("DBQuery error. ", e);
        }
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setParameterList(final String name, final Object[] vals) {
        try {
            query.setParameterList(name, vals);
        } catch (final HibernateException e) {
            throw new DBRuntimeException("DBQuery error. ", e);
        }
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setProperties(final Object bean) {
        try {
            query.setProperties(bean);
        } catch (final HibernateException e) {
            throw new DBRuntimeException("DBQuery error. ", e);
        }
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setSerializable(final int position, final Serializable val) {
        query.setSerializable(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setSerializable(final String name, final Serializable val) {
        query.setSerializable(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setShort(final int position, final short val) {
        query.setShort(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setShort(final String name, final short val) {
        query.setShort(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setString(final int position, final String val) {
        query.setString(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setText(final int position, final String val) {
        query.setText(position, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setText(final String name, final String val) {
        query.setText(name, val);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setTime(final int position, final Date date) {
        query.setTime(position, date);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setTimeout(final int timeout) {
        query.setTimeout(timeout);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setTimestamp(final int position, final Date date) {
        query.setTimestamp(position, date);
        return this;
    }

    /**
	 */
    @Override
    public DBQuery setTimestamp(final String name, final Date date) {
        query.setTimestamp(name, date);
        return this;
    }

    /**
	 */
    @Override
    public Object uniqueResult() {
        try {
            return query.uniqueResult();
        } catch (final HibernateException e) {
            throw new DBRuntimeException("DBQuery error. ", e);
        }
    }

}
