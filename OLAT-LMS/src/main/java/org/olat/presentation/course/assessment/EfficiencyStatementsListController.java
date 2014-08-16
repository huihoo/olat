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

package org.olat.presentation.course.assessment;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.EfficiencyStatementArtefact;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.assessment.EfficiencyStatement;
import org.olat.lms.course.assessment.EfficiencyStatementManager;
import org.olat.lms.portfolio.PortfolioModule;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.BooleanColumnDescriptor;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.dtabs.DTab;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.portfolio.artefacts.collect.ArtefactWizzardStepsController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Shows the list of all efficiency statements for this user
 * <P>
 * Initial Date: 11.07.2005 <br>
 * 
 * @author gnaegi
 */
public class EfficiencyStatementsListController extends BasicController {
    private static final String CMD_SHOW = "cmd.show";
    private static final String CMD_LAUNCH_COURSE = "cmd.launch.course";
    private static final String CMD_DELETE = "cmd.delete";
    private static final String CMD_ARTEFACT = "cmd.artefact";

    private final TableController tableCtr;
    private final EfficiencyStatementsListModel efficiencyStatementsListModel;
    private DialogBoxController confirmDeleteCtr;
    private EfficiencyStatement efficiencyStatement;
    private Controller ePFCollCtrl;
    private final PortfolioModule portfolioModule;

    /**
     * Constructor
     * 
     * @param wControl
     * @param ureq
     */
    public EfficiencyStatementsListController(final WindowControl wControl, final UserRequest ureq) {
        super(ureq, wControl);

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("efficiencyStatementsPortlet.nostatements"));
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.course", 0, null, getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.score", 1, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
        tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.passed", 2, translate("passed.true"), translate("passed.false")));
        final StaticColumnDescriptor cd3 = new StaticColumnDescriptor(CMD_SHOW, "table.header.show", translate("table.action.show"));
        cd3.setIsPopUpWindowAction(true, "height=600, width=800, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
        tableCtr.addColumnDescriptor(cd3);
        tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_LAUNCH_COURSE, "table.header.launchcourse", translate("table.action.launchcourse")));
        tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_DELETE, "table.header.delete", translate("table.action.delete")));

        portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
        final EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(EfficiencyStatementArtefact.ARTEFACT_TYPE);
        if (portfolioModule.isEnabled() && artHandler != null && artHandler.isEnabled()) {
            tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.artefact", 5, CMD_ARTEFACT, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_CENTER,
                    new AsArtefactCellRenderer()));
        }
        listenTo(tableCtr);

        final EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
        final List<EfficiencyStatement> efficiencyStatementsList = esm.findEfficiencyStatements(ureq.getIdentity());
        efficiencyStatementsListModel = new EfficiencyStatementsListModel(efficiencyStatementsList);
        tableCtr.setTableDataModel(efficiencyStatementsListModel);

        putInitialPanel(tableCtr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to catch
    }

    /**
     * org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                efficiencyStatement = efficiencyStatementsListModel.getEfficiencyStatementAt(rowid);
                if (actionid.equals(CMD_SHOW)) {
                    // will not be disposed on course run dispose, popus up as new browserwindow
                    final ControllerCreator ctrlCreator = new ControllerCreator() {
                        @Override
                        public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                            return new EfficiencyStatementController(lwControl, lureq, efficiencyStatement.getCourseRepoEntryKey());
                        }
                    };
                    // wrap the content controller into a full header layout
                    final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
                    // open in new browser window
                    final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
                    pbw.open(ureq);
                    //
                } else if (actionid.equals(CMD_LAUNCH_COURSE)) {
                    final RepositoryService rm = RepositoryServiceImpl.getInstance();
                    final RepositoryEntry re = rm.lookupRepositoryEntry(efficiencyStatement.getCourseRepoEntryKey());
                    if (re == null) {
                        showWarning("efficiencyStatements.course.noexists");
                    } else if (!rm.isAllowedToLaunch(ureq.getIdentity(), ureq.getUserSession().getRoles(), re)) {
                        showWarning("efficiencyStatements.course.noaccess");
                    } else {
                        final OLATResourceable ores = re.getOlatResource();
                        // was brasato:: DTabs dts = getWindowControl().getDTabs();
                        final DTabs dts = (DTabs) Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
                        DTab dt = dts.getDTab(ores);
                        if (dt == null) {
                            // does not yet exist -> create and add
                            dt = dts.createDTab(ores, efficiencyStatement.getCourseTitle());
                            if (dt == null) {
                                return;
                            }
                            final Controller launchController = ControllerFactory.createLaunchController(ores, null, ureq, dt.getWindowControl(), true);
                            dt.setController(launchController);
                            dts.addDTab(dt);
                        }
                        dts.activate(ureq, dt, null); // null: do not activate to a certain view

                    }
                } else if (actionid.equals(CMD_DELETE)) {
                    // show confirmation dialog
                    confirmDeleteCtr = activateYesNoDialog(ureq, null, translate("efficiencyStatements.delete.confirm", efficiencyStatement.getCourseTitle()),
                            confirmDeleteCtr);
                    return;
                } else if (actionid.equals(CMD_ARTEFACT)) {
                    popupArtefactCollector(ureq);
                }
            }
        } else if (source == confirmDeleteCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                // delete efficiency statement manager
                final EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
                esm.deleteEfficiencyStatement(ureq.getIdentity(), efficiencyStatement);
                efficiencyStatementsListModel.getObjects().remove(efficiencyStatement);
                efficiencyStatement = null;
                tableCtr.modelChanged();
                showInfo("info.efficiencyStatement.deleted");
            }
        }
    }

    private void popupArtefactCollector(final UserRequest ureq) {
        final EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(EfficiencyStatementArtefact.ARTEFACT_TYPE);
        if (artHandler != null && artHandler.isEnabled()) {
            final AbstractArtefact artefact = artHandler.createArtefact();
            artefact.setAuthor(getIdentity());// only author can create artefact
            // no business path becouse we cannot launch an efficiency statement
            artefact.setCollectionDate(new Date());
            artefact.setTitle(translate("artefact.title", new String[] { efficiencyStatement.getCourseTitle() }));
            artHandler.prefillArtefactAccordingToSource(artefact, efficiencyStatement);
            ePFCollCtrl = new ArtefactWizzardStepsController(ureq, getWindowControl(), artefact, (VFSContainer) null);
            listenTo(ePFCollCtrl);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // DialogBoxController and TableController get disposed by BasicController
    }

    public class AsArtefactCellRenderer implements CustomCellRenderer {
        /**
         * java.util.Locale, int, java.lang.String)
         */
        @SuppressWarnings("unused")
        @Override
        public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
            sb.append("<span class=\"b_eportfolio_add_again\" title=\"").append(translate("table.add.as.artefact"))
                    .append("\" style=\"background-repeat: no-repeat; background-position: center center; padding: 2px 8px;\">&nbsp;</span>");
        }
    }
}
