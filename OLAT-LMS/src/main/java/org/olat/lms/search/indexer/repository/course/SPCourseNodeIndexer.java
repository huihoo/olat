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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.SPCourseNode;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.FolderIndexer;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.repository.CourseIndexer;
import org.olat.presentation.course.nodes.sp.SPEditController;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Indexer for SP (SinglePage) course-node.
 * 
 * @author Christian Guretzki
 */
public class SPCourseNodeIndexer extends FolderIndexer implements CourseNodeIndexer {
    private static final Logger log = LoggerHelper.getLogger();

    // Must correspond with LocalString_xx.properties
    // Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_'
    public final static String TYPE = "type.course.node.sp";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.SPCourseNode";
    private final static boolean indexOnlyChosenFile = false;

    private static final Pattern HREF_PATTERN = Pattern.compile("href=\\\"([^\\\"]*)\\\"", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final String HTML_SUFFIXES = "html htm xhtml xml";
    private final CourseIndexer courseNodeIndexer;

    public SPCourseNodeIndexer() {
        courseNodeIndexer = new CourseIndexer();
    }

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException, InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Index SinglePage...");
        }

        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        courseNodeResourceContext.setDocumentType(TYPE);
        courseNodeResourceContext.setTitle(courseNode.getShortTitle());
        courseNodeResourceContext.setDescription(courseNode.getLongTitle());

        final VFSContainer rootContainer = SPCourseNode.getNodeFolderContainer((SPCourseNode) courseNode, course.getCourseEnvironment());
        final String chosenFile = (String) courseNode.getModuleConfiguration().get(SPEditController.CONFIG_KEY_FILE);
        // First: Index choosen HTML file
        if (log.isDebugEnabled()) {
            log.debug("Index chosen file in SP. chosenFile=" + chosenFile);
        }
        final VFSLeaf leaf = (VFSLeaf) rootContainer.resolve(chosenFile);
        if (leaf != null) {
            final String filePath = getPathFor(leaf);
            if (log.isDebugEnabled()) {
                log.debug("Found chosen file in SP. filePath=" + filePath);
            }
            doIndexVFSLeafByMySelf(courseNodeResourceContext, leaf, indexWriter, filePath);
            if (!indexOnlyChosenFile) {
                if (log.isDebugEnabled()) {
                    log.debug("Index sub pages in SP.");
                }
                final Set<String> alreadyIndexFileNames = new HashSet<String>();
                alreadyIndexFileNames.add(chosenFile);
                indexSubPages(courseNodeResourceContext, rootContainer, indexWriter, leaf, alreadyIndexFileNames, 0, filePath);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Index only chosen file in SP.");
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Can not found choosen file in SP => Nothing indexed.");
            }
        }
        // go further, index my child nodes
        courseNodeIndexer.doIndexCourse(repositoryResourceContext, course, courseNode, indexWriter);
    }

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        // no special check on SP-node -> Html-page needed
        return true;
    }

    private void indexSubPages(final SearchResourceContext courseNodeResourceContext, final VFSContainer rootContainer, final OlatFullIndexer indexWriter,
            final VFSLeaf leaf, final Set<String> alreadyIndexFileNames, final int subPageLevel, final String rootFilePath) throws IOException, InterruptedException {
        int mySubPageLevel = subPageLevel;
        // check deepness of recursion
        if (mySubPageLevel++ <= 5) {
            final List<String> links = getLinkListFrom(leaf);
            for (String link : links) {
                if (log.isDebugEnabled()) {
                    log.debug("link=" + link);
                }
                if (!alreadyIndexFileNames.contains(link)) {
                    if ((rootFilePath != null) && !rootFilePath.equals("")) {
                        if (rootFilePath.endsWith("/")) {
                            link = rootFilePath + link;
                        } else {
                            link = rootFilePath + "/" + link;
                        }
                    }
                    final VFSItem item = rootContainer.resolve(link);
                    if ((item != null) && (item instanceof VFSLeaf)) {
                        final VFSLeaf subPageLeaf = (VFSLeaf) item;
                        if (log.isDebugEnabled()) {
                            log.debug("subPageLeaf=" + subPageLeaf);
                        }
                        final String filePath = getPathFor(subPageLeaf);
                        doIndexVFSLeafByMySelf(courseNodeResourceContext, subPageLeaf, indexWriter, filePath);
                        alreadyIndexFileNames.add(subPageLeaf.getName());
                        indexSubPages(courseNodeResourceContext, rootContainer, indexWriter, subPageLeaf, alreadyIndexFileNames, mySubPageLevel, rootFilePath);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Could not found sub-page for link=" + link);
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("sub-page already indexed, link=" + link);
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Reach to many sub-page levels. Go not further with indexing sub-pages last leaf=" + leaf.getName());
            }
        }
    }

    private List<String> getLinkListFrom(final VFSLeaf leaf) {
        final List<String> linkList = new ArrayList<String>();
        // only dive into file if it is a html file
        final String suffix = getSuffix(leaf.getName());
        if (HTML_SUFFIXES.contains(suffix)) {
            final BufferedInputStream bis = new BufferedInputStream(leaf.getInputStream());
            final String inputString = FileUtils.load(bis, "utf-8");
            // Remove all HTML Tags
            final Matcher m = HREF_PATTERN.matcher(inputString);
            String match;
            while (m.find()) {
                final int groupCount = m.groupCount();
                if (groupCount > 0) {
                    match = m.group(1); // e.g. 'seite2.html'
                    if (!match.startsWith("http://")) { // TODO: Filter other url than http
                        linkList.add(match);
                    }
                }
            }
        }
        return linkList;
    }

    private String getSuffix(final String fileName) {
        final int dotpos = fileName.lastIndexOf('.');
        if (dotpos < 0 || dotpos == fileName.length() - 1) {
            return "";
        }
        final String suffix = fileName.substring(dotpos + 1).toLowerCase();
        return suffix;
    }

}
