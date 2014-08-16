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

package org.olat.presentation.ims.qti;

import org.dom4j.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.qti.QTIResultSet;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.ims.qti.process.FilePersister;
import org.olat.lms.ims.qti.render.LocalizedXSLTransformer;
import org.olat.lms.qti.QTIResultService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 12.01.2005
 * 
 * @author Mike Stock
 */
public class QTIResultDetailsController extends BasicController {

    private final Long courseResourceableId;
    private final String nodeIdent;
    private final Identity identity;
    private final RepositoryEntry repositoryEntry;
    private final String type;

    private VelocityContainer main, details;
    private QTIResultTableModel tableModel;
    private TableController tableCtr;

    private CloseableModalController cmc;

    /**
     * @param courseResourceableId
     * @param nodeIdent
     * @param identity
     * @param re
     * @param type
     * @param ureq
     * @param wControl
     */
    public QTIResultDetailsController(final Long courseResourceableId, final String nodeIdent, final Identity identity, final RepositoryEntry re, final String type,
            final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        this.courseResourceableId = courseResourceableId;
        this.nodeIdent = nodeIdent;
        this.identity = identity;
        this.repositoryEntry = re;
        this.type = type;

        init(ureq);
    }

    private void init(final UserRequest ureq) {
        main = createVelocityContainer("qtires");
        details = createVelocityContainer("qtires_details");

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("column.header.date", 0, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("column.header.duration", 1, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("column.header.assesspoints", 2, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new StaticColumnDescriptor("sel", "column.header.details", getTranslator().translate("select")));

        final QTIResultService qtiResultService = (QTIResultService) CoreSpringFactory.getBean(QTIResultService.class);
        tableModel = new QTIResultTableModel(qtiResultService.getResultSets(courseResourceableId, nodeIdent, repositoryEntry.getKey(), identity));
        tableCtr.setTableDataModel(tableModel);
        listenTo(tableCtr);

        main.put("qtirestable", tableCtr.getInitialComponent());
        putInitialPanel(main);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == main) {
            if (event.getCommand().equals("close")) {
                fireEvent(ureq, Event.DONE_EVENT);
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == tableCtr) {
            final TableEvent tEvent = (TableEvent) event;
            if (tEvent.getActionId().equals("sel")) {
                final QTIResultSet resultSet = tableModel.getResultSet(tEvent.getRowId());

                final Document doc = FilePersister.retreiveResultsReporting(identity, type, resultSet.getAssessmentID());
                if (doc == null) {
                    showInfo("error.resreporting.na");
                    return;
                }
                final StringBuilder resultsHTML = LocalizedXSLTransformer.getInstance(ureq.getLocale()).renderResults(doc);
                details.contextPut("reshtml", resultsHTML);

                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), details);
                listenTo(cmc);

                cmc.activate();
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
