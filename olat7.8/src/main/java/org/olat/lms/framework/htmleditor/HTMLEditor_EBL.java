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
package org.olat.lms.framework.htmleditor;

import java.io.InputStream;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.MergeSource;
import org.olat.data.commons.vfs.NamedContainerImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.version.Versionable;
import org.olat.lms.commons.SimpleHtmlParser;
import org.olat.lms.coordinate.LockingService;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * The HTMLEditorController provides a full-fledged WYSIWYG HTML editor with support for media and link browsing based on a VFS item. The editor will keep any header
 * information such as references to CSS or JS files, but those will not be active while editing the file.
 * <p>
 * Keep in mind that this editor might be destructive when editing files that have been created with an external, more powerful editor.
 * <p>
 * Use the WYSIWYGFactory to create an instance.
 * <P>
 * Initial Date: 08.05.2009 <br>
 * 
 * @author gnaegi
 */
@Component
public class HTMLEditor_EBL {
    // HTML constants
    static final String DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
    static final String OPEN_HTML = "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
    static final String OPEN_HEAD = "<head>";
    static final String CLOSE_HEAD = "</head>";
    static final String OPEN_TITLE = "<title>";
    static final String CLOSE_TITLE = "</title>";
    static final String EMTPY_TITLE = OPEN_TITLE + CLOSE_TITLE;
    static final String CLOSE_HTML = "\n<html>";
    static final String CLOSE_BODY_HTML = "</body></html>";
    static final String CLOSE_HEAD_OPEN_BODY = "</head><body>";
    // Editor version metadata to check if file has already been edited with this editor
    static final String GENERATOR = "olat-tinymce-";
    static final String GENERATOR_VERSION = "3";
    static final String GENERATOR_META = "<meta name=\"generator\" content=\"" + GENERATOR + GENERATOR_VERSION + "\" />\n";
    // Default char set for new files is UTF-8
    public static final String UTF_8 = "utf-8";
    static final String UTF8CHARSET = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n";

    @Autowired
    LockingService lockingService;

    public void releaseLock(LockResult lock) {
        if (lock != null) {
            lockingService.releaseLock(lock);
            lock = null;
        }
    }

    public LockResult getLockFor(VFSLeaf fileLeaf, VFSContainer baseContainer, String relFilePath, Identity identity) {
        // Cast to LocalFile necessary because the VFSItem is missing some
        // ID mechanism that identifies an item within the system
        OLATResourceable lockResourceable = OresHelper.createOLATResourceableTypeWithoutCheck(fileLeaf.toString());
        // OLAT-5066: the use of "fileName" gives users the (false) impression that the file they wish to access
        // is already locked by someone else. Since the lock token must be smaller than 50 characters we us an
        // MD5 hash of the absolute file path which will always be 32 characters long and virtually unique.

        /*
         * Issue OLAT-7090: this issue has not been correctly solved by issues OLAT-5066 or OLAT-4575 (see links in issue). To solve problem now finally absolute path for
         * lock token is used.
         */
        String lockToken = null;
        if (baseContainer instanceof MergeSource) {
            lockToken = Encoder.encrypt(((MergeSource) baseContainer).getRootWriteContainer().getPath());
        } else if (baseContainer instanceof NamedContainerImpl && ((NamedContainerImpl) baseContainer).getDelegate() instanceof MergeSource) {
            lockToken = Encoder.encrypt(((MergeSource) ((NamedContainerImpl) baseContainer).getDelegate()).getRootWriteContainer().getPath());
        } else {
            lockToken = Encoder.encrypt(getFileDebuggingPath(baseContainer, relFilePath));
        }
        return lockingService.acquireLock(lockResourceable, identity, lockToken);
    }

    /**
     * Helper method to get a meaningfull debugging filename from the vfs container and the file path
     * 
     * @param root
     * @param relPath
     * @return
     */
    public String getFileDebuggingPath(VFSContainer root, String relPath) {
        String path = relPath;
        VFSContainer dir = root;
        while (dir != null) {
            path = "/" + dir.getName() + path;
            dir = dir.getParentContainer();
        }
        return path;
    }

    public void saveData(String content, VFSLeaf fileLeaf, String preface, String charSet, Identity identity) {
        // No XSS checks, are done in the HTML editor - users can upload illegal
        // stuff, JS needs to be enabled for users

        // If preface was null -> append own head and save it in utf-8. Preface
        // is the header that was in the file when we opened the file
        StringBuffer fileContent = new StringBuffer();
        if (preface == null) {
            fileContent.append(DOCTYPE).append(OPEN_HTML).append(OPEN_HEAD);
            fileContent.append(GENERATOR_META).append(UTF8CHARSET);
            // In new documents, create empty title to be W3C conform. Title
            // is mandatory element in meta element.
            fileContent.append(EMTPY_TITLE);
            fileContent.append(CLOSE_HEAD_OPEN_BODY);
            fileContent.append(content);
            fileContent.append(CLOSE_BODY_HTML);
            charSet = UTF_8; // use utf-8 by default for new files
        } else {
            // existing preface, just reinsert so we don't lose stuff the user put
            // in there
            fileContent.append(preface).append(content).append(CLOSE_BODY_HTML);
        }

        // save the file
        if (fileLeaf instanceof Versionable && ((Versionable) fileLeaf).getVersions().isVersioned()) {
            InputStream inStream = FileUtils.getInputStream(fileContent.toString(), charSet);
            ((Versionable) fileLeaf).getVersions().addVersion(identity, "", inStream);
        } else {
            FileUtils.save(fileLeaf.getOutputStream(false), fileContent.toString(), charSet);
        }
    }

    /**
     * Internal helper to parse the page content
     * 
     * @param vfsLeaf
     * @return String containing the page body
     */
    public HtmlPage parsePage(VFSLeaf vfsLeaf, VFSContainer baseContainer, String fileRelPath) {
        // Load data with given encoding
        InputStream is = vfsLeaf.getInputStream();
        if (is == null) {
            throw new AssertException("Could not open input stream for file::" + getFileDebuggingPath(baseContainer, fileRelPath));
        }
        String charSet = SimpleHtmlParser.extractHTMLCharset(vfsLeaf.getInputStream());
        String leafData = FileUtils.load(is, charSet);
        if (leafData == null || leafData.length() == 0) {
            leafData = "";
        }
        int generatorPos = leafData.indexOf(GENERATOR);
        SimpleHtmlParser parser = new SimpleHtmlParser(leafData);
        StringBuilder sb = new StringBuilder();
        if (parser.getHtmlDocType() != null)
            sb.append(parser.getHtmlDocType());
        if (parser.getXhtmlNamespaces() != null) {
            sb.append(parser.getXhtmlNamespaces());
        } else {
            sb.append(CLOSE_HTML);
        }
        sb.append(OPEN_HEAD);
        // include generator so foreign editor warning only appears once
        if (generatorPos == -1)
            sb.append(GENERATOR_META);
        if (parser.getHtmlHead() != null)
            sb.append(parser.getHtmlHead());
        sb.append(CLOSE_HEAD);
        sb.append(parser.getBodyTag());
        String preface = sb.toString();

        if (leafData.length() == 0) {
            // set new one when file created with this editor
            preface = null;
        }
        // now get the body part
        return new HtmlPage(parser.getHtmlContent(), preface, hasNoGeneratorMetaData(generatorPos, leafData));
    }

    private boolean hasNoGeneratorMetaData(int generatorPos, String leafData) {
        return generatorPos == -1 && leafData.length() > 0;
    }

    /**
     * Factory method to create a well formed XHTML frame and inserts the content into the body.
     * 
     * @param bodyMarkup
     *            the XHTML content of the body part
     * @param title
     *            title of this page
     * @return XHTML page
     */
    public static String createXHtmlFileContent(String bodyMarkup, String title) {
        StringBuffer fileContent = new StringBuffer();
        fileContent.append(HTMLEditor_EBL.DOCTYPE).append(HTMLEditor_EBL.OPEN_HTML).append(HTMLEditor_EBL.OPEN_HEAD);
        fileContent.append(HTMLEditor_EBL.GENERATOR_META).append(HTMLEditor_EBL.UTF8CHARSET);
        fileContent.append(HTMLEditor_EBL.OPEN_TITLE).append(title).append(HTMLEditor_EBL.CLOSE_TITLE);
        fileContent.append(HTMLEditor_EBL.CLOSE_HEAD_OPEN_BODY);
        fileContent.append(bodyMarkup);
        fileContent.append(HTMLEditor_EBL.CLOSE_BODY_HTML);
        return fileContent.toString();
    }

}
