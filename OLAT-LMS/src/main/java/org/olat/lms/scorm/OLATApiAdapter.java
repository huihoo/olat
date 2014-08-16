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
package org.olat.lms.scorm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.lms.scorm.manager.ScormManager;
import org.olat.lms.scorm.server.beans.LMSDataFormBean;
import org.olat.lms.scorm.server.beans.LMSDataHandler;
import org.olat.lms.scorm.server.beans.LMSResultsBean;
import org.olat.system.commons.StringHelper;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * OLATApiAdapter implements the ApiAdapter Interface from the pfplms code which was initially designed for applet use. For the 'Backend' it uses portions of the code
 * developed for the reload scorm player. see: http://www.scorm.com/scorm-explained/technical-scorm/run-time/run-time-reference/ for an nice overview of the datamodel
 * 
 * @author guido
 */
public class OLATApiAdapter implements ch.ethz.pfplms.scorm.api.ApiAdapterInterface {

    private static final Logger log = LoggerHelper.getLogger();

    private final ch.ethz.pfplms.scorm.api.ApiAdapter core;
    // private ScormTrackingManager scormTracking;

    private final Hashtable<String, String> olatScoCmi = new Hashtable<String, String>();

    private String olatStudentId;
    private String olatStudentName;
    // was used as reference id like out repo id

    // the sco id
    private String olatScoId;

    private boolean isLaunched = false;
    private boolean isLaunching = false;

    private LMSDataHandler odatahandler;
    private ScormManager scormManager;
    private SettingsHandlerImpl scormSettingsHandler;
    private final ScormAPICallback apiCallback;
    //
    private Properties scoresProp; // keys: sahsId; values = raw score of an sco

    private final String SCORE_IDENT = "cmi.core.score.raw";
    private File scorePropsFile;

    /**
     * creates a new API adapter
     */
    public OLATApiAdapter(final ScormAPICallback apiCallback) {
        this.apiCallback = apiCallback;
        core = new ch.ethz.pfplms.scorm.api.ApiAdapter();
    }

    /**
     * @param scormFolderPath
     * @param repoId
     * @param courseId
     * @param userPath
     * @param studentId
     *            - the olat username
     * @param studentName
     *            - the students name
     * @param isVerbose
     *            prints out what is going on inside the scorm RTE
     */
    public final void init(final String scormFolderPath, final String repoId, final String courseId, final String storagePath, final String studentId,
            final String studentName, final String lesson_mode, final String credit_mode, final int controllerHashCode) {
        this.olatStudentId = studentId;
        this.olatStudentName = studentName;
        say("cmi.core.student_id=" + olatStudentId);
        say("cmi.core.student_name=" + olatStudentName);
        scormSettingsHandler = new SettingsHandlerImpl(scormFolderPath, repoId, courseId, storagePath, studentName, studentId, lesson_mode, credit_mode,
                controllerHashCode);

        // get a path for the scores per sco
        final String savePath = scormSettingsHandler.getFilePath();
        scorePropsFile = new File(savePath + "/_olat_score.properties");
        scoresProp = new Properties();
        if (scorePropsFile.exists()) {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(scorePropsFile));
                scoresProp.load(is);
            } catch (final IOException e) {
                throw new OLATRuntimeException(this.getClass(), "could not load existing scorm-score-properties file: " + scorePropsFile.getAbsolutePath(), e);
            } finally {
                if (is != null) {
                    FileUtils.closeSafely(is);
                }
            }
        }

        scormManager = new ScormManager(scormFolderPath, true, true, true, scormSettingsHandler);
    }

    private final void say(final String s) {

        log.debug("core: " + s);

    }

    /**
     * @param sahs_id
     */
    public final void launchItem(final String scoId) {

        if (isLaunching) {
            say("SCO " + olatScoId + " is launching.");
            return;
        }
        if (isLaunched && scoId.equals(olatScoId)) {
            say("SCO " + scoId + " is already running.");
            return;
        }
        olatScoCmi.clear();

        say("Launching sahs " + scoId);

        if (isLaunched) {
            say("SCO " + olatScoId + " will be unloaded.");

        } else {

            isLaunching = true;
            olatScoId = scoId;

            // putting all cmi from the olat storage to the local storage
            final LMSDataFormBean lmsDataBean = new LMSDataFormBean();
            lmsDataBean.setItemID(scoId);
            lmsDataBean.setLmsAction("get");
            odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
            final LMSResultsBean lmsBean = odatahandler.getResultsBean();
            olatScoCmi.clear();
            final String[][] strArr = lmsBean.getCmiStrings();
            String key = "";
            String value = "";

            if (strArr != null) {
                for (int i = 0; i < strArr.length; i++) {
                    key = strArr[i][0];
                    value = strArr[i][1];
                    olatScoCmi.put(key, value);
                    log.debug("passing cmi data to api adapter: " + key + ": " + value);

                }
            }
        }
    }

    public final void olatSetValue(final String l, String r) {
        if (r == null) {
            r = ""; // MSIE bug
        }
        say("OlatSetValue(" + l + "=" + r + ")");
        if (l != null) {
            olatScoCmi.put(l, r);
        }
    }

    public final void olatAbortSco(final String scoId) {
        if (!olatScoId.equals(scoId)) {
            return;
        }
        isLaunching = false;
        if (!isLaunched) {
            return;
        }
        say("Warning: sco " + scoId + " did not call LMSFinish()");
        olatFinish(false);
        core.reset();
    }

    private final void olatInitialize() {

        isLaunching = false;
        core.sysPut("cmi.core.student_id", olatStudentId);
        core.sysPut("cmi.core.student_name", olatStudentName);
        core.sysPut(olatScoCmi);
        core.transBegin();
        isLaunched = true;
    }

    private final void olatFinish(final boolean commit) {
        if (!isLaunched) {
            return;
        }
        isLaunched = false;
        if (commit) {
            olatCommit(false); // Stupid "implicit commit"
        }
    }

    /**
     * @param isACommit
     *            true, if the call comes from a lmscommit, false if it comes from a lmsfinish
     * @return
     */
    private final String olatCommit(final boolean isACommit) {
        if (olatScoId == null) {
            return "false";
        }

        core.transEnd();

        @SuppressWarnings("unchecked")
        final Hashtable<String, String> ins = core.getTransNew();
        @SuppressWarnings("unchecked")
        final Hashtable<String, String> mod = core.getTransMod();
        core.transBegin();

        final LMSDataFormBean lmsDataBean = new LMSDataFormBean();
        lmsDataBean.setItemID(olatScoId);
        // TODO:gs pass the dataBean for use, and do not get it a second time
        lmsDataBean.setNextAction("5");
        lmsDataBean.setLmsAction("update");
        final Map<String, String> cmiData = new HashMap<String, String>();

        // TODO:gs:c make it possible only to update the changed cmi data.
        if (ins.size() > 0) {
            final Set<String> set = ins.keySet();
            for (final Iterator<String> it = set.iterator(); it.hasNext();) {
                final String cmi = it.next();
                olatScoCmi.remove(cmi);
                olatScoCmi.put(cmi, ins.get(cmi));
            }
        }
        if (mod.size() > 0) {
            final Set<String> set = mod.keySet();
            for (final Iterator<String> it = set.iterator(); it.hasNext();) {
                final String cmi = it.next();
                olatScoCmi.remove(cmi);
                olatScoCmi.put(cmi, mod.get(cmi));
            }
        }
        cmiData.putAll(olatScoCmi);

        // work around for missing cmi's (needed by reload code, but not used in ilias code)
        if (cmiData.get("cmi.interactions._count") != null && cmiData.get("cmi.interactions._count") != "0") {
            final int count = Integer.parseInt(cmiData.get("cmi.interactions._count"));
            for (int i = 0; i < count; i++) {
                // OLAT-4271: check first if cmi.interactions.n.objectives._count exist before putting a default one
                final String objectivesCount = cmiData.get("cmi.interactions." + i + ".objectives._count");
                if (!StringHelper.containsNonWhitespace(objectivesCount)) {
                    cmiData.put("cmi.interactions." + i + ".objectives._count", "0");
                }
            }
        }
        if (isACommit) {
            final String rawScore = cmiData.get(SCORE_IDENT);
            if (rawScore != null && !rawScore.equals("")) {
                // we have a score set in this sco.
                // persist

                // to prevent problems with bad xmlhttprequest timings
                synchronized (this) { // o_clusterOK by:fj: instance is spawned by the ScormAPIandDisplayController
                    scoresProp.put(olatScoId, rawScore);
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(scorePropsFile));
                        scoresProp.store(os, null);
                    } catch (final IOException e) {
                        throw new OLATRuntimeException(this.getClass(), "could not save scorm-properties-file: " + scorePropsFile.getAbsolutePath(), e);
                    } finally {
                        FileUtils.closeSafely(os);
                    }
                    // notify
                    if (apiCallback != null) {
                        apiCallback.lmsCommit(olatScoId, scoresProp);
                    }
                }
            }
        }

        lmsDataBean.setDataAsMap(cmiData);
        odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
        odatahandler.updateCMIData(olatScoId);
        return "true";
    }

    /**
     * @return a String that points to the last accessed sco itemId
     */
    public String getScormLastAccessedItemId() {
        final LMSDataFormBean lmsDataBean = new LMSDataFormBean();
        lmsDataBean.setLmsAction("boot");
        odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
        final LMSResultsBean lmsBean = odatahandler.getResultsBean();
        return lmsBean.getItemID();
    }

    /**
     * @param itemId
     * @return true if the item is completed
     */
    public boolean isItemCompleted(final String itemId) {
        // TODO:gs make method faster by caching lmsBean, but when to set out of date?
        final LMSDataFormBean lmsDataBean = new LMSDataFormBean();
        lmsDataBean.setItemID(itemId);
        lmsDataBean.setLmsAction("get");
        odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
        final LMSResultsBean lmsBean = odatahandler.getResultsBean();
        return lmsBean.getIsItemCompleted().equals("true");
    }

    /**
     * @param itemId
     * @return true if item has any not fullfilled preconditions
     */
    public boolean hasItemPrerequisites(final String itemId) {
        // TODO:gs make method faster by caching lmsBean, but when to set out of date?
        final LMSDataFormBean lmsDataBean = new LMSDataFormBean();
        lmsDataBean.setItemID(itemId);
        lmsDataBean.setLmsAction("get");
        odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
        final LMSResultsBean lmsBean = odatahandler.getResultsBean();
        return lmsBean.getHasPrerequisites().equals("true");
    }

    /**
     * @return Map containing the recent sco items status
     */
    public Map<String, String> getScoItemsStatus() {
        final LMSDataFormBean lmsDataBean = new LMSDataFormBean();
        lmsDataBean.setLmsAction("boot");
        odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
        final LMSResultsBean lmsBean = odatahandler.getResultsBean();
        final String[][] preReqTbl = lmsBean.getPreReqTable();
        final Map<String, String> itemsStatus = new HashMap<String, String>();
        // put table into map
        for (int i = 0; i < preReqTbl.length; i++) {
            if (preReqTbl[i][1].equals("not attempted")) {
                preReqTbl[i][1] = "not_attempted";
            }
            itemsStatus.put(preReqTbl[i][0], preReqTbl[i][1]);
        }
        return itemsStatus;
    }

    /**
     * @param recentId
     * @return the previos Sco itemId
     */
    public Integer getPreviousSco(final String recentId) {
        // TODO:gs make method faster by caching lmsBean, but when to set out of date?
        final LMSDataFormBean lmsDataBean = new LMSDataFormBean();
        lmsDataBean.setItemID(recentId);
        lmsDataBean.setLmsAction("get");
        odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
        final LMSResultsBean lmsBean = odatahandler.getResultsBean();
        final String[][] pretable = lmsBean.getPreReqTable();
        String previousNavScoId = "-1";
        for (int i = 0; i < pretable.length; i++) {
            if (pretable[i][0].equals(recentId) && (i != 0)) {
                previousNavScoId = pretable[--i][0];
                break;
            }
        }
        return new Integer(previousNavScoId);
    }

    /**
     * @param recentId
     * @return the next Sco itemId
     */
    public Integer getNextSco(final String recentId) {
        // TODO:gs make method faster by chaching lmsBean, but when to set out of date?
        final LMSDataFormBean lmsDataBean = new LMSDataFormBean();
        lmsDataBean.setItemID(recentId);
        lmsDataBean.setLmsAction("get");
        odatahandler = new LMSDataHandler(scormManager, lmsDataBean, scormSettingsHandler);
        final LMSResultsBean lmsBean = odatahandler.getResultsBean();
        final String[][] pretable = lmsBean.getPreReqTable();
        String nextNavScoId = "-1";
        for (int i = 0; i < pretable.length; i++) {
            if (pretable[i][0].equals(recentId) && (i != pretable.length - 1)) {
                nextNavScoId = pretable[++i][0];
                break;
            }
        }
        return new Integer(nextNavScoId);
    }

    /****************************************************************************************
     * The API functions that an Scorm SCO can call
     * 
     */
    @Override
    public final String LMSInitialize(final String s) {
        String rv = core.LMSInitialize(s);
        say(" ----------------- ");
        say("LMSInitialize(" + s + ")=" + rv);
        if (rv.equals("false")) {
            return rv;
        }
        core.reset();
        rv = core.LMSInitialize(s);
        olatInitialize();
        return rv;
    }

    /**
	 */
    @Override
    public final String LMSCommit(final String s) {
        String rv = core.LMSCommit(s);
        if (rv.equals("false")) {
            return rv;
        }
        rv = olatCommit(true);
        say("LMSCommit(" + s + ")=" + rv);
        return rv;
    }

    /**
	 */
    @Override
    public final String LMSFinish(final String s) {
        final String rv = core.LMSFinish(s);
        say("LMSFinish(" + s + ")=" + rv);
        say(" ----------------- ");
        if (rv.equals("false")) {
            return rv;
        }
        olatFinish(true);
        core.reset();
        return rv;
    }

    /**
	 */
    @Override
    public final String LMSGetDiagnostic(final String e) {
        final String rv = core.LMSGetDiagnostic(e);
        say("LMSGetDiagnostic(" + e + ")=" + rv);
        return rv;
    }

    /**
	 */
    @Override
    public final String LMSGetErrorString(final String e) {
        final String rv = core.LMSGetErrorString(e);
        say("LMSGetErrorString(" + e + ")=" + rv);
        return rv;
    }

    /**
	 */
    @Override
    public final String LMSGetLastError() {
        final String rv = core.LMSGetLastError();
        say("LMSLastError()=" + rv);
        return rv;
    }

    /**
	 */
    @Override
    public final String LMSGetValue(final String l) {
        final String rv = core.LMSGetValue(l);
        say("LMSGetValue(" + l + ")=" + rv);
        return rv;
    }

    /**
	 */
    @Override
    public final String LMSSetValue(final String l, final String r) {
        final String rv = core.LMSSetValue(l, r);
        say("LMSSetValue(" + l + "=" + r + ")=" + rv);
        return rv;
    }
}
