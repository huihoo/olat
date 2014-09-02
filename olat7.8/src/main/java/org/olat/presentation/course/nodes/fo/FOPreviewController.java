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

package org.olat.presentation.course.nodes.fo;

import org.olat.lms.course.nodes.FOCourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
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
public class FOPreviewController extends DefaultController {
    private static final String PACKAGE = PackageUtil.getPackageName(FOPreviewController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(FOPreviewController.class);

    private final Translator trans;
    private final VelocityContainer previewVC;

    /**
     * @param ureq
     * @param wControl
     * @param node
     * @param ne
     */
    public FOPreviewController(final UserRequest ureq, final WindowControl wControl, final FOCourseNode node, final NodeEvaluation ne) {
        super(wControl);
        trans = new PackageTranslator(PACKAGE, ureq.getLocale());
        previewVC = new VelocityContainer("foPreviewVC", VELOCITY_ROOT + "/preview.html", trans, this);
        previewVC.contextPut("canRead", Boolean.valueOf(ne.isCapabilityAccessible("reader")));
        previewVC.contextPut("canPost", Boolean.valueOf(ne.isCapabilityAccessible("poster")));
        previewVC.contextPut("canModerate", Boolean.valueOf(ne.isCapabilityAccessible("moderator")));
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
