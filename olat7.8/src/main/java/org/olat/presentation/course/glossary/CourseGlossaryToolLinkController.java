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
package org.olat.presentation.course.glossary;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.preferences.Preferences;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.course.run.RunMainController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.dtabs.DTab;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.glossary.GlossaryMainController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Toolbox link that shows a link to open the glossary in read or read/write mode and a toggle link to enable/disable the glossary terms identifying process in the given
 * text marker controller.
 * <P>
 * Initial Date: Dec 06 2006 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class CourseGlossaryToolLinkController extends BasicController {

    private final VelocityContainer mainVC;
    private Link onCommand, offCommand;
    private final String guiPrefsKey;
    boolean allowGlossaryEditing;
    private final CourseEnvironment courseEnvir;
    private final GlossaryMarkupItemController glossMarkupItmCtr;

    public CourseGlossaryToolLinkController(final WindowControl wControl, final UserRequest ureq, final ICourse course, final Translator translator,
            final boolean allowGlossaryEditing, final CourseEnvironment courseEnvironment, final GlossaryMarkupItemController glossMarkupItmCtr) {
        super(ureq, wControl, translator);
        setBasePackage(RunMainController.class);
        this.allowGlossaryEditing = allowGlossaryEditing;
        courseEnvir = courseEnvironment;
        guiPrefsKey = CourseGlossaryFactory.createGuiPrefsKey(course);

        mainVC = createVelocityContainer("glossaryToolLink");

        final Preferences prefs = ureq.getUserSession().getGuiPreferences();
        final Boolean state = (Boolean) prefs.get(CourseGlossaryToolLinkController.class, guiPrefsKey);
        if (state == null || !state.booleanValue()) {
            onCommand = LinkFactory.createLink("command.glossary.on", mainVC, this);
            onCommand.setTitle("command.glossary.on.alt");
            onCommand.setCustomEnabledLinkCSS("b_toolbox_toggle");
        } else {
            offCommand = LinkFactory.createLink("command.glossary.off", mainVC, this);
            offCommand.setTitle("command.glossary.off.alt");
            offCommand.setCustomEnabledLinkCSS("b_toolbox_toggle");
        }

        // keep reference to textMarkerContainerCtr for later enabling/disabling
        this.glossMarkupItmCtr = glossMarkupItmCtr;

        putInitialPanel(mainVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == onCommand) {
            // toggle on
            final Preferences prefs = ureq.getUserSession().getGuiPreferences();
            prefs.put(CourseGlossaryToolLinkController.class, guiPrefsKey, Boolean.TRUE);
            prefs.save();
            // update gui
            mainVC.remove(onCommand);
            offCommand = LinkFactory.createLink("command.glossary.off", mainVC, this);
            offCommand.setTitle("command.glossary.off.alt");
            offCommand.setCustomEnabledLinkCSS("b_toolbox_toggle");
            // notify textmarker controller
            glossMarkupItmCtr.setTextMarkingEnabled(true);
            fireEvent(ureq, new Event("glossaryOn"));

        } else if (source == offCommand) {
            // toggle off
            final Preferences prefs = ureq.getUserSession().getGuiPreferences();
            prefs.put(CourseGlossaryToolLinkController.class, guiPrefsKey, Boolean.FALSE);
            prefs.save();
            // update gui
            mainVC.remove(offCommand);
            onCommand = LinkFactory.createLink("command.glossary.on", mainVC, this);
            onCommand.setTitle("command.glossary.on.alt");
            onCommand.setCustomEnabledLinkCSS("b_toolbox_toggle");
            // notify textmarker controller
            glossMarkupItmCtr.setTextMarkingEnabled(false);
            fireEvent(ureq, new Event("glossaryOff"));
        } else if (source == mainVC && event.getCommand().equals("command.glossary")) {
            // start glossary in window
            final CourseConfig cc = courseEnvir.getCourseConfig(); // do not cache cc, not save

            // if glossary had been opened from LR as Tab before, warn user:
            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(cc.getGlossarySoftKey(), false);
            final DTab dt = dts.getDTab(repoEntry.getOlatResource());
            if (dt != null) {
                dts.activate(ureq, dt, ((Boolean) allowGlossaryEditing).toString());
            } else {
                final ControllerCreator ctrlCreator = new ControllerCreator() {
                    @Override
                    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                        final GlossaryMainController glossaryController = CourseGlossaryFactory.createCourseGlossaryMainRunController(lwControl, lureq, cc,
                                allowGlossaryEditing);
                        if (glossaryController == null) {
                            // happens in the unlikely event of a user who is in a course and
                            // now
                            // tries to access the glossary
                            final String text = translate("error.noglossary");
                            return MessageUIFactory.createInfoMessage(lureq, lwControl, null, text);
                        } else {
                            // use a one-column main layout
                            final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null,
                                    glossaryController.getInitialComponent(), null);
                            // dispose glossary on layout dispose
                            layoutCtr.addDisposableChildController(glossaryController);
                            return layoutCtr;
                        }
                    }
                };

                final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
                // open in new browser window
                openInNewBrowserWindow(ureq, layoutCtrlr);
                return;// immediate return after opening new browser window!
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // no need to dispose the textMarkerContainerCtr - should be done by parent
        // controller
    }
}
