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
package org.olat.data.notification;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OptimisticLock;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 25.11.2011 <br>
 * 
 * A publisher is the source of the notification event (e.g. could be a Forum in a course). <br/>
 * The source of the event has a sourceType and a sourceId (e.g. sourceType is FORUM and <br/>
 * sourceId is the forumId). <br/>
 * The source of the event could live in a context (e.g. Course, Group, etc), so the contextType <br/>
 * reflects this. The subContext could be the courseNode in a course.
 * 
 * 
 * @author lavinia
 */
@Entity
@Table(name = "sy_publisher", uniqueConstraints = @UniqueConstraint(columnNames = { "context_id", "context_type", "source_id", "source_type" }))
public class Publisher {

    private static final Logger log = LoggerHelper.getLogger();

    public enum ContextType { // where the publisher lives
        COURSE, UNKNOWN;
    }

    public static final String EXISTS_PUBLISHER_SUBSCRIPTION_QUERY = "existsPublisherSubscription";
    public static final String CONTEXT_ID_PARAM = "contextId";
    public static final String CONTEXT_TYPE_PARAM = "contextType";
    public static final String SUBCONTEXT_ID_PARAM = "subcontextId";
    public static final String SOURCE_ID_PARAM = "sourceId";
    public static final String SOURCE_TYPE_PARAM = "sourceType";

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id = new Long(-1);

    @Version
    private Long version;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private String sourceType; // e.g. FORUM,WIKI

    @Basic(optional = false)
    @Column(name = "source_id")
    private Long sourceId; // e.g. resourceId

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "context_type")
    private ContextType contextType; // e.g. course

    @Basic(optional = false)
    @Column(name = "context_id")
    private Long contextId; // e.g. course id

    @Basic(optional = true)
    @Column(name = "subcontext_id")
    private Long subcontextId; // e.g. courseNode id

    // do not update the version when elements are added/deleted.
    @OptimisticLock(excluded = true)
    @OneToMany(mappedBy = "publisher", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<Subscription> subscriptions = new HashSet<Subscription>();

    public Long getId() {
        return id;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public ContextType getContextType() {
        return contextType;
    }

    public void setContextType(ContextType contextType) {
        this.contextType = contextType;
    }

    public Long getSubcontextId() {
        return subcontextId;
    }

    public void setSubcontextId(Long subcontextId) {
        this.subcontextId = subcontextId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Publisher))
            return false;
        Publisher theOther = (Publisher) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.sourceType, theOther.getSourceType());
        builder.append(this.sourceId, theOther.getSourceId());
        builder.append(this.contextType, theOther.getContextType());
        builder.append(this.contextId, theOther.getContextId());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(this.sourceType);
        builder.append(this.sourceId);
        builder.append(this.contextType);
        builder.append(this.contextId);
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("sourceType", getSourceType());
        builder.append("sourceId", getSourceId());
        builder.append("contextType", getContextType());
        builder.append("contextId", getContextId());
        return builder.toString();
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void removeSubscription(Subscription subscription) {
        subscriptions.remove(subscription);
    }

}
