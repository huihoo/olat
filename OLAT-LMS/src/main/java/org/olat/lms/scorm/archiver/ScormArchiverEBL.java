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
package org.olat.lms.scorm.archiver;

import java.io.File;

import javax.annotation.PostConstruct;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.MimedFileMediaResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 17.10.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
@Scope("prototype")
final public class ScormArchiverEBL {

    private static final String EXCEL_MIME_TYPE = "application/vnd.ms-excel; charset=";
    private final String exportDirPath;
    private String charset;
    private final Identity identity;

    @Autowired
    UserService userService;

    ScormArchiverEBL(Identity identity, String courseTitle) {
        this.identity = identity;
        exportDirPath = CourseFactory.getOrCreateDataExportDirectory(this.identity, courseTitle).getPath();
    }

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {
        charset = userService.getUserCharset(this.identity);
    }

    public MediaResource getScormArchiverMediaResource(String targetFileName) {
        final File exportFile = new File(exportDirPath, targetFileName);
        final MediaResource resource = new MimedFileMediaResource(exportFile, EXCEL_MIME_TYPE + charset, true);
        return resource;
    }

    public String getExportDirPath() {
        return exportDirPath;
    }

    public String getCharset() {
        return charset;
    }

}
