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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.data.infomessage;

import java.util.Date;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.PersistentObject;
import org.olat.system.commons.resource.OLATResourceable;

public class InfoMessageImpl extends PersistentObject implements InfoMessage {

    private Date modificationDate;

    private String title;
    private String message;

    private Long resId;
    private String resName;
    private String subPath;
    private String businessPath;

    private Identity author;
    private Identity modifier;

    public InfoMessageImpl() {
        //
    }

    @Override
    public Date getModificationDate() {
        return modificationDate;
    }

    @Override
    public void setModificationDate(final Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public Long getResId() {
        return resId;
    }

    public void setResId(final Long resId) {
        this.resId = resId;
    }

    @Override
    public String getResName() {
        return resName;
    }

    public void setResName(final String resName) {
        this.resName = resName;
    }

    @Override
    public String getResSubPath() {
        return subPath;
    }

    public void setResSubPath(final String subPath) {
        this.subPath = subPath;
    }

    @Override
    public String getBusinessPath() {
        return businessPath;
    }

    public void setBusinessPath(final String businessPath) {
        this.businessPath = businessPath;
    }

    @Override
    public Identity getAuthor() {
        return author;
    }

    public void setAuthor(final Identity author) {
        this.author = author;
    }

    @Override
    public Identity getModifier() {
        return modifier;
    }

    @Override
    public void setModifier(final Identity modifier) {
        this.modifier = modifier;
    }

    @Override
    public OLATResourceable getOLATResourceable() {
        final String name = resName;
        final Long id = resId;
        return new OLATResourceable() {
            @Override
            public String getResourceableTypeName() {
                return name;
            }

            @Override
            public Long getResourceableId() {
                return id;
            }
        };
    }
}
