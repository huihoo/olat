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

package org.olat.presentation.course.config;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.filters.VFSItemFileTypeFilter;
import org.olat.lms.activitylogging.ILoggingAction;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.course.config.CourseConfig;
import org.olat.presentation.framework.common.filechooser.FileChoosenEvent;
import org.olat.presentation.framework.common.filechooser.FileChooserController;
import org.olat.presentation.framework.common.filechooser.FileChooserUIFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.system.event.Event;

/**
 * Description: <br>
 * Configuration of course layout settings: standard or customized Initial Date: Jun 21, 2005 <br>
 * 
 * @author patrick
 */
public class CourseLayoutController extends BasicController {

    private static final String LOG_COURSELAYOUT_DEFAULT_ADDED = "COURSELAYOUT_DEFAULT_ADDED";
    private static final String LOG_COURSELAYOUT_CUSTOM_ADDED = "COURSELAYOUT_CUSTOM_ADDED";
    private static final VFSItemFileTypeFilter cssTypeFilter = new VFSItemFileTypeFilter(new String[] { "css" });
    private final VelocityContainer myContent;
    private FileChooserController fileChooserCtr;
    private final Link changeCustomCSSButton;
    private final Link chooseSystemCSSButton;
    private final Link chooseCustomCSSButton;
    private CloseableModalController cmc;
    private final VFSContainer vfsCourseRoot;
    private final CourseConfig courseConfig;
    private ILoggingAction loggingAction;

    /**
     * @param ureq
     * @param control
     * @param theCourse
     */
    public CourseLayoutController(final UserRequest ureq, final WindowControl wControl, final CourseConfig courseConfig, final VFSContainer vfsCourseRoot) {
        super(ureq, wControl);

        this.courseConfig = courseConfig;
        this.vfsCourseRoot = vfsCourseRoot;

        myContent = this.createVelocityContainer("CourseLayout");
        changeCustomCSSButton = LinkFactory.createButton("form.layout.changecustomcss", myContent, this);
        chooseSystemCSSButton = LinkFactory.createButton("form.layout.choosesystemcss", myContent, this);
        chooseCustomCSSButton = LinkFactory.createButton("form.layout.choosecustomcss", myContent, this);

        final String cssFileRef = courseConfig.getCssLayoutRef();
        myContent.contextPut("hasCustomCourseCSS", new Boolean(courseConfig.hasCustomCourseCSS()));
        myContent.contextPut("cssFileRef", cssFileRef);

        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {

        if (source == chooseSystemCSSButton) {
            // applying the default layout
            myContent.contextPut("cssFileRef", CourseConfig.VALUE_EMPTY_CSS_FILEREF);
            courseConfig.setCssLayoutRef(CourseConfig.VALUE_EMPTY_CSS_FILEREF);

            myContent.contextPut("hasCustomCourseCSS", new Boolean(courseConfig.hasCustomCourseCSS()));
            // log removing custom course layout
            loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COURSELAYOUT_DEFAULT_ADDED;
            this.fireEvent(ureq, Event.CHANGED_EVENT);

        } else if (source == changeCustomCSSButton || source == chooseCustomCSSButton) {

            removeAsListenerAndDispose(fileChooserCtr);
            fileChooserCtr = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), vfsCourseRoot, cssTypeFilter, true);
            listenTo(fileChooserCtr);
            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), fileChooserCtr.getInitialComponent());
            listenTo(cmc);
            cmc.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == fileChooserCtr) {
            // from file choosing, in any case remove modal dialog
            cmc.deactivate();

            if (event instanceof FileChoosenEvent) {

                final String relPath = FileChooserUIFactory.getSelectedRelativeItemPath((FileChoosenEvent) event, vfsCourseRoot, null);
                // user chose a file
                myContent.contextPut("cssFileRef", relPath);
                courseConfig.setCssLayoutRef(relPath);
                myContent.contextPut("hasCustomCourseCSS", new Boolean(courseConfig.hasCustomCourseCSS()));

                // log adding custom course layout
                loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COURSELAYOUT_CUSTOM_ADDED;
                this.fireEvent(ureq, Event.CHANGED_EVENT);
            }

        } // else user cancelled file selection
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // controllers autodisposed by basic controller
    }

    /**
     * @return Returns a log message if the course layout was changed, else null.
     */
    public ILoggingAction getLoggingAction() {
        return loggingAction;
    }

}
