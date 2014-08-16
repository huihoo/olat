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
package org.olat.data.properties;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.database.DBQuery;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 08.11.2011 <br>
 * 
 * @author cg
 */
@Repository
public class AssessmentPropertyDaoImpl implements AssessmentPropertyDao {

    /**
     * @param identity
     *            the identity for which to properties are to be loaded. if null, the properties of all identities (=all properties of this course) are loaded.
     * @return
     */
    public List loadPropertiesFor(final Identity identity, String resourceableTypeName, Long resourceableId) {
        final StringBuilder sb = new StringBuilder();
        sb.append("from org.olat.data.properties.PropertyImpl as p");
        sb.append(" inner join fetch p.identity as ident where");
        sb.append(" p.resourceTypeName = :restypename");
        sb.append(" and p.resourceTypeId = :restypeid");
        sb.append(" and ( p.name = '").append(ATTEMPTS);
        sb.append("' or p.name = '").append(SCORE);
        sb.append("' or p.name = '").append(PASSED);
        sb.append("' or p.name = '").append(ASSESSMENT_ID);
        sb.append("' or p.name = '").append(COMMENT);
        sb.append("' or p.name = '").append(COACH_COMMENT);
        sb.append("' )");
        if (identity != null) {
            sb.append(" and p.identity = :id");
        }
        final DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
        query.setString("restypename", resourceableTypeName); // course.getResourceableTypeName()
        query.setLong("restypeid", resourceableId.longValue()); // course.getResourceableId().longValue()
        if (identity != null) {
            query.setEntity("id", identity);
        }
        final List properties = query.list();
        return properties;
    }

    public List getAllIdentitiesWithCourseAssessmentData(String resourceableTypeName, Long resourceableId) {
        final StringBuffer query = new StringBuffer();
        query.append("select distinct i from ");
        query.append(" org.olat.data.basesecurity.IdentityImpl as i,");
        query.append(" org.olat.data.properties.PropertyImpl as p");
        query.append(" where i = p.identity and p.resourceTypeName = :resname");
        query.append(" and p.resourceTypeId = :resid");
        query.append(" and p.identity is not null");
        query.append(" and ( p.name = '").append(AssessmentPropertyDao.SCORE);
        query.append("' or p.name = '").append(AssessmentPropertyDao.PASSED);
        query.append("' )");

        final DB db = DBFactory.getInstance();
        final DBQuery dbq = db.createQuery(query.toString());
        // final ICourse course = CourseFactory.loadCourse(ores);
        dbq.setLong("resid", resourceableId.longValue()); // course.getResourceableId().longValue()
        dbq.setString("resname", resourceableTypeName); // course.getResourceableTypeName()

        final List res = dbq.list();
        return res;
    }

}
