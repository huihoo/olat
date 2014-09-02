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
package org.olat.lms.wiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * pack whole wiki with unparsed syntax files into zip for export
 * <P>
 * Initial Date: Dec 11, 2006 <br>
 * 
 * @author guido
 */
public class WikiToZipUtils {

    /**
     * creates an html page with the mappings between the pagename and the Base64 encoded filename.
     * 
     * @param vfsLeaves
     * @return
     */
    private static String createIndexPageForExport(final List vfsLeaves) {
        boolean hasProperties = false;
        final StringBuilder sb = new StringBuilder();
        sb.append("<html><head>");
        sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        sb.append("</head><body><ul>");
        for (final Iterator iter = vfsLeaves.iterator(); iter.hasNext();) {
            final VFSLeaf element = (VFSLeaf) iter.next();
            // destination.copyFrom(element);
            if (element.getName().endsWith(WikiManager.WIKI_PROPERTIES_SUFFIX)) {
                hasProperties = true;
                final Properties p = new Properties();
                try {
                    p.load(element.getInputStream());
                } catch (final IOException e) {
                    throw new AssertException("Wiki propterties couldn't be read! ", e);
                }
                sb.append("<li>");
                sb.append(p.getProperty(WikiManager.PAGENAME));
                sb.append(" ----> ");
                sb.append(element.getName().substring(0, element.getName().indexOf(".")));
                sb.append("</li>");
            }
        }
        sb.append("</ul></body></html>");
        if (!hasProperties) {
            return null;
        }
        return sb.toString();
    }

    /**
     * get the whole wiki as a zip file for export, content is unparsed!
     * 
     * @param rootContainer
     * @return
     */
    public static VFSLeaf getWikiAsZip(final VFSContainer rootContainer) {
        final List<VFSItem> folders = rootContainer.getItems();
        final VFSLeaf indexLeaf = (VFSLeaf) rootContainer.resolve("index.html");
        if (indexLeaf != null) {
            indexLeaf.delete();
        }
        final List<VFSItem> filesTozip = new ArrayList<VFSItem>();
        for (final Iterator<VFSItem> iter = folders.iterator(); iter.hasNext();) {
            final VFSItem item = iter.next();
            if (item instanceof VFSContainer) {
                final VFSContainer folder = (VFSContainer) item;
                List<VFSItem> items = folder.getItems();
                final String overviewPage = WikiToZipUtils.createIndexPageForExport(items);
                if (overviewPage != null) {
                    final VFSLeaf overview = rootContainer.createChildLeaf("index.html");
                    // items.add(overview); take care not to have duplicate entries in the list
                    FileUtils.save(overview.getOutputStream(false), overviewPage, "utf-8");
                }
                items = folder.getItems(); // reload list, maybe there is a new index.html file
                filesTozip.addAll(items);
            }
        }
        final VFSLeaf zipFile = (VFSLeaf) rootContainer.resolve("wiki.zip");
        if (rootContainer.resolve("wiki.zip") != null) {
            zipFile.delete();
        }
        ZipUtil.zip(filesTozip, rootContainer.createChildLeaf("wiki.zip"), true);
        return (VFSLeaf) rootContainer.resolve("wiki.zip");
    }

}
