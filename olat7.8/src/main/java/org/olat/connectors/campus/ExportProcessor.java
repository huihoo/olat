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
package org.olat.connectors.campus;

import java.util.Date;

import org.olat.data.course.campus.Export;
import org.olat.lms.core.course.campus.CampusConfiguration;
import org.olat.system.commons.date.DateUtil;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is an implementation of {@link ItemProcessor} that validates the input Export item, <br>
 * modifies it according to some criteria and returns it as output Export item. <br>
 * 
 * Initial Date: 23.06.2012 <br>
 * 
 * @author aabouc
 */
public class ExportProcessor implements ItemProcessor<Export, Export> {

    @Autowired
    CampusConfiguration campusConfiguration;

    /**
     * Checks the export and returns it modified
     * 
     * @param export
     *            the Export to be processed
     * 
     * @return the modified export
     * 
     * @throws CampusException
     *             if the exportDate is older than one day
     */
    public Export process(Export export) throws Exception {
        if (!export.getFileName().contains(campusConfiguration.getSapFilesSuffix())) {
            return null;
        }
        if (DateUtil.isMoreThanOneDayBefore(export.getExportDate())) {
            throw new CampusException("THE FILE [" + export.getFileName() + "] WONT BE IMPORTED BECAUSE OF THE OLD EXPORT DATE [" + export.getExportDate() + "]");
        }
        export.setCreationDate(new Date());
        return export;
    }

}
