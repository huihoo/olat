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

package org.olat.lms.ims.qti.process;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: 07.08.2003
 * 
 * @author Mike Stock
 */
public class QTIEditorResolver implements Resolver {

    private final Document doc;
    private final String staticsBaseURI;
    private final boolean isSurvey = false; // FIXME:ms:??

    /**
     * @param qtiPackage
     */
    public QTIEditorResolver(final QTIEditorPackageEBL qtiPackage) {
        doc = qtiPackage.getQTIDocument().getDocument();
        staticsBaseURI = qtiPackage.getMediaBaseURL();
    }

    /**
	 */
    @Override
    public Element getObjectBank(final String ident) {
        throw new AssertException("Not implemented.");
    }

    /**
	 */
    @Override
    public Document getQTIDocument() {
        return doc;
    }

    /**
     * @param ident
     * @return Section
     */
    @Override
    public Element getSection(final String ident) {
        // improve: put sections in a hashmap - see the timing difference
        final Element el_section = (Element) doc.selectSingleNode("questestinterop/assessment/section[@ident='" + ident + "']");
        return el_section;
    }

    /**
     * @param ident
     * @return Item
     */
    @Override
    public Element getItem(final String ident) {
        // ident of item must be "globally unique"(qti...), unique within a qti document
        final Element el_item = (Element) doc.selectSingleNode("//item[@ident='" + ident + "']");
        return el_item;
    }

    /**
	 */
    @Override
    public String getStaticsBaseURI() {
        return staticsBaseURI;
    }

    /**
     * @return true when the editor is in survey mode, false when in test/selftest mode
     */
    public boolean isSurvey() {
        return isSurvey;
    }

    /**
	 */
    @Override
    public boolean hasAutocompleteFiles() {
        return false; // TODO:chg: no autocomplete feature for editor ?
    }
}
