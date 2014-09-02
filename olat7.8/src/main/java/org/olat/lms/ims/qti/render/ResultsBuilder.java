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

package org.olat.lms.ims.qti.render;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.ims.qti.container.AssessmentContext;
import org.olat.lms.ims.qti.container.DecimalVariable;
import org.olat.lms.ims.qti.container.ItemContext;
import org.olat.lms.ims.qti.container.ItemInput;
import org.olat.lms.ims.qti.container.SectionContext;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.ims.qti.process.QTIHelper;
import org.olat.lms.ims.qti.process.Resolver;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.ims.qti.QTIResultDetailsController;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Felix Jost
 */
public class ResultsBuilder {
    /**
     * <code>STATICS_PATH</code>
     */
    public static final String STATICS_PATH = "staticspath";

    /**
     * Constructor for ResultsRenderer.
     */
    public ResultsBuilder() {
        super();
    }

    /**
     * Method getResDoc.
     * 
     * @param ai
     *            The assessment instance
     * @param locale
     *            The users locale
     * @param identity
     * @return Document The XML document
     */
    public Document getResDoc(final AssessmentInstance ai, final Locale locale, final Identity identity) {
        final AssessmentContext ac = ai.getAssessmentContext();
        final DocumentFactory df = DocumentFactory.getInstance();
        final Document res_doc = df.createDocument();
        final Element root = df.createElement("qti_result_report");
        res_doc.setRootElement(root);
        final Element result = root.addElement("result");
        final Element extension_result = result.addElement("extension_result");

        // add items (not qti standard, but nice to display original questions ->
        // put it into extensions)
        // extension_result.
        final int sectioncnt = ac.getSectionContextCount();
        for (int i = 0; i < sectioncnt; i++) {
            final SectionContext sc = ac.getSectionContext(i);
            final int itemcnt = sc.getItemContextCount();
            for (int j = 0; j < itemcnt; j++) {
                final ItemContext it = sc.getItemContext(j);
                final Element el_item = it.getEl_item();
                extension_result.add(el_item);
            }
        }

        // add ims cp id for any media references
        addStaticsPath(extension_result, ai);

        // add assessment_result

        // Add User information
        final Element context = result.addElement("context");
        final User user = identity.getUser();
        final String name = getUserService().getFirstAndLastname(user) + " (" + identity.getName() + ")";
        String instId = getUserService().getInstitutionalIdentifier(user);
        final String instName = getUserService().getUserProperty(user, UserConstants.INSTITUTIONALNAME, locale);

        if (instId == null) {
            instId = "N/A";
        }
        context.addElement("name").addText(name);

        String institution;
        if (instName == null) {
            institution = "N/A";
        } else {
            institution = instName;
        }

        // Add institutional identifier (e.g. Matrikelnummer)
        final Element generic_identifier = context.addElement("generic_identifier");
        generic_identifier.addElement("type_label").addText(institution);
        generic_identifier.addElement("identifier_string").addText(instId);

        // Add start and stop date formatted as datetime
        final Element beginDate = context.addElement("date");
        beginDate.addElement("type_label").addText("Start");
        beginDate.addElement("datetime").addText(Formatter.formatDatetime(new Date(ac.getTimeOfStart())));
        final Element stopDate = context.addElement("date");
        stopDate.addElement("type_label").addText("Stop");
        stopDate.addElement("datetime").addText(Formatter.formatDatetime(new Date(ac.getTimeOfStop())));

        final Element ares = result.addElement("assessment_result");
        ares.addAttribute("ident_ref", ac.getIdent());
        if (ac.getTitle() != null) {
            ares.addAttribute("asi_title", ac.getTitle());
        }

        // process assessment score
        final Element a_score = ares.addElement("outcomes").addElement("score");
        a_score.addAttribute("varname", "SCORE");
        String strVal = StringHelper.formatFloat(ac.getScore(), 2);
        a_score.addElement("score_value").addText(strVal);

        strVal = ac.getMaxScore() == -1.0f ? "N/A" : StringHelper.formatFloat(ac.getMaxScore(), 2);
        a_score.addElement("score_max").addText(strVal);

        strVal = ac.getCutvalue() == -1.0f ? "N/A" : StringHelper.formatFloat(ac.getCutvalue(), 2);
        a_score.addElement("score_cut").addText(strVal);

        addElementText(ares, "duration", QTIHelper.getISODuration(ac.getDuration()));
        addElementText(ares, "num_sections", "" + ac.getSectionContextCount());
        addElementText(ares, "num_sections_presented", "0");
        addElementText(ares, "num_items", "" + ac.getItemContextCount());
        addElementText(ares, "num_items_presented", "" + ac.getItemsPresentedCount());
        addElementText(ares, "num_items_attempted", "" + ac.getItemsAttemptedCount());

        // add section_result
        final int secnt = ac.getSectionContextCount();
        for (int i = 0; i < secnt; i++) {
            final SectionContext secc = ac.getSectionContext(i);
            final Element secres = ares.addElement("section_result");
            secres.addAttribute("ident_ref", secc.getIdent());
            if (secc.getTitle() != null) {
                secres.addAttribute("asi_title", secc.getTitle());
            }
            addElementText(secres, "duration", QTIHelper.getISODuration(secc.getDuration()));
            addElementText(secres, "num_items", "" + secc.getItemContextCount());
            addElementText(secres, "num_items_presented", "" + secc.getItemsPresentedCount());
            addElementText(secres, "num_items_attempted", "" + secc.getItemsAttemptedCount());

            // process section score
            final Element sec_score = secres.addElement("outcomes").addElement("score");
            sec_score.addAttribute("varname", "SCORE");
            strVal = secc.getScore() == -1.0f ? "N/A" : "" + StringHelper.formatFloat(secc.getScore(), 2);
            sec_score.addElement("score_value").addText(strVal);
            strVal = secc.getMaxScore() == -1.0f ? "N/A" : "" + StringHelper.formatFloat(secc.getMaxScore(), 2);
            sec_score.addElement("score_max").addText(strVal);
            strVal = secc.getCutValue() == -1 ? "N/A" : "" + secc.getCutValue();
            sec_score.addElement("score_cut").addText(strVal);

            // iterate over all items in this section context
            final List itemsc = secc.getSectionItemContexts();
            for (final Iterator it_it = itemsc.iterator(); it_it.hasNext();) {
                final ItemContext itemc = (ItemContext) it_it.next();
                final Element itres = secres.addElement("item_result");
                itres.addAttribute("ident_ref", itemc.getIdent());
                itres.addAttribute("asi_title", itemc.getEl_item().attributeValue("title"));
                final Element it_duration = itres.addElement("duration");
                it_duration.addText(QTIHelper.getISODuration(itemc.getTimeSpent()));

                // process item score
                final DecimalVariable scoreVar = (DecimalVariable) (itemc.getVariables().getSCOREVariable());
                final Element it_score = itres.addElement("outcomes").addElement("score");
                it_score.addAttribute("varname", "SCORE");
                it_score.addElement("score_value").addText(StringHelper.formatFloat(scoreVar.getTruncatedValue(), 2));
                strVal = scoreVar.hasMinValue() ? "" + scoreVar.getMinValue() : "0.0";
                it_score.addElement("score_min").addText(strVal);
                strVal = scoreVar.hasMaxValue() ? "" + scoreVar.getMaxValue() : "N/A";
                it_score.addElement("score_max").addText(strVal);
                strVal = scoreVar.hasCutValue() ? "" + scoreVar.getCutValue() : "N/A";
                it_score.addElement("score_cut").addText(strVal);

                final Element el_item = itemc.getEl_item();
                final Map res_responsehash = new HashMap(3);

                // iterate over all responses of this item
                final List resps = el_item.selectNodes(".//response_lid|.//response_xy|.//response_str|.//response_num|.//response_grp");
                for (final Iterator it_resp = resps.iterator(); it_resp.hasNext();) {
                    final Element resp = (Element) it_resp.next();
                    final String ident = resp.attributeValue("ident");
                    final String rcardinality = resp.attributeValue("rcardinality");
                    final String rtiming = resp.attributeValue("rtiming");

                    // add new response
                    final Element res_response = itres.addElement("response");
                    res_response.addAttribute("ident_ref", ident);
                    res_responsehash.put(ident, res_response); // enable lookup of
                                                               // @identref of <response>
                                                               // (needed with <varequal>
                                                               // elements

                    // add new response_form
                    // <response_lid ident="MR01" rcardinality="Multiple" rtiming="No">
                    final Element res_responseform = res_response.addElement("response_form");
                    res_responseform.addAttribute("cardinality", rcardinality);
                    res_responseform.addAttribute("timing", rtiming);
                    final String respName = resp.getName();
                    final String type = respName.substring(respName.indexOf("_") + 1);
                    res_responseform.addAttribute("response_type", type);

                    // add user answer
                    final ItemInput itemInp = itemc.getItemInput();
                    final Translator trans = PackageUtil.createPackageTranslator(QTIResultDetailsController.class, locale);
                    if (itemInp == null) { // user did not answer this question at all
                        res_response.addElement("response_value").addText(trans.translate("ResBuilder.NoAnswer"));
                    } else {
                        final List userAnswer = itemInp.getAsList(ident);
                        if (userAnswer == null) { // user did not answer this question at
                                                  // all
                            res_response.addElement("response_value").addText(trans.translate("ResBuilder.NoAnswer"));
                        } else { // the user chose at least one option of an answer (did not
                                 // simply click send)
                            for (final Iterator it_ans = userAnswer.iterator(); it_ans.hasNext();) {
                                res_response.addElement("response_value").addText((String) it_ans.next());
                            }
                        }
                    }

                }

                /*
                 * The simple element correct_response can only list correct elements, that is, no "or" or "and" elements may be in the conditionvar. Pragmatic solution:
                 * if condition has ors or ands, then put whole conditionvar into <extension_response> (proprietary), and for easier cases (just "varequal" "not"
                 * elements) use correct_response.
                 */

                final Map corr_answers = new HashMap(); // keys: respIdents, values: HashSet
                // of correct answers for this
                // respIdent
                final List respconds = el_item.selectNodes(".//respcondition");
                for (final Iterator it_respc = respconds.iterator(); it_respc.hasNext();) {
                    final Element el_respc = (Element) it_respc.next();

                    // check for add/set in setvar elements (check for single instance
                    // only -> spec allows for multiple instances)
                    final Element el_setvar = (Element) el_respc.selectSingleNode(".//setvar");
                    if (el_setvar == null) {
                        continue;
                    }
                    if (el_setvar.attributeValue("action").equals("Add") || el_setvar.attributeValue("action").equals("Set")) {
                        // This resrocessing gives points -> assume correct answer
                        float numPoints = 0;
                        try {
                            numPoints = Float.parseFloat(el_setvar.getTextTrim());
                        } catch (final NumberFormatException nfe) {
                            //
                        }
                        if (numPoints <= 0) {
                            continue;
                        }
                        Element conditionvar = (Element) el_respc.selectSingleNode(".//conditionvar");
                        // there is an evaluation defined (a "resprocessing" element exists)
                        // if (xpath(count(.//varequal) + count(.//not) = count(.//*)) is
                        // true, then there are only "not" and "varequal" elements
                        final XPath xCanHandle = DocumentHelper.createXPath("count(.//varequal) + count(.//not) = count(.//*)");
                        boolean canHandle = xCanHandle.matches(conditionvar);
                        if (!canHandle) { // maybe we have <condvar> <and> <...>, try again
                            final Element el_and = (Element) conditionvar.selectSingleNode("and");
                            if (el_and != null) {
                                canHandle = xCanHandle.matches(el_and);
                                if (canHandle) { // simultate the el_and to be the conditionvar
                                    conditionvar = el_and;
                                }
                            } else { // and finally, maybe we have an <or> element ..
                                final Element el_or = (Element) conditionvar.selectSingleNode("or");
                                if (el_or != null) {
                                    canHandle = xCanHandle.matches(el_or);
                                    if (canHandle) { // simultate the el_and to be the conditionvar
                                        conditionvar = el_or;
                                    }
                                }
                            }
                        }

                        if (!canHandle) {
                            // qti res 1.2.1 can't handle it
                            final Element condcopy = conditionvar.createCopy();
                            itres.addElement("extension_item_result").add(condcopy);
                        } else {
                            /*
                             * easy case: get all varequal directly under the conditionvar element and assume the "not" elements do not contain "not" elements again...
                             * <!ELEMENT response (qti_comment? , response_form? , num_attempts? , response_value* , extension_response?)> <!ELEMENT response_form
                             * (correct_response* , extension_responseform?)> <!ELEMENT correct_response (#PCDATA)>
                             */
                            final List vareqs = conditionvar.selectNodes("./varequal");
                            for (final Iterator it_vareq = vareqs.iterator(); it_vareq.hasNext();) {
                                /*
                                 * get the identifier of the response, so that we can attach the <correct_response> to the right <response> element quote: ims qti asi xml
                                 * binding :3.6.23.1 <varequal> Element: respident (required). The identifier of the corresponding <response_lid>, <response_xy>, etc.
                                 * element (this was assigned using its ident attribute).
                                 */
                                final Element vareq = (Element) it_vareq.next();
                                final String respIdent = vareq.attributeValue("respident");
                                Set respIdent_corr_answers = (Set) corr_answers.get(respIdent);
                                if (respIdent_corr_answers == null) {
                                    respIdent_corr_answers = new HashSet(3);
                                }
                                respIdent_corr_answers.add(vareq.getText());
                                corr_answers.put(respIdent, respIdent_corr_answers);
                            } // for varequal
                        } // else varequal
                    } // add/set setvar
                } // for resprocessing
                final Set resp_ids = corr_answers.keySet();
                for (final Iterator idents = resp_ids.iterator(); idents.hasNext();) {
                    final String respIdent = (String) idents.next();
                    final Set respIdent_corr_answers = (Set) corr_answers.get(respIdent);
                    final Element res_response = (Element) res_responsehash.get(respIdent);
                    final Element res_respform = res_response.element("response_form");
                    for (final Iterator iter = respIdent_corr_answers.iterator(); iter.hasNext();) {
                        final String answer = (String) iter.next();
                        res_respform.addElement("correct_response").addText(answer);
                    }
                }
            } // for response_xy
        }
        return res_doc;
    }

    /**
     * Strip extension_result and section_result tags
     * 
     * @param doc
     */
    public static void stripDetails(final Document doc) {
        detachNodes("//extension_result", doc);
        detachNodes("//section_result", doc);
    }

    /**
     * Strip Item-Result tags.
     * 
     * @param doc
     */
    public static void stripItemResults(final Document doc) {
        detachNodes("//item_result", doc);
    }

    private void addElementText(final Element parent, final String child, final String text) {
        final Element el = parent.addElement(child);
        el.setText(text);
    }

    private static void detachNodes(final String xPath, final Document doc) {
        final List xpathres = doc.selectNodes(xPath);
        for (final Iterator iter = xpathres.iterator(); iter.hasNext();) {
            final Node element = (Node) iter.next();
            element.detach();
        }
    }

    private static void addStaticsPath(final Element el_in, final AssessmentInstance ai) {
        Element el_staticspath = (Element) el_in.selectSingleNode(STATICS_PATH);
        if (el_staticspath == null) {
            final DocumentFactory df = DocumentFactory.getInstance();
            el_staticspath = df.createElement(STATICS_PATH);
            final Resolver resolver = ai.getResolver();
            el_staticspath.addAttribute("ident", resolver.getStaticsBaseURI());
            el_in.add(el_staticspath);
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
