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

package org.olat.lms.ims.qti.objects;

import org.dom4j.Element;
import org.olat.system.commons.CodeHelper;

/**
 * @author rkulow
 */
public class Mattext implements QTIObject, MatElement {

    private String id = null;
    private String content = null;

    public Mattext(final String content) {
        id = "" + CodeHelper.getRAMUniqueID();
        setContent(content);
    }

    /**
	 */
    @Override
    public void addToElement(final Element root) {
        if (content != null) {
            final Element mattext = root.addElement("mattext");
            // Since we use rich text (html) as content, the text type is set to
            // "text/html". This way the document conforms to the qti standard.
            mattext.addAttribute("texttype", "text/html");
            mattext.addCDATA(content);
        }
    }

    /**
	 */
    @Override
    public String renderAsHtml(final String mediaBaseURL) {
        return content;
    }

    /**
	 */
    @Override
    public String renderAsText() {
        return this.content;
    }

    /**
     * Returns the content.
     * 
     * @return String
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content.
     * 
     * @param content
     *            The content to set
     */
    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }

    /**
     * @return
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @param string
     */
    public void setId(final String string) {
        id = string;
    }

}
