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
package org.olat.lms.framework.core.media.fileresource;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.ims.qti.process.QTIHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 05.10.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
@Scope("prototype")
final public class FileDetailsFormEBL {

    private final OLATResourceable resourceable;
    private File file;
    private Document document;
    @Autowired
    BaseSecurity baseSecurityManager;
    @Autowired
    FileResourceManager fileresourceManager;

    FileDetailsFormEBL(OLATResourceable resourceable) {
        this.resourceable = resourceable;
    }

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {
        file = fileresourceManager.getFileResource(resourceable);
    }

    public String getFileSizeLabel() {
        return Long.valueOf(file.length() / 1024).toString() + " KB";
    }

    public String getFileLastModifiedLable(Locale locale) {
        final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        return df.format(new Date(file.lastModified()));
    }

    public void buildDocument() {
        final File unzippedRoot = fileresourceManager.unzipFileResource(resourceable);
        // with VFS FIXME:pb:c: remove casts to LocalFileImpl and LocalFolderImpl if no longer needed.
        final VFSContainer vfsUnzippedRoot = new LocalFolderImpl(unzippedRoot);
        final VFSItem vfsQTI = vfsUnzippedRoot.resolve("qti.xml");
        // getDocument(..) ensures that InputStream is closed in every case.
        document = QTIHelper.getDocument((LocalFileImpl) vfsQTI);
    }

    public String extractTitleFromDocument() {
        if (isNullDocument()) {
            return "";
        }
        final Element el_assess = (Element) document.selectSingleNode("questestinterop/assessment");
        final String title = el_assess.attributeValue("title");
        return title == null ? "-" : title;
    }

    public String extractObjectivesFromDocument() {
        if (isNullDocument()) {
            return "";
        }
        String objectives = "-";
        final Element el_objectives = (Element) document.selectSingleNode("//questestinterop/assessment/objectives");
        if (el_objectives != null) {
            final Element el_mat = (Element) el_objectives.selectSingleNode("material/mattext");
            if (el_mat != null) {
                objectives = el_mat.getTextTrim();
            }
        }
        return objectives;
    }

    public String extractNumberOfQuestionsFromDocument() {
        if (isNullDocument()) {
            return "";
        }
        @SuppressWarnings("rawtypes")
        final List items = document.selectNodes("//item");
        return items.size() > 0 ? "" + items.size() : "-";
    }

    public String extractTimeLimitFromDocument() {
        if (isNullDocument()) {
            return "";
        }
        String timeLimit = "-";
        final Element el_assess = (Element) document.selectSingleNode("questestinterop/assessment");
        final Element el_duration = (Element) el_assess.selectSingleNode("duration");
        if (el_duration != null) {
            final long dur = QTIHelper.parseISODuration(el_duration.getTextTrim());
            final long min = dur / 1024 / 60;
            final long sec = (dur - (min * 60 * 1024)) / 1024;
            timeLimit = "" + min + "' " + sec + "''";
        }
        return timeLimit;
    }

    public boolean isNullDocument() {
        return document == null ? true : false;
    }

    public boolean isNotNullDocument() {
        return !isNullDocument();
    }

}
