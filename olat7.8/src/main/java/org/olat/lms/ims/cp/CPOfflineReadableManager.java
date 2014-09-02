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

package org.olat.lms.ims.cp;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.olat.data.commons.fileutil.ExportUtil;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.ims.cp.CPManifestTreeModel;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description: <br>
 * Provides functionality to make a IMS-Content-Packaging offline readable with menu just using a browser. The menu, an ordinary
 * <ul>
 * list, is turned into a dynamic, expandable, collapsible tree structure with support from www.mattkruse.com/javascript. The Menu resides in the frame FRAME_NAME_MENU
 * and the Content in FRAME_NAME_CONTENT.
 * 
 * @author alex
 */

public class CPOfflineReadableManager {
    private static CPOfflineReadableManager instance = new CPOfflineReadableManager();

    private static final String DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">";
    private static final String IMSMANIFEST = "imsmanifest.xml";

    public static final String CPOFFLINEMENUMAT = "cp_offline_menu_mat";
    private static final String OLATICON = "olat_icon.gif";
    private static final String FAVICON = "favicon.ico";
    private static final String BRANDING = "provided by OLAT";
    private static final String MKTREEJS = "mktree.js"; // mattkruseTree ->
                                                        // www.mattkruse.com
    private static final String MKTREECSS = "mktree.css";

    private static final String MENU_FILE = "_MENU_.html";
    private static final String FRAME_FILE = "_START_.html";
    private static final String LOGO_FILE = "_LOGO_.html";
    private static final String FRAME_NAME_MENU = "menu";
    private static final String FRAME_NAME_CONTENT = "content";
    private static final String FRAME_NAME_LOGO = "logo";

    private String rootTitle;

    private CPOfflineReadableManager() {
        // private since singleton
    }

    /**
     * @return instance of CPOfflineReadableManager
     */
    public static CPOfflineReadableManager getInstance() {
        return instance;
    }

    /**
     * Used for migration purposes
     * 
     * @param unzippedDir
     * @param targetZip
     * @param cpOfflineMat
     */
    public void makeCPOfflineReadable(final File unzippedDir, final File targetZip, File cpOfflineMat) {
        writeOfflineCP(unzippedDir);
        // assign default mat if not specified
        if (cpOfflineMat == null) {
            cpOfflineMat = new File(WebappHelper.getContextRoot() + "/static/" + CPOFFLINEMENUMAT);
        }
        zipOfflineReadableCP(unzippedDir, targetZip, cpOfflineMat);
    }

    /**
     * Adds the folder CPOFFLINEMENUMAT and the two files MENU_FILE and FRAME_FILE to the _unzipped_-Folder.
     * 
     * @param ores
     * @param zipName
     */
    public void makeCPOfflineReadable(final OLATResourceable ores, final String zipName) {
        final String repositoryHome = FolderConfig.getCanonicalRepositoryHome();
        final FileResourceManager fm = FileResourceManager.getInstance();
        final String relPath = fm.getUnzippedDirRel(ores);
        final String resId = ores.getResourceableId().toString();

        final File unzippedDir = new File(repositoryHome + "/" + relPath);
        final File targetZip = new File(repositoryHome + "/" + resId + "/" + zipName);
        final File cpOfflineMat = new File(WebappHelper.getContextRoot() + "/static/" + CPOFFLINEMENUMAT);

        writeOfflineCP(unzippedDir);
        zipOfflineReadableCP(unzippedDir, targetZip, cpOfflineMat);
    }

    /**
     * writes the MENU_FILE to the _unzipped_-Folder
     * 
     * @param unzippedDir
     */
    private void writeOfflineCP(final File unzippedDir) {
        final File mani = new File(unzippedDir, IMSMANIFEST);
        final String s = createMenuAndFrame(unzippedDir, mani);

        writeContentToFile(unzippedDir, s, MENU_FILE);
    }

    private void writeContentToFile(final File unzippedDir, final String content, final String fileName) {
        final File f = new File(unzippedDir, fileName);
        if (f.exists()) {
            FileUtils.deleteDirsAndFiles(f, false, true);
        }
        ExportUtil.writeContentToFile(fileName, content, unzippedDir, "utf-8");
    }

    private String createMenuAndFrame(final File unzippedDir, final File mani) {
        final StringBuilder sb = createMenu(mani);

        writeOfflineHTMLFrameSetFile(unzippedDir);
        writeOfflineHTMLLogoFrame(unzippedDir);

        return sb.toString();
    }

    private StringBuilder createMenu(final File mani) {
        final TreeNode root = getRoot(mani);
        final StringBuilder sb = buildMenu(root);
        return sb;
    }

    private TreeNode getRoot(final File mani) {
        final LocalFileImpl vfsMani = new LocalFileImpl(mani);
        final CPManifestTreeModel ctm = new CPManifestTreeModel(vfsMani);
        final TreeNode root = ctm.getRootNode();
        this.rootTitle = root.getTitle();
        return root;
    }

    private StringBuilder buildMenu(final TreeNode root) {
        final StringBuilder sb = new StringBuilder();
        sb.append(DOCTYPE);
        sb.append("<html>\n<head>\n");
        sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        sb.append("<title>");
        sb.append(rootTitle);
        sb.append("</title>\n");
        sb.append("<SCRIPT SRC=\"");
        sb.append(CPOFFLINEMENUMAT);
        sb.append("/");
        sb.append(MKTREEJS);
        sb.append("\" LANGUAGE=\"JavaScript\"></SCRIPT>");
        sb.append("<LINK REL=\"stylesheet\" HREF=\"");
        sb.append(CPOFFLINEMENUMAT);
        sb.append("/");
        sb.append(MKTREECSS);
        sb.append("\"></head>\n<body>\n");
        sb.append("<div>");
        sb.append("<a href=\"#\" onclick=\"expandTree('tree1'); return false;\">Expand All</a>&nbsp;&nbsp;&nbsp;");
        sb.append("<a href=\"#\" onclick=\"collapseTree('tree1'); return false;\">Collapse All</a>&nbsp;&nbsp;&nbsp;");
        sb.append("<ul class=\"mktree\" ID=\"tree1\">");
        render(root, sb, 0);
        sb.append("</ul>");
        sb.append("</div>");
        sb.append("</body>");
        return sb;
    }

    /**
     * @param node
     * @param sb
     * @param indent
     */
    private void render(final TreeNode node, final StringBuilder sb, final int indent) {
        // set content to first accessible child or root node if no children
        // available
        // render current node

        final String nodeUri = (String) node.getUserObject();
        final String title = node.getTitle();
        String altText = node.getAltText();
        altText = StringHelper.escapeHtmlAttribute(altText);

        sb.append("<li>\n");
        if (node.isAccessible()) {
            sb.append("<a href=\"");
            sb.append(nodeUri);
            sb.append("\" target=\"");
            sb.append(FRAME_NAME_CONTENT);
            sb.append("\" alt=\"");
            sb.append(altText);
            sb.append("\" title=\"");
            sb.append(altText);
            sb.append("\">");
            sb.append(title);
            sb.append("</a>\n");
        } else {
            sb.append("<span title=\"");
            sb.append(altText);
            sb.append("\">");
            sb.append(title);
            sb.append("</span>");
        }

        // render all children
        boolean b = true;
        for (int i = 0; i < node.getChildCount(); i++) {
            if (b) {
                sb.append("<ul>\n");
            }
            final TreeNode child = (TreeNode) node.getChildAt(i);
            render(child, sb, indent + 1);
            b = false;
        }
        if (!b) {
            sb.append("</ul>\n");
        }
        sb.append("</li>\n");
    }

    /**
     * writes the FRAME_FILE to the _unzipped_-Folder
     * 
     * @param unzippedDir
     * @param rootTitle
     */
    private void writeOfflineHTMLFrameSetFile(final File unzippedDir) {
        final StringBuilder sb = buildFrameSet();
        writeContentToFile(unzippedDir, sb.toString(), FRAME_FILE);
    }

    private StringBuilder buildFrameSet() {
        final StringBuilder sb = new StringBuilder();
        sb.append(DOCTYPE);
        sb.append("<html>\n<head>\n");
        sb.append("<link rel=\"icon\" href=\"");
        sb.append(CPOFFLINEMENUMAT);
        sb.append("/");
        sb.append(FAVICON);
        sb.append("\" type=\"image/x-icon\">");
        sb.append("<LINK REL=\"stylesheet\" HREF=\"");
        sb.append(CPOFFLINEMENUMAT);
        sb.append("/");
        sb.append(MKTREECSS);
        sb.append("\">");
        sb.append("<title>");
        sb.append(rootTitle);
        sb.append("</title>\n</head>\n");
        sb.append("<frameset cols=\"250,*\" frameborder=\"0\" framespacing=\"0\" border=\"0\">");

        sb.append("<frameset rows=\"*,25\" frameborder=\"0\" framespacing=\"0\" border=\"0\">");

        sb.append("<frame src=\"");
        sb.append(MENU_FILE);
        sb.append("\" name=\"");
        sb.append(FRAME_NAME_MENU);
        sb.append("\">\n");

        sb.append("<frame src=\"");
        sb.append(LOGO_FILE);
        sb.append("\" name=\"");
        sb.append(FRAME_NAME_LOGO);
        sb.append("\">\n");

        sb.append("</frameset>");

        sb.append("<frame name=\"");
        sb.append(FRAME_NAME_CONTENT);
        sb.append("\">\n");

        sb.append("</frameset>\n</html>");
        return sb;
    }

    /**
     * writes the FRAME_FILE to the _unzipped_-Folder
     * 
     * @param unzippedDir
     * @param rootTitle
     */
    private void writeOfflineHTMLLogoFrame(final File unzippedDir) {
        final StringBuilder sb = buildLogoFrame();
        writeContentToFile(unzippedDir, sb.toString(), LOGO_FILE);
    }

    private StringBuilder buildLogoFrame() {
        final StringBuilder sb = new StringBuilder();
        sb.append(DOCTYPE);
        sb.append("<html>\n<head>\n");
        sb.append("<LINK REL=\"stylesheet\" HREF=\"");
        sb.append(CPOFFLINEMENUMAT);
        sb.append("/");
        sb.append(MKTREECSS);
        sb.append("\">");
        sb.append("<title>");
        sb.append(rootTitle);
        sb.append("</title>\n</head><body>\n");
        sb.append("<div id=\"branding\">");
        sb.append("<a target=\"_blank\" href=\"http://www.olat.org\"><img id=\"logo\" src=\"");
        sb.append(CPOFFLINEMENUMAT);
        sb.append("/");
        sb.append(OLATICON);
        sb.append("\" alt=\"OLAT_logo\">");
        sb.append(BRANDING);
        sb.append("</div>");

        sb.append("\n</body></html>");
        return sb;
    }

    /**
     * copy the whole CPOFFLINEMENUMAT-Folder (mktree.js, mktree.css and gifs) to the _unzipped_-Folder and zip everything that is in the _unzipped_-Folder
     * 
     * @param unzippedDir
     * @param targetZip
     * @param cpOfflineMat
     */
    private void zipOfflineReadableCP(final File unzippedDir, final File targetZip, final File cpOfflineMat) {
        FileUtils.copyDirToDir(cpOfflineMat, unzippedDir, "copy for offline readable cp");

        if (targetZip.exists()) {
            FileUtils.deleteDirsAndFiles(targetZip, false, true);
        }

        final Set<String> allFiles = new HashSet<String>();
        final String[] cpFiles = unzippedDir.list();
        for (int i = 0; i < cpFiles.length; i++) {
            allFiles.add(cpFiles[i]);
        }
        ZipUtil.zip(allFiles, unzippedDir, targetZip, true);

    }

}
