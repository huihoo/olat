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

import java.util.List;

import org.olat.data.portfolio.structure.EPPage;
import org.olat.data.portfolio.structure.EPStructuredMap;
import org.olat.data.portfolio.structure.EPStructuredMapTemplate;
import org.olat.data.portfolio.structure.EPTargetResource;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.portfolio.structure.StructureStatusEnum;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.EPLoggingAction;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.lms.portfolio.security.EPSecurityCallbackFactory;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.portfolio.structel.edit.EPStructureTreeAndDetailsEditController;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * shows a map itself with containing pages, structures, etc.
 * <P>
 * Initial Date: 04.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMapViewController extends BasicController {

    private PortfolioStructureMap map;
    private final EPFrontendManager ePFMgr;
    private EPMultiplePageController pageCtrl;
    private Link editButton;
    private Link backLink;
    private Link submitAssessLink;
    private EPStructureTreeAndDetailsEditController editCtrl;
    private DialogBoxController confirmationSubmissionCtr;
    private final boolean back;
    private EPSecurityCallback secCallback;
    private LockResult lockEntry;

    private final VelocityContainer mainVc;

    public EPMapViewController(final UserRequest ureq, final WindowControl control, final PortfolioStructureMap initialMap, final boolean back,
            final EPSecurityCallback secCallback) {
        super(ureq, control);
        this.map = initialMap;
        this.back = back;
        this.secCallback = secCallback;

        mainVc = createVelocityContainer("mapview");

        ePFMgr = CoreSpringFactory.getBean(EPFrontendManager.class);

        // if this is a structured map (assigned from a template) do a sync first
        if (map instanceof EPStructuredMap && (map.getStatus() == null || !map.getStatus().equals(StructureStatusEnum.CLOSED))) {
            map = (PortfolioStructureMap) ePFMgr.loadPortfolioStructureByKey(map.getKey());
            final boolean syncOk = ePFMgr.synchronizeStructuredMapToUserCopy(map);
            if (syncOk) {
                showInfo("synced.map.success");
            }
        }

        if (EPSecurityCallbackFactory.isLockNeeded(secCallback)) {
            lockEntry = getLockingService().acquireLock(initialMap, ureq.getIdentity(), "mmp");
            if (!lockEntry.isSuccess()) {
                this.secCallback = EPSecurityCallbackFactory.updateAfterFailedLock(secCallback);
                showWarning("map.already.edited");
            }
        }

        if (initialMap instanceof EPStructuredMapTemplate) {
            final boolean inUse = ePFMgr.isTemplateInUse(initialMap, null, null, null);
            if (inUse) {
                showWarning("template.alreadyInUse");
            }
        }

        initForm(ureq);
        putInitialPanel(mainVc);
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    protected void initForm(final UserRequest ureq) {
        mainVc.contextPut("map", map);
        mainVc.contextPut("style", ePFMgr.getValidStyleName(map));
        if (map.getDescription() == null) {
            map.setDescription("");
        }

        final Boolean editMode = editButton == null ? Boolean.FALSE : (Boolean) editButton.getUserObject();
        mainVc.remove(mainVc.getComponent("map.editButton"));
        if (secCallback.canEditStructure()) {
            editButton = LinkFactory.createButton("map.editButton", mainVc, this);
            if (Boolean.FALSE.equals(editMode)) {
                editButton.setCustomDisplayText(translate("map.editButton.on"));
            } else {
                editButton.setCustomDisplayText(translate("map.editButton.off"));
            }
            editButton.setUserObject(editMode);
        }
        if (back) {
            backLink = LinkFactory.createLinkBack(mainVc, this);
        }
        mainVc.remove(mainVc.getComponent("map.submit.assess"));
        if (secCallback.canSubmitAssess() && !StructureStatusEnum.CLOSED.equals(map.getStatus())) {
            submitAssessLink = LinkFactory.createButtonSmall("map.submit.assess", mainVc, this);
        }

        if (map instanceof EPStructuredMap) {
            final EPTargetResource resource = ((EPStructuredMap) map).getTargetResource();
            final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(resource.getOLATResourceable(), false);
            if (repoEntry != null) {
                mainVc.contextPut("courseName", StringHelper.escapeHtml(repoEntry.getDisplayname()));
                String url = Settings.getServerContextPathURI();
                url += "/url/RepositoryEntry/" + repoEntry.getKey() + "/CourseNode/" + resource.getSubPath();
                mainVc.contextPut("courseLink", url);
            }
        }

        mainVc.remove(mainVc.getComponent("addButton"));
        if (secCallback.canAddPage() && !StructureStatusEnum.CLOSED.equals(map.getStatus())) {
            final EPAddElementsController addButton = new EPAddElementsController(ureq, getWindowControl(), map);
            if (secCallback.canAddPage()) {
                addButton.setShowLink(EPAddElementsController.ADD_PAGE);
            }
            mainVc.put("addButton", addButton.getInitialComponent());
            listenTo(addButton);
        }
        mainVc.contextPut("closed", Boolean.valueOf((StructureStatusEnum.CLOSED.equals(map.getStatus()))));

        final List<PortfolioStructure> pageList = ePFMgr.loadStructureChildren(map);
        if (pageList != null && pageList.size() != 0) {
            // prepare to paint pages also
            removeAsListenerAndDispose(pageCtrl);
            pageCtrl = new EPMultiplePageController(ureq, getWindowControl(), pageList, secCallback);
            mainVc.put("pagesCtrl", pageCtrl.getInitialComponent());
            listenTo(pageCtrl);
        } else if (mainVc.getComponent("pagesCtrl") != null) {
            mainVc.remove(mainVc.getComponent("pagesCtrl"));
        }
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == editButton) {
            removeAsListenerAndDispose(editCtrl);
            if (Boolean.FALSE.equals(editButton.getUserObject())) {
                PortfolioStructure selectedPage = null;
                if (pageCtrl != null) {
                    selectedPage = pageCtrl.getSelectedPage();
                }
                initOrUpdateEditMode(ureq, selectedPage);
            } else {
                mainVc.remove(editCtrl.getInitialComponent());
                final PortfolioStructure currentEditedStructure = editCtrl.getSelectedStructure();
                initForm(ureq);
                editButton.setUserObject(Boolean.FALSE);
                editButton.setCustomDisplayText(translate("map.editButton.on"));
                if (currentEditedStructure != null && pageCtrl != null) {
                    final EPPage page = getSelectedPage(currentEditedStructure);
                    if (page != null) {
                        pageCtrl.selectPage(ureq, page);
                    }
                }
            }
        } else if (source == backLink) {
            fireEvent(ureq, new EPMapEvent(EPStructureEvent.CLOSE, map));
        } else if (source == submitAssessLink) {
            if (preCheckMapSubmit()) {
                submitAssess(ureq);
            } else {
                showWarning("map.cannot.submit.nomore.coursenode");
            }
        }
    }

    private EPPage getSelectedPage(final PortfolioStructure structure) {
        PortfolioStructure current = structure;

        do {
            if (current instanceof EPPage) {
                return (EPPage) current;
            }
            current = current.getRoot();
        } while (current != null);

        return null;
    }

    private boolean preCheckMapSubmit() {
        EPStructuredMap submittedMap = (EPStructuredMap) map;
        try {
            EPTargetResource resource = submittedMap.getTargetResource();
            OLATResourceable courseOres = resource.getOLATResourceable();
            ICourse course = CourseFactory.loadCourse(courseOres);
            CourseNode courseNode = course.getRunStructure().getNode(resource.getSubPath());
            if (courseNode == null)
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected void submitAssess(final UserRequest ureq) {
        if (ePFMgr.checkCollectRestrictionOfMap(map)) {
            final String title = translate("map.submit.assess.title");
            final String text = translate("map.submit.assess.description");
            confirmationSubmissionCtr = activateYesNoDialog(ureq, title, text, confirmationSubmissionCtr);
        } else {
            final String title = translate("map.submit.assess.restriction.error.title");
            String[] stats = ePFMgr.getRestrictionStatisticsOfMap(map);
            final String text = translate("map.submit.assess.restriction.error.description") + "<br/>" + translate("map.submit.assess.restriction.error.hint", stats);
            confirmationSubmissionCtr = activateYesNoDialog(ureq, title, text, confirmationSubmissionCtr);
            confirmationSubmissionCtr.setCssClass("b_warning_icon");
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (event instanceof EPStructureChangeEvent && event.getCommand().equals(EPStructureChangeEvent.ADDED)) {
            final EPStructureChangeEvent changeEvent = (EPStructureChangeEvent) event;
            final PortfolioStructure structure = changeEvent.getPortfolioStructure();
            // don't do reloadMapAndRefreshUI(ureq) here; no db-commit yet! no refresh -> stale object!
            if (structure instanceof EPPage) {
                // jump to the edit mode for new pages
                initOrUpdateEditMode(ureq, structure);
            }
        } else if (event instanceof EPStructureEvent && event.getCommand().equals(EPStructureEvent.CHANGE)) {
            // reload map
            reloadMapAndRefreshUI(ureq);
        } else if (source == editCtrl && event.equals(Event.CHANGED_EVENT)) {
            // refresh view on changes in TOC or style
            reloadMapAndRefreshUI(ureq);
            final PortfolioStructure selectedPage = editCtrl.getSelectedStructure();
            initOrUpdateEditMode(ureq, selectedPage);
        } else if (source == confirmationSubmissionCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                ePFMgr.submitMap(map);
                secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, map, ePFMgr);
                fireEvent(ureq, new EPMapEvent(EPStructureEvent.SUBMIT, map));
                mainVc.remove(mainVc.getComponent("editor")); // switch back to non-edit mode
                ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(map));
                ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_TASK_FINISHED, getClass());
                reloadMapAndRefreshUI(ureq);
            }
        }

        fireEvent(ureq, event); // fire to multiple maps controller, so it can refresh itself!
    }

    private void initOrUpdateEditMode(final UserRequest ureq, final PortfolioStructure startStruct) {
        removeAsListenerAndDispose(editCtrl);
        editCtrl = new EPStructureTreeAndDetailsEditController(ureq, getWindowControl(), startStruct, map, secCallback);
        mainVc.put("editor", editCtrl.getInitialComponent());
        listenTo(editCtrl);
        editButton.setUserObject(Boolean.TRUE);
        editButton.setCustomDisplayText(translate("map.editButton.off"));
    }

    private void reloadMapAndRefreshUI(final UserRequest ureq) {
        this.map = (PortfolioStructureMap) ePFMgr.loadPortfolioStructureByKey(map.getKey());
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (lockEntry != null) {
            getLockingService().releaseLock(lockEntry);
            lockEntry = null;
        }
    }
}
