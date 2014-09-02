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
public class Matimage implements QTIObject, MatElement {

    private String id = null;
    private String URI = null;

    public Matimage(final String uri) {
        id = "" + CodeHelper.getRAMUniqueID();
        setURI(uri);
    }

    /**
	 */
    @Override
    public void addToElement(final Element root) {
        if (URI != null) {
            final Element matimage = root.addElement("matimage");
            matimage.addAttribute("uri", URI);
        }
    }

    /**
	 */
    @Override
    public String renderAsHtml(final String mediaBaseURL) {
        if (URI == null) {
            return "[ IMAGE: no image selected ]";
        }
        final boolean relURI = (URI.indexOf("://") == -1);
        final StringBuilder sb = new StringBuilder("<img src=\"");
        if (relURI) {
            sb.append(mediaBaseURL + "/");
        }
        sb.append(URI);
        sb.append("\" border=\"0\" alt=\"");
        sb.append(URI);
        sb.append("\">");
        return sb.toString();
    }

    /**
	 */
    @Override
    public String renderAsText() {
        if (URI == null) {
            return "[ IMAGE: no image selected ]";
        }
        return "[ IMAGE: " + URI + " ]";
    }

    /**
     * @return
     */
    public String getURI() {
        return URI;
    }

    /**
     * @param string
     */
    public void setURI(final String string) {
        URI = string;
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
