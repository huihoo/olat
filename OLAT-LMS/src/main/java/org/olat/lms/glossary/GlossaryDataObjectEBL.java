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
package org.olat.lms.glossary;

import java.util.List;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;

/**
 * Initial Date: 03.10.2011 <br>
 * serves as return value object for GlossaryEBL
 * 
 * @author Branislav Balaz
 */
public class GlossaryDataObjectEBL {

    private final String glossaryMainTerm;
    private final VFSContainer glossaryFolder;
    private final NotFoundMediaResource notFoundMediaResource;
    private final boolean isNotFoundMediaResource;
    private final long lastModifiedTime;
    private final List<GlossaryItem> glossaryItems;

    public GlossaryDataObjectEBL(String glossaryMainTerm, VFSContainer glossaryFolder, String relativePath, boolean isNotFoundMediaResource, long lastModifiedTime,
            List<GlossaryItem> glossaryItems) {
        notFoundMediaResource = new NotFoundMediaResource(relativePath);
        this.glossaryMainTerm = glossaryMainTerm;
        this.glossaryFolder = glossaryFolder;
        this.isNotFoundMediaResource = isNotFoundMediaResource;
        this.lastModifiedTime = lastModifiedTime;
        this.glossaryItems = glossaryItems;
    }

    public String getGlossaryMainTerm() {
        return glossaryMainTerm;
    }

    public VFSContainer getGlossaryFolder() {
        return glossaryFolder;
    }

    public NotFoundMediaResource getNotFoundMediaResource() {
        return notFoundMediaResource;
    }

    public boolean isNotFoundMediaResource() {
        return isNotFoundMediaResource;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public List<GlossaryItem> getGlossaryItems() {
        return glossaryItems;
    }

}
