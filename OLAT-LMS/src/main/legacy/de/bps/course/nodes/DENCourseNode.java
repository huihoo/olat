package de.bps.course.nodes;

import java.util.ArrayList;
import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

import de.bps.course.nodes.den.DENEditController;
import de.bps.course.nodes.den.DENManager;
import de.bps.course.nodes.den.DENRunController;

/**
 * Date enrollment course node
 * 
 * @author skoeber
 */
public class DENCourseNode extends AbstractAccessableCourseNode {

	private static final String PACKAGE = Util.getPackageName(DENCourseNode.class);
	private static final String TYPE = "den";
	/** is cancel of the enrollment allowed */
	public static final String CONF_CANCEL_ENROLL_ENABLED = "cancel_enroll_enabled";
	public static final String CONF_COURSE_ID = "den_course_id";
	public static final String CONF_COURSE_NODE_ID = "den_course_node_id";

	/**
	 * Standard constructor
	 */
	public DENCourseNode() {
		super(TYPE);
		initDefaultConfig();
	}

	private void initDefaultConfig() {
		final ModuleConfiguration config = getModuleConfiguration();
		config.set(CONF_CANCEL_ENROLL_ENABLED, Boolean.TRUE);
	}

	@Override
	public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment userCourseEnv) {
		final DENEditController childTabCntrllr = new DENEditController(getModuleConfiguration(), ureq, wControl, this, course, userCourseEnv);
		final CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// needed for DENEditController.isConfigValid()
		getModuleConfiguration().set(CONF_COURSE_ID, course.getResourceableId());
		getModuleConfiguration().set(CONF_COURSE_NODE_ID, chosenNode.getIdent());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment().getCourseGroupManager(),
				userCourseEnv, childTabCntrllr);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
			final NodeEvaluation ne, final String nodecmd) {
		Controller controller;
		// Do not allow guests to enroll to dates
		final Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			final Translator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
			final String title = trans.translate("guestnoaccess.title");
			final String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			controller = new DENRunController(ureq, wControl, getModuleConfiguration(), this);
		}

		final Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_en_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
		final String translatorStr = Util.getPackageName(ConditionEditController.class);
		final List statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription sd = StatusDescription.NOERROR;

		if (!DENEditController.isConfigValid(getModuleConfiguration())) {
			final String transPackage = Util.getPackageName(DENEditController.class);
			sd = new StatusDescription(ValidationStatus.WARNING, "config.nodates.short", "config.nodates.long", null, transPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(DENEditController.PANE_TAB_DENCONFIG);
		}

		return sd;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	@Override
	public void cleanupOnDelete(final ICourse course) {
		super.cleanupOnDelete(course);
		final CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
		cpm.deleteNodeProperties(this, CONF_CANCEL_ENROLL_ENABLED);
		final DENManager denManager = DENManager.getInstance();
		// empty List as first argument, so all dates for this course node are going to delete
		denManager.persistDENSettings(new ArrayList(), course, this);
	}

}
