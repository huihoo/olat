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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.common.htmleditor;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.presentation.framework.common.linkchooser.CustomLinkTreeModel;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Description: The WYSIWYGFactory provides a full-fledged WYSIWYG HTML editor with support for media and link browsing based on a VFS item or a String.
 * <p>
 * The editor will keep any header information such as references to CSS or JS files, but those will not be active while editing the file.
 * <p>
 * Keep in mind that this editor might be destructive when editing files that have been created with an external, more powerful editor.
 * 
 * @author Felix Jost, Florian Gnägi Initial Date: Dez 10, 2005<br>
 */
public class WysiwygFactory {

    /**
     * Factory method to create a file based HTML editor instance that uses locking to prevent two people editing the same file.
     * 
     * @param ureq
     * @param wControl
     * @param rootDir
     *            the basedir (below that folder all images can be chosen)
     * @param filePath
     *            the file e.g. "index.html"
     * @param editorCheckEnabled
     *            true: check if file has been created with another tool and warn user about potential data loss; false: ignore other authoring tools
     * @param userActivityLogger
     *            the userActivity Logger if used
     * @return
     */
    public static HTMLEditorController createWysiwygController(UserRequest ureq, WindowControl wControl, VFSContainer rootDir, String filePath, boolean editorCheckEnabled) {
        return new HTMLEditorController(ureq, wControl, rootDir, filePath, null, editorCheckEnabled);
    }

    /**
     * Factory method to create a file based HTML editor instance that uses locking to prevent two people editing the same file. In this factory method, an custom link
     * tree model can be used to give users the possiblity to link to some of your component. The generated links must be dispatchable by the framework or absolut
     * external links.
     * 
     * @param ureq
     * @param wControl
     * @param baseContainer
     *            the baseContainer (below that folder all images can be chosen)
     * @param relFilePath
     *            the file e.g. "index.html"
     * @param userActivityLogger
     *            the userActivity Logger if used
     * @param customLinkTreeModel
     *            Model for internal-link tree e.g. course-node tree with link information
     * @param editorCheckEnabled
     *            true: check if file has been created with another tool and warn user about potential data loss; false: ignore other authoring tools
     * @return Controller with internal-link selector
     */
    public static HTMLEditorController createWysiwygControllerWithInternalLink(UserRequest ureq, WindowControl wControl, VFSContainer baseContainer, String relFilePath,
            boolean editorCheckEnabled, CustomLinkTreeModel customLinkTreeModel) {
        return new HTMLEditorController(ureq, wControl, baseContainer, relFilePath, customLinkTreeModel, editorCheckEnabled);
    }

    /**
     * Factory method to create a string based HTML editor instance. The string must not contain a HTML HEAD, just the body. If you want to use the HTML editor in your
     * form create a flexi form and use the FormUIFactory to create a RichTextElement instead.
     * <p>
     * This controller is a full-screen editor with a save and cancel button
     * 
     * @param ureq
     * @param wControl
     * @param rootDir
     *            the basedir (below that folder all images can be chosen)
     * @param htmlContent
     *            the HTMLContent to edit
     * @param userActivityLogger
     *            the userActivity Logger if used
     * @param customLinkTreeModel
     *            Model for internal-link tree e.g. course-node tree with link information
     * @return Controller with internal-link selector
     */
    public static HTMLEditorControllerWithoutFile createWysiwygControllerWithoutFile(UserRequest ureq, WindowControl wControl, VFSContainer rootDir, String htmlContent,
            CustomLinkTreeModel customLinkTreeModel) {
        return new HTMLEditorControllerWithoutFile(ureq, wControl, rootDir, htmlContent, customLinkTreeModel);
    }

}
