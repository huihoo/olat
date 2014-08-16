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
 * Copyright (c) since 2004 at frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.connectors.webdav;

import java.io.File;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.MergeSource;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.system.commons.WebappHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * 
 */
public class ThemesWebDAVProvider implements WebDAVProvider {

    private static final String MOUNTPOINT = "themes";

    @Override
    public String getMountPoint() {
        return MOUNTPOINT;
    }

    /**
	 */
    @Override
    public VFSContainer getContainer(final Identity identity) {
        final BaseSecurity secMgr = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        // FIXME: RH: check if it really should return something => why an empty container?
        if (!secMgr.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)) {
            return new MergeSource(null, null);
        }

        // mount /static/themes, filter out CVS!
        final String staticAbsPath = WebappHelper.getContextRoot() + "/static/themes";
        final File themesFile = new File(staticAbsPath);
        final LocalFolderImpl vfsThemes = new LocalFolderImpl(themesFile);
        vfsThemes.setDefaultItemFilter(new VFSItemExcludePrefixFilter(new String[] { "CVS", "cvs" }));
        final VFSContainer vfsCont = vfsThemes;
        return vfsCont;
    }

}
