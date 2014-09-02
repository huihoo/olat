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
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * @author Andreas Ch. Kapp
 * @deprecated
 */
public class AuditInterceptor extends EmptyInterceptor implements Interceptor, Serializable {

    private static final Logger log = LoggerHelper.getLogger();

    private int updates;
    private int creates;

    protected AuditInterceptor() {
    }

    @Override
    public Object instantiate(String arg0, EntityMode arg1, Serializable arg2) throws CallbackException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
	 */
    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        // nothing to do here.
    }

    /**
     * org.hibernate.type.Type[])
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (log.isDebugEnabled()) {
            log.debug("\nflush:" + entity + "\npre:" + (previousState == null ? "-" : Arrays.asList(previousState).toString()) + "\ncur:"
                    + (currentState == null ? "-" : Arrays.asList(currentState).toString()));
        }
        /*
         * if (entity instanceof Auditable) { updates++; for (int i = 0; i < propertyNames.length; i++) { if ("lastModified".equals(propertyNames[i])) { currentState[i] =
         * new Date(); return true; } } }
         */
        return false;
    }

    /**
	 */
    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        return false;
    }

    /**
	 */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        // automatically set the creationdate property on Auditable (all OLAT entities implement Auditable)
        if (entity instanceof CreateInfo) {
            creates++;
            for (int i = 0; i < propertyNames.length; i++) {
                if ("creationDate".equals(propertyNames[i])) {
                    // TODO:fj:a = new Timestamp(); should solve this buggy compares between a Timestamp (hibernate reflection set) and the date (set here)
                    state[i] = new Date();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * org.hibernate.type.Type[])
     */
    @Override
    public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        return null;
    }

    /**
	 */
    @Override
    public void postFlush(Iterator entities) {
        if (log.isDebugEnabled()) {
            log.debug("AuditInterceptor - Creations: " + creates + " , Updates: " + updates);
        }
    }

    /**
	 */
    @Override
    public void preFlush(Iterator entities) {
        updates = 0;
        creates = 0;
    }

    /**
	 */
    public Boolean isUnsaved(Object entity) {
        return null;
    }

}
