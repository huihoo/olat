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

package org.olat.data.reference;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.olat.data.commons.database.DB;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceImpl;
import org.olat.data.resource.OLATResourceManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: May 27, 2004
 * 
 * @author Mike Stock Comment:
 */
@Repository("referenceManager")
public class ReferenceDao extends BasicManager {

    private static ReferenceDao INSTANCE;
    @Autowired
    private OLATResourceManager olatResourceManager;
    @Autowired
    private DB db;

    /**
	 * 
	 */
    private ReferenceDao() {
        INSTANCE = this;
    }

    /**
     * @return Singleton.
     */
    @Deprecated
    public static ReferenceDao getInstance() {
        return INSTANCE;
    }

    public void addReference(final OLATResourceable source, final OLATResourceable target, final String userdata) {
        // FIXME:ms:b consider the case where source does not exists yet in the OLATResource db table
        final OLATResourceImpl sourceImpl = (OLATResourceImpl) olatResourceManager.findResourceable(source);
        final OLATResourceImpl targetImpl = (OLATResourceImpl) olatResourceManager.findResourceable(target);
        final ReferenceImpl ref = new ReferenceImpl(sourceImpl, targetImpl, userdata);
        db.saveObject(ref);
    }

    public List<Reference> getReferences(final OLATResourceable source) {
        final OLATResourceImpl sourceImpl = (OLATResourceImpl) olatResourceManager.findResourceable(source);
        if (sourceImpl == null) {
            return new ArrayList(0);
        }

        return db.find("select v from " + ReferenceImpl.class.getName() + " as v where v.source = ?", sourceImpl.getKey(), Hibernate.LONG);
    }

    public List<Reference> getReferencesTo(final OLATResourceable target) {
        final OLATResourceImpl targetImpl = (OLATResourceImpl) olatResourceManager.findResourceable(target);
        if (targetImpl == null) {
            return new ArrayList(0);
        }

        return db.find("select v from " + ReferenceImpl.class.getName() + " as v where v.target = ?", targetImpl.getKey(), Hibernate.LONG);
    }

    public boolean hasReferencesTo(final OLATResourceable target) {
        return (getReferencesTo(target).size() > 0);
    }

    public void delete(final Reference ref) {
        db.deleteObject(ref);
    }

    public void deleteAllReferencesOf(final OLATResource olatResource) {
        for (Reference ref : getReferences(olatResource)) {
            delete(ref);
        }
    }

}
