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
package org.olat.presentation.portfolio.artefacts.run;

import java.util.List;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.portfolio.structure.StructureStatusEnum;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.table.TableMultiSelectEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.portfolio.artefacts.collect.EPCollectStepForm03;
import org.olat.presentation.portfolio.artefacts.collect.EPReflexionChangeEvent;
import org.olat.presentation.portfolio.filter.PortfolioFilterController;
import org.olat.presentation.portfolio.structel.EPStructureChangeEvent;
import org.olat.system.commons.OutputEscapeType;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Controller to hold a table representation of artefacts - used with a struct (inside a map) it allows to unlink artefact - in chooseMode there is column to add artefact
 * to struct
 * <P>
 * Initial Date: 20.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMultipleArtefactsAsTableController extends BasicController implements EPMultiArtefactsController {

    private static final String CMD_DOWNLOAD = "download";
    private static final String CMD_CHOOSE = "choose";
    private static final String CMD_DELETE = "delete";
    private static final String CMD_UNLINK = "unlink";
    private static final String CMD_REFLEXION = "refl";
    private static final String CMD_TITLE = "title";
    private final VelocityContainer vC;
    private TableController artefactListTblCtrl;

    private CloseableModalWindowWrapperController artefactBox;
    private Controller reflexionCtrl;
    private PortfolioStructure struct;
    private CloseableModalWindowWrapperController reflexionBox;
    private final EPFrontendManager ePFMgr;
    private boolean mapClosed = false;
    private final boolean artefactChooseMode;
    private final EPSecurityCallback secCallback;
    private final PortfolioAbstractHandler portfolioModule;
    private ArtefactTableDataModel artefactListModel;

    public EPMultipleArtefactsAsTableController(final UserRequest ureq, final WindowControl wControl, final List<AbstractArtefact> artefacts,
            final PortfolioStructure struct, final boolean artefactChooseMode, final EPSecurityCallback secCallback) {
        super(ureq, wControl);
        this.artefactChooseMode = artefactChooseMode;
        this.secCallback = secCallback;
        vC = createVelocityContainer("multiArtefactTable");
        this.struct = struct;
        if (struct != null && struct.getRoot() instanceof PortfolioStructureMap) {
            mapClosed = StructureStatusEnum.CLOSED.equals(((PortfolioStructureMap) struct.getRoot()).getStatus());
        } else {
            mapClosed = false;
        }
        portfolioModule = CoreSpringFactory.getBean(PortfolioAbstractHandler.class);
        ePFMgr = CoreSpringFactory.getBean(EPFrontendManager.class);

        if (artefacts != null) {
            initOrUpdateTable(ureq, artefacts);
        }

        putInitialPanel(vC);
    }

    private void initOrUpdateTable(final UserRequest ureq, final List<AbstractArtefact> artefacts) {
        artefactListModel = new ArtefactTableDataModel(artefacts);
        artefactListModel.setLocale(getLocale());

        final TableGuiConfiguration tableGuiConfiguration = new TableGuiConfiguration();
        tableGuiConfiguration.setTableEmptyMessage(getTranslator().translate("table.empty"));
        tableGuiConfiguration.setPageingEnabled(true);
        tableGuiConfiguration.setDownloadOffered(struct == null); // offer download only when in artefact pool (no struct given)
        tableGuiConfiguration.setResultsPerPage(10);
        tableGuiConfiguration.setPreferencesOffered(true, "artefacts.as.table.prefs");
        artefactListTblCtrl = new TableController(tableGuiConfiguration, ureq, getWindowControl(), getTranslator());

        listenTo(artefactListTblCtrl);

        final String details = artefactChooseMode ? null : CMD_TITLE;
        DefaultColumnDescriptor descr = new DefaultColumnDescriptor("artefact.title", 0, details, getLocale(), OutputEscapeType.HTML);
        artefactListTblCtrl.addColumnDescriptor(descr);

        descr = new DefaultColumnDescriptor("artefact.description", 1, null, getLocale(), OutputEscapeType.ANTISAMY);
        artefactListTblCtrl.addColumnDescriptor(true, descr);

        descr = new DefaultColumnDescriptor("artefact.date", 2, null, getLocale());
        artefactListTblCtrl.addColumnDescriptor(true, descr);

        descr = new DefaultColumnDescriptor("artefact.author", 3, null, getLocale());
        artefactListTblCtrl.addColumnDescriptor(false, descr);

        descr = new DefaultColumnDescriptor("artefact.tags", 4, null, getLocale(), OutputEscapeType.HTML);
        artefactListTblCtrl.addColumnDescriptor(false, descr);

        descr = new CustomRenderColumnDescriptor("table.header.type", 5, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_CENTER, new ArtefactTypeImageCellRenderer()) {
            /**
			 */
            @Override
            public int compareTo(final int rowa, final int rowb) {
                final Object a = table.getTableDataModel().getValueAt(rowa, dataColumn);
                final Object b = table.getTableDataModel().getValueAt(rowb, dataColumn);
                final String typeA = getArtefactTranslatedTypeName((AbstractArtefact) a);
                final String typeB = getArtefactTranslatedTypeName((AbstractArtefact) b);
                return typeA.compareTo(typeB);
            }
        };
        artefactListTblCtrl.addColumnDescriptor(false, descr);

        StaticColumnDescriptor staticDescr;

        if (!artefactChooseMode) {
            if (mapClosed || !secCallback.canEditStructure()) { // change link-description in row, when map is closed or viewed by another person
                staticDescr = new StaticColumnDescriptor(CMD_REFLEXION, "table.header.reflexion", translate("table.header.view"));
            } else {
                staticDescr = new StaticColumnDescriptor(CMD_REFLEXION, "table.header.reflexion", translate("table.row.reflexion"));
            }
            artefactListTblCtrl.addColumnDescriptor(true, staticDescr);
        } else {
            staticDescr = new StaticColumnDescriptor(CMD_CHOOSE, "table.header.choose", translate("choose.artefact"));
            artefactListTblCtrl.addColumnDescriptor(true, staticDescr);
        }

        if (struct == null) {
            // artefact pool (no struct given) => deletion possible
            artefactListTblCtrl.setMultiSelect(true);
            artefactListTblCtrl.addMultiSelectAction("delete", CMD_DELETE);
        } else {
            // artifacts attached to struct => only unlinking possible
            if (secCallback.canRemoveArtefactFromStruct()) {
                artefactListTblCtrl.setMultiSelect(true);
                artefactListTblCtrl.addMultiSelectAction("remove.from.map", CMD_UNLINK);
            }
        }

        artefactListTblCtrl.setTableDataModel(artefactListModel);
        if (vC.getComponent("artefactTable") != null) {
            vC.remove(artefactListTblCtrl.getInitialComponent());
        }
        vC.put("artefactTable", artefactListTblCtrl.getInitialComponent());
    }

    // translate the type of artefact needed for sorting by type
    String getArtefactTranslatedTypeName(final AbstractArtefact artefact) {
        final EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(artefact.getResourceableTypeName());
        final Translator handlerTrans = artHandler.getHandlerTranslator(getTranslator());
        final String handlerClass = PortfolioFilterController.HANDLER_PREFIX + artHandler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
        final String artType = handlerTrans.translate(handlerClass);
        return artType;
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == artefactListTblCtrl) {
            if (event instanceof TableEvent) {
                final TableEvent te = (TableEvent) event;
                final AbstractArtefact artefact = (AbstractArtefact) artefactListTblCtrl.getTableDataModel().getObject(te.getRowId());
                final String action = te.getActionId();
                if (CMD_TITLE.equals(action)) {
                    popupArtefact(artefact, ureq);
                } else if (CMD_DOWNLOAD.equals(action)) {
                    downloadArtefact(artefact, ureq);
                } else if (CMD_REFLEXION.equals(action)) {
                    popupReflexion(artefact, ureq);
                } else if (CMD_CHOOSE.equals(action)) {
                    fireEvent(ureq, new EPArtefactChoosenEvent(artefact));
                }
            } else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
                final TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
                final List<AbstractArtefact> selectedArtifacts = artefactListModel.getObjects(tmse.getSelection());
                if (!selectedArtifacts.isEmpty()) {
                    if (tmse.getAction().equals(CMD_DELETE)) {
                        for (AbstractArtefact selectedArtifact : selectedArtifacts) {
                            ePFMgr.deleteArtefact(selectedArtifact);
                            artefactListModel.getObjects().remove(selectedArtifact);
                        }
                    } else if (tmse.getAction().equals(CMD_UNLINK)) {
                        for (AbstractArtefact selectedArtifact : selectedArtifacts) {
                            ePFMgr.removeArtefactFromStructure(selectedArtifact, struct);
                            artefactListModel.getObjects().remove(selectedArtifact);
                        }
                    }
                    artefactListTblCtrl.modelChanged();
                    fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.REMOVED, struct));
                }
            }
        } else if (source == reflexionCtrl && event instanceof EPReflexionChangeEvent) {
            final EPReflexionChangeEvent refEv = (EPReflexionChangeEvent) event;
            if (struct != null) {
                ePFMgr.setReflexionForArtefactToStructureLink(refEv.getRefArtefact(), struct, refEv.getReflexion());
                reflexionBox.deactivate();
                fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, struct));
            } else {
                final AbstractArtefact artefact = refEv.getRefArtefact();
                artefact.setReflexion(refEv.getReflexion());
                ePFMgr.updateArtefact(artefact);
                reflexionBox.deactivate();
                fireEvent(ureq, Event.DONE_EVENT);
            }
            removeAsListenerAndDispose(reflexionBox);
        } else if (source == reflexionBox && event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
            removeAsListenerAndDispose(reflexionBox);
            reflexionBox = null;
        }
        super.event(ureq, source, event);
    }

    protected void popupReflexion(final AbstractArtefact artefact, final UserRequest ureq) {
        removeAsListenerAndDispose(reflexionCtrl);
        String title = "";
        final boolean artClosed = ePFMgr.isArtefactClosed(artefact);
        if (mapClosed || !secCallback.canEditStructure() || (artClosed && struct == null)) {
            // reflexion cannot be edited, view only!
            reflexionCtrl = new EPReflexionViewController(ureq, getWindowControl(), artefact, struct);
        } else {
            // check for an existing reflexion on the artefact <-> struct link
            final String reflexion = ePFMgr.getReflexionForArtefactToStructureLink(artefact, struct);
            if (StringHelper.containsNonWhitespace(reflexion)) {
                // edit an existing reflexion
                reflexionCtrl = new EPCollectStepForm03(ureq, getWindowControl(), artefact, reflexion);
                title = translate("title.reflexion.link");
            } else if (struct != null) {
                // no reflexion on link yet, show warning and preset with artefacts-reflexion
                reflexionCtrl = new EPCollectStepForm03(ureq, getWindowControl(), artefact, true);
                title = translate("title.reflexion.artefact");
            } else {
                // preset controller with reflexion of the artefact. used by artefact-pool
                reflexionCtrl = new EPCollectStepForm03(ureq, getWindowControl(), artefact);
                title = translate("title.reflexion.artefact");
            }
        }
        listenTo(reflexionCtrl);
        removeAsListenerAndDispose(reflexionBox);
        reflexionBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, reflexionCtrl.getInitialComponent(), "reflexionBox");
        listenTo(reflexionBox);
        reflexionBox.setInitialWindowSize(550, 600);
        reflexionBox.activate();
    }

    protected void downloadArtefact(final AbstractArtefact artefact, final UserRequest ureq) {
        getWindowControl().setInfo("not yet possible");
    }

    protected void popupArtefact(final AbstractArtefact artefact, final UserRequest ureq) {
        final String title = translate("view.artefact.header");
        artefactBox = EPUIFactory.getAndActivatePopupArtefactController(artefact, ureq, getWindowControl(), title);
        listenTo(artefactBox);
    }

    @Override
    public void setNewArtefactsList(final UserRequest ureq, final List<AbstractArtefact> artefacts) {
        initOrUpdateTable(ureq, artefacts);
    }
}
