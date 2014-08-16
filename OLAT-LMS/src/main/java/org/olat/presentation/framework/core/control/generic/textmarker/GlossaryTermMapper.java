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
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.presentation.framework.core.control.generic.textmarker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.StringMediaResource;
import org.olat.lms.glossary.GlossaryDataObjectEBL;
import org.olat.lms.glossary.GlossaryEBL;
import org.olat.presentation.framework.dispatcher.mapper.Mapper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * delivers a js-file with all Terms as Arrays
 * <P>
 * Initial Date: 12.03.2009 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
class GlossaryTermMapper implements Mapper {

    private static final Logger log = LoggerHelper.getLogger();

    /**
	 */
    @Override
    public MediaResource handle(String relPath, HttpServletRequest request) {

        GlossaryDataObjectEBL glossaryDataObjectEBL = getGlossaryEBL().processGlossaryTermRelativePath(relPath);

        if (glossaryDataObjectEBL.isNotFoundMediaResource()) {
            return glossaryDataObjectEBL.getNotFoundMediaResource();
        }

        // Create a media resource
        StringMediaResource resource = new StringMediaResource() {
            @Override
            public void prepare(HttpServletResponse hres) {
                // don't use normal string media headers which prevent caching,
                // use standard browser caching based on last modified timestamp
            }
        };

        resource.setLastModified(glossaryDataObjectEBL.getLastModifiedTime());
        resource.setContentType("text/javascript");
        // Get data
        String glossaryArrayData = TextMarkerJsGenerator.loadGlossaryItemListAsJSArray(glossaryDataObjectEBL.getGlossaryFolder());

        resource.setData(glossaryArrayData);
        // UTF-8 encoding used in this js file since explicitly set in the ajax
        // call (usually js files are 8859-1)
        resource.setEncoding("utf-8");
        return resource;
    }

    private GlossaryEBL getGlossaryEBL() {
        return CoreSpringFactory.getBean(GlossaryEBL.class);
    }

}
