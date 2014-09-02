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

package org.olat.connectors.rest.support.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Description:<br>
 * TODO: srosse Class Description for CourseNodeVO
 * <P>
 * Initial Date: 20 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "courseNodeVO")
public class CourseNodeVO {

    private String id;
    private Integer position;
    private String parentId;

    private String shortTitle;
    private String shortName;
    private String longTitle;
    private String learningObjectives;

    public CourseNodeVO() {
        // make JAXB happy
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(final String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(final String shortName) {
        this.shortName = shortName;
    }

    public String getLongTitle() {
        return longTitle;
    }

    public void setLongTitle(final String longTitle) {
        this.longTitle = longTitle;
    }

    public String getLearningObjectives() {
        return learningObjectives;
    }

    public void setLearningObjectives(final String learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CourseNodeVO) {
            final CourseNodeVO vo = (CourseNodeVO) obj;
            return id != null && id.equals(vo.getId());
        }
        return false;
    }
}
