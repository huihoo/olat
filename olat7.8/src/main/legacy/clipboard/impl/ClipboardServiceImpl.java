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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.commons.services.clipboard.impl;

import org.olat.core.commons.services.clipboard.ClipboardEntry;
import org.olat.core.commons.services.clipboard.ClipboardEntryCreator;
import org.olat.core.commons.services.clipboard.ClipboardService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.springframework.stereotype.Service;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 */
@Service
public class ClipboardServiceImpl implements ClipboardService {

	private ClipboardEntry clipboardEntry;

	/*
	 * (non-Javadoc)
	 * @see org.olat.core.commons.services.clipboard.ClipboardService#createCopyToUIService()
	 */
	@Override
	public ControllerCreator createCopyToUIService(final ClipboardEntryCreator cbec) {
		return new ControllerCreator() {
			@Override
			public Controller createController(UserRequest ureq, WindowControl wControl) {
				return new CopyToClipboardController(ureq, wControl, ClipboardServiceImpl.this, cbec);
			}
		};
	}

	@Override
	public ControllerCreator createPasteFromUIService(final Class[] acceptedFlavorInterfaces) {
		return new ControllerCreator() {
			@Override
			public Controller createController(UserRequest ureq, WindowControl wControl) {
				return new PasteFromClipboardController(ureq, wControl, ClipboardServiceImpl.this, acceptedFlavorInterfaces);
			}
		};
	}

	@Override
	public ClipboardEntry getClipboardEntryFrom(Event event) {
		ClipboardEvent cbe = (ClipboardEvent) event;
		return cbe.getClipboardEntry();
	}

	ClipboardEntry getClipboardEntry() {
		return clipboardEntry;
	}

	void setClipboardEntry(ClipboardEntry clipboardEntry) {
		this.clipboardEntry = clipboardEntry;
	}

	/*
	 * (non-Javadoc)
	 * @see org.olat.core.commons.services.clipboard.ClipboardService#onceGetClipboardUI()
	 */
	@Override
	public ControllerCreator onceGetClipboardUI() {
		return new ControllerCreator() {
			@Override
			public Controller createController(UserRequest ureq, WindowControl wControl) {
				return new ClipboardTrayController(ureq, wControl, ClipboardServiceImpl.this);
			}
		};
	}

}
