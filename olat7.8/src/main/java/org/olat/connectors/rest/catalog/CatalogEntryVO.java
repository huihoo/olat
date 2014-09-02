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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.connectors.rest.support.vo.LinkVO;

/**
 * Description:<br>
 * A mapper class for the <code>CatalogEntry</code>
 * <P>
 * Initial Date: 5 may 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "catalogEntryVO")
public class CatalogEntryVO {

    private Long key;
    private String name;
    private String description;
    private String externalURL;
    private Integer type;
    private Long repositoryEntryKey;
    private Long parentKey;

    @XmlElement(name = "link", nillable = true)
    private List<LinkVO> link = new ArrayList<LinkVO>();

    public CatalogEntryVO() {
        // make JAXB happy
    }

    public Long getKey() {
        return key;
    }

    public void setKey(final Long key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getExternalURL() {
        return externalURL;
    }

    public void setExternalURL(final String externalURL) {
        this.externalURL = externalURL;
    }

    public Long getRepositoryEntryKey() {
        return repositoryEntryKey;
    }

    public void setRepositoryEntryKey(final Long repositoryEntryKey) {
        this.repositoryEntryKey = repositoryEntryKey;
    }

    public Integer getType() {
        return type;
    }

    public void setType(final Integer type) {
        this.type = type;
    }

    public Long getParentKey() {
        return parentKey;
    }

    public void setParentKey(final Long parentKey) {
        this.parentKey = parentKey;
    }

    public List<LinkVO> getLink() {
        return link;
    }

    public void setLink(final List<LinkVO> link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "catalogEntryVO[key=" + key + ":name=" + name + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CatalogEntryVO) {
            final CatalogEntryVO vo = (CatalogEntryVO) obj;
            return key != null && key.equals(vo.key);
        }
        return false;
    }
}
