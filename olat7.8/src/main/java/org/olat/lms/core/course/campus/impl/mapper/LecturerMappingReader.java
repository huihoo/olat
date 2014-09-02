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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.olat.data.course.campus.DaoManager;
import org.olat.data.course.campus.Lecturer;
import org.olat.system.commons.collections.ListUtil;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link ItemWriter} for
 * 
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
public class LecturerMappingReader implements ItemReader<Lecturer> {

    @Autowired
    DaoManager daoManager;

    private List<Lecturer> lecturers;

    @PostConstruct
    public void init() {
        daoManager.deleteOldLecturerMapping();
        lecturers = daoManager.getAllLecturers();
    }

    @PreDestroy
    public void destroy() {
        if (ListUtil.isNotBlank(lecturers)) {
            lecturers.clear();
        }
    }

    @Override
    public Lecturer read() throws Exception {
        if (ListUtil.isNotBlank(lecturers)) {
            return lecturers.remove(0);
        }
        return null;
    }

}
