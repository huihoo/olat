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
import org.olat.lms.ims.qti.objects.Assessment;
import org.olat.lms.ims.qti.objects.Control;
import org.olat.lms.ims.qti.objects.Metadata;
import org.olat.lms.ims.qti.objects.OutcomesProcessing;
import org.olat.lms.ims.qti.objects.QTIObject;

/**
 * @author rkulow
 */
public class AssessmentParser implements IParser {

    private final ParserManager parserManager = new ParserManager();

    /**
	 */
    @Override
    public Object parse(final Element element) {
        // assert element.getName().equalsIgnoreCase("assessment");
        final Assessment assessment = new Assessment();

        // attributes

        Attribute attr = element.attribute("ident");
        if (attr != null) {
            assessment.setIdent(attr.getValue());
        }
        attr = element.attribute("title");
        if (attr != null) {
            assessment.setTitle(attr.getValue());
        }

        // elements

        // DURATION
        final QTIObject duration = (QTIObject) parserManager.parse(element.element("duration"));
        assessment.setDuration(duration);

        // ASSESSMENTCONTROL
        final List assessmentcontrolsXML = element.elements("assessmentcontrol");
        final List assessmentcontrols = new ArrayList();
        for (final Iterator i = assessmentcontrolsXML.iterator(); i.hasNext();) {
            assessmentcontrols.add(parserManager.parse((Element) i.next()));
            assessment.setInheritControls(true);
        }
        if (assessmentcontrols.size() == 0) {
            assessmentcontrols.add(new Control());
            assessment.setInheritControls(false);
        }
        assessment.setAssessmentcontrols(assessmentcontrols);

        // OUTCOMES PROCESSING
        final OutcomesProcessing outcomesProcessing = (OutcomesProcessing) parserManager.parse(element.element("outcomes_processing"));
        if (outcomesProcessing != null) {
            assessment.setOutcomes_processing(outcomesProcessing);
        }

        // SECTIONS
        final List sectionsXML = element.elements("section");
        final List sections = new ArrayList();
        for (final Iterator i = sectionsXML.iterator(); i.hasNext();) {
            sections.add(parserManager.parse((Element) i.next()));
        }
        assessment.setSections(sections);

        // ITEMS
        final List itemsXML = element.elements("item");
        final List items = new ArrayList();
        for (final Iterator i = itemsXML.iterator(); i.hasNext();) {
            items.add(parserManager.parse((Element) i.next()));
        }
        assessment.setItems(items);

        // OBJECTIVES
        final Element mattext = (Element) element.selectSingleNode("./objectives/material/mattext");
        if (mattext != null) {
            assessment.setObjectives(mattext.getTextTrim());
        }

        // METADATA
        final Metadata metadata = (Metadata) parserManager.parse(element.element("qtimetadata"));
        if (metadata != null) {
            assessment.setMetadata(metadata);
        }

        return assessment;
    }

}
