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

package org.olat.lms.course.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.tree.GenericNode;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.interpreter.ConditionErrorMessage;
import org.olat.lms.course.condition.interpreter.ConditionExpression;
import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.TreeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.StringHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public abstract class GenericCourseNode extends GenericNode implements CourseNode {

    private static final Logger LOG = LoggerHelper.getLogger();

    private String type, shortTitle, longTitle, learningObjectives, displayOption;
    private ModuleConfiguration moduleConfiguration;
    private String noAccessExplanation;
    private Condition preConditionVisibility;
    private Condition preConditionAccess;
    protected transient StatusDescription[] oneClickStatusCache = null;

    /**
     * Generic course node constructor
     * 
     * @param type
     *            The course node type ATTENTION: all course nodes must call updateModuleConfigDefaults(true) here
     */
    public GenericCourseNode(final String type) {
        super();
        this.type = type;
        moduleConfiguration = new ModuleConfiguration();
    }

    /**
     * ATTENTION: all course nodes must call updateModuleConfigDefaults(false) here
     */
    @Override
    public abstract TabbableController createEditController(UserRequest ureq, WindowControl wControl, ICourse course, UserCourseEnvironment euce);

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation) ATTENTION: all course nodes must call
     * updateModuleConfigDefaults(false) here
     */
    @Override
    public abstract NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
            NodeEvaluation ne, String nodecmd);

    protected String getDefaultTitleOption() {
        return CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT;
    }

    /**
     * Default implementation of the peekview controller that returns NULL: no node specific peekview information should be shown<br>
     * Override this method with a specific implementation if you have something interesting to show in the peekview
     * 
     * org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public Controller createPeekViewRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        return null;
    }

    /**
     * default implementation of the previewController
     * 
     */
    @Override
    // no userCourseEnv or NodeEvaluation needed here
    public Controller createPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        final Translator translator = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_, ureq.getLocale());
        final String text = translator.translate("preview.notavailable");
        return MessageUIFactory.createInfoMessage(ureq, wControl, null, text);
    }

    /**
     * @return String
     */
    @Override
    public String getLearningObjectives() {
        return learningObjectives;
    }

    /**
     * @return String
     */
    @Override
    public String getLongTitle() {
        return longTitle;
    }

    /**
     * @return String
     */
    @Override
    public String getShortTitle() {
        return shortTitle;
    }

    /**
     * allows to specify if default value should be returned in case where there is no value.
     * 
     * @param returnDefault
     *            if false: null may be returned if no value found!
     * @return String
     */
    public String getDisplayOption(final boolean returnDefault) {
        if (!StringHelper.containsNonWhitespace(displayOption) && returnDefault) {
            return getDefaultTitleOption();
        }
        return displayOption;
    }

    /**
     * @return String with the old behavior (default value if none existing)
     */
    @Override
    public String getDisplayOption() {
        return getDisplayOption(true);
    }

    /**
     * @return String
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Sets the learningObjectives.
     * 
     * @param learningObjectives
     *            The learningObjectives to set
     */
    @Override
    public void setLearningObjectives(final String learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    /**
     * Sets the longTitle.
     * 
     * @param longTitle
     *            The longTitle to set
     */
    @Override
    public void setLongTitle(final String longTitle) {
        this.longTitle = longTitle;
    }

    /**
     * Sets the shortTitle.
     * 
     * @param shortTitle
     *            The shortTitle to set
     */
    @Override
    public void setShortTitle(final String shortTitle) {
        this.shortTitle = shortTitle;
    }

    /**
     * Sets the display option
     * 
     * @param displayOption
     */
    @Override
    public void setDisplayOption(final String displayOption) {
        this.displayOption = displayOption;
    }

    /**
     * Sets the type.
     * 
     * @param type
     *            The type to set
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return ModuleConfiguration
     */
    @Override
    public ModuleConfiguration getModuleConfiguration() {
        return moduleConfiguration;
    }

    /**
     * Sets the moduleConfiguration.
     * 
     * @param moduleConfiguration
     *            The moduleConfiguration to set
     */
    public void setModuleConfiguration(final ModuleConfiguration moduleConfiguration) {
        this.moduleConfiguration = moduleConfiguration;
    }

    @Override
    public void setRepositoryReference(RepositoryEntry repoEntry) {
        // sadly there where two different softkeys floating round when this method was moved to genericCourseNode
        // using them both makes it save also for exported course
        this.moduleConfiguration.set(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF, repoEntry.getSoftkey());
        this.moduleConfiguration.set(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY, repoEntry.getSoftkey());
    }

    /**
     * remove ref to wiki from the config
     * 
     * @param moduleConfig
     */
    @Override
    public void removeRepositoryReference() {
        this.moduleConfiguration.remove(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF);
        this.moduleConfiguration.remove(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
    }

    @Override
    public RepositoryEntry getReferencedRepositoryEntry() {
        return getReferencedRepositoryEntry(false);
    }

    /**
     * 
     * @param strict
     * @return
     */
    @Override
    public RepositoryEntry getReferencedRepositoryEntry(final boolean strict) {
        if (this.moduleConfiguration == null) {
            throw new AssertException("missing config in wiki course node");
        }
        final String repoSoftkey = (String) this.moduleConfiguration.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF);
        final String repoSoftkey2 = (String) this.moduleConfiguration.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        String repositorySoftkey = null;
        if (repoSoftkey == null && repoSoftkey2 == null) {
            if (strict)
                throw new AssertException("invalid config when being asked for references");
            else
                return null;
        } else {
            if (repoSoftkey != null)
                repositorySoftkey = repoSoftkey;
            if (repoSoftkey2 != null)
                repositorySoftkey = repoSoftkey2;
        }

        final RepositoryEntry entry = CoreSpringFactory.getBean(RepositoryService.class).lookupRepositoryEntryBySoftkey(repositorySoftkey, strict);
        return entry;
    }

    @Override
    public NodeEvaluation eval(final ConditionInterpreter ci, final TreeEvaluation treeEval, boolean evaluateExpertRules) {
        final NodeEvaluation nodeEval = new NodeEvaluation(this);
        calcAccessAndVisibility(ci, nodeEval);

        if (!evaluateExpertRules) {
            final Condition preConditionVisibility = getPreConditionVisibility();
            boolean visibilityExpertRule = preConditionVisibility.isExpertMode() && preConditionVisibility.getConditionExpression() != null
                    && !preConditionVisibility.getConditionExpression().trim().isEmpty();
            if (visibilityExpertRule) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("NodeEvaluation: visibility set to true ('ignoreExportRules' applied) for course node '" + getShortName() + "' (" + getIdent() + ")");
                }
                nodeEval.setVisible(true);
            }

            final Condition preConditionAccess = getPreConditionAccess();
            boolean accessExpertRule = preConditionAccess.isExpertMode() && preConditionAccess.getConditionExpression() != null
                    && !preConditionAccess.getConditionExpression().trim().isEmpty();
            if (accessExpertRule) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("NodeEvaluation: atLeastOneAccessible set to true ('ignoreExportRules' applied) for course node '" + getShortName() + "' (" + getIdent()
                            + ")");
                }
                nodeEval.setAtLeastOneAccessible(true);
            }
        }

        nodeEval.build();
        treeEval.cacheCourseToTreeNode(this, nodeEval.getTreeNode());
        // only add children (coursenodes/nodeeval) when I am visible and
        // atleastOneAccessible myself
        if (nodeEval.isVisible() && nodeEval.isAtLeastOneAccessible()) {
            final int childcnt = getChildCount();
            for (int i = 0; i < childcnt; i++) {
                final CourseNode cn = (CourseNode) this.getChildAt(i);
                final NodeEvaluation chdEval = cn.eval(ci, treeEval, evaluateExpertRules);
                if (chdEval.isVisible()) { // child is visible
                    nodeEval.addNodeEvaluationChild(chdEval);
                }
            }
        }
        return nodeEval;
    }

    /**
     * @param ci
     *            the ConditionInterpreter as the calculating machine
     * @param nodeEval
     *            the object to write the results into
     */
    protected abstract void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval);

    /**
     * @return String
     */
    @Override
    public String getNoAccessExplanation() {
        return noAccessExplanation;
    }

    /**
     * Sets the noAccessExplanation.
     * 
     * @param noAccessExplanation
     *            The noAccessExplanation to set
     */
    @Override
    public void setNoAccessExplanation(final String noAccessExplanation) {
        this.noAccessExplanation = noAccessExplanation;
    }

    /**
     * @return Condition
     */
    @Override
    public Condition getPreConditionVisibility() {
        if (preConditionVisibility == null) {
            preConditionVisibility = new Condition();
        }
        preConditionVisibility.setConditionId("visibility");
        return preConditionVisibility;
    }

    /**
     * Sets the preConditionVisibility.
     * 
     * @param preConditionVisibility
     *            The preConditionVisibility to set
     */
    @Override
    public void setPreConditionVisibility(Condition preConditionVisibility) {
        if (preConditionVisibility == null) {
            preConditionVisibility = getPreConditionVisibility();
        }
        this.preConditionVisibility = preConditionVisibility;
        this.preConditionVisibility.setConditionId("visibility");
    }

    /**
     * @return Condition
     */
    @Override
    public Condition getPreConditionAccess() {
        if (preConditionAccess == null) {
            preConditionAccess = new Condition();
        }
        preConditionAccess.setConditionId("accessability");
        return preConditionAccess;
    }

    /**
     * Generic interface implementation. May be overriden by specific node's implementation.
     * 
     */
    @Override
    public String informOnDelete(final Locale locale, final ICourse course) {
        return null;
    }

    /**
     * Generic interface implementation. May be overriden by specific node's implementation.
     * 
     */
    @Override
    public void cleanupOnDelete(final ICourse course) {
        /**
         * do nothing in default implementation
         */
    }

    /**
     * Generic interface implementation. May be overriden by specific node's implementation.
     * 
     */
    @Override
    // implemented by specialized node
    public boolean archiveNodeData(final Locale locale, final ICourse course, final File exportDirectory, final String charset) {
        // nothing to do in default implementation
        return true;
    }

    /**
	 */
    @Override
    // implemented by specialized node
    public void exportNode(final File exportDirectory, final ICourse course) {
        // nothing to do in default implementation
    }

    /**
	 */
    @Override
    // implemented by specialized node
    public Controller importNode(final File importDirectory, final ICourse course, final boolean unattendedImport, final UserRequest ureq, final WindowControl wControl) {
        // nothing to do in default implementation
        return null;
    }

    /**
	 */
    @Override
    public String getShortName() {
        return getShortTitle();
    }

    /**
	 */
    @Override
    public CourseNode createInstanceForCopy() {
        return createInstanceForCopy(true);
    }

    @Override
    public CourseNode createInstanceForCopy(final boolean isNewTitle) {
        final CourseNode copyInstance = (CourseNode) XStreamHelper.xstreamClone(this);
        copyInstance.setIdent(String.valueOf(CodeHelper.getForeverUniqueID()));
        copyInstance.setPreConditionVisibility(null);
        if (isNewTitle) {
            // FIXME:pb:ms translation for COPY OF
            String newTitle = "Copy of " + getShortTitle();
            if (newTitle.length() > CourseNode.SHORT_TITLE_MAX_LENGTH) {
                newTitle = newTitle.substring(0, CourseNode.SHORT_TITLE_MAX_LENGTH - 1);
            }
            copyInstance.setShortTitle(newTitle);
        }
        return copyInstance;
    }

    /**
	 */
    @Override
    public String toString() {
        return "Id: " + getIdent() + ", '" + getShortTitle() + "' " + super.toString();
    }

    /**
	 */
    @Override
    public List<ConditionExpression> getConditionExpressions() {
        final ArrayList<ConditionExpression> retVal = new ArrayList<ConditionExpression>();
        final String coS = getPreConditionVisibility().getConditionExpression();
        if (coS != null && !coS.equals("")) {
            // an active condition is defined
            final ConditionExpression ce = new ConditionExpression(getPreConditionVisibility().getConditionId());
            ce.setExpressionString(getPreConditionVisibility().getConditionExpression());
            retVal.add(ce);
        }
        //
        return retVal;
    }

    @Override
    public boolean isModuleConfigValid() {
        return (this.moduleConfiguration.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF) != null || this.moduleConfiguration
                .get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY) != null);
    }

    /**
     * must be implemented in the concrete subclasses as a translator is needed for the errormessages which comes with evaluating condition expressions
     * 
     */
    @Override
    public abstract StatusDescription[] isConfigValid(CourseEditorEnv cev);

    /**
     * @param userCourseEnv
     * @param translatorStr
     * @return
     */
    protected List<StatusDescription> isConfigValidWithTranslator(final CourseEditorEnv cev, final String translatorStr, final List<ConditionExpression> condExprs) {
        final List<StatusDescription> condExprsStatusDescs = new ArrayList<StatusDescription>();
        // check valid configuration without course environment
        final StatusDescription first = isConfigValid();
        // check valid configuration within the course environment
        if (cev == null) {
            // course environment not configured!??
            condExprsStatusDescs.add(first);
            return condExprsStatusDescs;
        }
        /*
         * there is course editor environment, we can check further. Iterate over all conditions of this course node, validate the condition expression and transform the
         * condition error message into a status description
         */
        for (int i = 0; i < condExprs.size(); i++) {
            final ConditionExpression ce = condExprs.get(i);
            final ConditionErrorMessage[] cems = cev.validateConditionExpression(ce);
            if (cems != null && cems.length > 0) {
                for (int j = 0; j < cems.length; j++) {
                    final StatusDescription sd = new StatusDescription(StatusDescription.WARNING, cems[j].errorKey, cems[j].solutionMsgKey, cems[j].errorKeyParams,
                            translatorStr);
                    sd.setDescriptionForUnit(getIdent());
                    condExprsStatusDescs.add(sd);
                }
            }
        }
        condExprsStatusDescs.add(first);
        return condExprsStatusDescs;
    }

    /**
	 */
    @Override
    public StatusDescription explainThisDuringPublish(final StatusDescription description) {
        if (description == null) {
            return null;
        }
        StatusDescription retVal = null;
        if (description.getShortDescriptionKey().equals("error.notfound.coursenodeid")) {
            retVal = description.transformTo("error.notfound.coursenodeid.publish", "error.notfound.coursenodeid.publish", null);
        } else if (description.getShortDescriptionKey().equals("error.notfound.name")) {
            retVal = description.transformTo("error.notfound.name.publish", "error.notfound.name.publish", null);
        } else if (description.getShortDescriptionKey().equals("error.notassessable.coursenodid")) {
            retVal = description.transformTo("error.notassessable.coursenodid.publish", "error.notassessable.coursenodid.publish", null);
        } else {
            // throw new OLATRuntimeException("node does not know how to translate <b
            // style='color:red'>" + description.getShortDescriptionKey()
            // + "</b> in publish env", new IllegalArgumentException());
            return description;
        }
        return retVal;
    }

    /**
     * Update the module configuration to have all mandatory configuration flags set to usefull default values
     * 
     * @param isNewNode
     *            true: an initial configuration is set; false: upgrading from previous node configuration version, set default to maintain previous behaviour This is the
     *            workflow: On every click on a entry of the navigation tree, this method will be called to ensure a valid configration of the depending module. This is
     *            only done in RAM. If the user clicks on that node in course editor and publishes the course after that, then the updated config will be persisted to
     *            disk. Otherwise everything what is done here has to be done once at every course start.
     */
    @Override
    // implemented by specialized node
    public void updateModuleConfigDefaults(final boolean isNewNode) {
        /**
         * Do NO updating here, since this method can be overwritten by all classes implementing this. This is only implemented here to avoid changing all couseNode
         * classes which do not implement this method.
         */
    }

}
