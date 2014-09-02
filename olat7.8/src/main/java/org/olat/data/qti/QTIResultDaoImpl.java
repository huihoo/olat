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

package org.olat.data.qti;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.user.UserConstants;
import org.olat.system.commons.manager.BasicManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description: Useful functions for download
 * 
 * @author Alexander Schneider, Christian Guretzki
 */
@Repository
public class QTIResultDaoImpl extends BasicManager implements QTIResultDao {
    @Autowired
    DB db;

    /**
     * [spring]
     */
    private QTIResultDaoImpl() {
    }

    /**
     * @see org.olat.data.qti.QTIResultDao#hasResultSets(java.lang.Long, java.lang.String, java.lang.Long)
     */
    @Override
    public boolean hasResultSets(final Long olatResource, final String olatResourceDetail, final Long repositoryRef) {
        return (getResultSets(olatResource, olatResourceDetail, repositoryRef, null).size() > 0);
    }

    /**
     * @see org.olat.data.qti.QTIResultDao#getResultSets(java.lang.Long, java.lang.String, java.lang.Long, org.olat.data.basesecurity.Identity)
     */
    @Override
    public List getResultSets(final Long olatResource, final String olatResourceDetail, final Long repositoryRef, final Identity identity) {
        final Long olatRes = olatResource;
        final String olatResDet = olatResourceDetail;
        final Long repRef = repositoryRef;
        final StringBuilder slct = new StringBuilder();
        slct.append("select rset from ");
        slct.append("QTIResultSet rset ");
        slct.append("where ");
        slct.append("rset.olatResource=? ");
        slct.append("and rset.olatResourceDetail=? ");
        slct.append("and rset.repositoryRef=? ");
        if (identity != null) {
            slct.append("and rset.identity.key=? ");
            return db.find(slct.toString(), new Object[] { olatRes, olatResDet, repRef, identity.getKey() }, new Type[] { Hibernate.LONG, Hibernate.STRING,
                    Hibernate.LONG, Hibernate.LONG });
        } else {
            return db.find(slct.toString(), new Object[] { olatRes, olatResDet, repRef }, new Type[] { Hibernate.LONG, Hibernate.STRING, Hibernate.LONG });
        }
    }

    /**
     * @see org.olat.data.qti.QTIResultDao#selectResults(java.lang.Long, java.lang.String, java.lang.Long, int)
     */
    @Override
    public List<QTIResult> selectResults(final Long olatResource, final String olatResourceDetail, final Long repositoryRef, final int type) {
        final Long olatRes = olatResource;
        final String olatResDet = olatResourceDetail;
        final Long repRef = repositoryRef;
        // join with user to sort by name
        final StringBuilder slct = new StringBuilder();
        slct.append("select res from ");
        slct.append("QTIResultSet rset, ");
        slct.append("QTIResult res, ");
        slct.append("org.olat.data.basesecurity.Identity identity, ");
        slct.append("UserImpl usr ");
        slct.append("where ");
        slct.append("rset.key = res.resultSet ");
        slct.append("and rset.identity = identity.key ");
        slct.append("and identity.user = usr.key ");
        slct.append("and rset.olatResource=? ");
        slct.append("and rset.olatResourceDetail=? ");
        slct.append("and rset.repositoryRef=? ");
        // 1 -> iqtest, 2 -> iqself
        if (type == 1 || type == 2) {
            slct.append("order by usr.properties['").append(UserConstants.LASTNAME).append("'] , rset.assessmentID, res.itemIdent");
        } else {
            slct.append("order by rset.creationDate, rset.assessmentID, res.itemIdent");
        }

        @SuppressWarnings("unchecked")
        final List<QTIResult> results = db.find(slct.toString(), new Object[] { olatRes, olatResDet, repRef }, new Type[] { Hibernate.LONG, Hibernate.STRING,
                Hibernate.LONG });

        return results;
    }

    /**
     * @see org.olat.data.qti.QTIResultDao#deleteAllResults(java.lang.Long, java.lang.String, java.lang.Long)
     */
    @Override
    public int deleteAllResults(final Long olatRes, final String olatResDet, final Long repRef) {
        final StringBuilder slct = new StringBuilder();
        slct.append("select rset from ");
        slct.append("QTIResultSet rset ");
        slct.append("where ");
        slct.append("rset.olatResource=? ");
        slct.append("and rset.olatResourceDetail=? ");
        slct.append("and rset.repositoryRef=? ");

        List results = null;
        results = db.find(slct.toString(), new Object[] { olatRes, olatResDet, repRef }, new Type[] { Hibernate.LONG, Hibernate.STRING, Hibernate.LONG });

        final String delRes = "from res in class QTIResult where res.resultSet.key = ?";
        final String delRset = "from rset in class QTIResultSet where rset.key = ?";

        int deletedRset = 0;

        for (final Iterator iter = results.iterator(); iter.hasNext();) {
            final QTIResultSet rSet = (QTIResultSet) iter.next();
            final Long rSetKey = rSet.getKey();
            db.delete(delRes, rSetKey, Hibernate.LONG);
            db.delete(delRset, rSetKey, Hibernate.LONG);
            deletedRset++;
        }
        return deletedRset;
    }

    /**
     * @see org.olat.data.qti.QTIResultDao#deleteResults(org.olat.data.qti.QTIResultSet)
     */
    @Override
    public void deleteResults(final QTIResultSet qtiResultSet) {
        deleteAllResults(qtiResultSet.getOlatResource(), qtiResultSet.getOlatResourceDetail(), qtiResultSet.getRepositoryRef());
    }

    /**
     * @see org.olat.data.qti.QTIResultDao#findQtiResultSets(org.olat.data.basesecurity.Identity)
     */
    @Override
    public List findQtiResultSets(final Identity identity) {
        return db.find("from q in class QTIResultSet where q.identity =?", identity.getKey(), Hibernate.LONG);
    }

    /**
     * Delete all qti-results and qti-result-set entry for certain result-set.
     * 
     * @param rSet
     */
    public void deleteResultSet(final QTIResultSet rSet) {
        final Long rSetKey = rSet.getKey();
        db.delete("from res in class QTIResult where res.resultSet.key = ?", rSetKey, Hibernate.LONG);
        db.delete("from rset in class QTIResultSet where rset.key = ?", rSetKey, Hibernate.LONG);
    }

}
