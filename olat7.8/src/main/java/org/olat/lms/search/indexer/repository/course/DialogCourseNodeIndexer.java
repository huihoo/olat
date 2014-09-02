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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSLeafFilter;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.dialogelements.DialogElement;
import org.olat.lms.dialogelements.DialogElementsPropertyManager;
import org.olat.lms.dialogelements.DialogPropertyElements;
import org.olat.lms.forum.ForumService;
import org.olat.lms.forum.Status;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.document.file.FileDocumentFactory;
import org.olat.lms.search.indexer.ForumIndexerHelper;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Indexer for dialog course-node.
 * 
 * @author Christian Guretzki
 */
public class DialogCourseNodeIndexer extends CourseNodeIndexer {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    private final static String TYPE_MESSAGE = "type.course.node.dialog.forum.message";

    private final static String TYPE_FILE = "type.course.node.dialog.file";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.DialogCourseNode";

    private static final Logger log = LoggerHelper.getLogger();

    private final DialogElementsPropertyManager dialogElmsMgr;

    public DialogCourseNodeIndexer() {
        dialogElmsMgr = DialogElementsPropertyManager.getInstance();
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean(ForumService.class);

    }

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException, DocumentException {

        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        courseNodeResourceContext.setDocumentType(TYPE_MESSAGE);
        final CoursePropertyManager coursePropMgr = course.getCourseEnvironment().getCoursePropertyManager();
        final DialogPropertyElements elements = dialogElmsMgr.findDialogElements(coursePropMgr, courseNode);
        List<DialogElement> list = new ArrayList<DialogElement>();
        if (elements != null) {
            list = elements.getDialogPropertyElements();
        }
        // loop over all dialog elements
        for (final Iterator<DialogElement> iter = list.iterator(); iter.hasNext();) {
            final DialogElement element = iter.next();
            element.getAuthor();
            element.getDate();
            final Forum forum = getForumService().loadForum(element.getForumKey());
            // do IndexForum
            ForumIndexerHelper.doIndexAllMessages(courseNodeResourceContext, forum, indexWriter, true);
            // do Index File
            doIndexFile(element.getFilename(), element.getForumKey(), courseNodeResourceContext, indexWriter);
        }
    }

    /**
     * Index a file of dialog-module.
     * 
     * @param filename
     * @param forumKey
     * @param leafResourceContext
     * @param indexWriter
     * @throws IOException
     * @throws DocumentException
     * @throws InterruptedException
     */
    private void doIndexFile(final String filename, final Long forumKey, final SearchResourceContext leafResourceContext, final OlatFullIndexer indexWriter)
            throws IOException, DocumentException {
        final VFSContainer forumContainer = getForumService().getForumContainer(forumKey);
        final VFSLeaf leaf = (VFSLeaf) forumContainer.getItems(new VFSLeafFilter()).get(0);
        if (log.isDebugEnabled()) {
            log.debug("Analyse VFSLeaf=" + leaf.getName());
        }
        if (SearchServiceFactory.getFileDocumentFactory().isFileSupported(leaf)) {
            leafResourceContext.setFilePath(filename);
            leafResourceContext.setDocumentType(TYPE_FILE);
            final Document document = FileDocumentFactory.createDocument(leafResourceContext, leaf);
            if (document != null) {
                indexWriter.addDocument(document);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Documenttype not supported. file=" + leaf.getName());
            }
        }
    }

    @Override
    public String getDocumentTypeName() {
        return TYPE_FILE;
    }

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public boolean checkAccess(ContextEntry courseNodeContextEntry, BusinessControl businessControl, Identity identity, Roles roles, boolean isCourseOwner) {
        final ContextEntry messageContextEntry = businessControl.popLauncherContextEntry();
        // we have no nested (message) context => apply access rules for course node
        if (messageContextEntry == null) {
            return true;
        }

        final OLATResourceable ores = messageContextEntry.getOLATResourceable();
        if (log.isDebugEnabled()) {
            log.debug("OLATResourceable=" + ores);
        }
        if ((ores != null) && (ores.getResourceableTypeName().startsWith("path="))) {
            // => it is a file element, typeName format: 'path=/test1/test2/readme.txt'
            return true;
        } else if ((ores != null) && ores.getResourceableTypeName().equals(OresHelper.calculateTypeName(Message.class))) {
            // it is message => check message access
            final Long resourceableId = ores.getResourceableId();
            final Message message = getForumService().loadMessage(resourceableId);
            Message threadtop = message.getThreadtop();
            if (threadtop == null) {
                threadtop = message;
            }
            final boolean isMessageHidden = Status.getStatus(threadtop.getStatusCode()).isHidden();
            // assumes that if is owner then is moderator so it is allowed to see the hidden forum threads
            if (isMessageHidden) {
                return isCourseOwner;
            }
            return true;
        } else {
            log.warn("In DialogCourseNode unknown OLATResourceable=" + ores);
            return false;
        }
    }

}
