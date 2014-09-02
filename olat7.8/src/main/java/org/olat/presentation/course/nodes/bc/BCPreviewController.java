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

package org.olat.presentation.course.nodes.bc;

import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.lms.commons.vfs.securitycallbacks.ReadOnlyCallback;
import org.olat.lms.course.nodes.BCCourseNode;
import org.olat.lms.course.nodes.FolderNodeCallback;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Description: <br>
 * Initial Date: 10.02.2005 <br>
 * 
 * @author Mike Stock
 */
public class BCPreviewController extends DefaultController {
    private static final String PACKAGE = PackageUtil.getPackageName(BCPreviewController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(BCPreviewController.class);

    private final Translator trans;
    private final VelocityContainer previewVC;

    /**
     * @param ureq
     * @param wControl
     * @param node
     * @param ne
     */
    public BCPreviewController(final UserRequest ureq, final WindowControl wControl, final BCCourseNode node, final CourseEnvironment courseEnv, final NodeEvaluation ne) {
        super(wControl);
        trans = new PackageTranslator(PACKAGE, ureq.getLocale());
        previewVC = new VelocityContainer("bcPreviewVC", VELOCITY_ROOT + "/preview.html", trans, this);
        final OlatNamedContainerImpl namedContainer = BCCourseNode.getNodeFolderContainer(node, courseEnv);
        namedContainer.setLocalSecurityCallback(new ReadOnlyCallback());
        final FolderRunController folder = new FolderRunController(namedContainer, false, ureq, getWindowControl());
        previewVC.put("folder", folder.getInitialComponent());
        // get additional infos
        final VFSSecurityCallback secCallback = new FolderNodeCallback(namedContainer.getRelPath(), ne, false, false, null);
        previewVC.contextPut("canUpload", Boolean.valueOf(secCallback.canWrite()));
        previewVC.contextPut("canDownload", Boolean.valueOf(secCallback.canRead()));
        final Quota q = secCallback.getQuota();
        previewVC.contextPut("quotaKB", (q != null) ? q.getQuotaKB().toString() : "-");
        setInitialComponent(previewVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
