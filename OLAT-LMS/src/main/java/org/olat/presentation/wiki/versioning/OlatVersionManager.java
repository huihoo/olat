/**
 * 
 */
package org.olat.presentation.wiki.versioning;

import java.util.List;

import org.olat.lms.wiki.WikiPage;

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

public class OlatVersionManager implements VersionManager {
    // private VersionStorage storage;
    private DifferenceService service;

    @Override
    public void storeVersion(final WikiPage page) {
        // TODO Auto-generated method stub

    }

    @Override
    public WikiPage loadVersion(final WikiPage page, final int version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List getHistory(final WikiPage page) {
        return null;
    }

    @Override
    public List diff(final WikiPage page, final int version1, final int version2) {
        return service.diff(loadVersion(page, version1).getContent(), loadVersion(page, version2).getContent());
    }

}
