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

package org.olat.lms.search;

import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.data.forum.Message;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Search internal class to build resourceUrl in indexer.
 * 
 * @author Christian Guretzki
 */
public class SearchResourceContext {

    private static final Logger log = LoggerHelper.getLogger();

    /** Workaround for forum message. Forum-Message is currently no OLATResourcable. */
    public static final String MESSAGE_RESOURCE_TYPE = "Message";

    private static final String FILEPATH_PREFIX = "[path=";
    private static final String ENDTAG = "]";

    // Parameter to pass from parent to child
    private Date lastModified;
    private Date createdDate;
    private String documentType = null;
    private String title = null;
    private String description = null;
    private String parentContextType = null;
    private String parentContextName = null;
    private String documentContext = null;

    private BusinessControl myBusinessControl;
    private final BusinessControl parentBusinessControl;

    private String filePath = null;

    /**
     * Constructor for root-object without any parent.
     */
    public SearchResourceContext() {
        parentBusinessControl = null;
    }

    /**
     * Constructor for child-object with a parent.
     */
    public SearchResourceContext(final SearchResourceContext parentResourceContext) {
        lastModified = parentResourceContext.getLastModified();
        createdDate = parentResourceContext.getCreatedDate();
        documentType = parentResourceContext.getDocumentType();
        parentBusinessControl = parentResourceContext.getBusinessControl();
        filePath = parentResourceContext.getFilePath();
        parentContextType = parentResourceContext.parentContextType;
        parentContextName = parentResourceContext.getParentContextName();
    }

    public String getFilePath() {
        return filePath;
    }

    public BusinessControl getBusinessControl() {
        return myBusinessControl;
    }

    /**
     * @return Returns the resourcePath.
     */
    public String getResourceUrl() {
        String resourceUrl = BusinessControlFactory.getInstance().getAsString(myBusinessControl);
        if (filePath != null) {
            // It is a file resource => Append file path
            final StringBuilder buf = new StringBuilder(resourceUrl);
            buf.append(FILEPATH_PREFIX);
            buf.append(filePath);
            buf.append(ENDTAG);
            resourceUrl = buf.toString();
        }
        return resourceUrl;
    }

    /**
     * @param olatResource
     */
    public void setBusinessControlFor(final OLATResourceable olatResource) {
        final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(olatResource);
        myBusinessControl = BusinessControlFactory.getInstance().createBusinessControl(ce, parentBusinessControl);
    }

    /**
     * Workaround for forum message. Forum-Message is currently no OLATResourcable.<br>
     * ResourceUrl-Format for Forum-Message :<br>
     * forum:<FORUM-ID>:message:<MESSAGE-ID>
     * 
     * @param message
     */
    public void setBusinessControlFor(final Message message) {
        setBusinessControlFor(OresHelper.createOLATResourceableInstance(Message.class, message.getKey()));
    }

    /**
     * Set BusinessControl for certain CourseNode.
     * 
     * @param courseNode
     */
    public void setBusinessControlFor(final CourseNode courseNode) {
        if (log.isDebugEnabled()) {
            log.debug("Course-node-ID=" + courseNode.getIdent());
        }
        setBusinessControlFor(OresHelper.createOLATResourceableInstance(CourseNode.class, new Long(courseNode.getIdent())));
    }

    /**
     * Pass lastModified parameter from parent to child.
     */
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Pass createdDate parameter from parent to child.
     */
    public void setCreatedDate(final Date creationDate) {
        this.createdDate = creationDate;
    }

    /**
     * Pass lastModified parameter from parent to child.
     * 
     * @return Returns the creationDate.
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Pass lastModified parameter from parent to child.
     * 
     * @return Returns the lastModified.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Pass filePath parameter from parent to child.
     */
    public void setFilePath(final String myFilePath) {
        this.filePath = myFilePath;
    }

    /**
     * Pass documentType parameter from parent to child.
     */
    public void setDocumentType(final String documentType) {
        this.documentType = documentType;
    }

    /**
     * Pass documentType parameter from parent to child.
     * 
     * @return
     */
    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentContext() {
        return documentContext;
    }

    public void setDocumentContext(final String documentContext) {
        this.documentContext = documentContext;
    }

    /**
     * Pass title parameter from parent to child.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Pass description parameter from parent to child.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Pass title parameter from parent to child.
     * 
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Pass description parameter from parent to child.
     * 
     * @return
     */
    public String getDescription() {
        return this.description;
    }

    public void setParentContextType(final String parentContextType) {
        this.parentContextType = parentContextType;
    }

    public void setParentContextName(final String parentContextName) {
        this.parentContextName = parentContextName;
    }

    public String getParentContextType() {
        return parentContextType;
    }

    public String getParentContextName() {
        return parentContextName;
    }

}
