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

package org.olat.lms.search.indexer.repository.course;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.infomessage.InfoMessage;
import org.olat.data.infomessage.InfoMessageDao;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.InfoMessageDocument;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * <P>
 * Initial Date: 29 juil. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoCourseNodeIndexer extends CourseNodeIndexer {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    private final static String TYPE = "type.course.node.info.message";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.InfoCourseNode";

    @Autowired
    private InfoMessageDao infoMessageManager;

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException {
        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        courseNodeResourceContext.setDocumentType(TYPE);
        doIndexInfos(courseNodeResourceContext, course, courseNode, indexWriter);
    }

    @Override
    public String getDocumentTypeName() {
        return TYPE;
    }

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public boolean checkAccess(ContextEntry courseNodeContextEntry, BusinessControl businessControl, Identity identity, Roles roles, boolean isCourseOwner) {
        return true;
    }

    private void doIndexInfos(final SearchResourceContext parentResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException {
        final List<InfoMessage> messages = infoMessageManager.loadInfoMessageByResource(course, courseNode.getIdent(), null, null, null, 0, -1);
        for (final InfoMessage message : messages) {
            final SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(InfoMessage.class, message.getKey());
            searchResourceContext.setBusinessControlFor(ores);
            searchResourceContext.setDocumentContext(parentResourceContext.getDocumentContext() + " " + message.getKey());
            final Document document = InfoMessageDocument.createDocument(searchResourceContext, message);
            indexWriter.addDocument(document);
        }
    }
}
