/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.onyx.plugin;

import java.io.File;
import java.io.FileFilter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.data.DB;
import org.olat.data.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.ims.qti.QTIResultSet;

import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManager;
import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManagerFactory;

/**
 * @author Ingmar Kroll
 */
public class OnyxResultManager {

	private static final String RES_REPORTING = "resreporting";

	private static final String REPORTER_NOT_FINISHED = "reporter_not_finshed";

	public static String getResReporting() {
		return RES_REPORTING;
	}

	public static void persistOnyxResults(final QTIResultSet qtiResultSet, final String resultfile) {

		// if onyx was started from learningressources or bookmark no results are persisted
		if (qtiResultSet == null || qtiResultSet == null) {
	private static final Logger log = LoggerHelper.getLogger();

			return;
		}

		// Get course and course node
		final ICourse course = CourseFactory.loadCourse(qtiResultSet.getOlatResource());
		final CourseNode courseNode = course.getRunStructure().getNode(qtiResultSet.getOlatResourceDetail());

		Boolean isSurvey = false;
		if (!courseNode.getClass().equals(IQTESTCourseNode.class)) {
			isSurvey = true;
		}
	private static final Logger log = LoggerHelper.getLogger();

				"persit onyx result: identiyname=" + qtiResultSet.getIdentity().getName() + "  nodeident=" + courseNode.getIdent() + "  resultfile=" + resultfile);
		String path = null;
		if (isSurvey) {
			final OlatRootFolderImpl courseRootContainer = course.getCourseEnvironment().getCourseBaseContainer();
			path = courseRootContainer.getBasefile() + File.separator + courseNode.getIdent() + File.separator;
		} else {
			path = WebappHelper.getUserDataRoot() + File.separator + RES_REPORTING + File.separator + qtiResultSet.getIdentity().getName() + File.separator
					+ courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString() + File.separator;
		}
		final File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		final File resultfileF = new File(resultfile);
		final File resultfileUnzippedDir = new File(resultfileF.getAbsolutePath().substring(0, resultfileF.getAbsolutePath().length() - 4) + "__unzipped");
		if (!resultfileUnzippedDir.exists()) {
			resultfileUnzippedDir.mkdir();
		}
		ZipUtil.unzip(resultfileF, resultfileUnzippedDir);
		final File[] results = resultfileUnzippedDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(final java.io.File result) {
				return result.getName().toLowerCase().endsWith("xml") && result.getName().toLowerCase().startsWith("result");
			}
		});

		if (results == null || results.length != 1) { throw new UnsupportedOperationException("onyx result zip contains not exactly 1 result file"); }
		final File result = results[0];
		// add onyx session id (assessment id in qtiresultset table) to identify the different test attempts
		final File file_s = new File(path + courseNode.getIdent() + "v" + qtiResultSet.getAssessmentID() + ".xml");
		// result.copyTo(file_s);
		FileUtils.copyFileToFile(result, file_s, false);
		result.delete();
		resultfileF.delete();
		resultfileUnzippedDir.delete();

		// if this is a onyx survey we are done here
		if (isSurvey) { return; }

		// before asking onyxReporter for resultsets, save the QTIResultSet with the flag "reporterFinsished = false"
		qtiResultSet.setLastModified(new Date());
		DBFactory.getInstance().saveObject(qtiResultSet);

		performOnyxReport(qtiResultSet, file_s);
	}

	public static String getUniqueId(final Identity identity, final CourseNode node, final UserCourseEnvironment userCourseEnv) {
		final String uId = String.valueOf(CodeHelper.getGlobalForeverUniqueID().hashCode());

		final QTIResultSet qtiResultSet = new QTIResultSet();
		qtiResultSet.setAssessmentID(Long.parseLong(uId));
		qtiResultSet.setOlatResource(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		qtiResultSet.setOlatResourceDetail(node.getIdent());
		qtiResultSet.setRepositoryRef(node.getReferencedRepositoryEntry().getKey().longValue());
		qtiResultSet.setIdentity(identity);
		qtiResultSet.setQtiType(1);
		qtiResultSet.setLastModified(new Date());
		DBFactory.getInstance().saveObject(qtiResultSet);
		DBFactory.getInstance().commit();

		return uId;
	}

	public static String getUniqueIdForShowOnly() {
		final String uId = String.valueOf(CodeHelper.getGlobalForeverUniqueID().hashCode());
		return uId;
	}

	public static QTIResultSet getResultSet(final long uniqueId) {
		final List<QTIResultSet> liste = getResultSetByAssassmentId(uniqueId);
		QTIResultSet qtiResultSet = null;
		if (liste != null && liste.size() > 0) {
			qtiResultSet = liste.get(0);
		}
		return qtiResultSet;
	}

	private static List<QTIResultSet> getResultSetByAssassmentId(final Long assessmentID) {
		final DB db = DBFactory.getInstance();

		final StringBuilder slct = new StringBuilder();
		slct.append("select rset from ");
		slct.append("org.olat.ims.qti.QTIResultSet rset ");
		slct.append("where ");
		slct.append("rset.assessmentID=? ");

		return db.find(slct.toString(), new Object[] { assessmentID }, new Type[] { Hibernate.LONG });
	}

	/**
	 * Ask the Onyx Reporter for a result.xml that has allready been saved but the reporter was not finished. This is called by a nightly job.
	 * 
	 * @param qtiResultSet
	 */
	private static boolean performOnyxReport(final QTIResultSet qtiResultSet) {
		return performOnyxReport(qtiResultSet, null);
	}

	/**
	 * Ask the Onyx Reporter with a given file and save the results to db.
	 * 
	 * @param qtiResultSet
	 * @param file_s
	 */
	private static boolean performOnyxReport(final QTIResultSet qtiResultSet, File file_s) {

		boolean reporterFinsished = true;

		// Get course and course node
		final ICourse course = CourseFactory.loadCourse(qtiResultSet.getOlatResource());
		final CourseNode courseNode = course.getRunStructure().getNode(qtiResultSet.getOlatResourceDetail());

		AssessableCourseNode node = null;
		if (courseNode.getClass().equals(IQTESTCourseNode.class)) {
			node = (AssessableCourseNode) courseNode;
		}
		final ScoreEvaluation sc = new ScoreEvaluation(null, null, qtiResultSet.getAssessmentID());
		// do not increment attempts again
		final IdentityEnvironment ienv = new IdentityEnvironment();
		ienv.setIdentity(qtiResultSet.getIdentity());
		final UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());

		node.updateUserScoreEvaluation(sc, uce, qtiResultSet.getIdentity(), false);
		uce.getScoreAccounting().scoreInfoChanged(node, sc);

		List<String[]> liste = new ArrayList<String[]>();

		// if no file was given use the qtiresultset to get the file
		if (file_s == null) {
			final String path = WebappHelper.getUserDataRoot() + File.separator + RES_REPORTING + File.separator + qtiResultSet.getIdentity().getName() + File.separator
					+ courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString() + File.separator;
			file_s = new File(path + courseNode.getIdent() + "v" + qtiResultSet.getAssessmentID() + ".xml");
			if (!file_s.exists()) {
	private static final Logger log = LoggerHelper.getLogger();

				return false;
			}
		}

		try {
			final OnyxReporterWebserviceManager onyxReporter = OnyxReporterWebserviceManagerFactory.getInstance().fabricate("OnyxReporterWebserviceClient");
			if (onyxReporter != null) {
				liste = onyxReporter.getResults(file_s, node);
			} else {
				LifeCycleManager.createInstanceFor(qtiResultSet).markTimestampFor(REPORTER_NOT_FINISHED);
				reporterFinsished = false;
	private static final Logger log = LoggerHelper.getLogger();

						"OnyxReporter was unreachable during get the results. An entry in Lifecyclemanager is done and the report will be finshed with a job.");
			}
		} catch (final RemoteException e) {
			LifeCycleManager.createInstanceFor(qtiResultSet).markTimestampFor(REPORTER_NOT_FINISHED);
			reporterFinsished = false;
	private static final Logger log = LoggerHelper.getLogger();

					"OnyxReporter was unreachable during get the results. An entry in Lifecyclemanager is done and the report will be finshed with a job.");
		}

		String score = null, passed = null;
		for (final String[] vars : liste) {
			if (vars.length == 2) {
				// only testoutcomes "score" and "passed" are stored at olat db
				if (vars[0].equalsIgnoreCase("score")) {
					score = vars[1];
				} else if (vars[0].equalsIgnoreCase("pass")) {
					passed = vars[1];
				} else {
					log.info("TestOutCome " + vars[0] + " is not stored in OLAT DB");
				}
			}
		}
		if (score != null || passed != null) {
			if (score != null) {
				qtiResultSet.setScore(Float.valueOf(score));
				// if own cutvalue for passed is configured use this instead of the PASS variable from onyx test.
				if (courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_CUTVALUE) != null) {
					if (Float.valueOf(score) >= courseNode.getModuleConfiguration().getIntegerSafe(IQEditController.CONFIG_KEY_CUTVALUE, 0)) {
						passed = "true";
					}
				}
			}
			if (passed != null) {
				qtiResultSet.setIsPassed(Boolean.valueOf(passed));
			}

			// update score and passed info
			final ScoreEvaluation sceval = new ScoreEvaluation(score != null ? Float.valueOf(score) : null, passed != null ? Boolean.valueOf(passed) : null,
					qtiResultSet.getAssessmentID());
			// do not increment attempts again
			node.updateUserScoreEvaluation(sceval, uce, qtiResultSet.getIdentity(), false);
			uce.getScoreAccounting().scoreInfoChanged(node, sceval);
		}
		qtiResultSet.setLastModified(new Date());
		DBFactory.getInstance().saveObject(qtiResultSet);

		return reporterFinsished;
	}

	/**
	 * This is called by a nightly job: update all resultsets where the Onyx Reporter has not finished yet. (maybe because the reporter was not available).
	 */
	public static void updateOnyxResults() {
		final List<QTIResultSet> liste = findResultSets();
		for (final QTIResultSet qTIResultSet : liste) {
			LifeCycleManager lcm = null;
			if (qTIResultSet != null) {
				lcm = LifeCycleManager.createInstanceFor(qTIResultSet);
			}
			if (lcm != null && lcm.lookupLifeCycleEntry(REPORTER_NOT_FINISHED) != null) {
				if (performOnyxReport(qTIResultSet)) {
					lcm.deleteAllEntriesForPersistentObject();
				}
			}
		}
	}

	public static List findResultSets() {
		final DB db = DBFactory.getInstance();

		final StringBuilder slct = new StringBuilder();
		slct.append("select rset from ");
		slct.append("org.olat.ims.qti.QTIResultSet rset ");

		return db.find(slct.toString());
	}
}
