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
package org.olat.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.olat.data.basesecurity.IdentityITCase;
import org.olat.data.commons.database.DBITCase;
import org.olat.data.commons.fileutil.FileUtilsITCase;
import org.olat.data.commons.fileutil.ZipUtilITCase;
import org.olat.data.coordinate.lock.LockITCase;
import org.olat.data.coordinate.lock.PLockITCase;
import org.olat.data.coordinate.lock.SingleVMLockerTest;
import org.olat.data.course.statistic.ITCaseLoggingVersionManagerImpl;
import org.olat.data.infoMessage.InfoManagerITCase;
import org.olat.data.lifecycle.LifeCycleManagerITCase;
import org.olat.data.note.NoteITCase;
import org.olat.data.resource.OLATResourceManagerITCase;
import org.olat.data.resource.ReferenceManagerITCase;
import org.olat.data.tagging.SimpleTagProposalManagerITCase;
import org.olat.data.tagging.TaggingManagerITCase;
import org.olat.lms.basesecurity.BaseSecurityITCase;
import org.olat.lms.basesecurity.SecurityManagerITCase;
import org.olat.lms.bookmark.BookmarkServiceITCase;
import org.olat.lms.calendar.CalendarUtilsITCase;
import org.olat.lms.calendar.ICalFileCalendarManagerITCase;
import org.olat.lms.catalog.CatalogServiceImplTest;
import org.olat.lms.commentandrate.UserCommentsAndRatingsITCase;
import org.olat.lms.commons.textservice.WordCountITCase;
import org.olat.lms.coordinate.LockingServiceITCase;
import org.olat.lms.course.EnrollmentManagerITCase;
import org.olat.lms.course.ITCaseDeployableRepositoryExport;
import org.olat.lms.course.assessment.AssessmentManagerITCase;
import org.olat.lms.course.auditing.UserNodeAuditManagerITCase;
import org.olat.lms.course.config.CourseConfigManagerImplITCase;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerITCase;
import org.olat.lms.forum.ForumServiceITCase;
import org.olat.lms.group.BusinessGroupITCase;
import org.olat.lms.group.BusinessGroupManagerImplITCase;
import org.olat.lms.group.learn.CourseGroupManagementITCase;
import org.olat.lms.ims.cp.CPManagerITCase;
import org.olat.lms.instantmessaging.IMPrefsUnitITCase;
import org.olat.lms.instantmessaging.IMUnitITCase;
import org.olat.lms.instantmessaging.IMUnitITCaseWithoutOLAT;
import org.olat.lms.marking.MarkingITCase;
import org.olat.lms.portfolio.EPArtefactManagerITCase;
import org.olat.lms.portfolio.EPFrontendManagerITCase;
import org.olat.lms.portfolio.EPStructureManagerITCase;
import org.olat.lms.portfolio.EPStructureToArtefactITCase;
import org.olat.lms.portfolio.PortfolioModuleITCase;
import org.olat.lms.properties.PropertyITCase;
import org.olat.lms.registration.RegistrationServiceITCase;
import org.olat.lms.security.authentication.ldap.LDAPLoginITCase;
import org.olat.lms.security.authentication.shibboleth.ShibbolethAttributeTest;
import org.olat.lms.upgrade.UpgradeDefinitionITCase;
import org.olat.lms.user.UserServiceITCase;
import org.olat.lms.user.administration.delete.UserDeletionManagerITCase;
import org.olat.lms.webfeed.FeedManagerITCaseWithMocking;
import org.olat.lms.webfeed.FeedManagerImplITCase;
import org.olat.lms.wiki.WikiUnitITCase;
import org.olat.presentation.calendar.components.WeeklyCalendarComponentITCase;
import org.olat.presentation.wiki.versioning.diff.CookbookDiffTest;
import org.olat.system.commons.configuration.CustomPropertyPlaceholderITCase;
import org.olat.system.coordinate.CoordinatorITCase;
import org.olat.system.coordinate.jms.JMSITCase;
import org.olat.test.scheduler.AnnotationSchedulerITCase;

/**
 * Description:<br>
 * JUnit suite runner There are basically three types of tests:<br>
 * <ul>
 * <li>Tests that extend from the olatTestCase (testcase loads a full olat before running the tests -- very slow and is an integration test)
 * <li>Tests that load their own little spring context with @ContextConfiguration (that's how it should be done)
 * <li>Tests that do not need any Spring context As tests with @ContextConfiguration can taint the context from olattestcase they must be placed on the end of the list!
 * </ul>
 * <P>
 * Initial Date: 15.02.2010 <br>
 * 
 * @author guido
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ FileUtilsITCase.class, ZipUtilITCase.class, BaseSecurityITCase.class, NoteITCase.class, UserServiceITCase.class,
        WeeklyCalendarComponentITCase.class, CalendarUtilsITCase.class, ICalFileCalendarManagerITCase.class, LifeCycleManagerITCase.class, JMSITCase.class,
        LockITCase.class, CoordinatorITCase.class, UserDeletionManagerITCase.class, BusinessGroupITCase.class, BusinessGroupManagerImplITCase.class, PLockITCase.class,
        ReferenceManagerITCase.class, OLATResourceManagerITCase.class, SecurityManagerITCase.class, IMUnitITCase.class, IMPrefsUnitITCase.class,
        EnrollmentManagerITCase.class, AssessmentManagerITCase.class, CourseConfigManagerImplITCase.class, CourseGroupManagementITCase.class, ForumServiceITCase.class,
        WikiUnitITCase.class, CookbookDiffTest.class, PropertyITCase.class, CatalogServiceImplTest.class, BookmarkServiceITCase.class, RegistrationServiceITCase.class,
        ProjectBrokerManagerITCase.class, DBITCase.class, CPManagerITCase.class, FeedManagerImplITCase.class, IdentityITCase.class, LDAPLoginITCase.class,
        MarkingITCase.class, SpringInitDestroyVerficationITCase.class, ITCaseLoggingVersionManagerImpl.class, UserCommentsAndRatingsITCase.class,
        UserNodeAuditManagerITCase.class, ShibbolethAttributeTest.class, PortfolioModuleITCase.class, EPArtefactManagerITCase.class, EPFrontendManagerITCase.class,
        EPStructureManagerITCase.class, EPStructureToArtefactITCase.class, InfoManagerITCase.class, SimpleTagProposalManagerITCase.class, SingleVMLockerTest.class,
        TaggingManagerITCase.class, LockingServiceITCase.class, NoDependencyBetweenLearningServiceITCase.class,
        /*
         * Place tests which load their own Spring context with @ContextConfiguration below the others as they may taint the cached Spring context IMPORTANT: If you
         * create mock spring contexts in the test source tree of olatcore and you like to use them in olat3 you have to copy them to the test source tree of olat3 as
         * well as the tests on hudson run agains a jar version of olatcore where the test source tree is not available
         */
        FeedManagerITCaseWithMocking.class, IMUnitITCaseWithoutOLAT.class, ITCaseDeployableRepositoryExport.class, SpringITCase.class,// ok
        UpgradeDefinitionITCase.class, WordCountITCase.class, CustomPropertyPlaceholderITCase.class, AnnotationSchedulerITCase.class })
public class AllITCases {
    //
}
