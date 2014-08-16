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
package org.olat.presentation.filebrowser.commands;

import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * TODO: rhaag Class Description for CmdAddToEPortfolio
 * <P>
 * Initial Date: 03.09.2010 <br>
 * 
 * @author rhaag
 */
public interface CmdAddToEPortfolio extends FolderCommand {

    /**
     * org.olat.presentation.framework.control.WindowControl, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator);

    /**
	 */
    @Override
    public int getStatus();

    /**
	 */
    @Override
    public boolean runsModal();

}
