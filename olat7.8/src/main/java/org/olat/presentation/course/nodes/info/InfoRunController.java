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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */

package org.olat.presentation.course.nodes.info;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.InfoCourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.infomessage.InfoSecurityCallback;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.infomessage.InfoDisplayController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Container for a InfodisplayController and the SubscriptionController
 * <P>
 * Initial Date: 27 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoRunController extends BasicController {

    private final VelocityContainer runVc;
    private final InfoDisplayController infoDisplayController;

    private final String businessPath;
    private final InfoCourseNode courseNode;
    private final ModuleConfiguration config;

    public InfoRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne,
            final InfoCourseNode courseNode) {
        super(ureq, wControl);

        this.courseNode = courseNode;
        this.config = courseNode.getModuleConfiguration();

        final Long resId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
        final ICourse course = CourseFactory.loadCourse(resId);
        final String resSubPath = this.courseNode.getIdent();
        final OLATResourceable infoResourceable = new InfoOLATResourceable(resId);
        businessPath = normalizeBusinessPath(wControl.getBusinessControl().getAsString());

        final Identity identity = ureq.getIdentity();
        final CourseGroupManager cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
        final boolean institutionalManager = RepositoryServiceImpl.getInstance().isInstitutionalRessourceManagerFor(
                RepositoryServiceImpl.getInstance().lookupRepositoryEntry(course, false), identity);
        OLATResourceable ores = userCourseEnv.getCourseEnvironment().getCourseOLATResourceable();
        final boolean courseAdmin = cgm.isIdentityCourseAdministrator(identity, ores);

        final boolean canAdd = courseAdmin || ne.isCapabilityAccessible(InfoCourseNode.EDIT_CONDITION_ID) || institutionalManager
                || ureq.getUserSession().getRoles().isOLATAdmin();

        final boolean canAdmin = courseAdmin || ne.isCapabilityAccessible(InfoCourseNode.ADMIN_CONDITION_ID) || institutionalManager
                || ureq.getUserSession().getRoles().isOLATAdmin();

        final InfoSecurityCallback secCallback = new InfoCourseSecurityCallback(canAdd, canAdmin);

        infoDisplayController = new InfoDisplayController(ureq, wControl, config, secCallback, infoResourceable, resSubPath, businessPath);
        listenTo(infoDisplayController);

        runVc = createVelocityContainer("run");
        runVc.put("displayInfos", infoDisplayController.getInitialComponent());
        putInitialPanel(runVc);

    }

    /**
     * Remove ROOT, remove identity context entry or duplicate,
     * 
     * @param url
     * @return
     */
    private String normalizeBusinessPath(String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("ROOT")) {
            url = url.substring(4, url.length());
        }
        final List<String> tokens = new ArrayList<String>();
        for (final StringTokenizer tokenizer = new StringTokenizer(url, "[]"); tokenizer.hasMoreTokens();) {
            final String token = tokenizer.nextToken();
            if (token.startsWith("Identity")) {
                // The portlet "My courses" add an Identity context entry to the business path
                // ignore it
                continue;
            }
            if (!tokens.contains(token)) {
                tokens.add(token);
            }
        }

        final StringBuilder sb = new StringBuilder();
        for (final String token : tokens) {
            sb.append('[').append(token).append(']');
        }
        return sb.toString();
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
    }

    private class InfoCourseSecurityCallback implements InfoSecurityCallback {
        private final boolean canAdd;
        private final boolean canAdmin;

        public InfoCourseSecurityCallback(final boolean canAdd, final boolean canAdmin) {
            this.canAdd = canAdd;
            this.canAdmin = canAdmin;
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canAdd() {
            return canAdd;
        }

        @Override
        public boolean canEdit() {
            return canAdmin;
        }

        @Override
        public boolean canDelete() {
            return canAdmin;
        }
    }

    private class InfoOLATResourceable implements OLATResourceable {
        private final Long resId;

        public InfoOLATResourceable(final Long resId) {
            this.resId = resId;
        }

        @Override
        public String getResourceableTypeName() {
            return OresHelper.calculateTypeName(CourseModule.class);
        }

        @Override
        public Long getResourceableId() {
            return resId;
        }
    }
}
