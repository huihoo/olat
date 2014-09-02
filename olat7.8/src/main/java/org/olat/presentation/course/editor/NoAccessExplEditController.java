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

package org.olat.presentation.course.editor;

import org.apache.log4j.Logger;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: Apr 7, 2004
 * 
 * @author gnaegi<br>
 *         Comment: This controller can be used to display and edit OLAT node noAccessExplenations. The main component of this controller can be rendered using the
 *         noAccessExplanationComp component name. When the condition experssion has been changed, the controller will fire a Event.CHANGED_EVENT. See
 *         STCourseNodeEditController to get a usage example.
 */
public class NoAccessExplEditController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String PACKAGE = PackageUtil.getPackageName(NoAccessExplEditController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(NoAccessExplEditController.class);

    private final VelocityContainer myContent;

    // render edit button
    private final Translator trans;

    private final NoAccessExplanationFormController noAccessExplanationController;
    private String noAccessExplanation;

    /**
     * Generates a form that can be embedded in the node configuration for editing the noAccessExplanation field.
     * 
     * @param ureq
     *            The UserRequest
     * @param noAccessExplanation
     *            The current no access explanation string or null
     * @param wContr
     *            The window controller
     */
    public NoAccessExplEditController(final UserRequest ureq, final WindowControl wControl, final String noAccessExplanation) {
        super(ureq, wControl);
        this.noAccessExplanation = noAccessExplanation;

        trans = new PackageTranslator(PACKAGE, ureq.getLocale());

        // Main component is a velocity container. It has a name choosen by the
        // controller who
        // called this constructor
        myContent = new VelocityContainer("noAccessExplanationComp", VELOCITY_ROOT + "/noAccessExplEdit.html", trans, this);

        noAccessExplanationController = new NoAccessExplanationFormController(ureq, wControl, noAccessExplanation);
        listenTo(noAccessExplanationController);
        myContent.put("noAccexplForm", noAccessExplanationController.getInitialComponent());

        putInitialPanel(myContent);

    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // Do nothing.
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == noAccessExplanationController) {
            if (event == Event.CANCELLED_EVENT) {
                // Reset form data and switch to read only mode
                noAccessExplanationController.setNoAccessExplanation(noAccessExplanation);
            } else if (event == Event.DONE_EVENT) {
                // Update condition data and switch to read only mode
                noAccessExplanation = noAccessExplanationController.getNoAccessExplanation();

                if (log.isDebugEnabled()) {
                    log.debug("New noAccessExplanation is: " + noAccessExplanation);
                }

                // Inform all listeners about the changed condition
                fireEvent(ureq, Event.CHANGED_EVENT);
            }

            // Add variable to velocity container
        }
    }

    /**
     * Returns the noAccessExplanation that has been editited in the form. Return null if the noAccessExplanation contains only whitespace.
     * 
     * @return String noAccessExplanation or null if empty
     */
    public String getNoAccessExplanation() {
        // only return string if there is something else than whitespace in it
        if (StringHelper.containsNonWhitespace(noAccessExplanation)) {
            return noAccessExplanation;
        }
        return null;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // do nothing here yet
    }
}
