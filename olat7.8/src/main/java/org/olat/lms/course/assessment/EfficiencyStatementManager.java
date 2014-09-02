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

package org.olat.lms.course.assessment;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.AssessmentPropertyDao;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.user.User;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.CourseXStreamAliases;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.user.UserDataDeletable;
import org.olat.lms.user.UserService;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Methods to update a users efficiency statement and to retrieve such statements from the database.
 * <P>
 * Initial Date: 11.08.2005 <br>
 * 
 * @author gnaegi
 */
public class EfficiencyStatementManager extends BasicManager implements UserDataDeletable {
    // TODO remove as already definded in basic manager
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private UserService userService;
    @Autowired
    private AssessmentPropertyDao assessmentPropertyDao;

    public static final String KEY_ASSESSMENT_NODES = "assessmentNodes";
    public static final String KEY_COURSE_TITLE = "courseTitle";
    private static final String PROPERTY_CATEGORY = "efficiencyStatement";
    private static EfficiencyStatementManager INSTANCE;

    /**
     * Constructor
     */
    private EfficiencyStatementManager() {
        INSTANCE = this;
    }

    /**
     * Factory method
     * 
     * @return
     */
    public static EfficiencyStatementManager getInstance() {
        return INSTANCE;
    }

    /**
     * Updates the users efficiency statement for this course.
     * <p>
     * Called in AssessmentManager in a <code>doInSync</code> block, toghether with the saveScore.
     * 
     * @param userCourseEnv
     */
    public void updateUserEfficiencyStatement(final UserCourseEnvironment userCourseEnv) {
        final Long courseResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId),
                false);
        final ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
        updateUserEfficiencyStatement(userCourseEnv, re.getKey(), course, true);
    }

    /**
     * Updates the users efficiency statement for this course
     * 
     * @param userCourseEnv
     * @param repoEntryKey
     * @param checkForExistingProperty
     */
    private void updateUserEfficiencyStatement(final UserCourseEnvironment userCourseEnv, final Long repoEntryKey, final ICourse course,
            final boolean checkForExistingProperty) {
        // o_clusterOK: by ld
        final CourseConfig cc = userCourseEnv.getCourseEnvironment().getCourseConfig();
        // write only when enabled for this course
        if (cc.isEfficencyStatementEnabled()) {
            final Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
            final PropertyManager pm = PropertyManager.getInstance();
            final String courseRepoEntryKey = getPropertyName(repoEntryKey);

            final CourseNode rootNode = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
            final List<Map<String, Object>> assessmentNodes = AssessmentHelper.addAssessableNodeAndDataToList(0, rootNode, userCourseEnv, true, true);

            final EfficiencyStatement efficiencyStatement = new EfficiencyStatement();
            efficiencyStatement.setAssessmentNodes(assessmentNodes);
            efficiencyStatement.setCourseTitle(userCourseEnv.getCourseEnvironment().getCourseTitle());
            efficiencyStatement.setCourseRepoEntryKey(repoEntryKey);
            final User user = identity.getUser();
            efficiencyStatement.setDisplayableUserInfo(userService.getFirstAndLastname(user) + " (" + identity.getName() + ")");
            efficiencyStatement.setLastUpdated(System.currentTimeMillis());

            // save efficiency statement as xtream persisted list
            final String efficiencyStatementX = CourseXStreamAliases.getEfficiencyStatementXStream().toXML(efficiencyStatement);
            PropertyImpl efficiencyProperty = null;
            if (checkForExistingProperty) {
                efficiencyProperty = pm.findUserProperty(identity, PROPERTY_CATEGORY, courseRepoEntryKey);
            }
            if (assessmentNodes != null) {
                if (efficiencyProperty == null) {
                    // create new
                    efficiencyProperty = pm.createUserPropertyInstance(identity, PROPERTY_CATEGORY, courseRepoEntryKey, null, null, null, efficiencyStatementX);
                    pm.saveProperty(efficiencyProperty);
                    if (log.isDebugEnabled()) {
                        log.debug("creating new efficiency statement property::" + efficiencyProperty.getKey() + " for id::" + identity.getName() + " repoEntry::"
                                + courseRepoEntryKey);
                    }
                } else {
                    // update existing
                    if (log.isDebugEnabled()) {
                        log.debug("updatting efficiency statement property::" + efficiencyProperty.getKey() + " for id::" + identity.getName() + " repoEntry::"
                                + courseRepoEntryKey);
                    }
                    efficiencyProperty.setTextValue(efficiencyStatementX);
                    pm.updateProperty(efficiencyProperty);
                }
            } else {
                if (efficiencyProperty != null) {
                    // remove existing since now empty empty efficiency statements
                    if (log.isDebugEnabled()) {
                        log.debug("removing efficiency statement property::" + efficiencyProperty.getKey() + " for id::" + identity.getName() + " repoEntry::"
                                + courseRepoEntryKey + " since empty");
                    }
                    pm.deleteProperty(efficiencyProperty);
                }
                // else nothing to create and nothing to delete
            }

            // send modified event to everybody
            final AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_EFFICIENCY_STATEMENT_CHANGED, identity);
            CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);
        }
    }

    /**
     * LD: Debug method.
     * 
     * @param efficiencyStatement
     */
    private void printEfficiencyStatement(final EfficiencyStatement efficiencyStatement) {
        final List<Map<String, Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
        if (assessmentNodes != null) {
            final Iterator<Map<String, Object>> iter = assessmentNodes.iterator();
            while (iter.hasNext()) {
                final Map<String, Object> nodeData = iter.next();
                final String title = (String) nodeData.get(AssessmentHelper.KEY_TITLE_SHORT);
                final String score = (String) nodeData.get(AssessmentHelper.KEY_SCORE);
                final Boolean passed = (Boolean) nodeData.get(AssessmentHelper.KEY_PASSED);
                final Integer attempts = (Integer) nodeData.get(AssessmentHelper.KEY_ATTEMPTS);
                final String attemptsStr = attempts == null ? null : String.valueOf(attempts.intValue());
                System.out.println("title: " + title + " score: " + score + " passed: " + passed + " attempts: " + attemptsStr);
            }
        }
    }

    /**
     * Get the user efficiency statement list for this course
     * 
     * @param courseRepoEntryKey
     * @param identity
     * @return Map containing a list of maps that contain the nodeData for this user and course using the keys defined in the AssessmentHelper and the title of the course
     */
    public EfficiencyStatement getUserEfficiencyStatement(final Long courseRepoEntryKey, final Identity identity) {
        final PropertyManager pm = PropertyManager.getInstance();
        PropertyImpl efficiencyProperty;

        efficiencyProperty = pm.findUserProperty(identity, PROPERTY_CATEGORY, getPropertyName(courseRepoEntryKey));
        if (efficiencyProperty == null) {
            return null;
        } else {
            return (EfficiencyStatement) CourseXStreamAliases.getEfficiencyStatementXStream().fromXML(efficiencyProperty.getTextValue());
        }
    }

    /**
     * Get the passed value of a course node of a specific efficiency statment
     * 
     * @param nodeIdent
     * @param efficiencyStatement
     * @return true if passed, false if not, null if node not found
     */
    public Boolean getPassed(final String nodeIdent, final EfficiencyStatement efficiencyStatement) {
        final List<Map<String, Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
        if (assessmentNodes != null) {
            final Iterator<Map<String, Object>> iter = assessmentNodes.iterator();
            while (iter.hasNext()) {
                final Map<String, Object> nodeData = iter.next();
                if (nodeData.get(AssessmentHelper.KEY_IDENTIFYER).equals(nodeIdent)) {
                    return (Boolean) nodeData.get(AssessmentHelper.KEY_PASSED);
                }
            }
        }
        return null;
    }

    /**
     * Get the score value of a course node of a specific efficiency statment
     * 
     * @param nodeIdent
     * @param efficiencyStatement
     * @return the score, null if node not found
     */
    public Double getScore(final String nodeIdent, final EfficiencyStatement efficiencyStatement) {
        final List<Map<String, Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
        if (assessmentNodes != null) {
            final Iterator<Map<String, Object>> iter = assessmentNodes.iterator();
            while (iter.hasNext()) {
                final Map<String, Object> nodeData = iter.next();
                if (nodeData.get(AssessmentHelper.KEY_IDENTIFYER).equals(nodeIdent)) {
                    final String scoreString = (String) nodeData.get(AssessmentHelper.KEY_SCORE);
                    return Double.valueOf(scoreString);
                }
            }
        }
        return null;
    }

    /**
     * Find all efficiency statements for a specific user
     * 
     * @param identity
     * @return List of efficiency statements
     */
    public List<EfficiencyStatement> findEfficiencyStatements(final Identity identity) {
        final PropertyManager pm = PropertyManager.getInstance();
        final List<PropertyImpl> esProperties = pm.listProperties(identity, null, null, PROPERTY_CATEGORY, null);
        final List<EfficiencyStatement> efficiencyStatements = new ArrayList<EfficiencyStatement>();
        final Iterator<PropertyImpl> iter = esProperties.iterator();
        while (iter.hasNext()) {
            final PropertyImpl efficiencyProperty = iter.next();
            final EfficiencyStatement efficiencyStatement = (EfficiencyStatement) CourseXStreamAliases.getEfficiencyStatementXStream().fromXML(
                    efficiencyProperty.getTextValue());
            efficiencyStatements.add(efficiencyStatement);
        }
        return efficiencyStatements;
    }

    /**
     * Find all identities who have an efficiency statement for this course repository entry
     * 
     * @param courseRepoEntryKey
     * @return List of identities
     */
    protected List<Identity> findIdentitiesWithEfficiencyStatements(final Long courseRepoEntryKey) {
        final PropertyManager pm = PropertyManager.getInstance();
        return pm.findIdentitiesWithProperty(null, PROPERTY_CATEGORY, getPropertyName(courseRepoEntryKey), false);
    }

    /**
     * Delete all efficiency statements from the given course for all users
     * 
     * @param courseRepoEntryKey
     * @return int number of deleted efficiency statements
     */
    public void deleteEfficiencyStatementsFromCourse(final Long courseRepoEntryKey) {
        final PropertyManager pm = PropertyManager.getInstance();
        pm.deleteProperties(null, null, null, PROPERTY_CATEGORY, getPropertyName(courseRepoEntryKey));
    }

    /**
     * Delete the given efficiency statement for this person
     * 
     * @param identity
     * @param efficiencyStatement
     */
    public void deleteEfficiencyStatement(final Identity identity, final EfficiencyStatement efficiencyStatement) {
        final PropertyManager pm = PropertyManager.getInstance();
        final String crourseRepoEntryKey = getPropertyName(efficiencyStatement.getCourseRepoEntryKey());
        pm.deleteProperties(identity, null, null, PROPERTY_CATEGORY, crourseRepoEntryKey);
    }

    /**
     * Internal helper: convert the course repository entry key to a value that is used in the property name field
     * 
     * @param courseRepoEntryKey
     * @return String converted course id
     */
    private String getPropertyName(final Long courseRepoEntryKey) {
        return courseRepoEntryKey.toString();
    }

    /**
     * Create or update all efficiency statment lists for the given list of identities and this course This is called from only one thread, since the course is locked at
     * editing (either CourseEdit or CourseDetails edit).
     * 
     * @param course
     * @param identities
     *            List of identities
     * @param checkForExistingRecord
     *            true: check if efficiency statement for this user exist; false: always create new one (be careful with this one!)
     */
    public void updateEfficiencyStatements(final OLATResourceable ores, final List<Identity> identities, final boolean checkForExistingProperty) {
        if (identities.size() > 0) {
            final ICourse course = CourseFactory.loadCourse(ores);
            log.info("Audit:Updating efficiency statements for course::" + course.getResourceableId() + ", this might produce temporary heavy load on the CPU");
            final Long courseResId = course.getCourseEnvironment().getCourseResourceableId();
            final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(
                    OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId), false);

            // preload cache to speed up things
            final AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();
            final long start = System.currentTimeMillis();
            am.preloadCache();
            final long between = System.currentTimeMillis();

            final Iterator<Identity> iter = identities.iterator();
            while (iter.hasNext()) {
                final Identity identity = iter.next();
                // o_clusterOK: by ld
                final OLATResourceable efficiencyStatementResourceable = am.createOLATResourceableForLocking(identity);
                CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(efficiencyStatementResourceable, new SyncerExecutor() {
                    @Override
                    public void execute() {
                        // create temporary user course env
                        final UserCourseEnvironment uce = AssessmentHelper.createAndInitUserCourseEnvironment(identity, course);
                        updateUserEfficiencyStatement(uce, re.getKey(), course, checkForExistingProperty);
                    }
                });
                if (Thread.interrupted()) {
                    break;
                }
            }
            // }
            final long end = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("Updated efficiency statements for course::" + course.getResourceableId() + ". Prepare cache: " + (between - start)
                        + "ms; Updating statements: " + (end - between) + "ms; Users: " + identities.size());
            }
        }
    }

    public void archiveUserData(final Identity identity, final File archiveDir) {
        final List<EfficiencyStatement> efficiencyStatements = this.findEfficiencyStatements(identity);
        EfficiencyStatementArchiver.getInstance().archive(efficiencyStatements, identity, archiveDir);
    }

    /**
     * Delete all efficiency-statements for certain identity.
     * 
     * @param identity
     *            Delete data for this identity.
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        final List<EfficiencyStatement> efficiencyStatements = this.findEfficiencyStatements(identity);
        for (final Iterator<EfficiencyStatement> iter = efficiencyStatements.iterator(); iter.hasNext();) {
            deleteEfficiencyStatement(identity, iter.next());
        }
        log.debug("All efficiency statements deleted for identity=" + identity);
    }

    public void updateAllEfficiencyStatementsOf(ICourse course) {
        List identitiesWithData = assessmentPropertyDao.getAllIdentitiesWithCourseAssessmentData(course.getResourceableTypeName(), course.getResourceableId());
        EfficiencyStatementManager.getInstance().updateEfficiencyStatements(course, identitiesWithData, false);
    }

}
