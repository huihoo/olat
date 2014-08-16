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

package org.olat.lms.commons.fileresource;

import java.io.File;

/**
 * Initial Date: Apr 6, 2004
 * 
 * @author Mike Stock
 */
public class TestFileResource extends FileResource {

    /**
     * IMS QTI Test file resource identifier.
     */
    public static final String TYPE_NAME = "FileResource.TEST";
    private static QTIFileResourceValidator validator;

    /**
     * Standard constructor.
     */
    protected TestFileResource() {
        super.setTypeName(TYPE_NAME);
    }

    /**
     * [SPRING]
     * 
     * @param validator
     */
    public void setValidator(final QTIFileResourceValidator validator) {
        TestFileResource.validator = validator;
    }

    /**
     * @param unzippedDir
     * @return True if is of type.
     */
    public static boolean validate(final File unzippedDir) {
        return TestFileResource.validator.validate(unzippedDir);
    }
}
