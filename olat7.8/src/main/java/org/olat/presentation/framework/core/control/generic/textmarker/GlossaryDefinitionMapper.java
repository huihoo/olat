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

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.lms.commons.mediaresource.StringMediaResource;
import org.olat.lms.glossary.GlossaryDataObjectEBL;
import org.olat.lms.glossary.GlossaryEBL;
import org.olat.lms.glossary.GlossaryItem;
import org.olat.presentation.framework.dispatcher.mapper.Mapper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * delivers definition for a term as HTML ext autoloads content when hovering a highlighted term
 * <P>
 * Initial Date: 05.02.2009 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
class GlossaryDefinitionMapper implements Mapper {

    private static final Logger log = LoggerHelper.getLogger();

    /**
	 */
    @Override
    public MediaResource handle(String relPath, HttpServletRequest request) {

        GlossaryDataObjectEBL glossaryDataObjectEBL = getGlossaryEBL().processGlossaryDefinitionRelativePath(relPath);

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
        resource.setContentType("text/html");

        List<GlossaryItem> glossItems = glossaryDataObjectEBL.getGlossaryItems();
        String description = "<dd><dt>" + glossaryDataObjectEBL.getGlossaryMainTerm() + "</dt>";
        // FIXME: have a way not to loop over all items, but get by Term
        boolean foundADescription = false;
        for (Iterator<GlossaryItem> iterator = glossItems.iterator(); iterator.hasNext();) {
            GlossaryItem glossaryItem = iterator.next();
            if (glossaryItem.getGlossTerm().toLowerCase().equals(glossaryDataObjectEBL.getGlossaryMainTerm().toLowerCase())) {
                description += "<dl>" + glossaryItem.getGlossDef() + "</dl>";
                foundADescription = true;
                break;
            }
        }
        description += "</dd>";
        if (!foundADescription)
            return new NotFoundMediaResource(relPath);

        resource.setData(description);
        resource.setEncoding("utf-8");

        if (log.isDebugEnabled())
            log.debug("loaded definition for " + glossaryDataObjectEBL.getGlossaryMainTerm(), null);
        return resource;
    }

    private GlossaryEBL getGlossaryEBL() {
        return CoreSpringFactory.getBean(GlossaryEBL.class);
    }

}
