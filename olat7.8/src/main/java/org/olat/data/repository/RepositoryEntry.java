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

package org.olat.data.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.ModifiedInfo;
import org.olat.data.commons.database.PersistentObject;
import org.olat.data.resource.OLATResource;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;

/**
 * Represents a repository entry.
 */
public class RepositoryEntry extends PersistentObject implements ModifiedInfo, OLATResourceable {

    public static final int MAX_DISPLAYNAME_LENGTH = 140;
    public static final int MAX_RESOURCENAME_LENGTH = 140;
    // IMPORTANT: Keep relation ACC_OWNERS < ACC_OWNERS_AUTHORS < ACC_USERS < ACC_USERS_GUESTS
    /**
     * limit access to owners
     */
    public static final int ACC_OWNERS = 1; // limit access to owners
    /**
     * limit access to owners and authors
     */
    public static final int ACC_OWNERS_AUTHORS = 2; // limit access to owners and authors
    /**
     * limit access to owners, authors and users
     */
    public static final int ACC_USERS = 3; // limit access to owners, authors and users
    /**
     * no limits
     */
    public static final int ACC_USERS_GUESTS = 4; // no limits

    private String softkey; // mandatory
    private OLATResource olatResource; // mandatory
    private SecurityGroup ownerGroup; // mandatory
    private String resourcename; // mandatory
    private String displayname; // mandatory
    private String description; // mandatory
    private String initialAuthor; // mandatory // login of the author of the first version
    private int access;
    private boolean canCopy;
    private boolean canReference;
    private boolean canLaunch;
    private boolean canDownload;
    private int statusCode;
    private List<MetaDataElement> metaDataElements;
    private long launchCounter;
    private long downloadCounter;
    private Date lastUsage;
    private int version;
    private Date lastModified;

    @Override
    public String toString() {
        return super.toString() + " [resourcename=" + resourcename + ", version=" + version + ", description=" + description + "]";
    }

    /**
     * Default constructor.
     */
    RepositoryEntry() {
        softkey = CodeHelper.getGlobalForeverUniqueID();
        metaDataElements = new ArrayList<MetaDataElement>();
        access = ACC_OWNERS;
    }

    /**
     * @return The softkey associated with this repository entry.
     */
    public String getSoftkey() {
        return softkey;
    }

    /**
     * Set the softkey of this repository entry.
     * 
     * @param softkey
     */
    public void setSoftkey(final String softkey) {
        if (softkey.length() > 30) {
            throw new AssertException("Trying to set a softkey which is too long...");
        }
        this.softkey = softkey;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @return description as HTML snippet
     */
    public String getFormattedDescription() {
        final String descr = Formatter.formatLatexFormulas(getDescription());
        return descr;
    }

    /**
     * @return Returns the initialAuthor.
     */
    public String getInitialAuthor() {
        return initialAuthor;
    }

    /**
     * @param initialAuthor
     *            The initialAuthor to set.
     */
    public void setInitialAuthor(String initialAuthor) {
        if (initialAuthor == null) {
            initialAuthor = "";
        }
        if (initialAuthor.length() > IdentityImpl.NAME_MAXLENGTH) {
            throw new AssertException("initialAuthor is limited to " + IdentityImpl.NAME_MAXLENGTH + " characters.");
        }
        this.initialAuthor = initialAuthor;
    }

    /**
     * @return Returns the metaDataElements.
     */
    public List<MetaDataElement> getMetaDataElements() {
        return metaDataElements;
    }

    /**
     * @param metaDataElements
     *            The metaDataElements to set.
     */
    public void setMetaDataElements(final List<MetaDataElement> metaDataElements) {
        this.metaDataElements = metaDataElements;
    }

    /**
     * @return Returns the statusCode.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode
     *            The statusCode to set.
     */
    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return Returns the name.
     */
    public String getResourcename() {
        return resourcename;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setResourcename(final String name) {
        if (name.length() > MAX_RESOURCENAME_LENGTH) {
            throw new AssertException("Resourcename is limited to " + MAX_RESOURCENAME_LENGTH + " characters.");
        }
        this.resourcename = name;
    }

    /**
     * @return Returns the olatResource.
     */
    public OLATResource getOlatResource() {
        return olatResource;
    }

    /**
     * @param olatResource
     *            The olatResource to set.
     */
    public void setOlatResource(final OLATResource olatResource) {
        this.olatResource = olatResource;
    }

    /**
     * @return Grou of owners of this repo entry.
     */
    public SecurityGroup getOwnerGroup() {
        return ownerGroup;
    }

    /**
     * Set the group of owners of this repo entry.
     * 
     * @param ownerGroup
     */
    public void setOwnerGroup(final SecurityGroup ownerGroup) {
        this.ownerGroup = ownerGroup;
    }

    /**
     * @return Wether this repo entry can be copied.
     */
    public boolean getCanCopy() {
        return canCopy;
    }

    /**
     * @return Wether this repo entry can be referenced by other people.
     */
    public boolean getCanReference() {
        return canReference;
    }

    /**
     * @return Wether this repo entry can be downloaded.
     */
    public boolean getCanDownload() {
        return canDownload;
    }

    /**
     * @return Wether this repo entry can be launched.
     */
    public boolean getCanLaunch() {
        return canLaunch;
    }

    /**
     * @return Access restrictions.
     */
    public int getAccess() {
        return access;
    }

    /**
     * @param b
     */
    public void setCanCopy(final boolean b) {
        canCopy = b;
    }

    /**
     * @param b
     */
    public void setCanReference(final boolean b) {
        canReference = b;
    }

    /**
     * @param b
     */
    public void setCanDownload(final boolean b) {
        canDownload = b;
    }

    /**
     * @param b
     */
    public void setCanLaunch(final boolean b) {
        canLaunch = b;
    }

    /**
     * Set access restrictions.
     * 
     * @param i
     */
    public void setAccess(final int i) {
        access = i;
    }

    /**
     * @return Download count for this repo entry.
     */
    public long getDownloadCounter() {
        return downloadCounter;
    }

    /**
     * @return Launch count for this repo entry.
     */
    public long getLaunchCounter() {
        return launchCounter;
    }

    /**
     * @param l
     */
    public void setDownloadCounter(final long l) {
        downloadCounter = l;
    }

    /**
     * @param l
     */
    public void setLaunchCounter(final long l) {
        launchCounter = l;
    }

    /**
     * Increment launch counter.
     */
    public void incrementLaunchCounter() {
        launchCounter++;
    }

    /**
     * Increment download counter.
     */
    public void incrementDownloadCounter() {
        downloadCounter++;
    }

    /**
     * @return Returns the displayname.
     */
    public String getDisplayname() {
        return displayname;
    }

    /**
     * @param displayname
     *            The displayname to set.
     */
    public void setDisplayname(final String displayname) {
        if (displayname.length() > MAX_DISPLAYNAME_LENGTH) {
            throw new AssertException("DisplayName is limited to " + MAX_DISPLAYNAME_LENGTH + " characters.");
        }
        this.displayname = displayname;
    }

    /**
	 */
    @Override
    public String getResourceableTypeName() {
        return OresHelper.calculateTypeName(RepositoryEntry.class);
    }

    /**
	 */
    @Override
    public Long getResourceableId() {
        return getKey();
    }

    /**
     * @return Returns the lastUsage.
     */
    public Date getLastUsage() {
        return lastUsage;
    }

    /**
     * @param lastUsage
     *            The lastUsage to set.
     */
    public void setLastUsage(final Date lastUsage) {
        this.lastUsage = lastUsage;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(final int v) {
        version = v;
    }

    /**
	 */
    @Override
    public Date getLastModified() {
        return lastModified;
    }

    /**
	 */
    @Override
    public void setLastModified(final Date date) {
        this.lastModified = date;
    }
}
