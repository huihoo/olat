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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.connectors.rest.catalog;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.olat.connectors.rest.support.vo.LinkVO;
import org.olat.data.catalog.CatalogEntry;
import org.olat.system.commons.Settings;

/**
 * Description:<br>
 * Object factory for the catalog entry
 * <P>
 * Initial Date: 5 may 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class CatalogVOFactory {

    public static CatalogEntryVO get(final CatalogEntry entry) {
        final CatalogEntryVO vo = new CatalogEntryVO();
        vo.setKey(entry.getKey());
        vo.setName(entry.getName());
        vo.setDescription(entry.getDescription());
        vo.setExternalURL(entry.getExternalURL());
        vo.setType(entry.getType());
        vo.setParentKey(entry.getParent() == null ? null : entry.getParent().getKey());
        vo.setRepositoryEntryKey(entry.getRepositoryEntry() == null ? null : entry.getRepositoryEntry().getKey());
        return vo;
    }

    public static CatalogEntryVO link(final CatalogEntryVO entryVo, final UriInfo uriInfo) {
        if (uriInfo != null) {
            final UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
            final URI getUri = baseUriBuilder.path("catalog").path(entryVo.getKey().toString()).build();
            entryVo.getLink().add(new LinkVO("self", getUri.toString(), ""));
            entryVo.getLink().add(new LinkVO("jumpin", Settings.getServerContextPathURI() + "/url/CatalogEntry/" + entryVo.getKey(), ""));
            entryVo.getLink().add(new LinkVO("edit", getUri.toString(), ""));
            entryVo.getLink().add(new LinkVO("delete", getUri.toString(), ""));

            final URI childrenUri = baseUriBuilder.path("catalog").path(entryVo.getKey().toString()).path("children").build();
            entryVo.getLink().add(new LinkVO("children", childrenUri.toString(), ""));
        }
        return entryVo;
    }
}
