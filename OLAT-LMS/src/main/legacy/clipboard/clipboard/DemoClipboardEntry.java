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
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class DemoClipboardEntry implements ClipboardEntry {

	private final String text;

	/**
	 * @param string
	 */
	public DemoClipboardEntry(final String text) {
		this.text = text;
	}

	/*
	 * (non-Javadoc)
	 * @see org.olat.core.commons.services.clipboard.ClipboardEntry#acceptsFlavor(java.lang.Class)
	 */
	@Override
	public boolean acceptsFlavor(final Class flavorInterfaceName) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.olat.core.commons.services.clipboard.ClipboardEntry#createTrayUI()
	 */
	@Override
	public ControllerCreator createTrayUI() {
		return new ControllerCreator() {
			@Override
			public Controller createController(final UserRequest ureq, final WindowControl wControl) {
				return MessageUIFactory.createSimpleMessage(ureq, wControl, DemoClipboardEntry.this.text);
			}
		};
	}

	/**
	 * @return
	 */
	public String getText() {
		return text;
	}

}
