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

package org.olat.lms.ims.qti.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.olat.lms.ims.qti.objects.Feedback;

/**
 * @author rkulow
 */
public class FeedbackParser implements IParser {

    private final ParserManager parserManager = new ParserManager();

    /**
	 */
    @Override
    public Object parse(final Element element) {
        // assert element.getName().equalsIgnoreCase("sectionfeedback")
        // || element.getName().equalsIgnoreCase("itemfeedback")
        // || element.getName().equalsIgnoreCase("assessmentfeedback");

        final List materialsXML = element.selectNodes(".//material");
        if (materialsXML.size() == 0) {
            return null;
        }

        final Feedback feedback = new Feedback();
        // attributes
        Attribute tmp = element.attribute("ident");
        if (tmp != null) {
            feedback.setIdent(tmp.getValue());
        }
        tmp = element.attribute("title");
        if (tmp != null) {
            feedback.setTitle(tmp.getValue());
        }
        tmp = element.attribute("view");
        if (tmp != null) {
            feedback.setView(tmp.getValue());
        }

        // get type
        if (element.element("solution") != null) {
            return null;
        } else if (element.element("hint") != null) {
            return null;
        }

        // parse Material
        // MATERIAL
        final List materials = new ArrayList();
        for (final Iterator i = materialsXML.iterator(); i.hasNext();) {
            materials.add(parserManager.parse((Element) i.next()));
        }
        feedback.setMaterials(materials);
        return feedback;
    }

}
