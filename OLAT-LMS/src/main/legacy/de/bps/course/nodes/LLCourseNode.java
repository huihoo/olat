/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.course.nodes;

import java.util.ArrayList;
import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.core.helpers.Settings;
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
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

import de.bps.course.nodes.ll.LLEditController;
import de.bps.course.nodes.ll.LLModel;
import de.bps.course.nodes.ll.LLRunController;

/**
 * Description:<br>
 * Link list course node.
 * <P>
 * Initial Date: 05.11.2008 <br>
 * 
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLCourseNode extends AbstractAccessableCourseNode {

	private static final String TYPE = "ll";
	public static final String CONF_COURSE_ID = "ll_course_id";
	public static final String CONF_COURSE_NODE_ID = "ll_course_node_id";
	public static final String CONF_LINKLIST = "ll_link_list";

	/**
	 * Create default link list course node.
	 */
	public LLCourseNode() {
		super(TYPE);
		initDefaultConfig();
	}

	private void initDefaultConfig() {
		final ModuleConfiguration config = getModuleConfiguration();
		// add an empty link entry as default if none existent
		if (config.get(CONF_LINKLIST) == null) {
			final List<LLModel> initialList = new ArrayList<LLModel>(1);
			initialList.add(new LLModel());
			config.set(CONF_LINKLIST, initialList);
		}
	}

	@Override
	public void updateModuleConfigDefaults(final boolean isNewNode) {
		final ModuleConfiguration config = getModuleConfiguration();
		if (config.getConfigurationVersion() < 2) {
			final List<LLModel> links = (List<LLModel>) config.get(CONF_LINKLIST);
			for (final LLModel link : links) {
				String linkValue = link.getTarget();
				if (!linkValue.contains("://")) {
					linkValue = "http://".concat(linkValue.trim());
				}
				if (linkValue.startsWith(Settings.getServerContextPathURI())) {
					link.setHtmlTarget("_self");
				} else {
					link.setHtmlTarget("_blank");
				}
			}
			config.setConfigurationVersion(2);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment userCourseEnv) {
		updateModuleConfigDefaults(false);
		final LLEditController childTabCntrllr = new LLEditController(getModuleConfiguration(), ureq, wControl, this, course, userCourseEnv);
		final CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// needed for DENEditController.isConfigValid()
		getModuleConfiguration().set(CONF_COURSE_ID, course.getResourceableId());
		getModuleConfiguration().set(CONF_COURSE_NODE_ID, chosenNode.getIdent());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment().getCourseGroupManager(),
				userCourseEnv, childTabCntrllr);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
			final NodeEvaluation ne, final String nodecmd) {
		updateModuleConfigDefaults(false);
		Controller controller = new LLRunController(ureq, wControl, getModuleConfiguration(), this, userCourseEnv, true);
		controller = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_ll_icon");
		return new NodeRunConstructionResult(controller);
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createPeekViewRunController(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment, org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPeekViewRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
		updateModuleConfigDefaults(false);
		// Use normal view as peekview
		final Controller controller = new LLRunController(ureq, wControl, getModuleConfiguration(), this, userCourseEnv, false);
		return controller;
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createPreviewController(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment, org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
		Controller controller = new LLRunController(ureq, wControl, getModuleConfiguration(), this, userCourseEnv, true);
		controller = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_ll_icon");
		return controller;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
		final String translatorStr = Util.getPackageName(ConditionEditController.class);
		final List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription sd = StatusDescription.NOERROR;

		if (!LLEditController.isConfigValid(getModuleConfiguration())) {
			final String transPackage = Util.getPackageName(LLEditController.class);
			sd = new StatusDescription(ValidationStatus.WARNING, "config.nolinks.short", "config.nolinks.long", null, transPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(LLEditController.PANE_TAB_LLCONFIG);
		}

		return sd;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

}
