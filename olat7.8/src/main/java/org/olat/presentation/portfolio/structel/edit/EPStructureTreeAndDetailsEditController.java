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
package org.olat.presentation.portfolio.structel.edit;

import java.util.List;

import org.olat.data.portfolio.structure.EPStructureElement;
import org.olat.data.portfolio.structure.EPStructuredMapTemplate;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.PortfolioModule;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.portfolio.structel.EPArtefactClicked;
import org.olat.presentation.portfolio.structel.EPMapViewController;
import org.olat.presentation.portfolio.structel.EPStructureChangeEvent;
import org.olat.presentation.portfolio.structel.EPStructureEvent;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * parent controller for toc- and structure-editing
 * <P>
 * Initial Date: 07.10.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPStructureTreeAndDetailsEditController extends FormBasicController {

    private final EPFrontendManager ePFMgr;
    private PortfolioStructure rootStructure;
    private PortfolioStructure selectedStructure;
    private final EPSecurityCallback secCallback;

    private EPTOCController tocCtrl;
    private EPStructureDetailsController editCtrl;
    private SingleSelection mapStyle;

    public EPStructureTreeAndDetailsEditController(final UserRequest ureq, final WindowControl wControl, PortfolioStructure selectedStructure,
            final PortfolioStructure rootStructure, final EPSecurityCallback secCallback) {
        super(ureq, wControl, "editor");
        this.secCallback = secCallback;
        this.rootStructure = rootStructure;
        this.selectedStructure = selectedStructure;
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);

        final Translator pt = PackageUtil.createPackageTranslator(EPMapViewController.class, ureq.getLocale(), getTranslator());
        setTranslator(pt);

        initForm(ureq);
        selectedStructure = null;// consume it
    }

    public PortfolioStructure getSelectedStructure() {
        return selectedStructure;
    }

    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final PortfolioModule portfolioModule = (PortfolioModule) CoreSpringFactory.getBean(PortfolioAbstractHandler.class);
        final List<String> allStyles = portfolioModule.getAvailableMapStyles();
        final int amount = allStyles.size();
        final String[] theKeys = new String[amount];
        final String[] theValues = new String[amount];
        final String[] theCssClasses = new String[amount];
        int i = 0;
        if (amount > 1) { // if no themes than default configured, no selection at all
            for (final String style : allStyles) {
                theKeys[i] = style;
                theValues[i] = translate("map.style." + style);
                theCssClasses[i] = style + "_icon";
                i++;
            }
            mapStyle = uifactory.addDropdownSingleselect("map.style", formLayout, theKeys, theValues, theCssClasses);
            mapStyle.addActionListener(this, FormEvent.ONCHANGE);
            final String givenStyle = ((EPStructureElement) rootStructure).getStyle();
            if (StringHelper.containsNonWhitespace(givenStyle)) {
                mapStyle.select(givenStyle, true);
                mapStyle.setUserObject(givenStyle);
            }
        }

        flc.contextPut("template-help", new Boolean(rootStructure instanceof EPStructuredMapTemplate));

        initOrUpdateToc(ureq);
        initOrUpdateDetailsEditor(ureq);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @SuppressWarnings("unused")
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == mapStyle) {
            if (!mapStyle.getSelectedKey().equals(mapStyle.getUserObject())) {
                final String newStyle = mapStyle.getSelectedKey();
                rootStructure = ePFMgr.loadPortfolioStructureByKey(rootStructure.getKey());
                ((EPStructureElement) rootStructure).setStyle(newStyle);
                ePFMgr.savePortfolioStructure(rootStructure);
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        }
    }

    private void initOrUpdateToc(final UserRequest ureq) {
        removeAsListenerAndDispose(tocCtrl);
        // with new links (pages, sub-elements or artefacts) to map, map gets a new version, therefore needs a refresh!
        rootStructure = ePFMgr.loadPortfolioStructureByKey(rootStructure.getKey());
        tocCtrl = new EPTOCController(ureq, getWindowControl(), selectedStructure, rootStructure, secCallback);
        listenTo(tocCtrl);
        flc.put("tocCtrl", tocCtrl.getInitialComponent());
    }

    private void initOrUpdateDetailsEditor(final UserRequest ureq) {
        removeAsListenerAndDispose(editCtrl);
        editCtrl = new EPStructureDetailsController(ureq, getWindowControl(), mainForm, rootStructure);
        if (selectedStructure != null) {
            editCtrl.setNewStructure(ureq, selectedStructure);
        }
        listenTo(editCtrl);
        flc.add("editCtrl", editCtrl.getInitialFormItem());
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    public FormItem getInitialFormItem() {
        return flc;
    }

    @Override
    @SuppressWarnings("unused")
    protected void formOK(final UserRequest ureq) {
        //
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == tocCtrl) {
            if (event.getCommand().equals(EPTOCController.ARTEFACT_NODE_CLICKED)) {
                editCtrl.setNoStructure();
                if (event instanceof EPArtefactClicked) {
                    selectedStructure = ((EPArtefactClicked) event).getStructure();
                }
            } else if (event instanceof EPStructureChangeEvent && event.getCommand().equals(EPStructureChangeEvent.SELECTED)) {
                final EPStructureChangeEvent selEv = (EPStructureChangeEvent) event;
                selectedStructure = selEv.getPortfolioStructure();
                if (selectedStructure != null) {
                    editCtrl.setNewStructure(ureq, selectedStructure);
                }
            } else if (event instanceof EPMoveEvent) {
                tocCtrl.refreshTree(rootStructure);
            } else if (event instanceof EPStructureChangeEvent && event.getCommand().equals(EPStructureChangeEvent.ADDED)) {
                // always reload to be on the save side!
                selectedStructure = ePFMgr.loadPortfolioStructureByKey(((EPStructureChangeEvent) event).getPortfolioStructure().getKey());
                initOrUpdateToc(ureq);
                initOrUpdateDetailsEditor(ureq);
            } else if (event.equals(Event.CHANGED_EVENT)) {
                // something got deleted
                initOrUpdateToc(ureq);
                // renew details controller
                selectedStructure = null;
                initOrUpdateDetailsEditor(ureq);
                editCtrl.setNoStructure();
                // refresh map on deletion:
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else if (source == editCtrl && event instanceof EPStructureEvent) {
            final EPStructureEvent structureEvent = (EPStructureEvent) event;
            if (EPStructureEvent.CHANGE.equals(structureEvent.getCommand())) {
                final PortfolioStructure structure = structureEvent.getStructure();
                if (rootStructure.equals(structure)) {
                    rootStructure = ePFMgr.loadPortfolioStructureByKey(rootStructure.getKey());
                }
                // refresh the tree on changes!
                tocCtrl.update(ureq, structure);
            }
            fireEvent(ureq, structureEvent);
        }
    }
}
