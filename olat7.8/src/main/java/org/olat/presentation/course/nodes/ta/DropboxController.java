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

package org.olat.presentation.course.nodes.ta;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Initial Date: 02.09.2004
 * 
 * @author Mike Stock
 */

public class DropboxController extends AbstractDropboxBaseController {

    public DropboxController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config, final CourseNode node,
            final UserCourseEnvironment userCourseEnv, final boolean previewMode) {
        super(ureq, wControl, config, node, userCourseEnv);
        init(ureq, wControl, previewMode, hasNotification(ureq.getIdentity(), userCourseEnv));
    }

    private boolean hasNotification(final Identity identity, final UserCourseEnvironment userCourseEnv) {
        OLATResourceable ores = userCourseEnv.getCourseEnvironment().getCourseOLATResourceable();
        final boolean isCourseAdmin = userCourseEnv.getCourseEnvironment().getCourseGroupManager().isIdentityCourseAdministrator(identity, ores);
        final boolean isCourseCoach = userCourseEnv.getCourseEnvironment().getCourseGroupManager().isIdentityCourseCoach(identity, ores);
        final boolean hasNotification = (isCourseAdmin || isCourseCoach);
        return hasNotification;
    }

    protected String getDropboxFolderForIdentity(final Identity identity) {
        return dropboxEbl.getDropboxFolderForIdentity(userCourseEnv.getCourseEnvironment(), node, identity);
    }

    protected boolean isEmailToTutorWanted() {
        Boolean sendEmailToOwnerAndTutor = (Boolean) config.get(TACourseNode.CONF_DROPBOX_CONFIRMATION_REQUESTED);
        if (sendEmailToOwnerAndTutor == null) {
            sendEmailToOwnerAndTutor = Boolean.FALSE;
        }
        return sendEmailToOwnerAndTutor;
    }

    @Override
    protected boolean sendStudentConfirmation(Identity identity, String fileName) {
        return sendStudentConfirmationAtDropTask(identity, fileName);
    }

    private boolean sendStudentConfirmationAtDropTask(Identity identity, String fileName) {
        return getConfirmationLearnService().sendTaskDropStudentConfirmation(identity, userCourseEnv.getCourseEnvironment().getCourseTitle(),
                userCourseEnv.getCourseEnvironment().getRepositoryEntryId(), new Long(node.getIdent()), fileName);
    }

    @Override
    protected boolean sendTutorConfirmation(List<Identity> tutors, Identity identity, String fileName) {
        return sendTutorConfirmationAtDropTask(tutors, identity, fileName);
    }

    private boolean sendTutorConfirmationAtDropTask(List<Identity> recipientIdentities, Identity originatorIdentity, String fileName) {
        return getConfirmationLearnService().sendTaskDropTutorConfirmation(recipientIdentities, originatorIdentity,
                userCourseEnv.getCourseEnvironment().getCourseTitle(), userCourseEnv.getCourseEnvironment().getRepositoryEntryId(), new Long(node.getIdent()), fileName);
    }

    @Override
    protected List<Identity> getTutors() {
        return userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseOwnersAndTutors(userCourseEnv.getCourseEnvironment().getCourseOLATResourceable());
    }

}
