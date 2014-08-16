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

package de.bps.course.assessment;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.admin.securitygroup.gui.UserControllerFactory;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.data.DBFactory;
import org.olat.data.PersistenceHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.MenuTree;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tool.ToolController;
import org.olat.presentation.framework.core.control.generic.tool.ToolFactory;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.util.Util;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessedIdentitiesTableDataModel;
import org.olat.course.assessment.AssessedIdentityWrapper;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.course.assessment.AssessmentEditController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentUIFactory;
import org.olat.course.assessment.BulkAssessmentMainController;
import org.olat.course.assessment.GroupAndContextTableModel;
import org.olat.course.assessment.IAssessmentCallback;
import org.olat.course.assessment.IdentityAssessmentEditController;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.OnlyGroupConditionInterpreter;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.context.BGContextTableModel;
import org.olat.user.UserManager;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;
import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManager;
import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManagerFactory;

/**
 * Initial Date: Jun 18, 2004
 * 
 * @author gnaegi Comment: This contoller can be used to control and change user score, passed, attempts and comment variables. It provides a menu that allows three
 *         different access paths to the same data: user centric, group centric or course node centric.
 */
public class AssessmentMainController extends MainLayoutBasicController implements Activateable, GenericEventListener {
	private static final Logger log = LoggerHelper.getLogger();


	private static final String CMD_INDEX = "cmd.index";
	private static final String CMD_USERFOCUS = "cmd.userfocus";
	private static final String CMD_GROUPFOCUS = "cmd.groupfocus";
	private static final String CMD_NODEFOCUS = "cmd.nodefocus";
	private static final String CMD_BULKFOCUS = "cmd.bulkfocus";

	private static final String CMD_CHOOSE_GROUP = "cmd.choose.group";
	private static final String CMD_CHOOSE_USER = "cmd.choose.user";
	private static final String CMD_SELECT_NODE = "cmd.select.node";

	private static final String CMD_SHOW_ONYXREPORT = "cmd.show.onyxreport";
	public static final String KEY_IS_ONYX = "isOnyx";

	private static final int MODE_USERFOCUS = 0;
	private static final int MODE_GROUPFOCUS = 1;
	private static final int MODE_NODEFOCUS = 2;
	private static final int MODE_BULKFOCUS = 3;
	private int mode;

	private final IAssessmentCallback callback;
	private final MenuTree menuTree;
	private final Panel main;

	private ToolController toolC;
	private final VelocityContainer index;

	private VelocityContainer groupChoose, userChoose, nodeChoose, wrapper;

	private VelocityContainer onyxReporterVC;

	private NodeTableDataModelOnyx nodeTableModel;
	private TableController groupListCtr, userListCtr, nodeListCtr;
	private List nodeFilters;
	private List<Identity> identitiesList;

	// Course assessment notification support fields
	private Controller csc;

	// Hash map to keep references to already created user course environments
	// Serves as a local cache to reduce database access - not shared by multiple threads
	Map<Long, UserCourseEnvironment> localUserCourseEnvironmentCache; // package visibility for avoiding synthetic accessor method
	// List of groups to which the user has access rights in this course
	private List<BusinessGroup> coachedGroups;

	// some state variables
	private AssessableCourseNode currentCourseNode;
	private AssessedIdentityWrapper assessedIdentityWrapper;
	private AssessmentEditController assessmentEditController;
	private IdentityAssessmentEditController identityAssessmentController;
	private BusinessGroup currentGroup;

	private Thread assessmentCachPreloaderThread;
	private Link backLinkUC;
	private Link backLinkGC;

	// back button for the Onyx Reporter
	private Link backLinkOR;
	// backbutton needs information where it should go back
	private String onyxReporterBackLocation;

	private Link allUsersButton;

	private final boolean isAdministrativeUser;
	private final Translator propertyHandlerTranslator;

	private boolean isFiltering = true;
	private Link showAllCourseNodesButton;
	private Link filterCourseNodesButton;

	private BulkAssessmentMainController bamc;

	private final OLATResourceable ores;

	private Link showOnyxReporterButton;
	private final LayoutMain3ColsController columLayoutCtr;

	/**
	 * Constructor for the assessment tool controller.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param assessmentCallback
	 */
	AssessmentMainController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores, final IAssessmentCallback assessmentCallback) {
		super(ureq, wControl);

		final Translator translator = Util.createPackageTranslator(org.olat.course.assessment.AssessmentMainController.class, getTranslator().getLocale(),
				getTranslator());
		setTranslator(translator);

		getUserActivityLogger().setStickyActionType(ActionType.admin);
		this.ores = ores;
		this.callback = assessmentCallback;
		this.localUserCourseEnvironmentCache = new HashMap<Long, UserCourseEnvironment>();

		// use the PropertyHandlerTranslator as tableCtr translator
		propertyHandlerTranslator = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());

		final Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = roles.isAdministrativeUser();

		main = new Panel("assessmentmain");

		// Intro page, static
		index = createVelocityContainer("assessment_index");

		Identity focusOnIdentity = null;
		final ICourse course = CourseFactory.loadCourse(ores);
		final boolean hasAssessableNodes = course.hasAssessableNodes();
		if (hasAssessableNodes) {
			final BusinessControl bc = getWindowControl().getBusinessControl();
			final ContextEntry ceIdentity = bc.popLauncherContextEntry();
			if (ceIdentity != null) {
				final OLATResourceable oresIdentity = ceIdentity.getOLATResourceable();
				if (OresHelper.isOfType(oresIdentity, Identity.class)) {
					final Long identityKey = oresIdentity.getResourceableId();
					focusOnIdentity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey);
				}
			}

			index.contextPut("hasAssessableNodes", Boolean.TRUE);

			// --- assessment notification subscription ---
			csc = AssessmentUIFactory.createContextualSubscriptionController(ureq, wControl, course);
			if (csc != null) {
				listenTo(csc); // cleanup on dispose
				index.put("assessmentSubscription", csc.getInitialComponent());
			}

			// Wrapper container: adds header
			wrapper = createVelocityContainer("wrapper");

			// Init the group and the user chooser view velocity container
			groupChoose = createVelocityContainer("groupchoose");
			allUsersButton = LinkFactory.createButtonSmall("cmd.all.users", groupChoose, this);
			groupChoose.contextPut("isFiltering", Boolean.TRUE);
			backLinkGC = LinkFactory.createLinkBack(groupChoose, this);

			userChoose = createVelocityContainer("userchoose");

			showOnyxReporterButton = LinkFactory.createButtonSmall("cmd.showOnyxReporter", userChoose, this);

			showAllCourseNodesButton = LinkFactory.createButtonSmall("cmd.showAllCourseNodes", userChoose, this);
			filterCourseNodesButton = LinkFactory.createButtonSmall("cmd.filterCourseNodes", userChoose, this);
			userChoose.contextPut("isFiltering", Boolean.TRUE);
			backLinkUC = LinkFactory.createLinkBack(userChoose, this);

			onyxReporterVC = createVelocityContainer("onyxreporter");
			backLinkOR = LinkFactory.createLinkBack(onyxReporterVC, this);

			nodeChoose = createVelocityContainer("nodechoose");

			// Initialize all groups that the user is allowed to coach
			coachedGroups = getAllowedGroupsFromGroupmanagement(ureq.getIdentity());

			// preload the assessment cache to speed up everything as background thread
			// the thread will terminate when finished
			assessmentCachPreloaderThread = new AssessmentCachePreloadThread("assessmentCachPreloader-" + course.getResourceableId());
			assessmentCachPreloaderThread.setDaemon(true);
			assessmentCachPreloaderThread.start();

		} else {
			index.contextPut("hasAssessableNodes", Boolean.FALSE);
		}

		// Navigation menu
		menuTree = new MenuTree("menuTree");
		final TreeModel tm = buildTreeModel(hasAssessableNodes);
		menuTree.setTreeModel(tm);
		menuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		menuTree.addListener(this);

		// Tool and action box
		toolC = ToolFactory.createToolController(getWindowControl());
		listenTo(toolC);
		toolC.addHeader(translate("tool.name"));
		toolC.addLink("cmd.close", translate("command.closeassessment"), null, "b_toolbox_close");

		// Start on index page
		main.setContent(index);

		columLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, toolC.getInitialComponent(), main, "course" + course.getResourceableId());

		listenTo(columLayoutCtr); // cleanup on dispose
		putInitialPanel(columLayoutCtr.getInitialComponent());

		if (focusOnIdentity != null) {
			// fill the user list for the
			this.mode = MODE_USERFOCUS;
			this.identitiesList = getAllIdentitisFromGroupmanagement();
			doSimpleUserChoose(ureq, this.identitiesList);

			final GenericTreeModel menuTreeModel = (GenericTreeModel) menuTree.getTreeModel();
			final TreeNode userNode = menuTreeModel.findNodeByUserObject(CMD_USERFOCUS);
			if (userNode != null) {
				menuTree.setSelectedNode(userNode);
			}

			// select user
			this.assessedIdentityWrapper = AssessmentHelper.wrapIdentity(focusOnIdentity, this.localUserCourseEnvironmentCache, course, null);

			final UserCourseEnvironment chooseUserCourseEnv = assessedIdentityWrapper.getUserCourseEnvironment();
			identityAssessmentController = new IdentityAssessmentEditController(getWindowControl(), ureq, chooseUserCourseEnv, course, true);
			listenTo(identityAssessmentController);
			setContent(identityAssessmentController.getInitialComponent());
		}

		// Register for assessment changed events
		course.getCourseEnvironment().getAssessmentManager().registerForAssessmentChangeEvents(this, ureq.getIdentity());
	}

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.components.Component, org.olat.presentation.framework.control.Event)
	 */
	public void event(final UserRequest ureq, final Component source, final Event event) {
		if (source == menuTree) {
			disposeChildControllerAndReleaseLocks(); // first cleanup old locks
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				final TreeNode selTreeNode = menuTree.getSelectedNode();
				final String cmd = (String) selTreeNode.getUserObject();

				resetToolController();

				// reset helper variables
				this.currentCourseNode = null;
				this.currentGroup = null;
				this.isFiltering = true;
				if (cmd.equals(CMD_INDEX)) {
					main.setContent(index);
				} else if (cmd.equals(CMD_USERFOCUS)) {
					this.mode = MODE_USERFOCUS;
					this.identitiesList = getAllIdentitisFromGroupmanagement();
					doSimpleUserChoose(ureq, this.identitiesList);
				} else if (cmd.equals(CMD_GROUPFOCUS)) {
					this.mode = MODE_GROUPFOCUS;
					doGroupChoose(ureq);
				} else if (cmd.equals(CMD_NODEFOCUS)) {
					this.mode = MODE_NODEFOCUS;
					doNodeChoose(ureq);
				} else if (cmd.equals(CMD_BULKFOCUS)) {
					this.mode = MODE_BULKFOCUS;
					doBulkChoose(ureq);
				}
			}
		} else if (source == allUsersButton) {
			this.identitiesList = getAllIdentitisFromGroupmanagement();
			// Init the user list with this identitites list
			this.currentGroup = null;
			doUserChooseWithData(ureq, this.identitiesList, null, this.currentCourseNode);
		} else if (source == backLinkGC) {
			setContent(nodeListCtr.getInitialComponent());
		} else if (source == backLinkUC) {
			setContent(groupChoose);
		} else if (source == showAllCourseNodesButton) {
			enableFilteringCourseNodes(false);
		} else if (source == filterCourseNodesButton) {
			enableFilteringCourseNodes(true);

		} else if (source == showOnyxReporterButton) {
			this.identitiesList = getGroupIdentitiesFromGroupmanagement(this.currentGroup);
			this.onyxReporterBackLocation = "userChoose";
			if (!showOnyxReporter(ureq)) {
				getWindowControl().setError(translate("onyxreporter.error"));
			}
		} else if (source == backLinkOR) {
			if (onyxReporterBackLocation.equals("userChoose")) {
				setContent(userChoose);
			} else if (onyxReporterBackLocation.equals("nodeListCtr")) {
				setContent(nodeListCtr.getInitialComponent());
			}
		}

	}

	/**
	 * This methods calls the OnyxReporter and shows it in an iframe.
	 * 
	 * @param ureq The UserRequest for getting the identity and role of the current user.
	 */
	private boolean showOnyxReporter(final UserRequest ureq) {
		if (OnyxModule.isOnyxTest(currentCourseNode.getReferencedRepositoryEntry().getOlatResource())) {
			final OnyxReporterWebserviceManager onyxReporter = OnyxReporterWebserviceManagerFactory.getInstance().fabricate("OnyxReporterWebserviceClient");
			if (onyxReporter != null) {
				if (this.identitiesList == null) {
					this.identitiesList = getAllIdentitisFromGroupmanagement();
				}
				String iframeSrc = "";
				try {
					iframeSrc = onyxReporter.startReporter(ureq, this.identitiesList, currentCourseNode, false);
				} catch (final RemoteException e) {
					e.printStackTrace();
					return false;
				} catch (final OnyxReporterException orE) {
					if (orE.getMessage().equals("noresults")) {
						onyxReporterVC.contextPut("iframeOK", Boolean.FALSE);
						onyxReporterVC.contextPut("showBack", Boolean.TRUE);
						onyxReporterVC.contextPut("message", translate("no.testresults"));
						setContent(onyxReporterVC);
						return true;
					}
					return false;
				}
				onyxReporterVC.contextPut("showBack", Boolean.TRUE);
				onyxReporterVC.contextPut("iframeOK", Boolean.TRUE);
				onyxReporterVC.contextPut("onyxReportLink", iframeSrc);
				setContent(onyxReporterVC);
				return true;
			} else {
				userChoose.contextPut("iframeOK", Boolean.FALSE);
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Enable/disable filtering of course-nodes in user-selection table and update new course-node-list. (Assessemnt-tool =>
	 * 
	 * @param enableFiltering
	 */
	private void enableFilteringCourseNodes(final boolean enableFiltering) {
		final ICourse course = CourseFactory.loadCourse(ores);
		this.isFiltering = enableFiltering;
		userChoose.contextPut("isFiltering", enableFiltering);
		this.nodeFilters = addAssessableNodesToList(course.getRunStructure().getRootNode(), this.currentGroup);
		userListCtr.setFilters(this.nodeFilters, null);
	}

	/**
	 * Enable/disable filtering of groups in
	 * 
	 * @param enableFiltering
	 * @param ureq
	 */
	private void enableFilteringGroups(final boolean enableFiltering, final UserRequest ureq) {
		this.isFiltering = enableFiltering;
		groupChoose.contextPut("isFiltering", enableFiltering);
		doGroupChoose(ureq);
	}

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.control.Event)
	 */
	public void event(final UserRequest ureq, final Controller source, final Event event) {
		if (source == toolC) {
			if (event.getCommand().equals("cmd.close")) {
				disposeChildControllerAndReleaseLocks(); // cleanup locks from children
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if (source == groupListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				final TableEvent te = (TableEvent) event;
				final String actionid = te.getActionId();
				if (actionid.equals(CMD_CHOOSE_GROUP)) {
					final int rowid = te.getRowId();
					final GroupAndContextTableModel groupListModel = (GroupAndContextTableModel) groupListCtr.getTableDataModel();
					this.currentGroup = groupListModel.getBusinessGroupAt(rowid);
					this.identitiesList = getGroupIdentitiesFromGroupmanagement(this.currentGroup);
					// Init the user list with this identitites list
					doUserChooseWithData(ureq, this.identitiesList, this.currentGroup, this.currentCourseNode);
				}
			}
		} else if (source == userListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				final TableEvent te = (TableEvent) event;
				final String actionid = te.getActionId();
				if (actionid.equals(CMD_CHOOSE_USER)) {
					final int rowid = te.getRowId();
					final ICourse course = CourseFactory.loadCourse(ores);
					if (userListCtr.getTableDataModel() instanceof UserTableDataModel) {
						// in user MODE_USERFOCUS, a simple identity table is used, no wrapped identites
						final UserTableDataModel userListModel = (UserTableDataModel) userListCtr.getTableDataModel();
						final Identity assessedIdentity = userListModel.getIdentityAt(rowid);
						this.assessedIdentityWrapper = AssessmentHelper.wrapIdentity(assessedIdentity, this.localUserCourseEnvironmentCache, course, null);
					} else {
						// all other cases where user can be choosen the assessed identity wrapper is used
						final AssessedIdentitiesTableDataModel userListModel = (AssessedIdentitiesTableDataModel) userListCtr.getTableDataModel();
						this.assessedIdentityWrapper = userListModel.getWrappedIdentity(rowid);
					}
					// init edit controller for this identity and this course node
					// or use identity assessment overview if no course node is defined
					if (this.currentCourseNode == null) {
						final UserCourseEnvironment chooseUserCourseEnv = assessedIdentityWrapper.getUserCourseEnvironment();
						removeAsListenerAndDispose(identityAssessmentController);
						identityAssessmentController = new IdentityAssessmentEditController(getWindowControl(), ureq, chooseUserCourseEnv, course, true);
						listenTo(identityAssessmentController);
						setContent(identityAssessmentController.getInitialComponent());
					} else {
						removeAsListenerAndDispose(assessmentEditController);
						assessmentEditController = new AssessmentEditController(ureq, getWindowControl(), course, currentCourseNode, assessedIdentityWrapper);
						listenTo(assessmentEditController);
						main.setContent(assessmentEditController.getInitialComponent());
					}
				}
			} else if (event.equals(TableController.EVENT_FILTER_SELECTED)) {
				this.currentCourseNode = (AssessableCourseNode) userListCtr.getActiveFilter();
				doUserChooseWithData(ureq, this.identitiesList, this.currentGroup, this.currentCourseNode);
			}
		} else if (source == nodeListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				final TableEvent te = (TableEvent) event;
				final String actionid = te.getActionId();
				// moved variable declaration for course above the if-block
				final ICourse course = CourseFactory.loadCourse(ores);
				if (actionid.equals(CMD_SELECT_NODE)) {
					final int rowid = te.getRowId();
					final Map<String, Object> nodeData = (Map<String, Object>) nodeTableModel.getObject(rowid);
					final CourseNode node = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
					this.currentCourseNode = (AssessableCourseNode) node;
					// cast should be save, only assessable nodes are selectable
					doGroupChoose(ureq);

				} else if (actionid.equals(CMD_SHOW_ONYXREPORT)) {
					final int rowid = te.getRowId();
					final Map<String, Object> nodeData = (Map<String, Object>) nodeTableModel.getObject(rowid);
					final CourseNode node = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
					this.currentCourseNode = (AssessableCourseNode) node;
					this.onyxReporterBackLocation = "nodeListCtr";
					if (!showOnyxReporter(ureq)) {
						getWindowControl().setError(translate("onyxreporter.error"));
					}
				}

			} else if (event.equals(TableController.EVENT_FILTER_SELECTED)) {
				this.currentCourseNode = (AssessableCourseNode) nodeListCtr.getActiveFilter();
				doUserChooseWithData(ureq, this.identitiesList, null, this.currentCourseNode);
			}
		} else if (source == assessmentEditController) {
			if (event.equals(Event.CHANGED_EVENT)) {
				// refresh identity in list model
				if (userListCtr != null && userListCtr.getTableDataModel() instanceof AssessedIdentitiesTableDataModel) {
					final AssessedIdentitiesTableDataModel atdm = (AssessedIdentitiesTableDataModel) userListCtr.getTableDataModel();
					final List<AssessedIdentityWrapper> aiwList = atdm.getObjects();
					if (aiwList.contains(this.assessedIdentityWrapper)) {
						final ICourse course = CourseFactory.loadCourse(ores);
						aiwList.remove(this.assessedIdentityWrapper);
						this.assessedIdentityWrapper = AssessmentHelper.wrapIdentity(this.assessedIdentityWrapper.getIdentity(), this.localUserCourseEnvironmentCache,
								course, currentCourseNode);
						aiwList.add(this.assessedIdentityWrapper);
						userListCtr.modelChanged();
					}
				}
			} // else nothing special to do
			setContent(userChoose);
		} else if (source == identityAssessmentController) {
			if (event.equals(Event.CANCELLED_EVENT)) {
				setContent(userChoose);
			}
		}
	}

	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.presentation.framework.control.Event)
	 */
	public void event(final Event event) {
		if ((event instanceof AssessmentChangedEvent) && event.getCommand().equals(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED)) {
			final AssessmentChangedEvent ace = (AssessmentChangedEvent) event;
			doUpdateLocalCacheAndUserModelFromAssessmentEvent(ace);
		}
	}

	public void setToolController(final ToolController toolC) {
		if (toolC != null && columLayoutCtr != null) {
			columLayoutCtr.setCol2(toolC.getInitialComponent());
		}
	}

	public void resetToolController() {
		if (toolC != null && columLayoutCtr != null) {
			columLayoutCtr.setCol2(toolC.getInitialComponent());
		}
	}

	/**
	 * Notify subscribers when test are passed or attemps count change EXPERIMENTAL!!!!!
	 */
	/*
	 * private void doNotifyAssessmentEvent(AssessmentChangedEvent ace) { String assessmentChangeType = ace.getCommand(); // notify only comment has been changed if
	 * (assessmentChangeType == AssessmentChangedEvent.TYPE_PASSED_CHANGED || assessmentChangeType == AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED) { // if notification
	 * is enabled -> notify the publisher about news if (subsContext != null) { NotificationsManagerImpl.getInstance().markPublisherNews(subsContext, ace.getIdentity());
	 * } } }
	 */

	/**
	 * Updates the local user course environment cache if the given event is for an identity cached in the local cache. Also updates the user list table model if the
	 * identity from the event is in the model.
	 * 
	 * @param ace
	 */
	private void doUpdateLocalCacheAndUserModelFromAssessmentEvent(final AssessmentChangedEvent ace) {
		final String assessmentChangeType = ace.getCommand();
		// do not re-evaluate things if only comment has been changed
		if (assessmentChangeType.equals(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED) || assessmentChangeType.equals(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED)) {

			// Check if the identity in the event is in our local user course environment
			// cache. If so, update the look-uped users score accounting information.
			// Identity identityFromEvent = ace.getIdentity();
			final Long identityKeyFromEvent = ace.getIdentityKey();
			if (localUserCourseEnvironmentCache.containsKey(identityKeyFromEvent)) {
				final UserCourseEnvironment uce = localUserCourseEnvironmentCache.get(identityKeyFromEvent);
				// 1) update score accounting
				if (uce != null) {
					uce.getScoreAccounting().evaluateAll();
				}
				// 2) update user table model
				if (userListCtr != null && userListCtr.getTableDataModel() instanceof AssessedIdentitiesTableDataModel) {
					// 2.1) search wrapper object in model
					final AssessedIdentitiesTableDataModel aitd = (AssessedIdentitiesTableDataModel) userListCtr.getTableDataModel();
					final List<AssessedIdentityWrapper> wrappers = aitd.getObjects();
					final Iterator<AssessedIdentityWrapper> iter = wrappers.iterator();
					AssessedIdentityWrapper wrappedIdFromModel = null;
					while (iter.hasNext()) {
						final AssessedIdentityWrapper wrappedId = iter.next();
						if (wrappedId.getIdentity().getKey().equals(identityKeyFromEvent)) {
							wrappedIdFromModel = wrappedId;
						}
					}
					// 2.2) update wrapper object
					if (wrappedIdFromModel != null) {
						wrappers.remove(wrappedIdFromModel);
						wrappedIdFromModel = AssessmentHelper.wrapIdentity(wrappedIdFromModel.getUserCourseEnvironment(), currentCourseNode);
						wrappers.add(wrappedIdFromModel);
						userListCtr.modelChanged();
					}
				}
			}
			// else user not in our local cache -> nothing to do
		}
	}

	/**
	 * @param selectedGroup
	 * @return List of participant identities from this group
	 */
	private List<Identity> getGroupIdentitiesFromGroupmanagement(final BusinessGroup selectedGroup) {
		final SecurityGroup selectedSecurityGroup = selectedGroup.getPartipiciantGroup();
		return BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(selectedSecurityGroup);
	}

	/**
	 * @return List of all course participants
	 */
	List<Identity> getAllIdentitisFromGroupmanagement() {
		final List<Identity> allUsersList = new ArrayList<Identity>();
		final BaseSecurity secMgr = BaseSecurityManager.getInstance();
		final Iterator<BusinessGroup> iter = this.coachedGroups.iterator();
		while (iter.hasNext()) {
			final BusinessGroup group = iter.next();
			final SecurityGroup secGroup = group.getPartipiciantGroup();
			final List<Identity> identities = secMgr.getIdentitiesOfSecurityGroup(secGroup);
			for (final Iterator<Identity> identitiyIter = identities.iterator(); identitiyIter.hasNext();) {
				final Identity identity = identitiyIter.next();
				if (!PersistenceHelper.listContainsObjectByKey(allUsersList, identity)) {
					// only add if not already in list
					allUsersList.add(identity);
				}
			}
		}
		return allUsersList;
	}

	/**
	 * @param identity
	 * @return List of all course groups if identity is course admin, else groups that are coached by this identity
	 */
	private List<BusinessGroup> getAllowedGroupsFromGroupmanagement(final Identity identity) {
		final ICourse course = CourseFactory.loadCourse(ores);
		final CourseGroupManager gm = course.getCourseEnvironment().getCourseGroupManager();
		if (callback.mayAssessAllUsers() || callback.mayViewAllUsersAssessments()) {
			return gm.getAllLearningGroupsFromAllContexts();
		} else if (callback.mayAssessCoachedUsers()) {
			return gm.getOwnedLearningGroupsFromAllContexts(identity);
		} else {
			throw new OLATSecurityException("No rights to assess or even view any groups");
		}
	}

	/**
	 * Initialize the group list table according to the users access rights
	 * 
	 * @param ureq The user request
	 */
	private void doGroupChoose(final UserRequest ureq) {
		final ICourse course = CourseFactory.loadCourse(ores);
		removeAsListenerAndDispose(groupListCtr);
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("groupchoose.nogroups"));
		groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(groupListCtr);
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.name", 0, CMD_CHOOSE_GROUP, ureq.getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.desc", 1, null, ureq.getLocale()));
		final CourseGroupManager gm = course.getCourseEnvironment().getCourseGroupManager();
		if (gm.getLearningGroupContexts().size() > 1) {
			// show groupcontext row only if multiple contexts are found
			groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.context", 2, null, ureq.getLocale()));
		}

		final Translator defaultContextTranslator = new PackageTranslator(Util.getPackageName(BGContextTableModel.class), ureq.getLocale());
		// loop over all groups to filter depending on condition
		final List<BusinessGroup> currentGroups = new ArrayList<BusinessGroup>();
		for (final Iterator iter = this.coachedGroups.iterator(); iter.hasNext();) {
			final BusinessGroup group = (BusinessGroup) iter.next();
			if (!isFiltering || isVisibleAndAccessable(this.currentCourseNode, group)) {
				currentGroups.add(group);
			}
		}
		final GroupAndContextTableModel groupTableDataModel = new GroupAndContextTableModel(currentGroups, defaultContextTranslator);
		groupListCtr.setTableDataModel(groupTableDataModel);
		groupChoose.put("grouplisttable", groupListCtr.getInitialComponent());

		// render all-groups button only if goups are available
		if (this.coachedGroups.size() > 0) {
			groupChoose.contextPut("hasGroups", Boolean.TRUE);
		} else {
			groupChoose.contextPut("hasGroups", Boolean.FALSE);
		}

		if (mode == MODE_NODEFOCUS) {
			groupChoose.contextPut("showBack", Boolean.TRUE);
		} else {
			groupChoose.contextPut("showBack", Boolean.FALSE);
		}

		// set main content to groupchoose
		setContent(groupChoose);
	}

	private void doUserChooseWithData(final UserRequest ureq, final List<Identity> identities, final BusinessGroup group, AssessableCourseNode courseNode) {
		final ICourse course = CourseFactory.loadCourse(ores);

		userChoose.contextPut("iframeOK", Boolean.FALSE);

		if (mode == MODE_GROUPFOCUS) {
			this.nodeFilters = addAssessableNodesToList(course.getRunStructure().getRootNode(), group);
			if (courseNode == null && this.nodeFilters.size() > 0) {
				this.currentCourseNode = (AssessableCourseNode) this.nodeFilters.get(0);
				courseNode = this.currentCourseNode;
			}
		}
		// Init table headers
		removeAsListenerAndDispose(userListCtr);
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("userchoose.nousers"));

		if (mode == MODE_GROUPFOCUS) {
			userListCtr = new TableController(tableConfig, ureq, getWindowControl(), this.nodeFilters, courseNode, translate("nodesoverview.filter.title"), null,
					propertyHandlerTranslator);
		} else {
			userListCtr = new TableController(tableConfig, ureq, getWindowControl(), propertyHandlerTranslator);
		}
		listenTo(userListCtr);

		// Wrap identities with user course environment and user score view
		final List<AssessedIdentityWrapper> wrappedIdentities = new ArrayList<AssessedIdentityWrapper>();
		for (int i = 0; i < identities.size(); i++) {
			final Identity identity = (Identity) identities.get(i);
			// if course node is null the wrapper will only contain the identity and no score information
			final AssessedIdentityWrapper aiw = AssessmentHelper.wrapIdentity(identity, this.localUserCourseEnvironmentCache, course, courseNode);
			wrappedIdentities.add(aiw);
		}
		// Add the wrapped identities to the table data model
		final AssessedIdentitiesTableDataModel tdm = new AssessedIdentitiesTableDataModel(wrappedIdentities, courseNode, ureq.getLocale(), isAdministrativeUser);
		tdm.addColumnDescriptors(userListCtr, CMD_CHOOSE_USER, mode == MODE_NODEFOCUS || mode == MODE_GROUPFOCUS);
		userListCtr.setTableDataModel(tdm);

		if (mode == MODE_USERFOCUS) {
			userChoose.contextPut("showBack", Boolean.FALSE);
		} else {
			userChoose.contextPut("showBack", Boolean.TRUE);

			if (currentCourseNode != null && currentCourseNode.getReferencedRepositoryEntry() != null
					&& currentCourseNode.getReferencedRepositoryEntry().getOlatResource() != null
					&& OnyxModule.isOnyxTest(currentCourseNode.getReferencedRepositoryEntry().getOlatResource())) {
				userChoose.contextPut("showOnyxReporterButton", Boolean.TRUE);
			} else {
				userChoose.contextPut("showOnyxReporterButton", Boolean.FALSE);
			}

			if (mode == MODE_NODEFOCUS) {
				userChoose.contextPut("showFilterButton", Boolean.FALSE);
			} else {
				userChoose.contextPut("showFilterButton", Boolean.TRUE);
			}
		}

		if (group == null) {
			userChoose.contextPut("showGroup", Boolean.FALSE);
		} else {
			userChoose.contextPut("showGroup", Boolean.TRUE);
			userChoose.contextPut("groupName", StringEscapeUtils.escapeHtml(group.getName()));
		}

		userChoose.put("userlisttable", userListCtr.getInitialComponent());
		// set main vc to userchoose
		setContent(userChoose);
	}

	private void doSimpleUserChoose(final UserRequest ureq, final List<Identity> identities) {

		userChoose.contextPut("iframeOK", Boolean.FALSE);

		// Init table headers
		removeAsListenerAndDispose(userListCtr);
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setPreferencesOffered(true, "assessmentSimpleUserList");
		tableConfig.setTableEmptyMessage(translate("userchoose.nousers"));

		userListCtr = UserControllerFactory.createTableControllerFor(tableConfig, identities, ureq, getWindowControl(), CMD_CHOOSE_USER);
		listenTo(userListCtr);

		userChoose.contextPut("showBack", Boolean.FALSE);
		userChoose.contextPut("showGroup", Boolean.FALSE);

		userChoose.put("userlisttable", userListCtr.getInitialComponent());
		// set main vc to userchoose
		setContent(userChoose);
	}

	private void doNodeChoose(final UserRequest ureq) {
		final ICourse course = CourseFactory.loadCourse(ores);
		removeAsListenerAndDispose(nodeListCtr);
		// table configuraton
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setColumnMovingOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);

		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(nodeListCtr);
		// table columns
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
				new IndentedNodeRenderer()));
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1, CMD_SELECT_NODE, ureq.getLocale()));

		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.overallselect", 2, CMD_SHOW_ONYXREPORT, ureq.getLocale()));

		// get list of course node data and populate table data model
		final CourseNode rootNode = course.getRunStructure().getRootNode();
		final List<Map<String, Object>> nodesTableObjectArrayList = addAssessableNodesAndParentsToList(0, rootNode);

		// only populate data model if data available
		if (nodesTableObjectArrayList == null) {
			final String text = translate("nodesoverview.nonodes");
			final Controller messageCtr = MessageUIFactory.createSimpleMessage(ureq, getWindowControl(), text);
			listenTo(messageCtr);// dispose if this one gets disposed
			nodeChoose.put("nodeTable", messageCtr.getInitialComponent());
		} else {
			nodeTableModel = new NodeTableDataModelOnyx(nodesTableObjectArrayList, getTranslator());
			nodeListCtr.setTableDataModel(nodeTableModel);
			nodeChoose.put("nodeTable", nodeListCtr.getInitialComponent());
		}

		// set main content to nodechoose, do not use wrapper
		main.setContent(nodeChoose);
	}

	private void doBulkChoose(final UserRequest ureq) {
		final ICourse course = CourseFactory.loadCourse(ores);
		final List<Identity> allowedIdentities = getAllIdentitisFromGroupmanagement();
		removeAsListenerAndDispose(bamc);
		bamc = new BulkAssessmentMainController(ureq, getWindowControl(), course, allowedIdentities);
		listenTo(bamc);
		main.setContent(bamc.getInitialComponent());
	}

	/**
	 * Recursive method that adds assessable nodes and all its parents to a list
	 * 
	 * @param recursionLevel
	 * @param courseNode
	 * @return A list of maps containing the node data
	 */
	private List<Map<String, Object>> addAssessableNodesAndParentsToList(final int recursionLevel, final CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		final List<Map<String, Object>> childrenData = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			final CourseNode child = (CourseNode) courseNode.getChildAt(i);
			final List<Map<String, Object>> childData = addAssessableNodesAndParentsToList((recursionLevel + 1), child);
			if (childData != null) {
				childrenData.addAll(childData);
			}
		}

		boolean hasDisplayableValuesConfigured = false;
		if (childrenData.size() > 0 || courseNode instanceof AssessableCourseNode) {
			// Store node data in hash map. This hash map serves as data model for
			// the user assessment overview table. Leave user data empty since not used in
			// this table. (use only node data)
			final Map<String, Object> nodeData = new HashMap<String, Object>();
			// indent
			nodeData.put(AssessmentHelper.KEY_INDENT, new Integer(recursionLevel));
			// course node data
			nodeData.put(AssessmentHelper.KEY_TYPE, courseNode.getType());
			nodeData.put(AssessmentHelper.KEY_TITLE_SHORT, courseNode.getShortTitle());

			if (courseNode.getReferencedRepositoryEntry() != null) {
				if (OnyxModule.isOnyxTest(courseNode.getReferencedRepositoryEntry().getOlatResource())) {
					nodeData.put(KEY_IS_ONYX, Boolean.TRUE);
					if (getAllIdentitisFromGroupmanagement().size() <= 0) {
						nodeData.put(KEY_IS_ONYX, Boolean.FALSE);
					}
				} else {
					nodeData.put(KEY_IS_ONYX, Boolean.FALSE);
				}
			}

			nodeData.put(AssessmentHelper.KEY_TITLE_LONG, courseNode.getLongTitle());
			nodeData.put(AssessmentHelper.KEY_IDENTIFYER, courseNode.getIdent());

			if (courseNode instanceof AssessableCourseNode) {
				final AssessableCourseNode assessableCourseNode = (AssessableCourseNode) courseNode;
				if (assessableCourseNode.hasDetails() || assessableCourseNode.hasAttemptsConfigured() || assessableCourseNode.hasScoreConfigured()
						|| assessableCourseNode.hasPassedConfigured() || assessableCourseNode.hasCommentConfigured()) {
					hasDisplayableValuesConfigured = true;
				}
				if (assessableCourseNode.isEditableConfigured()) {
					// Assessable course nodes are selectable when they are aditable
					nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.TRUE);
				} else if (courseNode instanceof STCourseNode && (assessableCourseNode.hasScoreConfigured() || assessableCourseNode.hasPassedConfigured())) {
					// st node is special case: selectable on node choose list as soon as it
					// has score or passed
					nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.TRUE);
				} else {
					// assessable nodes that do not have score or passed are not selectable
					// (e.g. a st node with no defined rule
					nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.FALSE);
				}
			} else {
				// Not assessable nodes are not selectable. (e.g. a node that
				// has an assessable child node but is itself not assessable)
				nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.FALSE);
			}
			// 3) Add data of this node to mast list if node assessable or children list has any data.
			// Do only add nodes when they have any assessable element, otherwhise discard (e.g. empty course,
			// structure nodes without scoring rules)! When the discardEmptyNodes flag is set then only
			// add this node when there is user data found for this node.
			if (childrenData.size() > 0 || hasDisplayableValuesConfigured) {
				final List<Map<String, Object>> nodeAndChildren = new ArrayList<Map<String, Object>>();
				nodeAndChildren.add(nodeData);
				// 4) Add children data list to master list
				nodeAndChildren.addAll(childrenData);
				return nodeAndChildren;
			}
		}
		return null;
	}

	/**
	 * Recursive method to add all assessable course nodes to a list
	 * 
	 * @param courseNode
	 * @return List of course Nodes
	 */
	private List addAssessableNodesToList(final CourseNode courseNode, final BusinessGroup group) {
		final List result = new ArrayList();
		if (courseNode instanceof AssessableCourseNode) {
			final AssessableCourseNode assessableCourseNode = (AssessableCourseNode) courseNode;
			if (assessableCourseNode.hasDetails() || assessableCourseNode.hasAttemptsConfigured() || assessableCourseNode.hasScoreConfigured()
					|| assessableCourseNode.hasPassedConfigured() || assessableCourseNode.hasCommentConfigured()) {
				if (!isFiltering || isVisibleAndAccessable(assessableCourseNode, group)) {
					result.add(assessableCourseNode);
				}
			}
		}
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			final CourseNode child = (CourseNode) courseNode.getChildAt(i);
			result.addAll(addAssessableNodesToList(child, group));
		}
		return result;
	}

	/**
	 * Check if a course node is visiable and accessibale for certain group. Because the condition-interpreter works with identities, take the frist identity from list of
	 * participants.
	 * 
	 * @param courseNode
	 * @param group
	 * @return
	 */
	private boolean isVisibleAndAccessable(final CourseNode courseNode, final BusinessGroup group) {
		if ((courseNode == null) || (group == null)) { return true; }
		if (getGroupIdentitiesFromGroupmanagement(group).size() == 0) {
			// group has no participant, can not evalute
			return false;
		}
		final ICourse course = CourseFactory.loadCourse(ores);
		// check if course node is visible for group
		// get first identity to use this identity for condition interpreter
		final Identity identity = getGroupIdentitiesFromGroupmanagement(group).get(0);
		final IdentityEnvironment identityEnvironment = new IdentityEnvironment();
		identityEnvironment.setIdentity(identity);
		final UserCourseEnvironment uce = new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
		final OnlyGroupConditionInterpreter ci = new OnlyGroupConditionInterpreter(uce);
		final List listOfConditionExpressions = courseNode.getConditionExpressions();
		boolean allConditionAreValid = true;
		// loop over all conditions, all must be true
		for (final Iterator iter = listOfConditionExpressions.iterator(); iter.hasNext();) {
			final ConditionExpression conditionExpression = (ConditionExpression) iter.next();
			log.debug("conditionExpression=" + conditionExpression);
			log.debug("conditionExpression.getId()=" + conditionExpression.getId());
			final Condition condition = new Condition();
			condition.setConditionId(conditionExpression.getId());
			condition.setConditionExpression(conditionExpression.getExptressionString());
			if (!ci.evaluateCondition(condition)) {
				allConditionAreValid = false;
			}
		}
		return allConditionAreValid;
	}

	/**
	 * @param content Content to put in wrapper and set to main
	 */
	private void setContent(final Component content) {
		if (this.currentCourseNode == null) {
			wrapper.contextRemove("courseNode");
		} else {
			wrapper.contextPut("courseNode", this.currentCourseNode);
			// push node css class
			wrapper.contextPut("courseNodeCss", CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(currentCourseNode.getType()).getIconCSSClass());

		}
		wrapper.put("content", content);
		main.setContent(wrapper);
	}

	/**
	 * @param hasAssessableNodes true: show menu, false: hide menu
	 * @return The tree model
	 */
	private TreeModel buildTreeModel(final boolean hasAssessableNodes) {
		GenericTreeNode root, gtn;

		final GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(translate("menu.index"));
		root.setUserObject(CMD_INDEX);
		root.setAltText(translate("menu.index.alt"));
		gtm.setRootNode(root);

		// show real menu only when there are some assessable nodes
		if (hasAssessableNodes) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.groupfocus"));
			gtn.setUserObject(CMD_GROUPFOCUS);
			gtn.setAltText(translate("menu.groupfocus.alt"));
			root.addChild(gtn);

			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.nodefocus"));
			gtn.setUserObject(CMD_NODEFOCUS);
			gtn.setAltText(translate("menu.nodefocus.alt"));
			root.addChild(gtn);

			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.userfocus"));
			gtn.setUserObject(CMD_USERFOCUS);
			gtn.setAltText(translate("menu.userfocus.alt"));
			root.addChild(gtn);

			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.bulkfocus"));
			gtn.setUserObject(CMD_BULKFOCUS);
			gtn.setAltText(translate("menu.bulkfocus.alt"));
			root.addChild(gtn);
		}

		return gtm;
	}

	public ToolController getToolController() {
		return this.toolC;
	}

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controllers disposed by BasicController
		toolC = null;
		userListCtr = null;
		nodeListCtr = null;
		groupListCtr = null;
		csc = null;
		assessmentEditController = null;
		identityAssessmentController = null;

		// deregister from assessment changed events
		final ICourse course = CourseFactory.loadCourse(ores);
		course.getCourseEnvironment().getAssessmentManager().deregisterFromAssessmentChangeEvents(this);

		// stop assessment cache preloader thread if still running
		if (assessmentCachPreloaderThread != null && assessmentCachPreloaderThread.isAlive()) {
			assessmentCachPreloaderThread.interrupt();
			if (log.isDebugEnabled()) {
				log.debug("Interrupting assessment cache preload in course::" + course.getResourceableId() + " while in doDispose()");
			}
		}
	}

	/**
	 * Release resources used by child controllers. This must be called to release locks produced by certain child controllers and to help the garbage collector.
	 */
	private void disposeChildControllerAndReleaseLocks() {
		removeAsListenerAndDispose(assessmentEditController);
		assessmentEditController = null;
		removeAsListenerAndDispose(identityAssessmentController);
		identityAssessmentController = null;
	}

	/**
	 * Description:<BR>
	 * Thread that preloads the assessment cache and the user environment cache
	 * <P>
	 * Initial Date: Mar 2, 2005
	 * 
	 * @author gnaegi
	 */
	class AssessmentCachePreloadThread extends Thread {
		/**
		 * @param name Thread name
		 */
		AssessmentCachePreloadThread(final String name) {
			super(name);
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			boolean success = false;
			try {
				final ICourse course = CourseFactory.loadCourse(ores);
				// 1) preload assessment cache with database properties
				long start = 0;
				final boolean log.debug = log.isDebugEnabled();
				if (log.debug) {
					start = System.currentTimeMillis();
				}
				course.getCourseEnvironment().getAssessmentManager().preloadCache();
				// 2) preload controller local user environment cache
				start = System.currentTimeMillis();
				final List<Identity> identities = getAllIdentitisFromGroupmanagement();
				for (final Iterator<Identity> iter = identities.iterator(); iter.hasNext();) {
					final Identity identity = iter.next();
					AssessmentHelper.wrapIdentity(identity, localUserCourseEnvironmentCache, course, null);
					if (Thread.interrupted()) {
						break;
					}
				}
				if (log.debug) {
					log.debug("Preloading of user course environment cache for course::" + course.getResourceableId() + " for " + localUserCourseEnvironmentCache.size()
							+ " user course environments. Loading time::" + (System.currentTimeMillis() - start) + "ms");
				}
				// TODO: cg(04.09.2008): replace 'commit/closeSession' with doInManagedBlock
				// finished in this thread, close database session of this thread!
				DBFactory.getInstance(false).commitAndCloseSession();
				success = true;
			} finally {
				if (!success) {
					DBFactory.getInstance(false).rollbackAndCloseSession();
				}
			}
		}
	}

	/**
	 * @param ureq
	 * @param viewIdentifier if 'node-choose' does activate node-choose view
	 */
	public void activate(final UserRequest ureq, final String viewIdentifier) {
		if (viewIdentifier != null && viewIdentifier.equals("node-choose")) {
			// jump to state node-choose
			doNodeChoose(ureq);
		}
	}
}
