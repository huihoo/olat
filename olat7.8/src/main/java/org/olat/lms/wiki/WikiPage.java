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

package org.olat.lms.wiki;

import org.apache.log4j.Logger;
import org.olat.presentation.wiki.WikiXSSProcessor;
import org.olat.presentation.wiki.wikitohtml.FilterUtil;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Data object for a single wiki page with metadata.
 * <P>
 * Initial Date: May 7, 2006 <br>
 * 
 * @author guido
 */
public class WikiPage {
    public static final String WIKI_INDEX_PAGE = "Index";
    public static final String WIKI_MENU_PAGE = "Menu";
    public static final String WIKI_RECENT_CHANGES_PAGE = "O_recent_changes";
    public static final String WIKI_A2Z_PAGE = "O_a_to_z";
    public static final String WIKI_ERROR = "O_error";
    private static final Logger log = LoggerHelper.getLogger();
    private String content = "";
    private final String pageName;
    private String pageId;
    private long forumKey = 0;
    private boolean dirty = false;
    private int version = 0;
    private String updateComment = "";
    private long initalAuthor = 0;
    private long modifyAuthor = 0;
    private long modificationTime = 0;
    private long creationTime;
    // TODO:gs viewCout does not work properly as it would save the property file
    // on each click which would invalidate
    // the cache for each page view which is nonsense.
    // remove or find better solution which works also in a cluster cache scenario
    private int viewCount = 0;

    /**
     * @param id
     * @param name
     */
    public WikiPage(final String name) {
        this.pageName = FilterUtil.normalizeWikiLink(name);
        this.pageId = WikiManager.generatePageId(pageName);
    }

    public String getContent() {
        return WikiXSSProcessor.getFilteredWikiContent(content);
    }

    public void setContent(final String content) {
        if (content == null) {
            log.error("I-130606-0080: set content==null for WikiPage[name=" + pageName + ", id=" + pageId + "]");
        }
        this.content = content;
        dirty = true;
    }

    public long getForumKey() {
        return forumKey;
    }

    public void setForumKey(final String forumKey) {
        this.forumKey = Long.parseLong(forumKey);
    }

    public void setForumKey(final long forumKey) {
        this.forumKey = forumKey;
    }

    /**
     * used by velocity
     * 
     * @return the pageName
     */
    public String getPageName() {
        return pageName;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(final boolean b) {
        this.dirty = b;
    }

    /**
     * @return a calculated one way hash used for persisting the pages to the filesystem.
     */
    public String getPageId() {
        if (pageId == null) {
            pageId = WikiManager.generatePageId(pageName);
        }
        return pageId;
    }

    /**
     * @return
     */
    public int getVersion() {
        return this.version;
    }

    protected void incrementVersion() {
        this.version++;
    }

    public long getInitalAuthor() {
        return initalAuthor;
    }

    public void setInitalAuthor(final String initalAuthor) {
        this.initalAuthor = Long.parseLong(initalAuthor);
    }

    public void setInitalAuthor(final long initalAuthor) {
        this.initalAuthor = initalAuthor;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(final String modificationTime) {
        this.modificationTime = Long.parseLong(modificationTime);
    }

    public void setModificationTime(final long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public long getModifyAuthor() {
        return modifyAuthor;
    }

    public void setModifyAuthor(final String modifyAuthor) {
        this.modifyAuthor = Long.parseLong(modifyAuthor);
    }

    public void setModifyAuthor(final long modifyAuthor) {
        this.modifyAuthor = modifyAuthor;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void setViewCount(final String viewCount) {
        this.viewCount = Integer.parseInt(viewCount);
    }

    public void setViewCount(final int viewCount) {
        this.viewCount = viewCount;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final String creationTime) {
        this.creationTime = Long.parseLong(creationTime);
    }

    public void setCreationTime(final long creationTime) {
        this.creationTime = creationTime;
    }

    public void setVersion(final String version) {
        this.version = Integer.parseInt(version);
    }

    public String getUpdateComment() {
        return updateComment;
    }

    public void setUpdateComment(final String updateComment) {
        this.updateComment = updateComment;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("PageName: ").append(this.pageName).append("\n");
        sb.append("pageId: ").append(this.pageId).append("\n");
        sb.append("forumKey: ").append(this.forumKey).append("\n");
        sb.append("version: ").append(this.version).append("\n");
        sb.append("modifyAuthor: ").append(this.modifyAuthor).append("\n");
        sb.append("modificationTime: ").append(this.modificationTime).append("\n");
        sb.append("creationTime: ").append(this.creationTime).append("\n");
        sb.append("viewCount: ").append(this.viewCount).append("\n");
        sb.append("up-comment: ").append(this.updateComment).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    public void resetCopiedPage() {
        this.version = 0;
        this.modificationTime = 0;
        this.forumKey = 0;
        this.modifyAuthor = 0;
        this.updateComment = "";
        this.viewCount = 0;
    }
}
