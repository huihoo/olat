/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.course.nodes.ll;

import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.LLCourseNode;

/**
 * Description:<br>
 * Run controller for link list nodes.
 * <P>
 * Initial Date: 05.11.2008 <br>
 * 
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLRunController extends BasicController {

	private final VelocityContainer runVC;

	public LLRunController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration moduleConfig, final LLCourseNode llCourseNode,
			final UserCourseEnvironment userCourseEnv, final boolean showLinkComments) {
		super(ureq, wControl);
		this.runVC = this.createVelocityContainer("run");
		final List<LLModel> linkList = (List<LLModel>) llCourseNode.getModuleConfiguration().get(LLCourseNode.CONF_LINKLIST);
		this.runVC.contextPut("linkList", linkList);
		this.runVC.contextPut("showLinkComments", Boolean.valueOf(showLinkComments));
		putInitialPanel(runVC);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose here

	}

	@Override
	protected void event(final UserRequest ureq, final Component source, final Event event) {
		// nothing to do here
	}

}
