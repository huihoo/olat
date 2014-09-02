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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.course.nodes.st;

import java.util.ArrayList;
import java.util.List;

import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlsite.OlatCmdEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * <h3>Description:</h3> The structure node peek view controller displays the title, the description and the first level of child nodes
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>none</li>
 * </ul>
 * <p>
 * Initial Date: 23.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class STPeekViewController extends BasicController {
    private final VelocityContainer genericPeekViewVC;

    /**
     * Constructor
     * 
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param ne
     */
    public STPeekViewController(final UserRequest ureq, final WindowControl wControl, final NodeEvaluation ne) {
        super(ureq, wControl);

        genericPeekViewVC = createVelocityContainer("stPeekView");
        final List<CourseNode> childNodes = new ArrayList<CourseNode>();
        // Loop over node evaluations of visible nodes
        final int chdCnt = ne.getChildCount();
        for (int i = 0; i < chdCnt; i++) {
            final NodeEvaluation neChd = ne.getNodeEvaluationChildAt(i);
            if (neChd.isVisible()) {
                // Build and add child generic or specific peek view
                final CourseNode child = neChd.getCourseNode();
                childNodes.add(child);
                // Add link to jump to course node
                final Link nodeLink = LinkFactory.createLink("nodeLink_" + child.getIdent(), genericPeekViewVC, this);
                nodeLink.setCustomDisplayText(child.getShortTitle());
                // Add css class for course node type
                final String iconCSSClass = CourseNodeFactory.getInstance().getCourseNodeConfiguration(child.getType()).getIconCSSClass();
                nodeLink.setCustomEnabledLinkCSS("b_with_small_icon_left o_gotoNode " + iconCSSClass);
                nodeLink.setUserObject(child.getIdent());
            }
        }
        // Add course node to get title etc
        genericPeekViewVC.contextPut("childNodes", childNodes);
        // Add css class for course node type
        final CourseNodeFactory courseNodeFactory = CourseNodeFactory.getInstance();
        genericPeekViewVC.contextPut("courseNodeFactory", courseNodeFactory);
        //
        putInitialPanel(genericPeekViewVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source instanceof Link) {
            final Link nodeLink = (Link) source;
            // get node ID and fire activation event
            final String nodeId = (String) nodeLink.getUserObject();
            fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));
        }
    }

}
