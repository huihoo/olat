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

package de.bps.ims.qti;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.QTIResultTableModel;
import org.olat.lms.ims.qti.process.FilePersister;
import org.olat.ims.qti.render.LocalizedXSLTransformer;
import org.olat.repository.RepositoryEntry;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;
import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManager;
import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManagerFactory;

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
	private VelocityContainer onyxReporterVC;
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

		final Translator translator = Util.createPackageTranslator(org.olat.ims.qti.QTIResultDetailsController.class, getTranslator().getLocale(), getTranslator());
		setTranslator(translator);

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

		final QTIResultManager qrm = QTIResultManager.getInstance();
		tableModel = new QTIResultTableModel(qrm.getResultSets(courseResourceableId, nodeIdent, repositoryEntry.getKey(), identity));
		tableCtr.setTableDataModel(tableModel);
		listenTo(tableCtr);

		main.put("qtirestable", tableCtr.getInitialComponent());
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.components.Component, org.olat.presentation.framework.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		if (source == main) {
			if (event.getCommand().equals("close")) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Controller source, final Event event) {
		if (source == tableCtr) {
			final TableEvent tEvent = (TableEvent) event;
			if (tEvent.getActionId().equals("sel")) {
				if (OnyxModule.isOnyxTest(repositoryEntry.getOlatResource())) {
					final QTIResultSet resultSet = tableModel.getResultSet(tEvent.getRowId());
					onyxReporterVC = createVelocityContainer("onyxreporter");
					if (showOnyxReporter(ureq, resultSet.getAssessmentID())) {
						final CloseableModalController cmc = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), onyxReporterVC);
						cmc.activate();
					} else {
						getWindowControl().setError(getTranslator().translate("onyxreporter.error"));
					}
				} else {
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
	}

	/**
	 * This methods calls the OnyxReporter and shows it in an iframe.
	 * 
	 * @param ureq The UserRequest for getting the identity and role of the current user.
	 */
	private boolean showOnyxReporter(final UserRequest ureq, final long assassmentId) {

		final OnyxReporterWebserviceManager onyxReporter = OnyxReporterWebserviceManagerFactory.getInstance().fabricate("OnyxReporterWebserviceClient");
		if (onyxReporter != null) {
			// make a list of this one student because onyxReporter needs a list
			final List<Identity> identityList = new ArrayList<Identity>();
			identityList.add(identity);

			final CourseNode cn = CourseFactory.loadCourse(courseResourceableId).getEditorTreeModel().getCourseNode(this.nodeIdent);
			onyxReporter.setAssassmentId(assassmentId);
			String iframeSrc = "";
			try {
				iframeSrc = onyxReporter.startReporter(ureq, identityList, cn, true);
			} catch (final RemoteException e) {
				e.printStackTrace();
				return false;
			} catch (final OnyxReporterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			onyxReporterVC.contextPut("showBack", Boolean.TRUE);
			onyxReporterVC.contextPut("iframeOK", Boolean.TRUE);
			onyxReporterVC.contextPut("onyxReportLink", iframeSrc);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}

}
