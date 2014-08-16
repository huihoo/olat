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
import org.olat.lms.ims.qti.objects.ChoiceQuestion;
import org.olat.lms.ims.qti.objects.Control;
import org.olat.lms.ims.qti.objects.Duration;
import org.olat.lms.ims.qti.objects.EssayQuestion;
import org.olat.lms.ims.qti.objects.FIBQuestion;
import org.olat.lms.ims.qti.objects.Item;
import org.olat.lms.ims.qti.objects.QTIObject;
import org.olat.lms.ims.qti.objects.QTIXMLWrapper;
import org.olat.lms.ims.qti.objects.Question;
import org.olat.system.commons.CodeHelper;

/**
 * @author rkulow
 */
public class ItemParser implements IParser {

    public static final String ITEM_PREFIX_SCQ = "QTIEDIT:SCQ:";
    public static final String ITEM_PREFIX_MCQ = "QTIEDIT:MCQ:";
    public static final String ITEM_PREFIX_FIB = "QTIEDIT:FIB:";
    public static final String ITEM_PREFIX_ESSAY = "QTIEDIT:ESSAY:";
    public static final String ITEM_PREFIX_KPRIM = "QTIEDIT:KPRIM:";

    private final ParserManager parserManager = new ParserManager();

    /**
	 */
    @Override
    public Object parse(final Element element) {
        // assert element.getName().equalsIgnoreCase("item");
        final Item item = new Item();
        Attribute tmp = element.attribute("ident");
        if (tmp != null) {
            item.setIdent(tmp.getValue());
        } else {
            item.setIdent("" + CodeHelper.getRAMUniqueID());
        }

        tmp = element.attribute("title");
        if (tmp != null) {
            item.setTitle(tmp.getValue());
        }

        tmp = element.attribute("label");
        if (tmp != null) {
            item.setLabel(tmp.getValue());
        }

        tmp = element.attribute("maxattempts");
        if (tmp != null) {
            try {
                item.setMaxattempts(Integer.parseInt(tmp.getValue()));
            } catch (final NumberFormatException nfe) {
                item.setMaxattempts(0);
            }
        }

        // if editor can't handle type of item, just keep raw XML
        if (!(item.getIdent().startsWith(ITEM_PREFIX_SCQ) || item.getIdent().startsWith(ITEM_PREFIX_MCQ) || item.getIdent().startsWith(ITEM_PREFIX_FIB)
                || item.getIdent().startsWith(ITEM_PREFIX_ESSAY) || item.getIdent().startsWith(ITEM_PREFIX_KPRIM))) {
            item.setRawXML(new QTIXMLWrapper(element));
            return item;
        }

        // exported olat surveys don't have the correct essay prefix. Search
        // for render_fib that contains rows attribute and convert them to essay
        if (item.getIdent().startsWith(ITEM_PREFIX_FIB) && element.selectNodes(".//render_fib[@rows]").size() > 0) {
            item.setIdent(item.getIdent().replaceFirst("FIB", "ESSAY"));
        }

        // DURATION
        final Duration duration = (Duration) parserManager.parse(element.element("duration"));
        item.setDuration(duration);

        // CONTROLS
        final List itemcontrolsXML = element.elements("itemcontrol");
        final List itemcontrols = new ArrayList();
        for (final Iterator i = itemcontrolsXML.iterator(); i.hasNext();) {
            itemcontrols.add(parserManager.parse((Element) i.next()));
        }
        if (itemcontrols.size() == 0) {
            itemcontrols.add(new Control());
        }
        item.setItemcontrols(itemcontrols);

        // OBJECTIVES
        final Element mattext = (Element) element.selectSingleNode("./objectives/material/mattext");
        if (mattext != null) {
            item.setObjectives(mattext.getTextTrim());
        }

        // QUESTIONS
        if (item.getIdent().startsWith(ITEM_PREFIX_SCQ)) {
            item.setQuestion(ChoiceQuestion.getInstance(element));
        } else if (item.getIdent().startsWith(ITEM_PREFIX_MCQ)) {
            item.setQuestion(ChoiceQuestion.getInstance(element));
        } else if (item.getIdent().startsWith(ITEM_PREFIX_FIB)) {
            item.setQuestion(FIBQuestion.getInstance(element));
        } else if (item.getIdent().startsWith(ITEM_PREFIX_ESSAY)) {
            item.setQuestion(EssayQuestion.getInstance(element));
        } else if (item.getIdent().startsWith(ITEM_PREFIX_KPRIM)) {
            item.setQuestion(ChoiceQuestion.getInstance(element));
        }

        // FEEDBACKS
        final List feedbacksXML = element.elements("itemfeedback");
        final List feedbacks = new ArrayList();
        item.setItemfeedbacks(feedbacks);
        final Question question = item.getQuestion();
        for (final Iterator i = feedbacksXML.iterator(); i.hasNext();) {
            final Element el_feedback = (Element) i.next();
            if (el_feedback.element("solution") != null) { // fetch solution
                final Element el_solution = el_feedback.element("solution");
                question.setSolutionText(getMaterialAsString(el_solution));
            } else if (el_feedback.element("hint") != null) { // fetch hint
                final Element el_hint = el_feedback.element("hint");
                question.setHintText(getMaterialAsString(el_hint));
            } else {
                final QTIObject tmpObj = (QTIObject) parserManager.parse(el_feedback);
                if (tmpObj != null) {
                    feedbacks.add(tmpObj);
                }
            }
        }

        return item;
    }

    private String getMaterialAsString(final Element el_root) {
        final StringBuilder result = new StringBuilder();
        final List materials = el_root.selectNodes(".//mattext");
        for (final Iterator iter = materials.iterator(); iter.hasNext();) {
            final Element el_mattext = (Element) iter.next();
            result.append(el_mattext.getTextTrim() + "\n");
        }
        return result.toString();
    }
}
