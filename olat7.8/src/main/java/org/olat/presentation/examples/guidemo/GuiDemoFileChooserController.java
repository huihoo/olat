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
 * Copyright (c) 2008 frentix GmbH,<br>
 * http://www.frentix.com
 * <p>
 */
package org.olat.presentation.examples.guidemo;

/* TODO: ORID-1007 'File' */
import java.io.File;

import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.LocalImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.data.commons.vfs.filters.VFSItemFilter;
import org.olat.presentation.framework.common.filechooser.FileChoosenEvent;
import org.olat.presentation.framework.common.filechooser.FileChooserController;
import org.olat.presentation.framework.common.filechooser.FileChooserUIFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Demo of the filechooser. For this demo we can browse through the olat app code
 * <P>
 * Initial Date: 12.06.2008 <br>
 * 
 * @author gnaegi
 */
public class GuiDemoFileChooserController extends BasicController {
    private FileChooserController chooserCtr;
    private VelocityContainer contentVC;

    /**
     * Constructor for the file chooser gui demo
     * 
     * @param ureq
     * @param wControl
     */
    public GuiDemoFileChooserController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        // Main view is a velocity container
        contentVC = createVelocityContainer("guidemo-filechooser");

        // Create File chooser for the web app root directory
        final File webappRootFile = new File(WebappHelper.getContextRoot());
        final VFSContainer webappRoot = new LocalFolderImpl(webappRootFile);
        // don't show cvs and hidden files meta files
        final VFSItemFilter filter = new VFSItemExcludePrefixFilter(new String[] { "CVS", "cvs", "." });
        // create file chooser with the filter and the web app root
        chooserCtr = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), webappRoot, filter, false);
        listenTo(chooserCtr);
        contentVC.put("chooserCtr", chooserCtr.getInitialComponent());

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), contentVC);
        contentVC.put("sourceview", sourceview.getInitialComponent());

        putInitialPanel(contentVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        contentVC = null;
        // Controllers auto disposed by basic controller
        chooserCtr = null;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == chooserCtr) {
            // catch the events from the file chooser controller here
            if (event instanceof FileChoosenEvent) {
                // User pressed select button, get selected item from controller
                final VFSItem selectedItem = FileChooserUIFactory.getSelectedItem((FileChoosenEvent) event);
                // for this demo just get file path and display an info message
                final LocalImpl localFile = (LocalImpl) selectedItem;
                final String absPath = localFile.getBasefile().getAbsolutePath();
                final String relPath = absPath.substring(WebappHelper.getContextRoot().length());
                getWindowControl().setInfo("user selected " + relPath);

            } else if (event == Event.CANCELLED_EVENT) {
                // user pressed cancel
                getWindowControl().setInfo("user cancelled");

            } else if (event == Event.FAILED_EVENT) {
                // selection failed for unknown reason
                getWindowControl().setError("Ups, an error occured");
            }
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events to catch
    }

}
