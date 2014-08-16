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

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.ILoggingAction;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.commons.fileresource.SharedFolderFileResource;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.system.event.Event;

/**
 * Description: <br>
 * User (un)selects one shared folder per course. The softkey of the shared folder repository entry is saved in the course config. Also the reference (course -> repo
 * entry)is saved, that nobody can delete a shared folder which is still referenced from a course.
 * <P>
 * 
 * @version Initial Date: July 11, 2005
 * @author Alexander Schneider
 */
public class CourseSharedFolderController extends DefaultController implements ControllerEventListener {

    private static final String PACKAGE = PackageUtil.getPackageName(CourseSharedFolderController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(PACKAGE);

    // private ICourse course;
    private final PackageTranslator translator;
    private final VelocityContainer myContent;

    private ReferencableEntriesSearchController searchController;

    private final RepositoryService rm = RepositoryServiceImpl.getInstance();

    private boolean hasSF; // has shared folder configured
    private final Link changeSFResButton;
    private final Link unselectSFResButton;
    private final Link selectSFResButton;
    private CloseableModalController cmc;
    private final CourseConfig courseConfig;
    private ILoggingAction loggingAction;
    private RepositoryEntry sharedFolderRepositoryEntry;

    /**
     * @param ureq
     * @param wControl
     * @param theCourse
     */
    public CourseSharedFolderController(final UserRequest ureq, final WindowControl wControl, final CourseConfig courseConfig) {
        super(wControl);

        this.courseConfig = courseConfig;
        translator = new PackageTranslator(PACKAGE, ureq.getLocale());

        myContent = new VelocityContainer("courseSharedFolderTab", VELOCITY_ROOT + "/CourseSharedFolder.html", translator, this);
        changeSFResButton = LinkFactory.createButton("sf.changesfresource", myContent, this);
        unselectSFResButton = LinkFactory.createButton("sf.unselectsfresource", myContent, this);
        selectSFResButton = LinkFactory.createButton("sf.selectsfresource", myContent, this);

        final String softkey = courseConfig.getSharedFolderSoftkey();
        String name;

        if (!courseConfig.hasCustomSharedFolder()) {
            name = translator.translate("sf.notconfigured");
            hasSF = false;
            myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
        } else {
            final RepositoryEntry re = rm.lookupRepositoryEntryBySoftkey(softkey, false);
            if (re == null) {
                // log.warning("Removed configured sahred folder from course config, because repo entry does not exist anymore.");
                courseConfig.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
                name = translator.translate("sf.notconfigured");
                hasSF = false;
                myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
            } else {
                name = re.getDisplayname();
                hasSF = true;
                myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
            }
        }
        myContent.contextPut("resourceTitle", name);

        setInitialComponent(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == selectSFResButton || source == changeSFResButton) { // select or change shared folder
            // let user choose a shared folder
            searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, SharedFolderFileResource.TYPE_NAME,
                    translator.translate("command.choose"));
            searchController.addControllerListener(this);
            cmc = new CloseableModalController(getWindowControl(), translator.translate("close"), searchController.getInitialComponent());
            cmc.activate();
        } else if (source == unselectSFResButton) { // unselect shared folder
            if (courseConfig.hasCustomSharedFolder()) {
                // delete reference from course to sharedfolder
                // get unselected shared folder's softkey used for logging
                final String softkeyUsf = courseConfig.getSharedFolderSoftkey();
                final RepositoryEntry usfRe = rm.lookupRepositoryEntryBySoftkey(softkeyUsf, true);
                if (usfRe != null) {
                    sharedFolderRepositoryEntry = usfRe;
                }
                // set default value to delete configured value in course config
                courseConfig.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
                // deleteRefTo(course);
                // course.getCourseEnvironment().setCourseConfig(cc);
                final String emptyKey = translator.translate(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
                myContent.contextPut("resourceTitle", emptyKey);
                hasSF = false;
                myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
                loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_REMOVED;
                this.fireEvent(ureq, Event.CHANGED_EVENT);
                // AuditManager am = course.getCourseEnvironment().getAuditManager();
                // am.log(LogLevel.ADMIN_ONLY_FINE, ureq.getIdentity(),LOG_SHARED_FOLDER_REMOVED, null, usfRe.getDisplayname());
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == searchController) {
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                // repository search controller done
                sharedFolderRepositoryEntry = searchController.getSelectedEntry();
                final String softkey = sharedFolderRepositoryEntry.getSoftkey();
                courseConfig.setSharedFolderSoftkey(softkey);
                // updateRefTo(sharedFolderRe, course);
                // course.getCourseEnvironment().setCourseConfig(cc);
                hasSF = true;
                myContent.contextPut("hasSharedFolder", new Boolean(hasSF));

                myContent.contextPut("resourceTitle", sharedFolderRepositoryEntry.getDisplayname());

                loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_ADDED;
                this.fireEvent(ureq, Event.CHANGED_EVENT);
                /*
                 * AuditManager am = course.getCourseEnvironment().getAuditManager(); am.log(LogLevel.ADMIN_ONLY_FINE, ureq.getIdentity(),LOG_SHARED_FOLDER_ADDED, null,
                 * sharedFolderRe.getDisplayname());
                 */
            }
            cmc.deactivate();
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (searchController != null) {
            searchController.dispose();
            searchController = null;
        }

    }

    /**
     * @return Returns a log message if the course shared folder was added or removed, null otherwise.
     */
    public ILoggingAction getLoggingAction() {
        return loggingAction;
    }

    public RepositoryEntry getSharedFolderRepositoryEntry() {
        return sharedFolderRepositoryEntry;
    }

}
