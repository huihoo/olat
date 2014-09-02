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

import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;

/**
 * Initial Date: 08.09.2003
 * 
 * @author Mike Stock
 */
public class Metadata implements QTIObject {

    private final HashMap metadata = new HashMap();

    /*
     * (non-Javadoc)
     */
    @Override
    public void addToElement(final Element root) {
        if (metadata.size() == 0) {
            return;
        }
        final Element qtimetadata = root.addElement("qtimetadata");
        for (final Iterator iter = metadata.keySet().iterator(); iter.hasNext();) {
            final String key = (String) iter.next();
            final String value = (String) metadata.get(key);
            final Element metadatafield = qtimetadata.addElement("qtimetadatafield");
            metadatafield.addElement("fieldlabel").setText(key);
            metadatafield.addElement("fieldentry").setText(value);
        }
    }

    public String getField(final String key) {
        return (String) metadata.get(key);
    }

    public void setField(final String key, final String value) {
        metadata.put(key, value);
    }

}
