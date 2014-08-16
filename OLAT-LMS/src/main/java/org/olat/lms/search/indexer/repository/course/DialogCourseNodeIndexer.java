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
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSLeafFilter;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
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
import org.olat.lms.search.document.ForumMessageDocument;
import org.olat.lms.search.document.file.DocumentAccessException;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.document.file.DocumentNotImplementedException;
import org.olat.lms.search.document.file.FileDocumentFactory;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.repository.CourseIndexer;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Indexer for dialog course-node.
 * 
 * @author Christian Guretzki
 */
public class DialogCourseNodeIndexer implements CourseNodeIndexer {
    private static final Logger log = LoggerHelper.getLogger();

    // Must correspond with LocalString_xx.properties
    // Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_'
    public final static String TYPE_MESSAGE = "type.course.node.dialog.forum.message";
    public final static String TYPE_FILE = "type.course.node.dialog.file";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.DialogCourseNode";

    private final DialogElementsPropertyManager dialogElmsMgr;

    private final CourseIndexer courseNodeIndexer;

    public DialogCourseNodeIndexer() {
        dialogElmsMgr = DialogElementsPropertyManager.getInstance();
        courseNodeIndexer = new CourseIndexer();
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean(ForumService.class);

    }

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException, InterruptedException {
        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        courseNodeResourceContext.setTitle(courseNode.getShortTitle());
        courseNodeResourceContext.setDescription(courseNode.getLongTitle());

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
            doIndexAllMessages(courseNodeResourceContext, forum, indexWriter);
            // do Index File
            doIndexFile(element.getFilename(), element.getForumKey(), courseNodeResourceContext, indexWriter);
        }

        // go further, index my child nodes
        courseNodeIndexer.doIndexCourse(repositoryResourceContext, course, courseNode, indexWriter);
    }

    /**
     * Index a file of dialog-module.
     * 
     * @param filename
     * @param forumKey
     * @param leafResourceContext
     * @param indexWriter
     * @throws IOException
     * @throws InterruptedException
     */
    private void doIndexFile(final String filename, final Long forumKey, final SearchResourceContext leafResourceContext, final OlatFullIndexer indexWriter)
            throws IOException, InterruptedException {
        final OlatRootFolderImpl forumContainer = getForumService().getForumContainer(forumKey);
        final VFSLeaf leaf = (VFSLeaf) forumContainer.getItems(new VFSLeafFilter()).get(0);
        if (log.isDebugEnabled()) {
            log.debug("Analyse VFSLeaf=" + leaf.getName());
        }
        try {
            if (SearchServiceFactory.getFileDocumentFactory().isFileSupported(leaf)) {
                leafResourceContext.setFilePath(filename);
                leafResourceContext.setDocumentType(TYPE_FILE);
                final Document document = FileDocumentFactory.createDocument(leafResourceContext, leaf);
                indexWriter.addDocument(document);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Documenttype not supported. file=" + leaf.getName());
                }
            }
        } catch (final DocumentAccessException e) {
            if (log.isDebugEnabled()) {
                log.debug("Can not access document." + e.getMessage());
            }
        } catch (final DocumentNotImplementedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Documenttype not implemented.");
            }
        } catch (final DocumentException dex) {
            if (log.isDebugEnabled()) {
                log.debug("DocumentException: Can not index leaf=" + leaf.getName());
            }
        } catch (final IOException ioEx) {
            log.warn("IOException: Can not index leaf=" + leaf.getName(), ioEx);
        } catch (final InterruptedException iex) {
            throw new InterruptedException(iex.getMessage());
        } catch (final Exception ex) {
            log.warn("Exception: Can not index leaf=" + leaf.getName(), ex);
        }
    }

    private void doIndexAllMessages(final SearchResourceContext parentResourceContext, final Forum forum, final OlatFullIndexer indexWriter) throws IOException,
            InterruptedException {
        // loop over all messages of a forum
        final List<Message> messages = getForumService().getMessagesByForum(forum);
        for (final Message message : messages) {
            final SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
            searchResourceContext.setBusinessControlFor(message);
            searchResourceContext.setDocumentType(TYPE_MESSAGE);
            searchResourceContext.setDocumentContext(parentResourceContext.getDocumentContext() + " " + forum.getKey());
            final Document document = ForumMessageDocument.createDocument(searchResourceContext, message);
            indexWriter.addDocument(document);
        }
    }

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        final ContextEntry ce = businessControl.popLauncherContextEntry();
        final OLATResourceable ores = ce.getOLATResourceable();
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
            // TODO: (LD) fix this!!! - the contextEntry is not the right context for this check
            final boolean isOwner = getBaseSecurity().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ACCESS, contextEntry.getOLATResourceable());
            if (isMessageHidden && !isOwner) {
                return false;
            }
            return true;
        } else {
            log.warn("In DialogCourseNode unkown OLATResourceable=" + ores);
            return false;
        }
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

}
