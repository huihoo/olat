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

package org.olat.presentation.group.wizard;

import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.GroupLoggingAction;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.WizardController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.BGTranslatorFactory;
import org.olat.presentation.group.BusinessGroupFormController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <BR>
 * Two step wizard to copy a given business group. In the first step the user is asked about what should be copied, in the second step the usr is asked for a groupname
 * and the groups details.
 * <P>
 * Initial Date: Sep 11, 2004
 * 
 * @author Florian Gnägi
 */
public class BGCopyWizardController extends WizardController {

    private static final String PACKAGE = PackageUtil.getPackageName(BGCopyWizardController.class);

    private final BGCopyWizardCopyForm copyForm;
    private BusinessGroupFormController groupController;
    private final Translator trans;
    private final BGConfigFlags flags;
    private BusinessGroup originalGroup, copiedGroup;

    /**
     * Constructor fot the business group copy wizard
     * 
     * @param ureq
     * @param wControl
     * @param originalGroup
     *            original business group: master that should be copied
     * @param flags
     */
    public BGCopyWizardController(final UserRequest ureq, final WindowControl wControl, final BusinessGroup originalGroup, final BGConfigFlags flags) {
        super(ureq, wControl, 2);
        setBasePackage(BGCopyWizardController.class);
        this.trans = BGTranslatorFactory.createBGPackageTranslator(PACKAGE, originalGroup.getType(), ureq.getLocale());
        this.flags = flags;
        this.originalGroup = originalGroup;
        // init wizard step 1
        copyForm = new BGCopyWizardCopyForm(ureq, wControl);
        listenTo(copyForm);
        // init wizard title and set step 1
        setWizardTitle(trans.translate("bgcopywizard.title"));
        setNextWizardStep(trans.translate("bgcopywizard.copyform.title"), copyForm.getInitialComponent());
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == this.groupController) {
            if (event == Event.DONE_EVENT) {
                final BusinessGroup newGroup = doCopyGroup();
                if (newGroup == null) {
                    this.groupController.setGroupNameExistsError(null);
                } else {
                    this.copiedGroup = newGroup;
                    // finished event
                    fireEvent(ureq, Event.DONE_EVENT);
                    // do logging
                    ThreadLocalUserActivityLogger.log(GroupLoggingAction.BG_GROUP_COPIED, getClass(), LoggingResourceable.wrap(originalGroup),
                            LoggingResourceable.wrap(copiedGroup));
                }
            }
        } else if (source == copyForm) {
            if (event == Event.DONE_EVENT) {
                removeAsListenerAndDispose(groupController);
                groupController = new BusinessGroupFormController(ureq, getWindowControl(), this.originalGroup, this.flags.isEnabled(BGConfigFlags.GROUP_MINMAX_SIZE));
                listenTo(groupController);

                groupController.setGroupName(this.originalGroup.getName() + " " + this.trans.translate("bgcopywizard.copyform.name.copy"));

                setNextWizardStep(this.trans.translate("bgcopywizard.detailsform.title"), this.groupController.getInitialComponent());
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // default wizard will lissen to cancel wizard event
        super.event(ureq, source, event);
        // now wizard steps events

    }

    private BusinessGroup doCopyGroup() {
        BusinessGroupService businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
        // reload original group to prevent context proxy problems
        this.originalGroup = businessGroupService.loadBusinessGroup(this.originalGroup);
        final BGContext bgContext = this.originalGroup.getGroupContext();
        final String bgName = this.groupController.getGroupName();
        final String bgDesc = this.groupController.getGroupDescription();
        final Integer bgMax = this.groupController.getGroupMax();
        final Integer bgMin = this.groupController.getGroupMin();
        final boolean copyAreas = (this.flags.isEnabled(BGConfigFlags.AREAS) && this.copyForm.isCopyAreas());

        final BusinessGroup newGroup = businessGroupService.copyBusinessGroup(this.originalGroup, bgName, bgDesc, bgMin, bgMax, bgContext, null, copyAreas,
                copyForm.isCopyTools(), copyForm.isCopyRights(), copyForm.isCopyOwners(), copyForm.isCopyParticipants(), copyForm.isCopyMembersVisibility(),
                copyForm.isCopyWaitingList());
        return newGroup;
    }

    /**
     * @return The new business group created as a copy of the original business group
     */
    public BusinessGroup getNewGroup() {
        return this.copiedGroup;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        super.doDispose();
    }
}
