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
package org.olat.presentation.course.nodes.iq;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSStatus;
import org.olat.data.qti.QTIResult;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.core.notification.service.TestConfirmation;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ExportResult;
import org.olat.lms.course.nodes.QtiExportEBL;
import org.olat.lms.ims.qti.IQManager;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.lms.qti.QTIResultService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * 
 * Counts how many users have finished their test, but send email to all learners that started the test (some users did not finish their test).<br/>
 * Exports the test results, cleans up after replacing the test and sends confirmation. <br/>
 * 
 * Contains the course, node info and the list of identities which started and/or finished the test. <br/>
 * 
 * Initial Date: 18.10.2012 <br>
 * 
 * @author lavinia
 */
public class TestReplacer {

    private static final Logger LOG = LoggerHelper.getLogger();

    private final QtiExportEBL qtiExportEBL;
    private final QTIResultService qtiResultService;
    private ExportResult exportResult;

    private final ICourse course;
    private final CourseNode courseNode;
    private final ModuleConfiguration moduleConfiguration;

    private List<QTIResult> results;
    private final List<Identity> finishedLearners;// contains identities having results for this test
    private final List<Identity> allLearners;// contains all identities having results for this test, or having this test started

    private TestReplacer(ICourse course, CourseNode courseNode) {
        this.qtiExportEBL = CoreSpringFactory.getBean(QtiExportEBL.class);
        this.qtiResultService = CoreSpringFactory.getBean(QTIResultService.class);

        this.course = course;
        this.courseNode = courseNode;
        this.moduleConfiguration = courseNode.getModuleConfiguration();

        this.finishedLearners = new ArrayList<Identity>();
        List<Identity> inProgressLearners = new ArrayList<Identity>();
        this.allLearners = new ArrayList<Identity>();

        final RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
        // look if there are PASSED entries in changelog
        // if yes create archive of results and all users can be notified about the changed test configuration
        final String repositorySoftKey = (String) courseNode.getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        if (repositorySoftKey != null) {
            final Long repKey = getRepositoryService().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
            this.results = qtiResultService.selectResults(course.getResourceableId(), courseNode.getIdent(), repKey, 1);
            // test was finished from an user
            final boolean passed = (results != null && results.size() > 0) ? true : false;
            if (passed) {
                for (final QTIResult result : results) {
                    final Identity identity = result.getResultSet().getIdentity();
                    if (identity != null && !finishedLearners.contains(identity)) {
                        finishedLearners.add(identity);
                    }
                }
            }

            // test was started and not finished
            // it exists partly results for this test
            final List<Identity> identitiesWithQtiSerEntry = getIQManager().getIdentitiesWithQtiSerEntry(course.getResourceableId(), courseNode.getIdent());

            // add identities with qti.ser entry
            for (final Identity identity : identitiesWithQtiSerEntry) {
                if (!inProgressLearners.contains(identity)) {
                    inProgressLearners.add(identity);
                }
            }

            allLearners.addAll(finishedLearners);
            allLearners.addAll(inProgressLearners);
        }
    }

    public static TestReplacer createTestReplacer(ICourse course, CourseNode courseNode) {
        return new TestReplacer(course, courseNode);
    }

    /**
     * Exports this test results.
     * 
     * @return Returns the exported results file name.
     */
    public String exportResults(final UserRequest ureq, String courseTitle, CourseNode courseNode) {
        if (results != null && results.size() > 0) {
            exportResult = qtiExportEBL.exportResults(ureq.getIdentity(), ureq.getLocale(), results, courseTitle, courseNode);
            return exportResult.getExportFileName();
        }
        return "NOTHING_EXPORTED";
    }

    /**
     * Evaluates whether there are any results in DB (if the test was finished) <br/>
     * 
     * @return Returns true if this test has results for <code>learners</code>.
     */
    public boolean hasStoredResults() {
        return finishedLearners.size() > 0;
    }

    /**
     * Evaluates whether there is any data stored: either results in DB (if the test was finished) <br/>
     * or serialized data (if test was not finished).
     */
    public boolean hasAnyStoredData() {
        return allLearners.size() > 0;
    }

    /**
     * @return How many learners have results for this test.
     */
    public int getFinishedLearnersSize() {
        return finishedLearners.size();
    }

    /**
     * Send confirmation mail to <code>allLearners</code>, if the nodeType is not questionnaire.
     */
    public boolean sendConfirmation(Identity originatorIdentity, String nodeType) {
        boolean confirmationSent = false;
        if (!nodeType.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
            Long repositoryEntryId = course.getCourseEnvironment().getRepositoryEntryId();
            TestConfirmation.TYPE confirmationType = TestConfirmation.TYPE.TEST_REPLACED;
            confirmationSent = getConfirmationLearnService().sendTestReplacedConfirmation(allLearners, originatorIdentity, course.getCourseTitle(), repositoryEntryId,
                    new Long(courseNode.getIdent()), courseNode.getShortTitle(), confirmationType);
        } else {
            confirmationSent = true;
        }

        reset();

        return confirmationSent;
    }

    private void reset() {
        finishedLearners.clear();
        allLearners.clear();
    }

    /**
     * Removes this test results from DB, and references from course, and file system. (The started test have stored serialized data in the file system).
     */
    public void removeTestData() {
        final String repositorySoftKey = (String) moduleConfiguration.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = getRepositoryService().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
        qtiResultService.deleteAllResults(course.getResourceableId(), courseNode.getIdent(), repKey);
        courseNode.removeRepositoryReference();
        final VFSStatus isDeleted = getIQManager().removeQtiSerFiles(course.getResourceableId(), courseNode.getIdent());
        if (!isDeleted.equals(VFSConstants.YES)) {
            // couldn't removed qtiser files
            LOG.warn("Couldn't removed course node folder! Course resourceable id: " + course.getResourceableId() + ", Course node ident: " + courseNode.getIdent());
        }
    }

    private IQManager getIQManager() {
        return CoreSpringFactory.getBean(IQManager.class);
    }

    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryServiceImpl.class);
    }

    private ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }
}
