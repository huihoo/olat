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

import java.util.List;

import org.olat.data.basesecurity.Identity;

/**
 * TODO: Class Description for QtiResultDao
 * 
 * <P>
 * Initial Date: 06.06.2011 <br>
 * 
 * @author guretzki
 */
public interface QTIResultDao {

    /**
     * @param olatResource
     * @param olatResourceDetail
     * @param repositoryRef
     * @return True if true, false otherwise.
     */
    public abstract boolean hasResultSets(final Long olatResource, final String olatResourceDetail, final Long repositoryRef);

    /**
     * Get the resulkt sets.
     * 
     * @param olatResource
     * @param olatResourceDetail
     * @param repositoryRef
     * @param identity
     *            May be null
     * @return List of resultsets
     */
    public abstract List getResultSets(final Long olatResource, final String olatResourceDetail, final Long repositoryRef, final Identity identity);

    /**
     * selects all resultsets of a IQCourseNode of a particular course
     * 
     * @param olatResource
     * @param olatResourceDetail
     * @param repositoryRef
     * @return List of QTIResult objects
     */
    public abstract List<QTIResult> selectResults(final Long olatResource, final String olatResourceDetail, final Long repositoryRef, final int type);

    /**
     * deletes all Results and ResultSets of a test, selftest or survey
     * 
     * @param olatRes
     * @param olatResDet
     * @param repRef
     * @return deleted ResultSets
     */
    public abstract int deleteAllResults(final Long olatRes, final String olatResDet, final Long repRef);

    /**
     * Deletes all Results and ResultSets for certain QTI-ResultSet.
     * 
     * @param qtiResultSet
     */
    public abstract void deleteResults(final QTIResultSet qtiResultSet);

    /**
     * Find all ResultSets for certain identity.
     * 
     * @param identity
     * @param assessmentID
     * @return
     */
    public abstract List findQtiResultSets(final Identity identity);

    /**
     * Delete all qti-results and qti-result-set entry for certain result-set.
     * 
     * @param rSet
     */
    public void deleteResultSet(final QTIResultSet rSet);

}
