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
package org.olat.presentation.examples.guidemo.clipboard;

import org.olat.core.commons.services.clipboard.ClipboardEntry;
import org.olat.core.commons.services.clipboard.ClipboardEntryCreator;
import org.olat.core.commons.services.clipboard.ClipboardService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClipboardDemoController extends BasicController {

	private Controller copyToC;
	private Controller pasteFromC;
	private ClipboardService cps;

	/**
	 * @param ureq
	 * @param wControl
	 */
	public ClipboardDemoController(final UserRequest ureq, final WindowControl wControl) {
		super(ureq, wControl);

		cps = (ClipboardService) CoreSpringFactory.getBean(ClipboardService.class);
		// if cps == null --> service not available in this setup: does this make sense at all?

		final VelocityContainer mainVc = createVelocityContainer("clipdemo");

		// copyTo demo
		copyToC = cps.createCopyToUIService(new ClipboardEntryCreator() {
			@Override
			public ClipboardEntry createClipboardEntry() {
				return createCurrentClipboardEntry();
			}

		}).createController(ureq, getWindowControl());
		mainVc.put("copyto", copyToC.getInitialComponent());

		// pasteFrom demo
		pasteFromC = cps.createPasteFromUIService(new Class[] { DemoClipboardEntry.class }).createController(ureq, getWindowControl());
		pasteFromC.addControllerListener(this);
		mainVc.put("pastefrom", pasteFromC.getInitialComponent());

		putInitialPanel(mainVc);
	}

	ClipboardEntry createCurrentClipboardEntry() {
		return new DemoClipboardEntry("time:" + System.currentTimeMillis());
	}

	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		// nothing to do yet
	}

	@Override
	public void event(final UserRequest ureq, final Controller source, final Event event) {
		if (source == pasteFromC) {
			final ClipboardEntry cbe = cps.getClipboardEntryFrom(event);
			final DemoClipboardEntry dcbe = (DemoClipboardEntry) cbe;
			getWindowControl().setInfo("content of clipboard is:" + dcbe.getText());
		}
	}

	@Override
	protected void doDispose() {
		if (copyToC != null) {
			copyToC.dispose();
		}
		if (pasteFromC != null) {
			pasteFromC.dispose();
		}
	}

}
