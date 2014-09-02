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

package org.olat.presentation.course.archiver;

/* TODO: ORID-1007 'File' */
import org.olat.lms.course.archiver.CourseArchiverEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: Course-Results-Archiver using ScoreAccountingHelper.class Initial Date: Sep 23, 2004
 * 
 * @author gnaegi
 */
public class ScoreAccountingArchiveController extends DefaultController {
    private static final String PACKAGE = PackageUtil.getPackageName(ScoreAccountingArchiveController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(PACKAGE);

    private final OLATResourceable ores;
    private final Panel myPanel;
    private final VelocityContainer myContent;
    private VelocityContainer vcFeedback;
    private final Translator t;
    private final Link startButton;

    /**
     * Constructor for the score accounting archive controller
     * 
     * @param ureq
     * @param course
     */
    public ScoreAccountingArchiveController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores) {
        super(wControl);
        this.ores = ores;

        this.t = new PackageTranslator(PACKAGE, ureq.getLocale());

        this.myPanel = new Panel("myPanel");
        myPanel.addListener(this);

        myContent = new VelocityContainer("myContent", VELOCITY_ROOT + "/start.html", t, this);
        startButton = LinkFactory.createButtonSmall("cmd.start", myContent, this);

        myPanel.setContent(myContent);
        setInitialComponent(myPanel);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == startButton) {
            String fileName = getCourseArchiverEBL().archiveScoreAccounting(ureq.getIdentity(), ores, ureq.getLocale());
            vcFeedback = new VelocityContainer("feedback", VELOCITY_ROOT + "/feedback.html", t, this);
            vcFeedback.contextPut("body", vcFeedback.getTranslator().translate("course.res.feedback", new String[] { fileName }));
            myPanel.setContent(vcFeedback);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    private CourseArchiverEBL getCourseArchiverEBL() {
        return CoreSpringFactory.getBean(CourseArchiverEBL.class);
    }

}
