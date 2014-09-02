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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.AssessmentPropertyDao;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.activitylogging.ILoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.StringResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description:<BR>
 * The assessment manager is used by the assessable course nodes to store and retrieve user assessment data from the database. The assessment Manager should not be used
 * directly from the controllers but only via the assessable course nodes interface.<BR>
 * Exception are nodes that want to save or get node attempts variables for nodes that are not assessable nodes (e.g. questionnaire) <BR>
 * This implementation will store its values using the property manager and has a cache built in for frequently used assessment data like score, passed and attempts
 * variables.
 * <P>
 * the underlying cache is segmented as follows: 1.) by this class (=owner in singlevm, coowner in cluster mode) 2.) by course (so that e.g. deletion of a course removes
 * all caches) 3.) by identity, for preloading and invalidating (e.g. a user entering a course will cause the identity's cache to be loaded) each cache only has -one-
 * key, which is a hashmap with all the information (score,passed, etc) for the given user/course. the reason for this is that it must be possible to see a difference
 * between a null value (key expired) and a value which corresponds to e.g. "this user has never attempted this test in this course". since only the concrete set, but not
 * the possible set is known. (at least not in the database). so all keys of a given user/course will therefore expire together which also makes sense from a use point of
 * view. Cache usage with e.g. the wiki: wikipages should be saved as separate keys, since no batch updates are needed for perf. reasons. reason for 3: preloading all
 * data of all users of a course lasts up to 5 seconds and will waste memory. a user in a course only needs its own data. only when a tutor enters the assessment
 * functionality, all data of all users is needed -> do a full load only then. TODO: e.g. IQTEST.onDelete(..) cleans all data without going over the assessmentmanager
 * here. meaning that the cache has stale data in it. since coursenode.getIdent (partial key of this cache) is forever unique, then this doesn't really matter. - but it
 * is rather unintended... point is that a node can save lots of data that have nothing to do with assessments
 * 
 * @author Felix Jost
 */
public class NewCachePersistingAssessmentManager extends BasicManager implements AssessmentManager {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * the key under which a hashmap is stored in a cachewrapper. we only use one key so that either all values of a user are there or none are there. (otherwise we
     * cannot know whether a null value means expiration of cache or no-such-property-yet-for-user)
     */
    private static final String FULLUSERSET = "FULLUSERSET";

    // Float and Integer are immutable objects, we can reuse them.
    private static final Float FLOAT_ZERO = new Float(0);
    private static final Integer INTEGER_ZERO = new Integer(0);

    // one cache entry point to generate subcaches for all assessmentmanager instances
    private static CacheWrapper assessmentMainCache = CoordinatorManager.getInstance().getCoordinator().getCacher()
            .getOrCreateCache(NewCachePersistingAssessmentManager.class, null);

    // the cache per assessment manager instance (=per course)
    private final CacheWrapper courseCache;
    private final OLATResourceable ores;

    private AssessmentPropertyDao assessmentPropertyDao;

    // we cannot store the ref to cpm here, since at the time where the assessmentManager is initialized, the given course is not fully initialized yet.
    // does not work: final CoursePropertyManager cpm;

    /**
     * Get an instance of the persisting assessment manager. This will use the course property manager to persist assessment data. THIS METHOD MUST ONLY BE USED WITHIN
     * THE COURSE CONSTRUCTOR. Use course.getAssessmentManager() to use the assessment manager during runtime!
     * 
     * @param course
     * @return The assessment manager for this course
     */
    public static AssessmentManager getInstance(final ICourse course) {
        return new NewCachePersistingAssessmentManager(course);
    }

    /**
     * Private since singleton
     */
    private NewCachePersistingAssessmentManager(final ICourse course) {
        this.ores = course;
        courseCache = assessmentMainCache.getOrCreateChildCacheWrapper(course);
        // TODO: CoreSpringFactory : Use @Autowired instead, problem NewCachePersistingAssessmentManager is created with 'new'
        assessmentPropertyDao = CoreSpringFactory.getBean(AssessmentPropertyDao.class);
    }

    /**
	 */
    @Override
    public void preloadCache(final Identity identity) {
        // triggers loading of data of the given user.
        getOrLoadScorePassedAttemptsMap(identity, false);
        return;
    }

    @Override
    public void preloadCache() {
        // ignore, since lazy loading will load identities' cache
        // o_clusterREVIEW test performance when in assessment manager and course has e.g. 500 users -> how long do 500 queries take?
        // -> is one full fetch needed instead?
    }

    /**
     * retrieves the Map which contains all data for this course and the given user. if the cache evicted the map in the meantime, then it is recreated by querying the
     * database and fetching all that data in one query, and then reput into the cache. <br>
     * this method is threadsafe.
     * 
     * @param identity
     *            the identity
     * @param notify
     *            if true, then the
     * @return a Map containing nodeident+"_"+ e.g. PASSED as key, Boolean (for PASSED), Float (for SCORE), or Integer (for ATTEMPTS) as values
     */
    private Map<String, Serializable> getOrLoadScorePassedAttemptsMap(final Identity identity, final boolean prepareForNewData) {
        final CacheWrapper cw = getCacheWrapperFor(identity);
        synchronized (cw) { // o_clusterOK by:fj : we sync on the cache to protect access within the monitor "one user in a course".
            // a user is only active on one node at the same time.
            Map<String, Serializable> m = (Map<String, Serializable>) cw.get(FULLUSERSET);
            if (m == null) {
                // cache entry (=all data of the given identity in this course) has expired or has never been stored yet into the cache.
                // or has been invalidated (in cluster mode when puts occurred from an other node for the same cache)
                m = new HashMap<String, Serializable>();
                // load data
                ICourse course = CourseFactory.loadCourse(ores);
                final List properties = assessmentPropertyDao.loadPropertiesFor(identity, course.getResourceableTypeName(), course.getResourceableId().longValue());
                for (final Iterator iter = properties.iterator(); iter.hasNext();) {
                    final PropertyImpl property = (PropertyImpl) iter.next();
                    addPropertyToCache(m, property);
                }
                // we use a putSilent here (no invalidation notifications to other cluster nodes), since
                // we did not generate new data, but simply asked to reload it.
                if (prepareForNewData) {
                    cw.update(FULLUSERSET, (Serializable) m);
                } else {
                    cw.put(FULLUSERSET, (Serializable) m);
                }
            } else {
                // still in cache.
                if (prepareForNewData) { // but we need to notify that data has changed: we reput the data into the cache - a little hacky yes
                    cw.update(FULLUSERSET, (Serializable) m);
                }
            }
            return m;
        }
    }

    private CacheWrapper getCacheWrapperFor(final Identity identity) {
        // the ores is only for within the cache
        final OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck("Identity", identity.getKey());
        final CacheWrapper cw = courseCache.getOrCreateChildCacheWrapper(ores);
        return cw;
    }

    // package local for perf. reasons, threadsafe.
    /**
     * puts a property into the cache. since it only puts data into a map which in turn is put under the FULLUSERSET key into the cache, we need to explicitly reput that
     * key from the cache first, so that the cache notices that that data has changed (and can propagate to other nodes if applicable)
     */
    void putPropertyIntoCache(final Identity identity, final PropertyImpl property) {
        // load the data, and indicate it to reput into the cache so that the cache knows it is something new.
        final Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, true);
        addPropertyToCache(m, property);
    }

    /**
     * Removes a property from cache.
     * 
     * @param identity
     * @param property
     */
    void removePropertyFromCache(final Identity identity, final PropertyImpl property) {
        // load the data, and indicate it to reput into the cache so that the cache knows it is something new.
        final Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, true);
        this.removePropertyFromCache(m, property);
    }

    /**
     * thread safe.
     * 
     * @param property
     * @throws AssertionError
     */
    private void addPropertyToCache(final Map<String, Serializable> acache, final PropertyImpl property) throws AssertionError {
        final String propertyName = property.getName();
        Serializable value;
        if (propertyName.equals(AssessmentPropertyDao.ATTEMPTS)) {
            value = new Integer(property.getLongValue().intValue());
        } else if (propertyName.equals(AssessmentPropertyDao.SCORE)) {
            value = property.getFloatValue();
        } else if (propertyName.equals(AssessmentPropertyDao.PASSED)) {
            value = new Boolean(property.getStringValue());
        } else if (propertyName.equals(AssessmentPropertyDao.ASSESSMENT_ID)) {
            value = property.getLongValue();
        } else if (propertyName.equals(AssessmentPropertyDao.COMMENT) || propertyName.equals(AssessmentPropertyDao.COACH_COMMENT)) {
            value = property.getTextValue();
        } else {
            throw new AssertionError("property in list that is not of type attempts, score, passed or ASSESSMENT_ID, COMMENT and COACH_COMMENT :: " + propertyName);
        }

        // put in cache, maybe overriding old values
        final String cacheKey = getPropertyCacheKey(property);
        synchronized (acache) {// cluster_ok acache is an element from the cacher
            acache.put(cacheKey, value);
        }
    }

    /**
     * Removes property from cache
     * 
     * @param acache
     * @param property
     * @throws AssertionError
     */
    private void removePropertyFromCache(final Map<String, Serializable> acache, final PropertyImpl property) throws AssertionError {
        final String propertyName = property.getName();
        if (!(propertyName.equals(AssessmentPropertyDao.ATTEMPTS) || propertyName.equals(AssessmentPropertyDao.SCORE) || propertyName
                .equals(AssessmentPropertyDao.PASSED))) {
            throw new AssertionError("property in list that is not of type attempts, score or passed ::" + propertyName);
        }

        final String cacheKey = getPropertyCacheKey(property);
        synchronized (acache) {// cluster_ok acache is an elment from the cacher
            acache.remove(cacheKey);
        }
    }

    /**
     * @param courseNode
     * @param identity
     * @param assessedIdentity
     * @param score
     * @param coursePropManager
     */
    void saveNodeScore(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final Float score,
            final CoursePropertyManager coursePropManager) {
        // olat:::: introduce a createOrUpdate method in the cpm and also if applicable in the general propertymanager
        if (score != null) {
            PropertyImpl scoreProperty = coursePropManager.findCourseNodeProperty(courseNode, assessedIdentity, null, AssessmentPropertyDao.SCORE);
            if (scoreProperty == null) {
                scoreProperty = coursePropManager.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, AssessmentPropertyDao.SCORE, score, null, null,
                        null);
                coursePropManager.saveProperty(scoreProperty);
            } else {
                scoreProperty.setFloatValue(score);
                coursePropManager.updateProperty(scoreProperty);
            }
            // add to cache
            putPropertyIntoCache(assessedIdentity, scoreProperty);
        }
    }

    /**
     * java.lang.Integer)
     */
    @Override
    public void saveNodeAttempts(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final Integer attempts) {
        // A note on updating the EfficiencyStatement:
        // In the equivalent method incrementNodeAttempts() in this class, the following code is executed:
        // // Update users efficiency statement
        // EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
        // esm.updateUserEfficiencyStatement(userCourseEnv);
        // One would expect that saveNodeAttempts would also have to update the EfficiencyStatement - or
        // the caller of this method would have to make sure that this happens in the same transaction.
        // While this is not explicitly so, implicitly it is: currently the only user this method is
        // the AssessmentEditController - which as the 2nd last method calls into saveScoreEvaluation
        // - which in turn does update the EfficiencyStatement - at which point we're happy and everything works fine.
        // But it seems like this mechanism is a bit unobvious and might well be worth some refactoring...
        final ICourse course = CourseFactory.loadCourse(ores);
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(createOLATResourceableForLocking(assessedIdentity), new SyncerExecutor() {
            @Override
            public void execute() {
                PropertyImpl attemptsProperty = cpm.findCourseNodeProperty(courseNode, assessedIdentity, null, AssessmentPropertyDao.ATTEMPTS);
                if (attemptsProperty == null) {
                    attemptsProperty = cpm.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, AssessmentPropertyDao.ATTEMPTS, null,
                            new Long(attempts.intValue()), null, null);
                    cpm.saveProperty(attemptsProperty);
                } else {
                    attemptsProperty.setLongValue(new Long(attempts.intValue()));
                    cpm.updateProperty(attemptsProperty);
                }
                // add to cache
                putPropertyIntoCache(assessedIdentity, attemptsProperty);
            }
        });

        // node log
        final UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
        am.appendToUserNodeLog(courseNode, identity, assessedIdentity, AssessmentPropertyDao.ATTEMPTS + " set to: " + String.valueOf(attempts));

        // notify about changes
        final AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED, assessedIdentity);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

        // user activity logging
        ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, getClass(), LoggingResourceable.wrap(assessedIdentity),
                LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));
    }

    /**
     * @param courseNode
     * @param identity
     * @param assessedIdentity
     * @param passed
     * @param coursePropManager
     */
    void saveNodePassed(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final Boolean passed,
            final CoursePropertyManager coursePropManager) {
        PropertyImpl passedProperty = coursePropManager.findCourseNodeProperty(courseNode, assessedIdentity, null, AssessmentPropertyDao.PASSED);
        if (passedProperty == null && passed != null) {
            final String pass = passed.toString();
            passedProperty = coursePropManager.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, AssessmentPropertyDao.PASSED, null, null, pass, null);
            coursePropManager.saveProperty(passedProperty);
        } else if (passedProperty != null) {
            if (passed != null) {
                passedProperty.setStringValue(passed.toString());
                coursePropManager.updateProperty(passedProperty);
            } else {
                removePropertyFromCache(assessedIdentity, passedProperty);
                coursePropManager.deleteProperty(passedProperty);
            }
        }

        // add to cache
        if (passed != null && passedProperty != null) {
            putPropertyIntoCache(assessedIdentity, passedProperty);
        }
    }

    /**
     * java.lang.String)
     */
    @Override
    public void saveNodeComment(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final String comment) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(createOLATResourceableForLocking(assessedIdentity), new SyncerExecutor() {
            @Override
            public void execute() {
                PropertyImpl commentProperty = cpm.findCourseNodeProperty(courseNode, assessedIdentity, null, AssessmentPropertyDao.COMMENT);
                if (commentProperty == null) {
                    commentProperty = cpm.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, AssessmentPropertyDao.COMMENT, null, null, null, comment);
                    cpm.saveProperty(commentProperty);
                } else {
                    commentProperty.setTextValue(comment);
                    cpm.updateProperty(commentProperty);
                }
                // add to cache
                putPropertyIntoCache(assessedIdentity, commentProperty);
            }
        });
        // node log
        final UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
        am.appendToUserNodeLog(courseNode, identity, assessedIdentity, AssessmentPropertyDao.COMMENT + " set to: " + comment);

        // notify about changes
        final AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_USER_COMMENT_CHANGED, assessedIdentity);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

        // user activity logging
        ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_USERCOMMENT_UPDATED, getClass(), LoggingResourceable.wrap(assessedIdentity),
                LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiUserComment, "", StringHelper.stripLineBreaks(comment)));
    }

    /**
	 */
    @Override
    public void saveNodeCoachComment(final CourseNode courseNode, final Identity assessedIdentity, final String comment) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(createOLATResourceableForLocking(assessedIdentity), new SyncerExecutor() {
            @Override
            public void execute() {
                PropertyImpl commentProperty = cpm.findCourseNodeProperty(courseNode, assessedIdentity, null, AssessmentPropertyDao.COACH_COMMENT);
                if (commentProperty == null) {
                    commentProperty = cpm.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, AssessmentPropertyDao.COACH_COMMENT, null, null, null,
                            comment);
                    cpm.saveProperty(commentProperty);
                } else {
                    commentProperty.setTextValue(comment);
                    cpm.updateProperty(commentProperty);
                }
                // add to cache
                putPropertyIntoCache(assessedIdentity, commentProperty);
            }
        });
        // olat::: no node log here? (because what we did above is a node log with custom text AND by a coach)?

        // notify about changes
        final AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_COACH_COMMENT_CHANGED, assessedIdentity);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

        // user activity logging
        ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_COACHCOMMENT_UPDATED, getClass(), LoggingResourceable.wrap(assessedIdentity),
                LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiCoachComment, "", StringHelper.stripLineBreaks(comment)));
    }

    /**
	 */
    @Override
    public void incrementNodeAttempts(final CourseNode courseNode, final Identity identity, final UserCourseEnvironment userCourseEnv) {
        incrementNodeAttempts(courseNode, identity, userCourseEnv, true);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment)
     */
    @Override
    public void incrementNodeAttemptsInBackground(final CourseNode courseNode, final Identity identity, final UserCourseEnvironment userCourseEnv) {
        incrementNodeAttempts(courseNode, identity, userCourseEnv, false);
    }

    private void incrementNodeAttempts(final CourseNode courseNode, final Identity identity, final UserCourseEnvironment userCourseEnv, final boolean logActivity) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        final long attempts = CoordinatorManager.getInstance().getCoordinator().getSyncer()
                .doInSync(createOLATResourceableForLocking(identity), new SyncerCallback<Long>() {
                    @Override
                    public Long execute() {
                        final long attempts = incrementNodeAttemptsProperty(courseNode, identity, cpm);
                        if (courseNode instanceof AssessableCourseNode) {
                            // Update users efficiency statement
                            final EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
                            esm.updateUserEfficiencyStatement(userCourseEnv);
                        }
                        return attempts;
                    }
                });

        // notify about changes
        final AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED, identity);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

        if (logActivity) {
            // user activity logging
            ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, getClass(), LoggingResourceable.wrap(identity),
                    LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));
        }
    }

    /**
     * Private method. Increments the attempts property.
     * 
     * @param courseNode
     * @param identity
     * @param cpm
     * @return the resulting new number of node attempts
     */
    private long incrementNodeAttemptsProperty(final CourseNode courseNode, final Identity identity, final CoursePropertyManager cpm) {
        Long attempts;
        PropertyImpl attemptsProperty = cpm.findCourseNodeProperty(courseNode, identity, null, AssessmentPropertyDao.ATTEMPTS);
        if (attemptsProperty == null) {
            attempts = new Long(1);
            attemptsProperty = cpm.createCourseNodePropertyInstance(courseNode, identity, null, AssessmentPropertyDao.ATTEMPTS, null, attempts, null, null);
            cpm.saveProperty(attemptsProperty);
        } else {
            attempts = new Long(attemptsProperty.getLongValue().longValue() + 1);
            attemptsProperty.setLongValue(attempts);
            cpm.updateProperty(attemptsProperty);
        }
        // add to cache
        putPropertyIntoCache(identity, attemptsProperty);

        return attempts;
    }

    /**
	 */
    @Override
    public Float getNodeScore(final CourseNode courseNode, final Identity identity) {
        // Check if courseNode exist
        if (courseNode == null) {
            return FLOAT_ZERO; // return default value
        }

        final String cacheKey = getCacheKey(courseNode, AssessmentPropertyDao.SCORE);
        final Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, false);
        synchronized (m) {// o_clusterOK by:fj is per vm only
            final Float result = (Float) m.get(cacheKey);
            return result;
        }
    }

    /**
	 */
    @Override
    public Boolean getNodePassed(final CourseNode courseNode, final Identity identity) {
        // Check if courseNode exist
        if (courseNode == null) {
            return Boolean.FALSE; // return default value
        }

        final String cacheKey = getCacheKey(courseNode, AssessmentPropertyDao.PASSED);
        final Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, false);
        synchronized (m) {// o_clusterOK by:fj is per vm only
            final Boolean result = (Boolean) m.get(cacheKey);
            return result;
        }
    }

    /**
	 */
    @Override
    public Integer getNodeAttempts(final CourseNode courseNode, final Identity identity) {
        // Check if courseNode exist
        if (courseNode == null) {
            return INTEGER_ZERO; // return default value
        }

        final String cacheKey = getCacheKey(courseNode, AssessmentPropertyDao.ATTEMPTS);
        final Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, false);
        synchronized (m) {// o_clusterOK by:fj is per vm only
            final Integer result = (Integer) m.get(cacheKey);
            // see javadoc AssessmentManager#getNodeAttempts
            return result == null ? INTEGER_ZERO : result;
        }
    }

    /**
	 */
    @Override
    public String getNodeComment(final CourseNode courseNode, final Identity identity) {
        if (courseNode == null) {
            return null; // return default value
        }

        final String cacheKey = getCacheKey(courseNode, AssessmentPropertyDao.COMMENT);
        final Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, false);
        synchronized (m) {// o_clusterOK by:fj is per vm only
            final String result = (String) m.get(cacheKey);
            return result;
        }
    }

    /**
	 */
    @Override
    public String getNodeCoachComment(final CourseNode courseNode, final Identity identity) {
        if (courseNode == null) {
            return null; // return default value
        }

        final String cacheKey = getCacheKey(courseNode, AssessmentPropertyDao.COACH_COMMENT);
        final Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, false);
        synchronized (m) {// o_clusterOK by:fj is per vm only
            final String result = (String) m.get(cacheKey);
            return result;
        }
    }

    /**
     * Internal method to create a cache key for a given node, and property
     * 
     * @param identity
     * @param nodeIdent
     * @param propertyName
     * @return String the key
     */
    private String getCacheKey(final CourseNode courseNode, final String propertyName) {
        final String nodeIdent = courseNode.getIdent();
        return getCacheKey(nodeIdent, propertyName);
    }

    /**
     * threadsafe.
     * 
     * @param nodeIdent
     * @param propertyName
     * @return
     */
    private String getCacheKey(final String nodeIdent, final String propertyName) {
        final StringBuilder key = new StringBuilder(nodeIdent.length() + propertyName.length() + 1);
        key.append(nodeIdent).append('_').append(propertyName);
        return key.toString();
    }

    /**
     * Finds the cacheKey for the input property.
     * 
     * @param property
     * @return Returns the cacheKey
     */
    private String getPropertyCacheKey(final PropertyImpl property) {
        // - node id is coded into property category like this: NID:ms::12345667
        // olat::: move the extract method below to the CoursePropertyManager - since the generation/concat method is also there.
        final String propertyName = property.getName();
        final String propertyCategory = property.getCategory();
        final String nodeIdent = propertyCategory.substring(propertyCategory.indexOf("::") + 2);
        final String cacheKey = getCacheKey(nodeIdent, propertyName);
        // cacheKey is now e.g. 12345667_PASSED
        return cacheKey;
    }

    /**
	 */
    @Override
    public void registerForAssessmentChangeEvents(final GenericEventListener gel, final Identity identity) {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(gel, identity, ores);
    }

    /**
	 */
    @Override
    public void deregisterFromAssessmentChangeEvents(final GenericEventListener gel) {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(gel, ores);
    }

    // package local for perf. reasons
    void courseLog(final ILoggingAction action, final CourseNode cn, final LoggingResourceable... details) {
        if (Settings.isJUnitTest()) {
            return;
        }
        final ICourse course = CourseFactory.loadCourse(ores);

        final LoggingResourceable[] infos = new LoggingResourceable[2 + details.length];
        infos[0] = LoggingResourceable.wrap(course);
        infos[1] = LoggingResourceable.wrap(cn);
        for (int i = 0; i < details.length; i++) {
            final LoggingResourceable lri = details[i];
            infos[i + 2] = lri;
        }

        ThreadLocalUserActivityLogger.log(action, getClass(), details);
    }

    /**
     * @param courseNode
     * @param assessedIdentity
     * @param assessmentID
     * @param coursePropManager
     */
    void saveAssessmentID(final CourseNode courseNode, final Identity assessedIdentity, final Long assessmentID, final CoursePropertyManager coursePropManager) {
        if (assessmentID != null) {
            PropertyImpl assessmentIDProperty = coursePropManager.findCourseNodeProperty(courseNode, assessedIdentity, null, AssessmentPropertyDao.ASSESSMENT_ID);
            if (assessmentIDProperty == null) {
                assessmentIDProperty = coursePropManager.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, AssessmentPropertyDao.ASSESSMENT_ID, null,
                        assessmentID, null, null);
                coursePropManager.saveProperty(assessmentIDProperty);
            } else {
                assessmentIDProperty.setLongValue(assessmentID);
                coursePropManager.updateProperty(assessmentIDProperty);
            }
            // add to cache
            putPropertyIntoCache(assessedIdentity, assessmentIDProperty);
        }
    }

    /**
     * No caching for the assessmentID.
     * 
     */
    @Override
    public Long getAssessmentID(final CourseNode courseNode, final Identity identity) {
        if (courseNode == null) {
            return Long.valueOf(0); // return default value
        }

        final String cacheKey = getCacheKey(courseNode, AssessmentPropertyDao.ASSESSMENT_ID);
        final Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, false);
        synchronized (m) {// o_clusterOK by:fj is per vm only
            final Long result = (Long) m.get(cacheKey);
            return result;
        }
    }

    /**
     * org.olat.lms.course.run.scoring.ScoreEvaluation)
     */
    @Override
    public void saveScoreEvaluation(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final ScoreEvaluation scoreEvaluation,
            final UserCourseEnvironment userCourseEnv, final boolean incrementUserAttempts) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        // o_clusterREVIEW we could sync on a element finer than course, e.g. the composite course+assessIdentity.
        // +: concurrency would be higher
        // -: many entries (num of courses * visitors of given course) in the locktable.
        // we could also sync on the assessedIdentity.

        Codepoint.codepoint(NewCachePersistingAssessmentManager.class, "beforeSyncUpdateUserEfficiencyStatement");
        final Long attempts = CoordinatorManager.getInstance().getCoordinator().getSyncer()
                .doInSync(createOLATResourceableForLocking(assessedIdentity), new SyncerCallback<Long>() {
                    @Override
                    public Long execute() {
                        Long attempts = null;
                        Codepoint.codepoint(NewCachePersistingAssessmentManager.class, "doInSyncUpdateUserEfficiencyStatement");
                        log.debug("codepoint reached: doInSyncUpdateUserEfficiencyStatement by identity: " + identity.getName());
                        saveNodeScore(courseNode, identity, assessedIdentity, scoreEvaluation.getScore(), cpm);
                        saveNodePassed(courseNode, identity, assessedIdentity, scoreEvaluation.getPassed(), cpm);
                        saveAssessmentID(courseNode, assessedIdentity, scoreEvaluation.getAssessmentID(), cpm);
                        if (incrementUserAttempts) {
                            attempts = incrementNodeAttemptsProperty(courseNode, assessedIdentity, cpm);
                        }
                        if (courseNode instanceof AssessableCourseNode) {
                            userCourseEnv.getScoreAccounting().scoreInfoChanged((AssessableCourseNode) courseNode, scoreEvaluation);
                            // Update users efficiency statement
                            final EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
                            esm.updateUserEfficiencyStatement(userCourseEnv);
                        }
                        return attempts;
                    }
                });
        // here used to be a codepoint which lead to error (OLAT-3570) in AssessmentWithCodepointsTest.
        // The reason for this error was that the AuditManager appendToUserNodeLog() is not synchronized, so could be called by several threads simultaneously.
        // The end effect of this error is data inconsistency: the score/passed info is stored but the userNodeLog info is not updated and the AssessmentChangedEvent is
        // not fired.
        // This case is very seldom, but could be avoided if the test could be protected by a lock.

        // node log
        final UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
        am.appendToUserNodeLog(courseNode, identity, assessedIdentity, AssessmentPropertyDao.SCORE + " set to: " + String.valueOf(scoreEvaluation.getScore()));
        if (scoreEvaluation.getPassed() != null) {
            am.appendToUserNodeLog(courseNode, identity, assessedIdentity, AssessmentPropertyDao.PASSED + " set to: " + scoreEvaluation.getPassed().toString());
        } else {
            am.appendToUserNodeLog(courseNode, identity, assessedIdentity, AssessmentPropertyDao.PASSED + " set to \"undefined\"");
        }
        if (scoreEvaluation.getAssessmentID() != null) {
            am.appendToUserNodeLog(courseNode, assessedIdentity, assessedIdentity, AssessmentPropertyDao.ASSESSMENT_ID + " set to: "
                    + scoreEvaluation.getAssessmentID().toString());
        }

        Codepoint.codepoint(NewCachePersistingAssessmentManager.class, "afterSyncUpdateUserEfficiencyStatement");
        log.debug("codepoint reached: afterSyncUpdateUserEfficiencyStatement by identity: " + identity.getName());
        // notify about changes
        final AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED, assessedIdentity);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

        // user activity logging
        if (scoreEvaluation.getScore() != null) {
            ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_SCORE_UPDATED, getClass(), LoggingResourceable.wrap(assessedIdentity),
                    LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiScore, "", String.valueOf(scoreEvaluation.getScore())));
        }

        if (scoreEvaluation.getPassed() != null) {
            ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_PASSED_UPDATED, getClass(), LoggingResourceable.wrap(assessedIdentity),
                    LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiPassed, "", String.valueOf(scoreEvaluation.getPassed())));
        } else {
            ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_PASSED_UPDATED, getClass(), LoggingResourceable.wrap(assessedIdentity),
                    LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiPassed, "", "undefined"));
        }

        if (incrementUserAttempts && attempts != null) {
            ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, getClass(), LoggingResourceable.wrap(identity),
                    LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));
        }
    }

    /**
     * Always use this to get a OLATResourceable for doInSync locking! Uses the assessIdentity.
     * 
     * @param course
     * @param assessedIdentity
     * @param courseNode
     * @return
     */
    @Override
    public OLATResourceable createOLATResourceableForLocking(final Identity assessedIdentity) {
        final String type = "AssessmentManager::Identity";
        final OLATResourceable oLATResourceable = OresHelper.createOLATResourceableInstance(type, assessedIdentity.getKey());
        return oLATResourceable;
    }

}
