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

package org.olat.presentation.framework.dispatcher.legacy;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.olat.data.commons.vfs.FolderConfig;
import org.olat.presentation.commons.session.UserSession;

/**
 * Initial Date: 16.06.2003
 * 
 * @author Mike Stock<br>
 *         Comment: Inherited FilePathHandler.root must be set by IMSCpModule
 */
public class QTIStaticsHandler extends FilePathHandler {

    public QTIStaticsHandler() {
        super();
        setRoot(FolderConfig.getCanonicalRepositoryHome());
    }

    /**
	 */
    @Override
    public void init(final String config) {
        // no need to do an init...
    }

    @Override
    public InputStream getInputStream(final HttpServletRequest request, final ResourceDescriptor rd) {
        return super.getInputStream(request, rd);
    }

    @Override
    public ResourceDescriptor getResourceDescriptor(final HttpServletRequest request, final String relPath) {
        if (UserSession.getUserSession(request).isAuthenticated()) {
            if (relPath.endsWith("qti.xml")) {
                return null;
            }
            return super.getResourceDescriptor(request, relPath);
        } else {
            return null;
        }
    }

}
