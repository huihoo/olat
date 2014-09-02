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

import org.dom4j.Element;
import org.olat.lms.ims.qti.objects.Control;
import org.olat.lms.ims.qti.objects.QTIObject;
import org.olat.lms.ims.qti.objects.Section;
import org.olat.lms.ims.qti.objects.SelectionOrdering;

/**
 * @author rkulow
 */
public class SectionParser implements IParser {
    private final ParserManager parserManager = new ParserManager();

    @Override
    public Object parse(final Element element) {
        // assert element.getName().equalsIgnoreCase("questestinterop");
        final Section section = new Section();

        // attributes
        section.setIdent(element.attribute("ident").getValue());
        section.setTitle(element.attribute("title").getValue());

        // elements

        // DURATION
        final QTIObject duration = (QTIObject) parserManager.parse(element.element("duration"));
        section.setDuration(duration);

        final List sectioncontrolsXML = element.elements("sectioncontrol");
        final List sectioncontrols = new ArrayList();
        for (final Iterator i = sectioncontrolsXML.iterator(); i.hasNext();) {
            sectioncontrols.add(parserManager.parse((Element) i.next()));
        }
        if (sectioncontrols.size() == 0) {
            sectioncontrols.add(new Control());
        }
        section.setSectioncontrols(sectioncontrols);

        // SELECTION ORDERING
        final SelectionOrdering selectionOrdering = (SelectionOrdering) parserManager.parse(element.element("selection_ordering"));
        if (selectionOrdering != null) {
            section.setSelection_ordering(selectionOrdering);
        } else {
            section.setSelection_ordering(new SelectionOrdering());
        }

        // SECTIONS
        final List sectionsXML = element.elements("section");
        final List sections = new ArrayList();
        for (final Iterator i = sectionsXML.iterator(); i.hasNext();) {
            sections.add(parserManager.parse((Element) i.next()));
        }
        section.setSections(sections);

        // ITEMS
        final List itemsXML = element.elements("item");
        final List items = new ArrayList();
        for (final Iterator i = itemsXML.iterator(); i.hasNext();) {
            items.add(parserManager.parse((Element) i.next()));
        }
        section.setItems(items);

        // OBJECTIVES
        final Element mattext = (Element) element.selectSingleNode("./objectives/material/mattext");
        if (mattext != null) {
            section.setObjectives(mattext.getTextTrim());
        }

        // FEEDBACKS
        final List feedbacksXML = element.elements("sectionfeedback");
        final List feedbacks = new ArrayList();
        for (final Iterator i = feedbacksXML.iterator(); i.hasNext();) {
            final QTIObject tmp = (QTIObject) parserManager.parse((Element) i.next());
            feedbacks.add(tmp);
        }
        section.setSectionfeedbacks(feedbacks);

        // OUTCOMES_PROCESSING
        // TODO: maybe we should use the OutcomesProcessing object and parser here? Same as on
        // assessment level?
        final QTIObject outcomes_processing = (QTIObject) parserManager.parse(element.element("outcomes_processing"));
        section.setOutcomes_processing(outcomes_processing);

        return section;
    }

}
