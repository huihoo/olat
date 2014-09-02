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

package org.olat.presentation.course.calendar;

import java.util.Iterator;
import java.util.List;

import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.CalendarEntryLink;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.calendar.CalendarController;
import org.olat.presentation.calendar.LinkProvider;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.SelectionTree;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.commons.Settings;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

public class CourseLinkProviderController extends BasicController implements LinkProvider {

    private static final String COURSE_LINK_PROVIDER = "COURSE";
    private static final String CAL_LINKS_SUBMIT = "cal.links.submit";
    private final VelocityContainer clpVC;
    private CalendarEntry calendarEntry;
    private final SelectionTree selectionTree;
    private final OLATResourceable ores;
    private final CalendarService calendarService;

    public CourseLinkProviderController(final ICourse course, final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl, new PackageTranslator(PackageUtil.getPackageName(CalendarController.class), ureq.getLocale()));
        this.ores = course;
        calendarService = CoreSpringFactory.getBean(CalendarService.class);
        setVelocityRoot(PackageUtil.getPackageVelocityRoot(CalendarController.class));
        clpVC = createVelocityContainer("calCLP");
        selectionTree = new SelectionTree("clpTree", getTranslator());
        selectionTree.addListener(this);
        selectionTree.setMultiselect(true);
        selectionTree.setAllowEmptySelection(true);
        selectionTree.setShowCancelButton(true);
        selectionTree.setFormButtonKey(CAL_LINKS_SUBMIT);
        selectionTree.setTreeModel(new CourseNodeSelectionTreeModel(course));
        clpVC.put("tree", selectionTree);
        putInitialPanel(clpVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == selectionTree) {
            final TreeEvent te = (TreeEvent) event;
            if (event.getCommand().equals(TreeEvent.COMMAND_TREENODES_SELECTED)) {
                // rebuild calendar entry links
                // we do not use the tree event's getSelectedNodeIDs, instead
                // we walk through the model and fetch the children in order
                // to keep the sorting.
                final List calendarEntryLinks = calendarEntry.getCalendarEntryLinks();
                final TreeNode rootNode = selectionTree.getTreeModel().getRootNode();
                calendarEntryLinks.clear();
                clearSelection(rootNode);
                rebuildKalendarEventLinks(rootNode, te.getNodeIds(), calendarEntryLinks);
                // if the calendarevent is already associated with a calendar, save the modifications.
                // otherwise, the modifications will be saver, when the user saves
                // the calendar event.
                if (calendarEntry.getCalendar() != null) {
                    calendarService.addEntryTo(calendarEntry.getCalendar(), calendarEntry);
                }
                fireEvent(ureq, Event.DONE_EVENT);
            } else if (event.getCommand().equals(TreeEvent.CANCELLED_TREEEVENT.getCommand())) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    private void rebuildKalendarEventLinks(final TreeNode node, final List selectedNodeIDs, final List calendarEntryLinks) {
        if (selectedNodeIDs.contains(node.getIdent())) {
            // assemble link
            final StringBuilder extLink = new StringBuilder();
            extLink.append(Settings.getServerContextPathURI()).append("/auth/repo/go?rid=");
            final ICourse course = CourseFactory.loadCourse(ores);
            final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(course, true);
            extLink.append(re.getKey()).append("&amp;par=").append(node.getIdent());
            final CalendarEntryLink link = new CalendarEntryLink(COURSE_LINK_PROVIDER, node.getIdent(), node.getTitle(), extLink.toString(), node.getIconCssClass());
            calendarEntryLinks.add(link);
            node.setSelected(true);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            rebuildKalendarEventLinks((TreeNode) node.getChildAt(i), selectedNodeIDs, calendarEntryLinks);
        }
    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub
    }

    @Override
    public CourseLinkProviderController getControler() {
        return this;
    }

    public Long getCourseID() {
        return ores.getResourceableId();
    }

    @Override
    public void setCalendarEntry(final CalendarEntry calendarEntry) {
        this.calendarEntry = calendarEntry;
        clearSelection(selectionTree.getTreeModel().getRootNode());
        for (final Iterator iter = calendarEntry.getCalendarEntryLinks().iterator(); iter.hasNext();) {
            final CalendarEntryLink link = (CalendarEntryLink) iter.next();
            if (!link.getProvider().equals(COURSE_LINK_PROVIDER)) {
                continue;
            }
            final String nodeId = link.getId();
            final TreeNode node = selectionTree.getTreeModel().getNodeById(nodeId);
            if (node != null) {
                node.setSelected(true);
            }
        }
    }

    @Override
    public void setDisplayOnly(final boolean displayOnly) {
        if (displayOnly) {
            clpVC.contextPut("displayOnly", Boolean.TRUE);
            selectionTree.setVisible(false);
            clpVC.contextPut("links", calendarEntry.getCalendarEntryLinks());
        } else {
            clpVC.contextPut("displayOnly", Boolean.FALSE);
            selectionTree.setVisible(true);
            clpVC.contextRemove("links");
        }
    }

    private void clearSelection(final TreeNode node) {
        node.setSelected(false);
        for (int i = 0; i < node.getChildCount(); i++) {
            final TreeNode childNode = (TreeNode) node.getChildAt(i);
            clearSelection(childNode);
        }
    }

    @Override
    public void addControllerListener(final ControllerEventListener controller) {
        super.addControllerListener(controller);
    }

}

class CourseNodeSelectionTreeModel extends GenericTreeModel {

    public CourseNodeSelectionTreeModel(final ICourse course) {
        setRootNode(buildTree(course.getRunStructure().getRootNode()));
    }

    private GenericTreeNode buildTree(final CourseNode courseNode) {
        final GenericTreeNode node = new GenericTreeNode(courseNode.getShortTitle(), null);
        node.setAltText(courseNode.getLongTitle());
        node.setIdent(courseNode.getIdent());
        node.setIconCssClass("o_" + courseNode.getType() + "_icon");
        for (int i = 0; i < courseNode.getChildCount(); i++) {
            final CourseNode childNode = (CourseNode) courseNode.getChildAt(i);
            node.addChild(buildTree(childNode));
        }
        return node;
    }

}
