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

package org.olat.lms.qti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.qti.QTIResult;
import org.olat.data.qti.QTIResultDao;
import org.olat.data.qti.QTIResultSet;
import org.olat.lms.user.UserDataDeletable;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description: Useful functions for download
 * 
 * @author Christian Guretzki
 */
@Service("qtiResultService")
public class QTIResultServiceImpl extends BasicManager implements UserDataDeletable, QTIResultService {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    QTIResultDao qtiResultDao;

    /**
     * [Spring] Constructor for QTIResultManager.
     */
    private QTIResultServiceImpl() {
    }

    /**
     * [testing]
     * 
     * @param qtiResultDaoMock
     */
    protected QTIResultServiceImpl(QTIResultDao qtiResultDaoMock) {
        this.qtiResultDao = qtiResultDaoMock;
    }

    /**
     * @see org.olat.lms.qti.QTIResultService#hasResultSets(java.lang.Long, java.lang.String, java.lang.Long)
     */
    @Override
    public boolean hasResultSets(final Long olatResource, final String olatResourceDetail, final Long repositoryRef) {
        return qtiResultDao.hasResultSets(olatResource, olatResourceDetail, repositoryRef);
    }

    /**
     * @see org.olat.lms.qti.QTIResultService#getResultSets(java.lang.Long, java.lang.String, java.lang.Long, org.olat.data.basesecurity.Identity)
     */
    @Override
    public List getResultSets(final Long olatResource, final String olatResourceDetail, final Long repositoryRef, final Identity identity) {
        return qtiResultDao.getResultSets(olatResource, olatResourceDetail, repositoryRef, identity);
    }

    /**
     * @see org.olat.lms.qti.QTIResultService#selectResults(java.lang.Long, java.lang.String, java.lang.Long, int)
     */
    @Override
    public List<QTIResult> selectResults(final Long olatResource, final String olatResourceDetail, final Long repositoryRef, final int type) {
        return qtiResultDao.selectResults(olatResource, olatResourceDetail, repositoryRef, type);
    }

    /**
     * @see org.olat.lms.qti.QTIResultService#deleteAllResults(java.lang.Long, java.lang.String, java.lang.Long)
     */
    @Override
    public int deleteAllResults(final Long olatRes, final String olatResDet, final Long repRef) {
        return qtiResultDao.deleteAllResults(olatRes, olatResDet, repRef);
    }

    /**
     * @see org.olat.lms.qti.QTIResultService#deleteResults(org.olat.data.qti.QTIResultSet)
     */
    @Override
    public void deleteResults(final QTIResultSet qtiResultSet) {
        qtiResultDao.deleteAllResults(qtiResultSet.getOlatResource(), qtiResultSet.getOlatResourceDetail(), qtiResultSet.getRepositoryRef());
    }

    /**
     * translates the answerstring stored in table o_qtiresult
     * 
     * @param answerCode
     * @return translation
     */
    public static Map<String, String> parseResponseStrAnswers(final String answerCode) {
        // calculate the correct answer, if eventually needed
        int modus = 0;
        int startIdentPosition = 0;
        int startCharacterPosition = 0;
        String tempIdent = null;
        final Map<String, String> result = new HashMap<String, String>();
        char c;

        for (int i = 0; i < answerCode.length(); i++) {
            c = answerCode.charAt(i);
            if (modus == 0) {
                if (c == '[') {
                    final String sIdent = answerCode.substring(startIdentPosition, i);
                    if (sIdent.length() > 0) {
                        tempIdent = sIdent;
                        modus = 1;
                    }
                }
            } else if (modus == 1) {
                if (c == '[') {
                    startCharacterPosition = i + 1;
                    modus = 2;
                } else if (c == ']') {
                    startIdentPosition = i + 1;
                    tempIdent = null;
                    modus = 0;
                }
            } else if (modus == 2) {
                if (c == ']') {
                    if (answerCode.charAt(i - 1) != '\\') {
                        final String s = answerCode.substring(startCharacterPosition, i);
                        if (tempIdent != null) {
                            result.put(tempIdent, s.replaceAll("\\\\\\]", "]"));
                        }
                        modus = 1;
                    }
                }
            }
        }
        return result;
    }

    /**
     * translates the answerstring stored in table o_qtiresult
     * 
     * @param answerCode
     * @return translation
     */
    public static List parseResponseLidAnswers(final String answerCode) {
        // calculate the correct answer, if eventually needed
        int modus = 0;
        int startCharacterPosition = 0;
        final List result = new ArrayList();
        char c;

        for (int i = 0; i < answerCode.length(); i++) {
            c = answerCode.charAt(i);
            if (modus == 0) {
                if (c == '[') {
                    modus = 1;
                }
            } else if (modus == 1) {
                if (c == '[') {
                    startCharacterPosition = i + 1;
                    modus = 2;
                } else if (c == ']') {
                    modus = 0;
                }
            } else if (modus == 2) {
                if (c == ']') {
                    if (answerCode.charAt(i - 1) != '\\') {
                        final String s = answerCode.substring(startCharacterPosition, i);
                        result.add(s.replaceAll("\\\\\\]", "]"));
                        modus = 1;
                    }
                }
            }
        }
        return result;
    }

    /**
     * @see org.olat.lms.qti.QTIResultService#deleteUserData(org.olat.data.basesecurity.Identity, java.lang.String)
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        final List qtiResults = qtiResultDao.findQtiResultSets(identity);
        for (final Iterator iter = qtiResults.iterator(); iter.hasNext();) {
            qtiResultDao.deleteResultSet((QTIResultSet) iter.next());
        }
        log.debug("Delete all QTI result data in db for identity=" + identity);
    }

}
