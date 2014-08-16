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

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.WebappHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;

import com.thoughtworks.xstream.XStream;

import de.bps.course.nodes.cl.ChecklistEditController;
import de.bps.olat.modules.cl.Checklist;
import de.bps.olat.modules.cl.ChecklistManager;
import de.bps.olat.modules.cl.ChecklistUIFactory;
import de.bps.olat.modules.cl.Checkpoint;
import de.bps.olat.modules.cl.CheckpointMode;

/**
 * Description:<br>
 * Checklist Course Node
 * <P>
 * Initial Date: 23.07.2009 <br>
 * 
 * @author bja <bja@bps-system.de>
 * @author skoeber <skoeber@bps-system.de>
 */
public class ChecklistCourseNode extends AbstractAccessableCourseNode {

	private static final String TYPE = "cl";
	public static final String CONF_COURSE_ID = "cl_course_id";
	public static final String CONF_COURSE_NODE_ID = "cl_course_node_id";
	public static final String CONF_CHECKLIST = "cl_checklist";
	public static final String CONF_CHECKLIST_COPY = "cl_checklist_copy";
	public static final String PROPERTY_CHECKLIST_KEY = CONF_CHECKLIST;

	public ChecklistCourseNode() {
		super(TYPE);
		initDefaultConfig();
	}

	private void initDefaultConfig() {
		final ModuleConfiguration config = getModuleConfiguration();
		// add an empty checkpoint entry as default if none existent
		if (config.get(CONF_CHECKLIST) == null) {
			final Checklist initialChecklist = new Checklist();
			// set to config
			config.set(CONF_CHECKLIST, initialChecklist);
			// save to db
			ChecklistManager.getInstance().saveChecklist(initialChecklist);
		}
	}

	/**
	 * Internal helper: save node configuration
	 * 
	 * @param cpm
	 * @param checklistKey
	 */
	private void setChecklistKey(final CoursePropertyManager cpm, final Long checklistKey) {
		final Property checklistKeyProperty = cpm.createCourseNodePropertyInstance(this, null, null, PROPERTY_CHECKLIST_KEY, null, checklistKey, null, null);
		cpm.saveProperty(checklistKeyProperty);
		/*
		 * Save reference to checklist additionally in module configuration since the CoursePropertyManager is not always available.
		 */
		getModuleConfiguration().set(CONF_CHECKLIST, ChecklistManager.getInstance().loadChecklist(checklistKey));
	}

	/**
	 * Internal helper: load node configuration
	 * 
	 * @param cpm
	 * @return checklistKey
	 */
	private Long getChecklistKey(final CoursePropertyManager cpm) {
		final Property checklistKeyProperty = cpm.findCourseNodeProperty(this, null, null, PROPERTY_CHECKLIST_KEY);

		return checklistKeyProperty != null ? checklistKeyProperty.getLongValue() : null;
	}

	/**
	 * Internal helper: delete node configuration
	 * 
	 * @param cpm
	 */
	private void deleteChecklistKeyConf(final CoursePropertyManager cpm) {
		cpm.deleteNodeProperties(this, PROPERTY_CHECKLIST_KEY);
		getModuleConfiguration().remove(CONF_CHECKLIST);
	}

	/**
	 * Load referenced checklist or create a new one
	 * 
	 * @param cpm
	 * @return Checklist
	 */
	public Checklist loadOrCreateChecklist(final CoursePropertyManager cpm) {
		Checklist checklist;

		if (getChecklistKey(cpm) == null) {
			// this is a copied node, the checklist is referenced by createInstanceForCopy()
			if (getModuleConfiguration().get(CONF_CHECKLIST_COPY) != null) {
				checklist = ChecklistManager.getInstance().loadChecklist((Checklist) getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST_COPY));
				getModuleConfiguration().remove(CONF_CHECKLIST_COPY);
			} else
			// this is part of a copied course, the original checklist will be copied
			if (getModuleConfiguration().get(CONF_CHECKLIST) != null) {
				final Checklist orgChecklist = ChecklistManager.getInstance().loadChecklist((Checklist) getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST));
				checklist = ChecklistManager.getInstance().copyChecklist(orgChecklist);
			} else {
				// no checklist available, create new one
				checklist = new Checklist();
				ChecklistManager.getInstance().saveChecklist(checklist);
			}
			// set referenced checklist in configuration
			setChecklistKey(cpm, checklist.getKey());
		} else {
			checklist = ChecklistManager.getInstance().loadChecklist(getChecklistKey(cpm));
		}

		return checklist;
	}

	@Override
	public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
		final CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		final ChecklistEditController editController = new ChecklistEditController(ureq, wControl, this, course, euce);
		getModuleConfiguration().set(CONF_COURSE_ID, course.getResourceableId());
		getModuleConfiguration().set(CONF_COURSE_NODE_ID, chosenNode.getIdent());
		final NodeEditController nodeEditController = new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course
				.getCourseEnvironment().getCourseGroupManager(), euce, editController);
		nodeEditController.addControllerListener(editController);
		return nodeEditController;
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
			final NodeEvaluation ne, final String nodecmd) {
		final ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		final boolean canEdit = ureq.getUserSession().getRoles().isOLATAdmin() || cgm.isIdentityCourseAdministrator(ureq.getIdentity());
		boolean canManage;
		if (canEdit) {
			canManage = true;
		} else {
			canManage = cgm.isIdentityCourseCoach(ureq.getIdentity()) | cgm.hasRight(ureq.getIdentity(), CourseRights.RIGHT_GROUPMANAGEMENT);
		}
		Controller controller = ChecklistUIFactory.getInstance().createDisplayController(ureq, wControl,
				loadOrCreateChecklist(userCourseEnv.getCourseEnvironment().getCoursePropertyManager()), null, canEdit, canManage, course, this);
		// Add title and descrition
		controller = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_cl_icon");
		return new NodeRunConstructionResult(controller);
	}

	@Override
	public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
		oneClickStatusCache = null;
		final String translatorStr = Util.getPackageName(ConditionEditController.class);
		final List statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription sd = StatusDescription.NOERROR;
		final String transPackage = ChecklistEditController.class.getPackage().getName();

		// no configuration available hence there is no checklist with checkpoints
		if (getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST) == null) {
			sd = new StatusDescription(ValidationStatus.ERROR, "config.nocheckpoints.short", "config.nocheckpoints.long", null, transPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(ChecklistEditController.PANE_TAB_CLCONFIG);
			return sd;
		}

		final Checklist checklist = (Checklist) getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST);
		// checklist without any checkpoints makes no sense
		if (!checklist.hasCheckpoints()) {
			sd = new StatusDescription(ValidationStatus.ERROR, "config.nocheckpoints.short", "config.nocheckpoints.long", null, transPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(ChecklistEditController.PANE_TAB_CLCONFIG);
			return sd;
		}

		// information, if all checkpoints are invisible
		boolean allUnvisible = true;
		boolean noLearners = false;
		if (checklist.hasCheckpoints()) {
			final List<Checkpoint> checkpoints = ((Checklist) getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST)).getCheckpoints();
			for (final Checkpoint checkpoint : checkpoints) {
				if (!checkpoint.getMode().equals(CheckpointMode.MODE_HIDDEN)) {
					allUnvisible = false;
				}
			}
			if (allUnvisible) {
				final Condition cond = getPreConditionVisibility();
				if (cond.isEasyModeCoachesAndAdmins()) {
					noLearners = true;
				}
				if (!noLearners) {
					sd = new StatusDescription(ValidationStatus.WARNING, "config.allhidden.short", "config.allhidden.long", null, transPackage);
					sd.setDescriptionForUnit(getIdent());
					sd.setActivateableViewIdentifier(ChecklistEditController.PANE_TAB_CLCONFIG);
				}
			}
		}
		return sd;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	@Override
	public void cleanupOnDelete(final ICourse course) {
		super.cleanupOnDelete(course);
		// delete checklist in db
		Checklist checklist = loadOrCreateChecklist(course.getCourseEnvironment().getCoursePropertyManager());
		ChecklistManager.getInstance().deleteChecklist(checklist);
		checklist = null;
		// delete node configuration
		deleteChecklistKeyConf(course.getCourseEnvironment().getCoursePropertyManager());
	}

	@Override
	public void exportNode(final File exportDirectory, final ICourse course) {
		final XStream xstream = new XStream();
		final ChecklistManager cm = ChecklistManager.getInstance();
		final Checklist checklist = loadOrCreateChecklist(course.getCourseEnvironment().getCoursePropertyManager());
		final Checklist copy = cm.copyChecklistInRAM(checklist);
		final String exportContent = xstream.toXML(copy);
		ExportUtil.writeContentToFile(getExportFilename(), exportContent, exportDirectory, WebappHelper.getDefaultCharset());
	}

	@Override
	public Controller importNode(final File importDirectory, final ICourse course, final boolean unattendedImport, final UserRequest ureq, final WindowControl wControl) {
		final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		if (getChecklistKey(cpm) != null) {
			deleteChecklistKeyConf(cpm);
		}

		final File importFile = new File(importDirectory, getExportFilename());
		final String importContent = FileUtils.load(importFile, WebappHelper.getDefaultCharset());
		if (importContent == null || importContent.isEmpty()) { return null; }

		final XStream xstream = new XStream();
		Checklist checklist = (Checklist) xstream.fromXML(importContent);
		if (checklist != null) {
			checklist = ChecklistManager.getInstance().copyChecklist(checklist);
			setChecklistKey(cpm, checklist.getKey());
		}

		return null;
	}

	@Override
	public boolean archiveNodeData(final Locale locale, final ICourse course, final File exportDirectory, final String charset) {
		final XStream xstream = new XStream();
		final Checklist checklist = loadOrCreateChecklist(course.getCourseEnvironment().getCoursePropertyManager());
		final String exportContent = xstream.toXML(checklist);
		final String exportFilename = ExportUtil.createFileNameWithTimeStamp("checklist_" + this.getIdent(), "xml");
		ExportUtil.writeContentToFile(exportFilename, exportContent, exportDirectory, WebappHelper.getDefaultCharset());

		return true;
	}

	private String getExportFilename() {
		return "checklist_" + this.getIdent() + ".xml";
	}

	@Override
	public CourseNode createInstanceForCopy(final boolean isNewTitle) {
		final CourseNode copyInstance = super.createInstanceForCopy(isNewTitle);
		final ChecklistManager cm = ChecklistManager.getInstance();
		// load checklist
		final Checklist checklist = cm.loadChecklist((Checklist) getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST));
		// remove old config
		copyInstance.getModuleConfiguration().remove(ChecklistCourseNode.CONF_CHECKLIST);
		// create new checklist with same settings and save to db
		final Checklist initialChecklist = cm.copyChecklist(checklist);
		// set to config
		copyInstance.getModuleConfiguration().set(CONF_CHECKLIST_COPY, initialChecklist);

		return copyInstance;
	}

}
