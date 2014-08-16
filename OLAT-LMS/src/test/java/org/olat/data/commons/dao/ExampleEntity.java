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
package org.olat.data.commons.dao;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "o_tst_daotest")
public class ExampleEntity implements Serializable {

    @Id
    @Column(name = "dao_id")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id = new Long(0);

    @Version
    private Long version;

    @Basic(optional = true)
    @Column(name = "name")
    private String name;

    @Basic(optional = true)
    @Column(name = "description")
    private String description;

    public ExampleEntity() {
        super();
    }

    public ExampleEntity(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Zwei Order-Objekte sind gleich, wenn TrackingNumber �bereinstimmt.
     */
    // @Override
    // public boolean equals(final Object obj) {
    // if (obj == this) {
    // return true;
    // }
    // if (!(obj instanceof Order))
    // return false;
    // Order rhs = (Order) obj;
    // EqualsBuilder builder = new EqualsBuilder();
    // builder.append(this.trackingNumber, rhs.getTrackingNumber());
    // return builder.isEquals();
    // }

    /**
     * @return Hashcode auf Basis von TrackingNumber.
     */
    // @Override
    // public int hashCode() {
    // HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
    // builder.append(this.trackingNumber);
    // return builder.toHashCode();
    // }

    /**
     * Gibt ID, Version und TrackingNumber zur�ck.
     */
    // @Override
    // public String toString() {
    // ToStringBuilder builder = new ToStringBuilder(this);
    // builder.append("id", getId());
    // builder.append("version", getVersion());
    // builder.append("trackingNumber", getTrackingNumber());
    // return builder.toString();
    // }

}
