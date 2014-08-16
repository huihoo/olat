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
package org.olat.connectors.rest.support;

import javax.ws.rs.core.EntityTag;

import org.olat.connectors.rest.support.vo.AuthenticationVO;
import org.olat.connectors.rest.support.vo.CourseConfigVO;
import org.olat.connectors.rest.support.vo.CourseNodeVO;
import org.olat.connectors.rest.support.vo.CourseVO;
import org.olat.connectors.rest.support.vo.ErrorVO;
import org.olat.connectors.rest.support.vo.GroupVO;
import org.olat.connectors.rest.support.vo.RepositoryEntryVO;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.system.commons.resource.OresHelper;

/**
 * Description:<br>
 * Factory for object needed by the REST Api
 * <P>
 * Initial Date: 7 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class ObjectFactory {

    public static GroupVO get(final BusinessGroup grp) {
        final GroupVO vo = new GroupVO();
        vo.setKey(grp.getKey());
        vo.setName(grp.getName());
        vo.setDescription(grp.getDescription());
        vo.setMaxParticipants(grp.getMaxParticipants());
        vo.setMinParticipants(grp.getMinParticipants());
        vo.setType(grp.getType());
        return vo;
    }

    public static AuthenticationVO get(final Authentication authentication, final boolean withCred) {
        final AuthenticationVO vo = new AuthenticationVO();
        vo.setKey(authentication.getKey());
        vo.setIdentityKey(authentication.getIdentity().getKey());
        vo.setAuthUsername(authentication.getAuthusername());
        vo.setProvider(authentication.getProvider());
        if (withCred) {
            vo.setCredential(authentication.getCredential());
        }
        return vo;
    }

    public static RepositoryEntryVO get(final RepositoryEntry entry) {
        final RepositoryEntryVO vo = new RepositoryEntryVO();
        vo.setKey(entry.getKey());
        vo.setSoftkey(entry.getSoftkey());
        vo.setResourcename(entry.getResourcename());
        vo.setDisplayname(entry.getDisplayname());
        vo.setResourceableId(entry.getResourceableId());
        vo.setResourceableTypeName(entry.getResourceableTypeName());
        return vo;
    }

    public static CourseVO get(final ICourse course) {
        final CourseVO vo = new CourseVO();
        vo.setKey(course.getResourceableId());
        final String typeName = OresHelper.calculateTypeName(CourseModule.class);
        final OLATResource ores = OLATResourceManager.getInstance().findResourceable(course.getResourceableId(), typeName);
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(ores, false);
        vo.setSoftKey(re.getSoftkey());
        vo.setRepoEntryKey(re.getKey());
        vo.setTitle(course.getCourseTitle());
        vo.setEditorRootNodeId(course.getEditorTreeModel().getRootNode().getIdent());
        return vo;
    }

    public static CourseConfigVO getConfig(final ICourse course) {
        final CourseConfigVO vo = new CourseConfigVO();
        final CourseConfig config = course.getCourseEnvironment().getCourseConfig();
        vo.setSharedFolderSoftKey(config.getSharedFolderSoftkey());
        return vo;
    }

    public static CourseNodeVO get(final CourseNode node) {
        final CourseNodeVO vo = new CourseNodeVO();
        vo.setId(node.getIdent());
        vo.setPosition(node.getPosition());
        vo.setParentId(node.getParent() == null ? null : node.getParent().getIdent());

        vo.setShortTitle(node.getShortTitle());
        vo.setShortName(node.getShortName());
        vo.setLongTitle(node.getLongTitle());
        vo.setLearningObjectives(node.getLearningObjectives());

        return vo;
    }

    public static EntityTag computeEtag(final RepositoryEntry re) {
        final int version = re.getVersion();
        final Long key = re.getKey();
        return new EntityTag("RepositoryEntry-" + key + "-" + version);
    }

    public static ErrorVO get(final String pack, final String key, final String translation) {
        final ErrorVO vo = new ErrorVO();
        vo.setCode(pack + ":" + key);
        vo.setTranslation(translation);
        return vo;
    }

    public static ErrorVO get(final ValidationError error) {
        final ErrorVO vo = new ErrorVO();
        vo.setCode("unkown" + ":" + error.getErrorKey());
        vo.setTranslation("Hello");
        return vo;
    }
}
