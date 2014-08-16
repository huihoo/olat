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
package org.olat.data.commons.dao;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Repository
@Scope("prototype")
public class GenericDaoImpl<T> implements GenericDao<T> {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    @Qualifier("daoSessionFactory")
    SessionFactory sessionFactory;

    Class<T> type;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    // TODO: DO WE NEED THIS METHOD or should we call 'new Entity()'
    @Override
    public T create() {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new DataRuntimeException(type, "Could not create instance", e.getCause());
        } catch (IllegalAccessException e) {
            throw new DataRuntimeException(type, "Could not create instance", e.getCause());
        }
    }

    @Override
    /**
     * see <code>org.hibernate.Session</code> for javadoc.
     * <br/>
     *  Remove a persistent instance from the datastore. The argument may be an instance associated with the <br/>
     *  receiving Session or a transient instance with an identifier associated with existing persistent state. <br/>
     *  This operation cascades to associated instances if the association is mapped with cascade="delete". 
     */
    public void delete(T entity) {
        log.debug("delete [" + entity + "]");
        getCurrentSession().delete(entity);
        // getCurrentSession().flush();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T findById(Long id) {
        return (T) getCurrentSession().get(type, id);
    }

    @Override
    public T save(T entity) {
        log.debug("save [" + entity + "]");
        getCurrentSession().save(entity);
        // outcommented flush since causes performance problems at publishEvents
        // getCurrentSession().flush(); //It is important to flush after save in case of any ConstraintViolationException could occur.
        return entity;
    }

    @Override
    public T update(T entity) {
        log.debug("update [" + entity + "]");
        getCurrentSession().update(entity);
        return entity;
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        Criteria criteria = getCurrentSession().createCriteria(type);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public Criteria createCriteria() {
        return getCurrentSession().createCriteria(type);
    }

    @SuppressWarnings("unchecked")
    public List<T> findByCriteria(Map<String, Object> restrictionNameValues) {
        Criteria criteria = getCurrentSession().createCriteria(type);
        Iterator<String> keys = restrictionNameValues.keySet().iterator();
        while (keys.hasNext()) {
            String restrictionName = keys.next();
            Object restrictionValue = restrictionNameValues.get(restrictionName);
            criteria = criteria.add(Restrictions.eq(restrictionName, restrictionValue));
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public Query getNamedQuery(String name) {
        return getCurrentSession().getNamedQuery(name);
    }

    // TODO: Ideas for other generic methods implemented when needed
    // - T findById(Long id); // Returns null if entity not found. ??? Bessere Idee ?
    // - List<T> findByCriteria(Criteria criteria);
    // - List<T> findByCriteria(int firstResult, int maxResults, Criteria criteria);
    // - List<T> findAll();
    // - List<T> findAll(int firstResult, int maxResults)
    // - int count();
    // - boolean contains(T entity)
    // - T insert(T entity);
    // - T update(T entity); // oder save ???
    // - void delete(T entity);
    // - void deleteByCriteria(Criteria criteria); // noetig ? wuerde ich im Moment noch weglassen
    // - load vs find (feedback BB)
    // - merge (feedback BB) <?> too low-level?
    // - refresh (feedback BB) <?> too low-level?

    // TODO: REVIEW cg/5.3.2012 Refactoring GenericDao without passing type in method calls
    public void setType(Class<T> type) {
        this.type = type;
    }

    // TODO: REVIEW INPUT: bb/09.03.2012 - created generic method for named query
    @Override
    public List<T> getNamedQueryListResult(String queryName, Map<String, Object> queryParameters) {
        return getQueryList(queryName, queryParameters);
    }

    @Override
    public List<Long> getNamedQueryEntityIds(String queryName, Map<String, Object> queryParameters) {
        return getQueryList(queryName, queryParameters);
    }

    @SuppressWarnings("unchecked")
    private <K> List<K> getQueryList(String queryName, Map<String, Object> queryParameters) {
        Query query = getNamedQuery(queryName);
        for (String key : queryParameters.keySet()) {
            /** TODO: REVIEW PUBLISH PERFORMANCE (used general solution when parameter is collection): bb/11.03.2012 **/
            if (queryParameters.get(key) instanceof Collection) {
                if (!((Collection<T>) queryParameters.get(key)).isEmpty()) {
                    query.setParameterList(key, ((Collection<T>) queryParameters.get(key)));
                }
            } else {
                query.setParameter(key, queryParameters.get(key));
            }
        }
        return query.list();
    }

    @Override
    public Iterator<T> getNamedQueryIteratorResult(String queryName, Map<String, Object> queryParameters) {
        return getQueryIterator(queryName, queryParameters);
    }

    @SuppressWarnings("unchecked")
    private <K> Iterator<K> getQueryIterator(String queryName, Map<String, Object> queryParameters) {
        Query query = getNamedQuery(queryName);

        for (String key : queryParameters.keySet()) {
            query.setParameter(key, queryParameters.get(key));
        }
        return query.iterate();
    }

}
