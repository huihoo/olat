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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */

package org.olat.presentation.course.nodes.portfolio;

import org.olat.data.portfolio.structure.PortfolioStructureDao;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.lms.course.nodes.PortfolioCourseNode;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.EPTemplateMapResource;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.lms.portfolio.security.EPSecurityCallbackImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsBackController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: srosse Class Description for PortfolioConfigForm
 * <P>
 * Initial Date: 6 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioConfigForm extends FormBasicController {
    private final EPFrontendManager ePFMgr;
    private final PortfolioStructureDao eSTMgr;
    private final ModuleConfiguration config;

    private boolean inUse;
    private PortfolioStructureMap map;
    private RepositoryEntry mapEntry;

    private ReferencableEntriesSearchController searchController;
    private CloseableModalController cmc;

    private FormLink chooseMapLink;
    private FormLink changeMapLink;
    private FormLink editMapLink;
    private FormLink previewMapLink;
    private StaticTextElement mapNameElement;

    private Controller previewCtr;
    private LayoutMain3ColsBackController columnLayoutCtr;
    private final PortfolioCourseNode courseNode;
    private boolean isDirty;

    public PortfolioConfigForm(final UserRequest ureq, final WindowControl wControl, final ICourse course, final PortfolioCourseNode courseNode) {
        super(ureq, wControl);
        this.courseNode = courseNode;
        this.config = courseNode.getModuleConfiguration();

        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        eSTMgr = (PortfolioStructureDao) CoreSpringFactory.getBean(PortfolioStructureDao.class);

        mapEntry = courseNode.getReferencedRepositoryEntry();
        if (mapEntry != null) {
            map = (PortfolioStructureMap) ePFMgr.loadPortfolioStructure(mapEntry.getOlatResource());
            final Long courseResId = course.getResourceableId();
            final OLATResourceable courseOres = OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId);
            if (map != null) {
                inUse = ePFMgr.isTemplateInUse(map, courseOres, courseNode.getIdent(), null);
            }
        }

        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("pane.tab.portfolio_config.title");
        setFormContextHelp("org.olat.presentation.course.nodes.portfolio", "ced_portfolio.html", "ced.hover");

        final String name = map == null ? translate("error.noreference.short", courseNode.getShortTitle()) : map.getTitle();
        mapNameElement = uifactory.addStaticTextElement("map-name", "selected.map", name, formLayout);
        mapNameElement.setVisible(map == null);

        previewMapLink = uifactory.addFormLink("preview", "selected.map", "selected.map", formLayout, Link.LINK);
        previewMapLink.setCustomEnabledLinkCSS("b_preview");
        ((Link) previewMapLink.getComponent()).setCustomDisplayText(name);
        previewMapLink.setVisible(map != null);

        if (formLayout instanceof FormLayoutContainer) {
            final FormLayoutContainer layoutContainer = (FormLayoutContainer) formLayout;

            final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
            buttonGroupLayout.setRootForm(mainForm);
            layoutContainer.add(buttonGroupLayout);
            chooseMapLink = uifactory.addFormLink("select_or_import.map", buttonGroupLayout, Link.BUTTON);
            changeMapLink = uifactory.addFormLink("select.map", buttonGroupLayout, Link.BUTTON);
            editMapLink = uifactory.addFormLink("edit.map", buttonGroupLayout, Link.BUTTON);

            chooseMapLink.setVisible(map == null);
            chooseMapLink.setEnabled(!inUse);
            changeMapLink.setVisible(map != null);
            changeMapLink.setEnabled(!inUse);
            editMapLink.setVisible(map != null);
            editMapLink.setEnabled(!inUse);
        }
    }

    public void setDirtyFromOtherForm(boolean dirty) {
        this.isDirty = dirty;
    }

    protected ModuleConfiguration getUpdatedConfig() {
        courseNode.setRepositoryReference(mapEntry);
        return courseNode.getModuleConfiguration();
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (isDirty) {
            showWarning("form.dirty");
            return;
        }
        if (source == changeMapLink || source == chooseMapLink) {
            removeAsListenerAndDispose(searchController);
            searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, new String[] { EPTemplateMapResource.TYPE_NAME },
                    translate("select.map2"), false, true, false);
            listenTo(searchController);
            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate("select.map"));
            listenTo(cmc);
            cmc.activate();
        } else if (source == editMapLink) {
            CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, courseNode);
        } else if (source == previewMapLink) {
            final EPSecurityCallback secCallback = new EPSecurityCallbackImpl(false, true);

            if (previewCtr != null) {
                removeAsListenerAndDispose(previewCtr);
                removeAsListenerAndDispose(columnLayoutCtr);
            }
            previewCtr = EPUIFactory.createPortfolioStructureMapController(ureq, getWindowControl(), map, secCallback);
            listenTo(previewCtr);
            final LayoutMain3ColsBackController ctr = new LayoutMain3ColsBackController(ureq, getWindowControl(), null, null, previewCtr.getInitialComponent(),
                    "portfolio" + map.getKey());
            ctr.activate();
            columnLayoutCtr = ctr;
            listenTo(columnLayoutCtr);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == searchController) {
            cmc.deactivate();
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                // search controller done
                mapEntry = searchController.getSelectedEntry();
                if (mapEntry != null) {
                    map = (PortfolioStructureMap) eSTMgr.loadPortfolioStructure(mapEntry.getOlatResource());
                    fireEvent(ureq, Event.DONE_EVENT);
                }
                final String name = map == null ? "" : map.getTitle();
                mapNameElement.setValue(name);
                chooseMapLink.setVisible(map == null);
                changeMapLink.setVisible(map != null);
                editMapLink.setVisible(map != null);

                mapNameElement.setVisible(map == null);
                previewMapLink.setVisible(map != null);
                ((Link) previewMapLink.getComponent()).setCustomDisplayText(name);
            }
        }
    }
}
