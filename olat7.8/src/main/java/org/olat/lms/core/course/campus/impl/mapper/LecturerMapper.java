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
package org.olat.lms.core.course.campus.impl.mapper;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.Lecturer;
import org.olat.data.course.campus.SapOlatUserDao;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class LecturerMapper {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    LecturerMappingByPersonalNumber mappingByPersonalNumber;
    @Autowired
    MappingByFirstNameAndLastName mappingByFirstNameAndLastName;
    @Autowired
    MappingByEmail mappingByEmail;
    @Autowired
    SapOlatUserDao userMappingDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MappingResult synchronizeLecturerMapping(Lecturer lecturer) {
        if (!userMappingDao.existsMappingForSapUserId(lecturer.getPersonalNr())) {
            // first try to map by personal number
            Identity mappedIdentity = mappingByPersonalNumber.tryToMap(lecturer.getPersonalNr());
            if (mappedIdentity != null) {
                userMappingDao.saveMapping(lecturer, mappedIdentity);
                return MappingResult.NEW_MAPPING_BY_PERSONAL_NR;
            }
            // second try to map by Email
            mappedIdentity = mappingByEmail.tryToMap(lecturer);
            if (mappedIdentity != null) {
                userMappingDao.saveMapping(lecturer, mappedIdentity);
                return MappingResult.NEW_MAPPING_BY_EMAIL;
            }

            // third try to map by additional personal number
            if (StringUtils.isNotBlank(lecturer.getAdditionalPersonalNrs())) {
                List<String> personalNrs = Arrays.asList(lecturer.getAdditionalPersonalNrs().split("\\s*,\\s*"));
                for (String personalNr : personalNrs) {
                    mappedIdentity = mappingByPersonalNumber.tryToMap(Long.valueOf(personalNr));
                    if (mappedIdentity != null) {
                        userMappingDao.saveMapping(lecturer, mappedIdentity);
                        return MappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NR;
                    }
                }
            }

            // forth try to map by firstName and lastName
            mappedIdentity = mappingByFirstNameAndLastName.tryToMap(lecturer.getFirstName(), lecturer.getLastName());
            if (mappedIdentity != null) {
                // DO NOT SAVE THIS MAPPING, BECAUSE IT HAS TO BE DONE MANUALLY
                return MappingResult.COULD_BE_MAPPED_MANUALLY;
            } else {
                // log.info("Could not map lecturer:" + lecturer);
                return MappingResult.COULD_NOT_MAP;
            }
        }
        return MappingResult.MAPPING_ALREADY_EXIST;
    }

}
