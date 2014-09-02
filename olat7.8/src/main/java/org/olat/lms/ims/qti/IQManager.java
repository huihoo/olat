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

package org.olat.lms.ims.qti;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.VFSStatus;
import org.olat.data.qti.QTIResult;
import org.olat.data.qti.QTIResultSet;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.ims.qti.container.AssessmentContext;
import org.olat.lms.ims.qti.container.HttpItemInput;
import org.olat.lms.ims.qti.container.ItemContext;
import org.olat.lms.ims.qti.container.ItemInput;
import org.olat.lms.ims.qti.container.ItemsInput;
import org.olat.lms.ims.qti.container.SectionContext;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.ims.qti.process.FilePersister;
import org.olat.lms.ims.qti.process.Resolver;
import org.olat.lms.ims.qti.render.LocalizedXSLTransformer;
import org.olat.lms.ims.qti.render.ResultsBuilder;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.user.UserDataDeletable;
import org.olat.presentation.commons.OLATResourceableListeningWrapperController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.GenericMainController;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.control.generic.messages.MessageController;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.ims.qti.run.IQDisplayController;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.LockResult;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date: Mar 4, 2004
 * 
 * @author Mike Stock
 */
@Service
public class IQManager extends BasicManager implements UserDataDeletable {

    private static final Logger log = LoggerHelper.getLogger();

    private static IQManager INSTANCE;

    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    LockingService lockingService;

    /**
     * [spring]
     */
    private IQManager() {
        INSTANCE = this;
    }

    /**
     * @return Singleton.
     */
    @Deprecated
    public static IQManager getInstance() {
        return INSTANCE;
    }

    // --- methods for controller creation
    /**
     * IMS QTI Display Controller from within course -> moduleConfiguration concurrent access check needed -> Editor may save (commit changes) while displaying reads
     * old/new data mix (files and xml structure)
     */
    public Controller createIQDisplayController(final ModuleConfiguration moduleConfiguration, final IQSecurityCallback secCallback, final UserRequest ureq,
            final WindowControl wControl, final long callingResId, final String callingResDetail) {

        // two cases:
        // -- VERY RARE CASE -- 1) qti is open in an editor session right now on the screen (or session on the way to timeout)
        // -- 99% of cases -- 2) qti is ready to be run as test/survey
        final String repositorySoftkey = (String) moduleConfiguration.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true);
        if (lockingService.isLocked(re.getOlatResource(), null)) {
            final Translator translator = PackageUtil.createPackageTranslator(this.getClass(), ureq.getLocale());
            // so this resource is locked, let's find out who locked it
            final LockResult lockResult = lockingService.acquireLock(re.getOlatResource(), ureq.getIdentity(), null);
            return MessageUIFactory.createInfoMessage(ureq, wControl, translator.translate("status.currently.locked.title"),
                    translator.translate("status.currently.locked", new String[] { lockResult.getOwner().getName() }));
        } else {
            ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(re, OlatResourceableType.iq));
            return new IQDisplayController(moduleConfiguration, secCallback, ureq, wControl, callingResId, callingResDetail);
        }
    }

    /**
     * IMS QTI Display Controller used by QTI Editor for preview. no concurrency protection needed here -> it is Editor <-> Preview of edited file
     * 
     * @param resolver
     * @param type
     * @param secCallback
     * @param ureq
     * @param wControl
     */
    public IQDisplayController createIQDisplayController(final Resolver resolver, final String type, final IQSecurityCallback secCallback, final UserRequest ureq,
            final WindowControl wControl) {
        return new IQDisplayController(resolver, type, secCallback, ureq, wControl);
    }

    /**
     * IMS QTI Display Controller used for IMS course node run view, or for the direct launching from learning resources. concurrent access check needed -> Editor may
     * save (commit changes) while displaying reads old/new data mix (files and xml structure)
     * 
     * @param res
     * @param resolver
     * @param type
     * @param secCallback
     * @param ureq
     * @param wControl
     * @return
     */
    public MainLayoutController createIQDisplayController(final OLATResourceable res, final Resolver resolver, final String type, final IQSecurityCallback secCallback,
            final UserRequest ureq, final WindowControl wControl) {
        ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(res, OlatResourceableType.iq));

        // two cases:
        // -- VERY RARE CASE -- 1) qti is open in an editor session right now on the screen (or session on the way to timeout)
        // -- 99% of cases -- 2) qti is ready to be run as test/survey
        if (lockingService.isLocked(res, null)) {
            final GenericMainController glc = createLockedMessageController(ureq, wControl);
            glc.init(ureq);
            return glc;
        } else {
            final Controller controller = new IQDisplayController(resolver, type, secCallback, ureq, wControl);
            final OLATResourceableListeningWrapperController dwc = new OLATResourceableListeningWrapperController(ureq, wControl, res, controller, ureq.getIdentity());
            return dwc;
        }
    }

    private GenericMainController createLockedMessageController(final UserRequest ureq, final WindowControl wControl) {
        //
        // wrap simple message into mainLayout
        final GenericMainController glc = new GenericMainController(ureq, wControl) {

            private MessageController contentCtr;
            private Panel empty;
            private LayoutMain3ColsController columnLayoutCtr;

            @Override
            public void init(final UserRequest ureq) {
                empty = new Panel("empty");
                final Translator translator = PackageUtil.createPackageTranslator(this.getClass(), ureq.getLocale());
                contentCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), translator.translate("status.currently.locked.title"),
                        translator.translate("status.currently.locked"));
                listenTo(contentCtr); // auto dispose later
                final Component resComp = contentCtr.getInitialComponent();

                columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), empty, empty, resComp, /* do not save no prefs */null);
                listenTo(columnLayoutCtr); // auto dispose later
                putInitialPanel(columnLayoutCtr.getInitialComponent());
            }

            @Override
            protected Controller handleOwnMenuTreeEvent(final Object uobject, final UserRequest ureq) {
                // no menutree means no menu events.
                return null;
            }

        };
        return glc;
    }

    // --- end of controller creation
    /**
     * @param ai
     * @param ureq
     * @return
     */
    public Document getResultsReporting(final AssessmentInstance ai, final UserRequest ureq) {
        final ResultsBuilder resB = new ResultsBuilder();
        return resB.getResDoc(ai, ureq.getLocale(), ureq.getIdentity());
    }

    /**
     * @param identity
     * @param type
     * @param assessID
     * @return
     */
    public Document getResultsReportingFromFile(final Identity identity, final String type, final long assessID) {
        return FilePersister.retreiveResultsReporting(identity, type, assessID);
    }

    /**
     * @param docResReporting
     * @param locale
     * @param detailed
     * @return
     */
    public String transformResultsReporting(final Document docResReporting, final Locale locale, final int summaryType) {
        switch (summaryType) {
        case AssessmentInstance.SUMMARY_COMPACT: // Result summary without solutions
            ResultsBuilder.stripDetails(docResReporting);
            break;
        case AssessmentInstance.SUMMARY_SECTION: // Section summary without solutions
            ResultsBuilder.stripItemResults(docResReporting);
            System.out.println("TEST: Section summary without solutions");
            break;
        case AssessmentInstance.SUMMARY_DETAILED:// Strip nothing
            break;
        default: // default => Strip nothing
            break;
        }
        final StringBuilder sb = LocalizedXSLTransformer.getInstance(locale).renderResults(docResReporting);
        return sb.toString();
    }

    /**
     * Extract item inputs from http request
     * 
     * @param ureq
     *            The request to extract item responses from.
     * @return ItemsInput
     */
    public ItemsInput getItemsInput(final UserRequest ureq) {
        final ItemsInput result = new ItemsInput(ureq);
        final Enumeration<?> params = ureq.getHttpReq().getParameterNames();
        while (params.hasMoreElements()) {
            final String paramKey = (String) params.nextElement();
            final StringTokenizer st = new StringTokenizer(paramKey, "§", false);
            final String value = ureq.getParameter(paramKey);
            if (st.countTokens() == 4) {
                final String itemType = st.nextToken();
                final String itemIdent = st.nextToken();
                final String responseID = st.nextToken();
                HttpItemInput itemInput = (HttpItemInput) result.getItemInput(itemIdent);
                if (itemInput == null) {
                    itemInput = new HttpItemInput(itemIdent);
                    result.addItemInput(itemInput);
                }
                // 'dummy' type is used to make sure iteminput is constructed for
                // all items. it does not provide any response data
                if (itemType.equals("qti")) {
                    itemInput.putSingle(responseID, value);
                }
            }
            // refactoring to new setFormDirty() javascript method sends now an additional param "olat_fosm" which has no tokens inside
            // so assertExc. is useless.
            // else {
            // throw new AssertException ("not 4 tokens in form name: orig='"+paramKey+"'");
            // }
            // <input id="QTI_1098869464495" type="checkbox"
            // name="qti§QTIEDIT:MCQ:1098869464490§1098869464492§1098869464495" ....
        }
        return result;
    }

    /**
     * Create the QTIResults on the database for a given assessments, self-assessment or survey. These database entries can be used for statistical downloads.
     * 
     * @param ai
     * @param resId
     * @param resDetail
     * @param ureq
     */

    public void persistResults(final AssessmentInstance ai, final long resId, final String resDetail, final UserRequest ureq) {
        final AssessmentContext ac = ai.getAssessmentContext();

        final QTIResultSet qtiResultSet = new QTIResultSet();
        qtiResultSet.setLastModified(new Date(System.currentTimeMillis()));
        qtiResultSet.setOlatResource(resId);
        qtiResultSet.setOlatResourceDetail(resDetail);
        qtiResultSet.setRepositoryRef(ai.getRepositoryEntryKey());
        qtiResultSet.setIdentity(ureq.getIdentity());
        qtiResultSet.setQtiType(ai.getType());
        qtiResultSet.setAssessmentID(ai.getAssessID());

        qtiResultSet.setDuration(new Long(ai.getAssessmentContext().getDuration()));
        // TODO qtiResultSet.setLastModified();

        if (ai.isSurvey()) {
            qtiResultSet.setScore(0);
            qtiResultSet.setIsPassed(true);
        } else {
            qtiResultSet.setScore(ac.getScore());
            qtiResultSet.setIsPassed(ac.isPassed());
        }

        DBFactory.getInstance().saveObject(qtiResultSet);

        // Loop over all sections in this assessment
        final int sccnt = ac.getSectionContextCount();
        for (int i = 0; i < sccnt; i++) {
            // Loop over all items in this section
            final SectionContext sc = ac.getSectionContext(i);
            final int iccnt = sc.getItemContextCount();
            for (int j = 0; j < iccnt; j++) {
                final ItemContext ic = sc.getItemContext(j);
                // Create new result item for this item
                final QTIResult qtiResult = new QTIResult();
                qtiResult.setResultSet(qtiResultSet);
                qtiResult.setItemIdent(ic.getIdent());
                qtiResult.setDuration(new Long(ic.getTimeSpent()));
                if (ai.isSurvey()) {
                    qtiResult.setScore(0);
                } else {
                    qtiResult.setScore(ic.getScore());
                }
                qtiResult.setTstamp(new Date(ic.getLatestAnswerTime()));
                qtiResult.setLastModified(new Date(System.currentTimeMillis()));
                qtiResult.setIp(ureq.getHttpReq().getRemoteAddr());

                // Get user answers for this item
                final StringBuilder sb = new StringBuilder();
                if (ic.getItemInput() == null) {
                } else {
                    final ItemInput inp = ic.getItemInput();
                    if (inp.isEmpty()) {
                        sb.append("[]");
                    } else {
                        final Map im = inp.getInputMap();
                        // Create answer block
                        final Set keys = im.keySet();
                        final Iterator iter = keys.iterator();
                        while (iter.hasNext()) {
                            final String ident = (String) iter.next();
                            sb.append(ident); // response_lid ident
                            sb.append("[");
                            final List answers = inp.getAsList(ident);
                            for (int y = 0; y < answers.size(); y++) {
                                sb.append("[");
                                String answer = (String) answers.get(y);
                                // answer is referenced to response_label ident, if
                                // render_choice
                                // answer is userinput, if render_fib
                                answer = quoteSpecialQTIResultCharacters(answer);
                                sb.append(answer);
                                sb.append("]");
                            }
                            sb.append("]");
                        }
                    }
                }
                qtiResult.setAnswer(sb.toString());
                // Persist result data in database
                DBFactory.getInstance().saveObject(qtiResult);
            }
        }
    }

    /**
     * Qotes special characters used by the QTIResult answer formatting. Special characters are '\', '[', ']', '\t', '\n', '\r', '\f', '\a' and '\e'
     * 
     * @param string
     *            The string to be quoted
     * @return The quoted string
     */
    public String quoteSpecialQTIResultCharacters(String string) {
        string = string.replaceAll("\\\\", "\\\\\\\\");
        string = string.replaceAll("\\[", "\\\\[");
        string = string.replaceAll("\\]", "\\\\]");
        string = string.replaceAll("\\t", "\\\\t");
        string = string.replaceAll("\\n", "\\\\n");
        string = string.replaceAll("\\r", "\\\\r");
        string = string.replaceAll("\\f", "\\\\f");
        string = string.replaceAll("\\a", "\\\\a");
        string = string.replaceAll("\\e", "\\\\e");
        return string;
    }

    /**
     * Unquotes special characters in the QTIResult answer texts.
     * 
     * @param string
     * @return The unquoted sting
     */
    public String unQuoteSpecialQTIResultCharacters(String string) {
        string = string.replaceAll("\\\\[", "\\[");
        string = string.replaceAll("\\\\]", "\\]");
        string = string.replaceAll("\\\\t", "\\t");
        string = string.replaceAll("\\\\n", "\\n");
        string = string.replaceAll("\\\\r", "\\r");
        string = string.replaceAll("\\\\f", "\\f");
        string = string.replaceAll("\\\\a", "\\a");
        string = string.replaceAll("\\\\e", "\\e");
        string = string.replaceAll("\\\\\\\\", "\\\\");
        return string;
    }

    /**
     * Delete all qti.ser and qti-resreporting files.
     * 
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        FilePersister.deleteUserData(identity);
        log.debug("Delete all qti.ser data and qti-resreporting data for identity=" + identity);
    }

    /**
     * Returns null if no QTIResultSet found.
     * 
     * @param identity
     * @param olatResource
     * @param olatResourceDetail
     * @return Returns the last recorded QTIResultSet
     */
    public QTIResultSet getLastResultSet(final Identity identity, final long olatResource, final String olatResourceDetail) {
        QTIResultSet returnQTIResultSet = null;
        final DB persister = DBFactory.getInstance();
        final List resultSetList = persister.find("from q in class QTIResultSet where "
                + "q.identity = ? and q.olatResource = ? and q.olatResourceDetail = ? order by q.creationDate desc", new Object[] { identity.getKey(),
                new Long(olatResource), new String(olatResourceDetail) }, new Type[] { Hibernate.LONG, Hibernate.LONG, Hibernate.STRING });
        final Iterator resultSetIterator = resultSetList.iterator();
        while (resultSetIterator.hasNext()) {
            returnQTIResultSet = (QTIResultSet) resultSetIterator.next();
            break;
        }
        return returnQTIResultSet;
    }

    /**
     * This should only be used as fallback solution if the assessmentID is not available via the AssessmentManager (migration of old tests)
     * 
     * @param identity
     * @param olatResource
     *            is the course id
     * @param olatResourceDetail
     *            is the node id
     * @return Returns the last assessmentID if at least a QTIResultSet was stored for the input variables, null otherwise.
     */
    public Long getLastAssessmentID(final Identity identity, final long olatResource, final String olatResourceDetail) {
        final QTIResultSet resultSet = getLastResultSet(identity, olatResource, olatResourceDetail);
        if (resultSet != null) {
            return resultSet.getAssessmentID();
        }
        return null;
    }

    /**
     * Get identities with exists qti.ser file.
     * 
     * @param resourceableId
     * @param ident
     * @return
     */
    public List<Identity> getIdentitiesWithQtiSerEntry(final Long resourceableId, final String ident) {
        final List<Identity> identities = new ArrayList<Identity>();
        final LocalFolderImpl item = new LocalFolderImpl(new File(FilePersister.getFullPathToCourseNodeDirectory(Long.toString(resourceableId), ident)));
        if (VFSManager.exists(item)) {
            for (final VFSItem identityFolder : item.getItems()) {
                final Identity identity = baseSecurity.findIdentityByName(identityFolder.getName());
                if (identity != null) {
                    identities.add(identity);
                }
            }
        }

        return identities;
    }

    /**
     * Removes course node directory including qti.ser files of different users.
     * 
     * @param resourceableId
     * @param ident
     * @return
     */
    public VFSStatus removeQtiSerFiles(final Long resourceableId, final String ident) {
        if (resourceableId == null || ident == null || ident.length() == 0) {
            return VFSConstants.NO;
        }
        final LocalFolderImpl item = new LocalFolderImpl(new File(FilePersister.getFullPathToCourseNodeDirectory(Long.toString(resourceableId), ident)));
        if (item.canDelete().equals(VFSConstants.YES)) {
            return item.delete();
        }
        return VFSConstants.NO;
    }

}
