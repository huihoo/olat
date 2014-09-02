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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.portfolio.structel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.portfolio.structure.EPStructuredMap;
import org.olat.data.portfolio.structure.EPTargetResource;
import org.olat.data.portfolio.structure.ElementType;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.EPLoggingAction;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.PortfolioModule;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.lms.portfolio.security.EPSecurityCallbackFactory;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.home.site.HomeSite;
import org.olat.presentation.portfolio.EPMapRunViewOption;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Present a list of maps. allows: - Open a map - delete a map - copy a map with or without artefacts and open it
 * <P>
 * Initial Date: 04.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMultipleMapController extends BasicController implements Activateable {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String RESTRICT_LINK = "restrictLink";
    private static final String VIEW_LINK_PREFIX = "viewLink";
    private static final String DELETE_LINK_PREFIX = "deleteLink";
    private static final String COPY_LINK_PREFIX = "copyLink";
    private static final String SHARE_LINK_PREFIX = "shareLink";
    private static final String PAGING_LINK_PREFIX = "pageLink";

    private static final int ITEMS_PER_PAGE = 9;

    private final VelocityContainer vC;
    private final EPFrontendManager ePFMgr;
    private DialogBoxController delMapCtrl;
    private DialogBoxController copyMapCtrl;
    private EPMapViewController mapViewCtrl;
    private EPShareListController shareListController;
    private CloseableModalWindowWrapperController shareBox;
    private final Panel myPanel;

    private final EPMapRunViewOption option;
    private final Identity mapOwner;
    private List<PortfolioStructureMap> userMaps;
    private boolean restrictShareView = true;
    private long start;
    private final PortfolioModule portfolioModule;

    // components for paging
    private Link forwardLink;
    private int currentPageNum = 1;
    private int currentPagingFrom = 0;
    private int currentPagingTo = ITEMS_PER_PAGE;
    private boolean pagingAvailable = false;

    public EPMultipleMapController(final UserRequest ureq, final WindowControl control, final EPMapRunViewOption option, final Identity mapOwner) {
        super(ureq, control);

        this.option = option;
        this.mapOwner = mapOwner;
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        portfolioModule = (PortfolioModule) CoreSpringFactory.getBean(PortfolioAbstractHandler.class);
        vC = createVelocityContainer("multiMaps");
        initOrUpdateMaps(ureq);

        myPanel = putInitialPanel(vC);
    }

    /**
     * returns a List of PortfolioStructures to display, depending on options (all OLAT-wide shared maps, only shared to me, paging)
     * 
     * @return
     */
    private List<PortfolioStructure> getUsersStructsToDisplay() {
        pagingAvailable = false;
        // get maps for this user
        List<PortfolioStructure> allUsersStruct;
        switch (option) {
        case OTHER_MAPS:// same as OTHERS_MAPS
        case OTHERS_MAPS:
            vC.remove(vC.getComponent(RESTRICT_LINK));
            if (restrictShareView) {
                if (portfolioModule.isOfferPublicMapList()) {
                    LinkFactory.createCustomLink(RESTRICT_LINK, "change", "restrict.show.all", Link.LINK, vC, this);
                }
                allUsersStruct = ePFMgr.getStructureElementsFromOthersWithoutPublic(getIdentity(), mapOwner, ElementType.STRUCTURED_MAP, ElementType.DEFAULT_MAP);
            } else {
                if (portfolioModule.isOfferPublicMapList()) {
                    LinkFactory.createCustomLink(RESTRICT_LINK, "change", "restrict.show.limited", Link.LINK, vC, this);
                }
                // this query can be quite time consuming, if fetching all structures -> do paging
                currentPagingFrom = (currentPageNum - 1) * ITEMS_PER_PAGE;
                currentPagingTo = currentPagingFrom + ITEMS_PER_PAGE;
                allUsersStruct = ePFMgr.getStructureElementsFromOthers(getIdentity(), mapOwner, currentPagingFrom, currentPagingTo, ElementType.STRUCTURED_MAP,
                        ElementType.DEFAULT_MAP);
                pagingAvailable = true;
            }
            break;
        case MY_EXERCISES_MAPS:
            allUsersStruct = ePFMgr.getStructureElementsForUser(getIdentity(), ElementType.STRUCTURED_MAP);
            break;
        default:// MY_DEFAULTS_MAPS
            allUsersStruct = ePFMgr.getStructureElementsForUser(getIdentity(), ElementType.DEFAULT_MAP);
        }
        if (log.isDebugEnabled()) {
            log.debug("got all structures to see at: " + String.valueOf(System.currentTimeMillis()));
        }
        return allUsersStruct;
    }

    /**
	 * 
	 */
    private void initOrUpdateMaps(final UserRequest ureq) {

        if (log.isDebugEnabled()) {
            start = System.currentTimeMillis();
            log.debug("start loading map overview at : " + String.valueOf(start));
        }

        List<PortfolioStructure> allUsersStruct = getUsersStructsToDisplay();
        userMaps = new ArrayList<PortfolioStructureMap>();
        if (allUsersStruct.isEmpty()) {
            vC.contextPut("noMaps", true);
            return;
        } else {
            vC.contextRemove("noMaps");
        }

        // remove forward link (maybe it's not needed (last page) )
        if (forwardLink != null)
            vC.remove(forwardLink);

        // now add paging-components if necessary and wanted
        int elementCount = ePFMgr.getStructureElementsFromOthers(getIdentity(), mapOwner, ElementType.DEFAULT_MAP).size();
        if (pagingAvailable && elementCount > ITEMS_PER_PAGE) {
            vC.contextPut("showPaging", true);

            int additionalPage = ((elementCount % ITEMS_PER_PAGE) > 0) ? 1 : 0;
            int pageCount = (elementCount / ITEMS_PER_PAGE) + additionalPage;
            List<Component> pagingLinks = new ArrayList<Component>();
            for (int i = 1; i < pageCount + 1; i++) {
                Link pageLink = LinkFactory.createCustomLink(PAGING_LINK_PREFIX + i, "switchPage", String.valueOf(i), Link.NONTRANSLATED, vC, this);
                pageLink.setUserObject(new Integer(i));
                pagingLinks.add(pageLink);
                if (i == currentPageNum) {
                    pageLink.setEnabled(false);
                }
            }

            vC.contextPut("pageLinks", pagingLinks);

            if (currentPageNum < pageCount) {
                forwardLink = LinkFactory.createCustomLink("forwardLink", "pagingFWD", "table.forward", Link.LINK, vC, this);
                forwardLink.setCustomEnabledLinkCSS("b_map_page_forward");
            }
        }

        // now display the maps
        final List<String> artAmount = new ArrayList<String>();
        final List<Integer> childAmount = new ArrayList<Integer>();
        final List<String> mapStyles = new ArrayList<String>();
        final List<Date> deadLines = new ArrayList<Date>();
        final List<String> restriStats = new ArrayList<String>();
        final List<String> owners = new ArrayList<String>();
        final List<String> amounts = new ArrayList<String>();

        int i = 1;
        for (final PortfolioStructure portfolioStructure : allUsersStruct) {
            if (portfolioStructure.getRoot() == null) { // only show maps
                final PortfolioStructureMap map = (PortfolioStructureMap) portfolioStructure;
                final EPSecurityCallback secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, map, ePFMgr);

                userMaps.add(map);
                final Link vLink = LinkFactory.createCustomLink(VIEW_LINK_PREFIX + i, "viewMap" + map.getResourceableId(), "view.map", Link.LINK, vC, this);
                vLink.setUserObject(map);
                vLink.setCustomEnabledLinkCSS("b_with_small_icon_right b_open_icon");

                vC.remove(vC.getComponent(DELETE_LINK_PREFIX + i)); // remove as update could require hiding it
                // can always try to delete your own map, but exercise only if the course was deleted
                final boolean myMaps = (option.equals(EPMapRunViewOption.MY_DEFAULTS_MAPS) || option.equals(EPMapRunViewOption.MY_EXERCISES_MAPS));
                boolean addDeleteLink = myMaps;

                if ((map instanceof EPStructuredMap) && (((EPStructuredMap) map).getReturnDate() != null)) {
                    addDeleteLink = false; // it's a portfolio-task that was already handed in, so do not display delete-link
                }

                if (addDeleteLink) {
                    final Link dLink = LinkFactory.createCustomLink(DELETE_LINK_PREFIX + i, "delMap" + map.getResourceableId(), "delete.map", Link.LINK, vC, this);
                    dLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_delete_icon");
                    dLink.setUserObject(map);
                }

                final Link cLink = LinkFactory.createCustomLink(COPY_LINK_PREFIX + i, "copyMap" + map.getResourceableId(), "copy.map", Link.LINK, vC, this);
                cLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_copy_icon");
                cLink.setUserObject(map);
                // its not allowed to copy maps from a portfolio-task
                if (map instanceof EPStructuredMap) {
                    cLink.setVisible(false);
                }

                vC.remove(vC.getComponent(SHARE_LINK_PREFIX + i)); // remove as update could require hiding it
                if (myMaps && secCallback.canShareMap()) {
                    final Link shareLink = LinkFactory.createCustomLink(SHARE_LINK_PREFIX + i, "shareMap" + map.getResourceableId(), "map.share", Link.LINK, vC, this);
                    shareLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_share_icon");
                    shareLink.setUserObject(map);
                    final boolean shared = ePFMgr.isMapShared(map);
                    if (shared) {
                        shareLink.setCustomDisplayText(translate("map.share.shared"));
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("  in loop : got share state at: " + String.valueOf(System.currentTimeMillis()));
                }

                // get deadline + link to course
                if (map instanceof EPStructuredMap) {
                    final EPStructuredMap structMap = (EPStructuredMap) map;
                    final Date deadLine = structMap.getDeadLine();
                    deadLines.add(deadLine);

                    final EPTargetResource resource = structMap.getTargetResource();
                    final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(resource.getOLATResourceable(), false);
                    if (repoEntry != null) {
                        vC.contextPut("courseName" + i, repoEntry.getDisplayname());
                        String url = Settings.getServerContextPathURI();
                        url += "/url/RepositoryEntry/" + repoEntry.getKey() + "/CourseNode/" + resource.getSubPath();
                        vC.contextPut("courseLink" + i, url);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("  in loop : looked up course at : " + String.valueOf(System.currentTimeMillis()));
                    }

                    // get some stats about the restrictions if available
                    String[] stats = ePFMgr.getRestrictionStatisticsOfMap(structMap);
                    int toCollect = 0;
                    if (stats != null) {
                        try {
                            toCollect = Integer.parseInt(stats[1]) - Integer.parseInt(stats[0]);
                        } catch (Exception e) {
                            // do nothing
                            toCollect = 0;
                        }
                    }
                    if (toCollect != 0) {
                        restriStats.add(String.valueOf(toCollect));
                    } else {
                        restriStats.add(null);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("  in loop : calculated restriction statistics at : " + String.valueOf(System.currentTimeMillis()));
                    }
                } else {
                    deadLines.add(null);
                    restriStats.add(null);
                }

                // show owner on shared maps
                if (!secCallback.isOwner()) {
                    final List<Identity> ownerIdents = getBaseSecurity().getIdentitiesOfSecurityGroup(map.getOwnerGroup());
                    final List<String> identNames = new ArrayList<String>();
                    for (final Identity identity : ownerIdents) {
                        final String fullName = getUserService().getFirstAndLastname(identity.getUser());
                        identNames.add(fullName);
                    }
                    owners.add(StringHelper.formatAsCSVString(identNames));
                } else {
                    owners.add(null);
                }

                final String artCount = String.valueOf(ePFMgr.countArtefactsInMap(map));
                artAmount.add(artCount);
                final Integer childs = ePFMgr.countStructureChildren(map);
                childAmount.add(childs);
                amounts.add(translate("map.contains", new String[] { childs.toString(), artCount }));

                mapStyles.add(ePFMgr.getValidStyleName(map));
                if (log.isDebugEnabled()) {
                    log.debug("  in loop : got map details (artefact-amount, child-struct-amount, style) at : " + String.valueOf(System.currentTimeMillis()));
                }
                i++;
            } else {
                log.info("not a map");
            }
        }
        vC.contextPut("owners", owners);
        vC.contextPut("deadLines", deadLines);
        vC.contextPut("restriStats", restriStats);
        vC.contextPut("mapStyles", mapStyles);
        vC.contextPut("childAmount", childAmount);
        vC.contextPut("artefactAmount", artAmount);
        vC.contextPut("amounts", amounts);
        vC.contextPut("userMaps", userMaps);
        if (log.isDebugEnabled()) {
            final long now = System.currentTimeMillis();
            log.debug("finished processing all maps at : " + String.valueOf(now));
            log.debug("Total processing time for " + (i - 1) + " maps was : " + String.valueOf(now - start));
        }
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    @Override
    public void activate(final UserRequest ureq, final String viewIdentifier) {
        final int index = viewIdentifier.indexOf("[map:");
        Long key = null;
        boolean idIsKey = true;
        try {
            key = Long.parseLong(viewIdentifier);
        } catch (final Exception e) {
            idIsKey = false;
        }

        if (index >= 0 && !idIsKey) {
            final int lastIndex = viewIdentifier.indexOf("]", index);
            if (lastIndex < viewIdentifier.length()) {
                final String keyStr = viewIdentifier.substring(index + 5, lastIndex);
                key = Long.parseLong(keyStr);
            }
        }
        for (final PortfolioStructureMap map : userMaps) {
            if (map.getKey().equals(key) || (idIsKey && map.getResourceableId().equals(key))) {
                activateMap(ureq, map);
                fireEvent(ureq, new EPMapEvent(EPStructureEvent.SELECT, map));
                break;
            }
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source instanceof Link) {
            final Link srcLink = (Link) source;
            if (srcLink.getUserObject() instanceof PortfolioStructureMap) {

                PortfolioStructureMap selMap = (PortfolioStructureMap) srcLink.getUserObject();
                if (srcLink.getComponentName().startsWith(VIEW_LINK_PREFIX)) {
                    activateMap(ureq, selMap);
                    fireEvent(ureq, new EPMapEvent(EPStructureEvent.SELECT, selMap));
                } else if (srcLink.getComponentName().startsWith(DELETE_LINK_PREFIX)) {
                    deleteMap(ureq, selMap);
                } else if (srcLink.getComponentName().startsWith(COPY_LINK_PREFIX)) {
                    final List<String> buttonLabels = new ArrayList<String>();
                    String introKey = "copy.map.intro";
                    if (ePFMgr.isMapOwner(getIdentity(), selMap)) {
                        buttonLabels.add(translate("copy.with.artefacts"));
                        introKey = "copy.map.intro2";
                    }
                    buttonLabels.add(translate("copy.without.artefacts"));
                    buttonLabels.add(translate("copy.cancel"));
                    String text = translate(introKey, StringHelper.escapeHtml(selMap.getTitle()));
                    copyMapCtrl = activateGenericDialog(ureq, translate("copy.map.title"), text, buttonLabels, copyMapCtrl);
                    copyMapCtrl.setUserObject(selMap);
                } else if (srcLink.getComponentName().startsWith(SHARE_LINK_PREFIX)) {
                    popUpShareBox(ureq, selMap);
                }
            } else {
                if (srcLink.equals(forwardLink)) {
                    currentPageNum++;
                    initOrUpdateMaps(ureq);
                } else if (srcLink.getComponentName().startsWith(PAGING_LINK_PREFIX)) {
                    Integer page = (Integer) srcLink.getUserObject();
                    currentPageNum = page.intValue();
                    initOrUpdateMaps(ureq);
                } else if (srcLink.getComponentName().equals(RESTRICT_LINK)) {
                    restrictShareView = !restrictShareView;
                    initOrUpdateMaps(ureq);
                }
            }

        }
    }

    private void deleteMap(final UserRequest ureq, final PortfolioStructureMap map) {
        String text = translate("delete.map.intro", StringHelper.escapeHtml(map.getTitle()));
        delMapCtrl = activateYesNoDialog(ureq, translate("delete.map.title"), text, delMapCtrl);
        delMapCtrl.setUserObject(map);
    }

    private void popUpShareBox(final UserRequest ureq, final PortfolioStructureMap map) {
        removeAsListenerAndDispose(shareListController);
        removeAsListenerAndDispose(shareBox);
        shareListController = new EPShareListController(ureq, getWindowControl(), map);
        listenTo(shareListController);

        final String title = translate("map.share");
        shareBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, shareListController.getInitialComponent(), "shareBox" + map.getKey());
        shareBox.setInitialWindowSize(800, 600);
        listenTo(shareBox);
        shareBox.activate();
    }

    public void activateMap(final UserRequest ureq, final PortfolioStructureMap struct) {
        if (userMaps != null && !userMaps.contains(struct)) {
            initOrUpdateMaps(ureq);
        }

        if (mapViewCtrl != null) {
            removeAsListenerAndDispose(mapViewCtrl);
        }

        final EPSecurityCallback secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, struct, ePFMgr);
        // release the previous if not correctly released by CLOSE events
        mapViewCtrl = new EPMapViewController(ureq, getWindowControl(), struct, true, secCallback);
        listenTo(mapViewCtrl);
        myPanel.pushContent(mapViewCtrl.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == delMapCtrl) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                final PortfolioStructure mapToDel = (PortfolioStructure) ((DialogBoxController) source).getUserObject();
                final String title = mapToDel.getTitle();
                ePFMgr.deletePortfolioStructure(mapToDel);
                showInfo("delete.map.success", title);
                ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(mapToDel));
                ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_MAP_REMOVED, getClass());
                initOrUpdateMaps(ureq);
            }
        } else if (source == copyMapCtrl) {
            if (event.equals(Event.CANCELLED_EVENT)) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
                return;
            }
            int pos = DialogBoxUIFactory.getButtonPos(event);
            boolean withArtefacts = false;
            final PortfolioStructure mapToCopy = (PortfolioStructure) ((DialogBoxController) source).getUserObject();
            if (!ePFMgr.isMapOwner(getIdentity(), mapToCopy)) {
                pos++; // shift clicked pos, when "with artefacts" was hidden before
            }
            if (pos == 2) {
                // clicked cancel button
                fireEvent(ureq, Event.CANCELLED_EVENT);
                return;
            } else if (pos == 0) {
                withArtefacts = true;
            }
            final PortfolioStructureMap targetMap = ePFMgr.createAndPersistPortfolioDefaultMap(getIdentity(), translate("map.copy.of", mapToCopy.getTitle()),
                    mapToCopy.getDescription());
            ePFMgr.copyStructureRecursively(mapToCopy, targetMap, withArtefacts);
            // open the map
            final String title = targetMap.getTitle();
            showInfo("copy.map.success", title);
            initOrUpdateMaps(ureq);
            final String activationCmd = targetMap.getClass().getSimpleName() + ":" + targetMap.getResourceableId();
            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            dts.activateStatic(ureq, HomeSite.class.getName(), activationCmd);
        } else if (source == mapViewCtrl) {
            if (EPStructureEvent.CLOSE.equals(event.getCommand())) {
                myPanel.popContent();
                fireEvent(ureq, event);
                removeAsListenerAndDispose(mapViewCtrl);
                mapViewCtrl = null;
                // refresh on close (back-link) to prevent stale object errors, when map got changed meanwhile
                initOrUpdateMaps(ureq);
            } else if (EPStructureEvent.SUBMIT.equals(event.getCommand()) || event.equals(Event.CHANGED_EVENT)) {
                // refresh on submission of a map or on any other changes which needs an ui-update
                initOrUpdateMaps(ureq);
            }
        } else if (source == shareListController) {
            shareBox.deactivate();
            removeAsListenerAndDispose(shareListController);
            initOrUpdateMaps(ureq);
        }
        if (event instanceof EPStructureChangeEvent) {
            // event from child
            final String evCmd = event.getCommand();
            if (evCmd.equals(EPStructureChangeEvent.ADDED) || evCmd.equals(EPStructureChangeEvent.CHANGED)) {
                initOrUpdateMaps(ureq);
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
