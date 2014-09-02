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

import java.util.List;

import org.olat.data.course.campus.CampusDao;
import org.springframework.batch.item.ItemWriter;

/**
 * This class is a generic {@link ItemWriter} that writes data to the database. <br>
 * It delegates the actual writing (save or update) of data to the database to a <br>
 * concrete implementation of {@link CampuskursDao}.<br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusWriter<T> implements ItemWriter<T> {
    private CampusDao<T> campuskursDao;

    /**
     * Sets the CampusDao to be used to save or update the items in {@link #write(List)}.
     * 
     * @param campuskursDao
     *            the CampusDao to set
     */
    public void setCampuskursDao(CampusDao<T> campuskursDao) {
        this.campuskursDao = campuskursDao;
    }

    /**
     * Returns the CampuskursDao
     */
    public CampusDao<T> getCampuskursDao() {
        return campuskursDao;
    }

    /**
     * Delegates the actual saving or updating of the given list of items to the <br>
     * concrete implementation of {@link CampuskursDao}
     * 
     * @param items
     *            the items to send
     * 
     * @see ItemWriter#write(List)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void write(List<? extends T> items) throws Exception {
        campuskursDao.saveOrUpdate((List) items);
    }

}
