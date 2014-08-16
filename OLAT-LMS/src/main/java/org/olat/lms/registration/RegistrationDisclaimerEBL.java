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
package org.olat.lms.registration;

import java.io.File;

import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.system.commons.WebappHelper;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 17.10.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class RegistrationDisclaimerEBL {

    private static final String CUSTOMIZING_DISCLAIMER_PATH = "/customizing/disclaimer/";

    public VFSContainer getDisclaimerVfsContainer() {
        final File disclaimerDir = new File(WebappHelper.getUserDataRoot() + CUSTOMIZING_DISCLAIMER_PATH);
        disclaimerDir.mkdirs();
        final VFSContainer disclaimerContainer = new LocalFolderImpl(disclaimerDir);
        return disclaimerContainer;
    }

}
