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
package org.olat.data.course.campus;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * Initial Date: 07.12.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_org")
@NamedQueries({ @NamedQuery(name = Org.GET_IDS_OF_ALL_ENABLED_ORGS, query = "select id from Org"),
        @NamedQuery(name = Org.GET_ALL_NOT_UPDATED_ORGS, query = "select id from Org o where o.modifiedDate < :lastImportDate"),
        @NamedQuery(name = Org.DELETE_ALL_NOT_UPDATED_ORGS, query = "delete from Org o where o.modifiedDate < :lastImportDate"),
        @NamedQuery(name = Org.DELETE_BY_ORG_IDS, query = "delete from Org o where o.id in ( :orgIds)") })
public class Org {
    @Id
    private Long id;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "name")
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    public static final String GET_IDS_OF_ALL_ENABLED_ORGS = "getIdsOfAllEnabledOrgs";
    public static final String GET_ALL_NOT_UPDATED_ORGS = "getAllNotUpdatedOrgs";
    public static final String DELETE_ALL_NOT_UPDATED_ORGS = "deleteAllNotUpdatedOrgs";
    public static final String DELETE_BY_ORG_IDS = "deleteByOrgIds";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

}
