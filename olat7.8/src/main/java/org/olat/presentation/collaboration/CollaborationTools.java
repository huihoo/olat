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

package org.olat.presentation.collaboration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.forum.Forum;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.resource.OLATResource;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.calendar.CalendarConfig;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.core.notification.service.Subscribed;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.forum.ForumCallback;
import org.olat.lms.forum.ForumService;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.lms.portfolio.security.EPSecurityCallbackImpl;
import org.olat.lms.properties.PropertyManagerEBL;
import org.olat.lms.properties.PropertyParameterObject;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiSecurityCallback;
import org.olat.lms.wiki.WikiSecurityCallbackImpl;
import org.olat.presentation.calendar.CalendarController;
import org.olat.presentation.calendar.WeeklyCalendarController;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.contactform.ContactFormController;
import org.olat.presentation.contactform.ContactFormView;
import org.olat.presentation.contactform.ContactUIModel;
import org.olat.presentation.course.calendar.CourseLinkProviderController;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.forum.ForumUIFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.title.TitleInfo;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.instantmessaging.groupchat.GroupChatManagerController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.wiki.WikiUIFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description:<BR>
 * The singleton used for retrieving a collaboration tools suite associated with the supplied OLATResourceable.
 * <P>
 * Description: <br>
 * The CollaborationTools represents a suite of collaborative tools addeable to any OLATResourceable. To get an instance of this suite, one has to use the collaboration
 * tools factory.
 * <p>
 * This collaboration tools class exposes the possibility to retrieve the appropriate controllers for the desired tools. And also provides the means to manage the
 * configuration of the provided tools. Moreover it is already shipped with a controller which can be used to display an administrative view for enabling/disabling such
 * tools for the supplied OLATResourceable.
 * <p>
 * All the future collaborative tools will be found here.
 * 
 * @author Felix Jost
 * @author guido
 */
public class CollaborationTools implements Serializable {

    boolean dirty = false;
    private final static String TRUE = "true";
    private final static String FALSE = "false";
    /**
     * constant used to identify the calendar for a BuddyGroup
     */
    public final static String TOOL_CALENDAR = "hasCalendar";
    /**
     * constant used to identify the forum for a BuddyGroup
     */
    public final static String TOOL_FORUM = "hasForum";
    /**
     * constant used to identify the folder for a BuddyGroup
     */
    public final static String TOOL_FOLDER = "hasFolder";
    /**
     * constant used to identify the chat for a BuddyGroup
     */
    public final static String TOOL_CHAT = "hasChat";
    /**
     * constant used to identify the contact form for a BuddyGroup
     */
    public final static String TOOL_CONTACT = "hasContactForm";
    /**
     * constant used to identify the contact form for a BuddyGroup
     */
    public final static String TOOL_NEWS = "hasNews";
    /**
     * constant used to identify the wiki for a BuddyGroup
     */
    public final static String TOOL_WIKI = "hasWiki";

    /**
     * constant used to identify the portfolio for a BuddyGroup
     */
    public final static String TOOL_PORTFOLIO = "hasPortfolio";

    /**
     * public for group test only, do not use otherwise convenience, helps iterating possible tools, i.e. in jUnit testCase, also for building up a tools choice
     */
    public static String[] TOOLS;

    /**
     * Only owners have write access to the calendar.
     */
    public static final int CALENDAR_ACCESS_OWNERS = 0;
    /**
     * Owners and members have write access to the calendar.
     */
    public static final int CALENDAR_ACCESS_ALL = 1;

    /**
     * cache for Boolean Objects representing the State
     */
    // o_clusterOK by guido
    Hashtable<String, Boolean> cacheToolStates;
    final OLATResourceable ores;

    private static final Logger log = LoggerHelper.getLogger();

    private transient CalendarService calendarService;

    public static String getFolderRelPath(final OLATResourceable ores) {
        return "/cts/folders/" + ores.getResourceableTypeName() + "/" + ores.getResourceableId();
    }

    /**
     * package local constructor only
     * 
     * @param ores
     */
    CollaborationTools(final OLATResourceable ores) {
        calendarService = CoreSpringFactory.getBean(CalendarService.class);
        this.ores = ores;
        cacheToolStates = new Hashtable<String, Boolean>();
        if (InstantMessagingModule.isEnabled()) {
            TOOLS = new String[] { TOOL_NEWS, TOOL_CONTACT, TOOL_CALENDAR, TOOL_FOLDER, TOOL_FORUM, TOOL_CHAT, TOOL_WIKI, TOOL_PORTFOLIO };
        } else {
            TOOLS = new String[] { TOOL_NEWS, TOOL_CONTACT, TOOL_CALENDAR, TOOL_FOLDER, TOOL_FORUM, TOOL_WIKI, TOOL_PORTFOLIO };
        }
    }

    /**
     * @param ureq
     * @return a news controller
     */
    public Controller createNewsController(final UserRequest ureq, final WindowControl wControl) {
        final String news = lookupNews();
        return new SimpleNewsController(ureq, wControl, news);
    }

    /**
     * TODO: rename to getForumController and save instance?
     * 
     * @param ureq
     * @param wControl
     * @param isAdmin
     * @param subsContext
     *            the subscriptioncontext if subscriptions to this forum should be possible
     * @return a forum controller
     */
    public Controller createForumController(final UserRequest ureq, final WindowControl wControl, final boolean isAdmin, final boolean isGuestOnly,
            final SubscriptionContext subsContext) {
        Codepoint.codepoint(CollaborationTools.class, "createForumController-init");
        final boolean isAdm = isAdmin;
        final boolean isGuest = isGuestOnly;

        // TODO: is there a nicer solution without setting an instance variable
        // final List<Forum> forumHolder = new ArrayList<Forum>();

        Codepoint.codepoint(CollaborationTools.class, "pre_sync_enter");

        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_FORUM).build();
        final Forum forum = getPropertyManagerEBL().getCollaborationToolsForum(propertyParameterObject);

        final Translator trans = PackageUtil.createPackageTranslator(this.getClass(), ureq.getLocale());
        final TitleInfo titleInfo = new TitleInfo(null, trans.translate("collabtools.named.hasForum"));
        titleInfo.setSeparatorEnabled(true);
        final Controller forumController = ForumUIFactory.getTitledForumController(ureq, wControl, forum, new ForumCallback() {

            @Override
            public boolean mayOpenNewThread() {
                return true;
            }

            @Override
            public boolean mayReplyMessage() {
                return true;
            }

            @Override
            public boolean mayEditMessageAsModerator() {
                return isAdm;
            }

            @Override
            public boolean mayDeleteMessageAsModerator() {
                return isAdm;
            }

            @Override
            public boolean mayArchiveForum() {
                return !isGuest;
            }

            @Override
            public boolean mayFilterForUser() {
                return isAdm;
            }

            @Override
            public SubscriptionContext getSubscriptionContext() {
                return subsContext;
            }
        }, titleInfo);
        return forumController;
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    public String getFolderRelPath() {
        return getFolderRelPath(ores);
    }

    /**
     * Creates a folder run controller with all rights enabled for everybody
     * 
     * @param ureq
     * @param wControl
     * @param subsContext
     * @return Configured FolderRunController
     */
    public FolderRunController createFolderController(final UserRequest ureq, final WindowControl wControl, final SubscriptionContext subsContext) {
        // do not use a global translator since in the future a
        // collaboration tool may be shared among users
        final Translator trans = PackageUtil.createPackageTranslator(this.getClass(), ureq.getLocale());
        final String relPath = getFolderRelPath();
        final OlatRootFolderImpl rootContainer = new OlatRootFolderImpl(relPath, null);
        final OlatNamedContainerImpl namedContainer = new OlatNamedContainerImpl(trans.translate("folder"), rootContainer);
        namedContainer.setLocalSecurityCallback(new CollabSecCallback(relPath, subsContext));
        final FolderRunController frc = new FolderRunController(namedContainer, true, true, ureq, wControl);
        return frc;
    }

    /**
     * Creates a calendar controller
     * 
     * @param ureq
     * @param wControl
     * @param resourceableId
     * @return Configured WeeklyCalendarController
     */
    public CalendarController createCalendarController(final UserRequest ureq, final WindowControl wControl, final BusinessGroup businessGroup, final boolean isAdmin) {
        // do not use a global translator since in the future a
        // collaboration tool may be shared among users
        final List<CalendarRenderWrapper> calendars = new ArrayList<CalendarRenderWrapper>();
        // get the calendar
        final OlatCalendar groupCalendar = calendarService.getGroupCalendar(businessGroup);
        final CalendarRenderWrapper calRenderWrapper = CalendarRenderWrapper.wrapGroupCalendar(groupCalendar, businessGroup);
        final boolean isOwner = getBaseSecurityEBL().isOwner(ureq.getIdentity(), businessGroup.getOwnerGroup());
        if (!(isAdmin || isOwner)) {
            // check if participants have read/write access
            int iCalAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
            final Long lCalAccess = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup).lookupCalendarAccess();
            if (lCalAccess != null) {
                iCalAccess = lCalAccess.intValue();
            }
            if (iCalAccess == CollaborationTools.CALENDAR_ACCESS_ALL) {
                calRenderWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_WRITE);
            } else {
                calRenderWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_ONLY);
            }
        } else {
            calRenderWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_WRITE);
        }
        final CalendarConfig config = calendarService.findCalendarConfigForIdentity(calRenderWrapper.getCalendar(), ureq.getUserSession().getGuiPreferences());
        if (config != null) {
            calRenderWrapper.getCalendarConfig().setCss(config.getCss());
            calRenderWrapper.getCalendarConfig().setVis(config.isVis());
        }
        calRenderWrapper.getCalendarConfig().setResId(businessGroup.getKey());
        if (businessGroup.getType().equals(BusinessGroup.TYPE_LEARNINGROUP)) {
            // add linking
            final List<OLATResource> resources = BGContextDaoImpl.getInstance().findOLATResourcesForBGContext(businessGroup.getGroupContext());
            for (final Iterator<OLATResource> iter = resources.iterator(); iter.hasNext();) {
                final OLATResource resource = iter.next();
                if (resource.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
                    final ICourse course = CourseFactory.loadCourse(resource);
                    final CourseLinkProviderController clp = new CourseLinkProviderController(course, ureq, wControl);
                    calRenderWrapper.setLinkProvider(clp);
                    // for the time being only internal learning groups are
                    // supported, therefore we only get
                    // the first course reference.
                    break;
                }
            }
        }
        calendars.add(calRenderWrapper);

        final WeeklyCalendarController calendarController = new WeeklyCalendarController(ureq, wControl, calendars, WeeklyCalendarController.CALLER_COLLAB, true);

        return calendarController;
    }

    /**
     * @param ureq
     * @param wControl
     * @return a contact form controller
     */
    public ContactFormController createContactFormController(final UserRequest ureq, final WindowControl wControl, final ContactMessage contactMessage) {
        boolean isCanceable = true;
        boolean isReadonly = false;
        boolean hasRecipientsEditable = false;
        boolean hasAtLeastOneAddress = contactMessage.hasAtLeastOneAddress();
        Identity emailFrom = ureq.getIdentity();

        ContactFormView contactFormView = new ContactFormView(ureq, wControl, emailFrom, hasAtLeastOneAddress, isReadonly, isCanceable, hasRecipientsEditable);
        ContactUIModel contactUIModel = new ContactUIModel(contactMessage);

        return new ContactFormController(contactFormView, contactUIModel);
    }

    /**
     * @param ureq
     * @param wControl
     * @param chatName
     * @return Controller
     */
    public Controller createChatController(final UserRequest ureq, final WindowControl wControl, final BusinessGroup grp) {
        if (InstantMessagingModule.isEnabled()) {
            GroupChatManagerController ccmc;
            ccmc = InstantMessagingModule.getAdapter().getGroupChatManagerController(ureq);
            ccmc.createGroupChat(ureq, wControl, ores, grp.getName(), false, false);
            return ccmc.getGroupChatController(ores);

        } else {
            throw new AssertException("cannot create a chat controller when instant messasging is disabled in the configuration");
        }
    }

    /**
     * return an controller for the wiki tool
     * 
     * @param ureq
     * @param wControl
     * @return
     */
    public Controller createWikiController(final UserRequest ureq, final WindowControl wControl) {
        // Check for jumping to certain wiki page
        final BusinessControl bc = wControl.getBusinessControl();
        final ContextEntry ce = bc.popLauncherContextEntry();

        // final SubscriptionContext subContext = new SubscriptionContext(ores, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
        SubscriptionContext noNotificationSubscriptionContext = null;
        final boolean isOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        final boolean isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
        final boolean isResourceOwner = getBaseSecurityEBL().isIdentityPermittedOnResourceable(ureq.getIdentity(), ores);
        final WikiSecurityCallback callback = new WikiSecurityCallbackImpl(null, isOlatAdmin, isGuestOnly, true, isResourceOwner, noNotificationSubscriptionContext);
        if (ce != null) { // jump to a certain context
            final OLATResourceable ceOres = ce.getOLATResourceable();
            final String typeName = ceOres.getResourceableTypeName();
            String page = typeName.substring("page=".length());
            if (page != null && page.endsWith(":0")) {
                page = page.substring(0, page.length() - 2);
            }
            return WikiUIFactory.getInstance().createWikiMainController(ureq, wControl, ores, callback, page);
        } else {
            return WikiUIFactory.getInstance().createWikiMainController(ureq, wControl, ores, callback, null);
        }
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return (BaseSecurityEBL) CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
     * return an controller for the wiki tool
     * 
     * @param ureq
     * @param wControl
     * @return
     */
    public Controller createPortfolioController(final UserRequest ureq, final WindowControl wControl, final BusinessGroup group) {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_PORTFOLIO).group(group).build();
        final PortfolioStructureMap map = getPropertyManagerEBL().getCollaborationToolsPortfolioStructureMap(propertyParameterObject);

        final EPSecurityCallback secCallback = new EPSecurityCallbackImpl(true, true);
        return EPUIFactory.createMapViewController(ureq, wControl, map, secCallback);
    }

    /**
     * @param toolToChange
     * @param enable
     */
    public void setToolEnabled(final String toolToChange, final boolean enable) {
        createOrUpdateProperty(toolToChange, enable);
    }

    /**
     * reads from the internal cache. <b>Precondition </b> cache must be filled at CollaborationTools creation time.
     * 
     * @param enabledTool
     * @return boolean
     */
    public boolean isToolEnabled(final String enabledTool) {
        // o_clusterOK as whole object gets invalidated if tool is added or
        // deleted
        if (!cacheToolStates.containsKey(enabledTool)) {
            // not in cache yet, read property first (see getPropertyOf(..))
            getPropertyOf(enabledTool);
        }
        // POSTCONDITION: cacheToolStates.get(enabledTool) != null
        final Boolean cachedValue = cacheToolStates.get(enabledTool);
        return cachedValue.booleanValue();
    }

    /**
     * delete all CollaborationTools stuff from the database, which is related to the calling OLATResourceable.
     */
    public void deleteTools(final BusinessGroup businessGroupTodelete) {
        /*
         * delete the forum, if existing
         */
        final ForumService fom = getForumService();
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_FORUM).build();
        final PropertyImpl forumKeyProperty = getPropertyManagerEBL().findProperty(propertyParameterObject);
        if (forumKeyProperty != null) {
            // if there was a forum, delete it
            final Long forumKey = forumKeyProperty.getLongValue();
            if (forumKey == null) {
                throw new AssertException("property had no longValue, prop:" + forumKeyProperty);
            }
            fom.deleteForum(forumKey);
        }

        getCollaborationToolsEBL().deleteCollaborationToolsFolder(getFolderRelPath());

        /*
         * delete the wiki if existing
         */
        final VFSContainer rootContainer = WikiManager.getInstance().getWikiRootContainer(ores);
        if (rootContainer != null) {
            rootContainer.delete();
        }

        /*
         * Delete calendar if exists
         */
        if (businessGroupTodelete != null) {
            calendarService.deleteGroupCalendar(businessGroupTodelete);
        }

        /*
         * delete chatRoom
         */
        // no cleanup needed, automatically done when last user exits the room
        /*
         * delete all Properties defining enabled/disabled CollabTool XY and the news content
         */
        propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS).build();
        getPropertyManagerEBL().deleteProperties(propertyParameterObject);

        /*
         * and last but not least the cache is reseted
         */
        cacheToolStates.clear();
        this.dirty = true;
    }

    /**
     * creates the property if non-existing, or updates the existing property to the supplied values. Real changes are made persistent immediately.
     * 
     * @param selectedTool
     * @param toolValue
     */
    private void createOrUpdateProperty(final String selectedTool, final boolean toolValue) {

        final Boolean cv = cacheToolStates.get(selectedTool);
        if (cv != null && cv.booleanValue() == toolValue) {
            return; // nice, cache saved a needless update
        }

        // handle Boolean Values via String Field in Property DB Table
        final String toolValueStr = toolValue ? TRUE : FALSE;
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS).resourceable(ores)
                .name(selectedTool).stringValue(toolValueStr).build();
        getPropertyManagerEBL().createOrUpdatePropertyForCollaborationTool(propertyParameterObject);
        this.dirty = true;
        cacheToolStates.put(selectedTool, Boolean.valueOf(toolValue));
    }

    private PropertyImpl getPropertyOf(final String selectedTool) {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(selectedTool).build();
        final PropertyImpl property = getPropertyManagerEBL().findProperty(propertyParameterObject);
        Boolean res;
        if (property == null) { // meaning false
            res = Boolean.FALSE;
        } else {
            final String val = property.getStringValue();
            res = val.equals(TRUE) ? Boolean.TRUE : Boolean.FALSE;
        }
        cacheToolStates.put(selectedTool, res);
        return property;
    }

    /**
     * create the Collaboration Tools Suite. This Controller handles the enabling/disabling of Collab Tools.
     * 
     * @param ureq
     * @return a collaboration tools settings controller
     */
    public CollaborationToolsSettingsController createCollaborationToolsSettingsController(final UserRequest ureq, final WindowControl wControl, final BGConfigFlags flags) {
        return new CollaborationToolsSettingsController(ureq, wControl, ores, flags);
    }

    /**
     * @return the news; if there is no news yet: return null;
     */
    public String lookupNews() {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_NEWS).build();
        return getPropertyManagerEBL().lookupCollaborationToolsNews(propertyParameterObject);
    }

    /**
     * @param news
     */
    public void saveNews(final String news) {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_NEWS).textValue(news).build();
        getPropertyManagerEBL().saveCollaborationToolsNews(propertyParameterObject);
    }

    public Long lookupCalendarAccess() {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_CALENDAR_ACCESS).build();
        return getPropertyManagerEBL().lookupCollaborationToolsCalendarAccess(propertyParameterObject);
    }

    public void saveCalendarAccess(final Long calendarAccess) {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_CALENDAR_ACCESS).longValue(calendarAccess).build();
        getPropertyManagerEBL().saveCollaborationToolsCalendarAccess(propertyParameterObject);
    }

    public class CollabSecCallback implements VFSSecurityCallback, Subscribed {

        private Quota folderQuota = null;
        private final SubscriptionContext subsContext;

        public CollabSecCallback(final String relPath, final SubscriptionContext subsContext) {
            this.subsContext = subsContext;
            initFolderQuota(relPath);
        }

        private void initFolderQuota(final String relPath) {
            final QuotaManager qm = QuotaManager.getInstance();
            folderQuota = qm.getCustomQuota(relPath);
            if (folderQuota == null) {
                final Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS);
                folderQuota = QuotaManager.getInstance().createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
            }
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public boolean canDelete() {
            return true;
        }

        @Override
        public boolean canList() {
            return true;
        }

        @Override
        public boolean canCopy() {
            return true;
        }

        @Override
        public boolean canDeleteRevisionsPermanently() {
            return true;
        }

        @Override
        public Quota getQuota() {
            return folderQuota;
        }

        @Override
        public void setQuota(final Quota quota) {
            this.folderQuota = quota;
        }

        @Override
        public SubscriptionContext getSubscriptionContext() {
            return subsContext;
        }
    }

    /**
     * It is assumed that this is only called by an administrator (e.g. at deleteGroup)
     * 
     * @param archivFilePath
     */
    public void archive(final String archivFilePath) {
        if (isToolEnabled(CollaborationTools.TOOL_FORUM)) {
            getCollaborationToolsEBL().archiveForum(ores, archivFilePath);
        }
        if (isToolEnabled(CollaborationTools.TOOL_WIKI)) {
            getCollaborationToolsEBL().archiveWiki(ores, archivFilePath);
        }
        if (isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
            getCollaborationToolsEBL().archiveCollaborationToolsFolder(ores, archivFilePath, getFolderRelPath());
        }
    }

    /**
     * whole object gets cached, if tool gets added or deleted the object becomes dirty and will be removed from cache.
     * 
     * @return
     */
    protected boolean isDirty() {
        return dirty;
    }

    private PropertyManagerEBL getPropertyManagerEBL() {
        return CoreSpringFactory.getBean(PropertyManagerEBL.class);
    }

    private CollaborationToolsEBL getCollaborationToolsEBL() {
        return CoreSpringFactory.getBean(CollaborationToolsEBL.class);
    }
}
