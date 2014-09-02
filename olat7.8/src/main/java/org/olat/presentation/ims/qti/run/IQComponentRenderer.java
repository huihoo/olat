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

package org.olat.presentation.ims.qti.run;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.olat.lms.ims.qti.QTIConstants;
import org.olat.lms.ims.qti.container.AssessmentContext;
import org.olat.lms.ims.qti.container.ItemContext;
import org.olat.lms.ims.qti.container.Output;
import org.olat.lms.ims.qti.container.SectionContext;
import org.olat.lms.ims.qti.container.qtielements.GenericQTIElement;
import org.olat.lms.ims.qti.container.qtielements.Hint;
import org.olat.lms.ims.qti.container.qtielements.Item;
import org.olat.lms.ims.qti.container.qtielements.ItemFeedback;
import org.olat.lms.ims.qti.container.qtielements.Material;
import org.olat.lms.ims.qti.container.qtielements.Objectives;
import org.olat.lms.ims.qti.container.qtielements.RenderInstructions;
import org.olat.lms.ims.qti.container.qtielements.Solution;
import org.olat.lms.ims.qti.navigator.Info;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.ims.qti.process.Resolver;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;

/**
 * @author Felix Jost
 */
public class IQComponentRenderer implements ComponentRenderer {

    @Deprecated
    // TODO remove as soon OLAT-6824 is solved
    protected static final Logger OLAT6824Logger = Logger.getLogger("OLAT-6824");

    /**
     * Default constructor
     */
    public IQComponentRenderer() {
        super();
    }

    /**
     * Render the QTI form
     * 
     * @param comp
     * @param translator
     * @param renderer
     * @return rendered form
     */
    private StringOutput buildForm(final IQComponent comp, final Translator translator, final Renderer renderer, final URLBuilder ubu) {
        final StringOutput sb = new StringOutput();
        final Info info = comp.getAssessmentInstance().getNavigator().getInfo();
        final AssessmentInstance ai = comp.getAssessmentInstance();
        final AssessmentContext act = ai.getAssessmentContext();

        boolean displaySingleItemFeedback = false;

        // first treat messages and errors
        if (info.containsMessage()) {
            switch (info.getMessage()) {
            case QTIConstants.MESSAGE_ITEM_SUBMITTED:
                // item hints?
                if (info.isHint()) {
                    final Hint el_hint = info.getCurrentOutput().getHint();
                    if (el_hint.getFeedbackstyle() == Hint.FEEDBACKSTYLE_INCREMENTAL) {
                        // increase the hint level so we know which hint to display
                        final ItemContext itc = act.getCurrentSectionContext().getCurrentItemContext();
                        int nLevel = itc.getHintLevel() + 1;
                        final int numofhints = el_hint.getChildCount();
                        if (nLevel > numofhints) {
                            nLevel = numofhints;
                        }
                        itc.setHintLevel(nLevel);
                        // <!ELEMENT hint (qticomment? , hintmaterial+)>

                        displayFeedback(sb, (GenericQTIElement) el_hint.getChildAt(nLevel - 1), ai, translator.getLocale());
                    } else {
                        displayFeedback(sb, el_hint, ai, translator.getLocale());
                    }
                }
                // item solution?
                if (info.isSolution()) {
                    final Solution el_solution = info.getCurrentOutput().getSolution();
                    displayFeedback(sb, el_solution, ai, translator.getLocale());
                }
                // item fb?
                if (info.isFeedback() && info.getCurrentOutput().hasItem_Responses()) {
                    final int fbcount = info.getCurrentOutput().getFeedbackCount();
                    int i = 0;
                    while (i < fbcount) {
                        final Element elemAnswerChosen = info.getCurrentOutput().getItemAnswerChosen(i);
                        if (elemAnswerChosen != null) {
                            sb.append("<br /><br /><i>");
                            displayFeedback(sb, new Material(elemAnswerChosen), ai, translator.getLocale());
                            sb.append("</i>");
                        }
                        final Element elemFeedback = info.getCurrentOutput().getItemFeedback(i);
                        displayFeedback(sb, new ItemFeedback(elemFeedback), ai, translator.getLocale());
                        i++;
                    }

                    // if Menu not visible or if visible but not selectable and itemPage sequence (one question per page)
                    // display feedback for CURRENT item and render "next" button to proceed with subsequent item
                    final IQMenuDisplayConf menuDisplayConfig = comp.getMenuDisplayConf();
                    if (!menuDisplayConfig.isEnabledMenu() && menuDisplayConfig.isItemPageSequence()) {
                        displaySingleItemFeedback = true;
                    }
                }
                if (!comp.getMenuDisplayConf().isEnabledMenu() && comp.getMenuDisplayConf().isItemPageSequence() && !info.isRenderItems()) {
                    // if item was submitted and sequence is pageSequence and menu not enabled and isRenderItems returns false show section info
                    final SectionContext sc = ai.getAssessmentContext().getCurrentSectionContext();
                    displaySectionInfo(sb, sc, ai, comp, ubu, translator);
                }
                break;

            case QTIConstants.MESSAGE_SECTION_SUBMITTED:
                // provide section feedback if enabled and existing
                // SectionContext sc = act.getCurrentSectionContext();
                if (info.isFeedback()) {
                    final Output outp = info.getCurrentOutput();
                    final GenericQTIElement el_feedback = outp.getEl_response();
                    if (el_feedback != null) {
                        displayFeedback(sb, el_feedback, ai, translator.getLocale());
                    }
                }
                if (!comp.getMenuDisplayConf().isEnabledMenu() && !comp.getMenuDisplayConf().isItemPageSequence()) {
                    final SectionContext sc = ai.getAssessmentContext().getCurrentSectionContext();
                    displaySectionInfo(sb, sc, ai, comp, ubu, translator);
                }
                break;

            case QTIConstants.MESSAGE_ASSESSMENT_SUBMITTED:
                // provide assessment feedback if enabled and existing
                if (info.isFeedback()) {
                    final Output outp = info.getCurrentOutput();
                    final GenericQTIElement el_feedback = outp.getEl_response();
                    if (el_feedback != null) {
                        displayFeedback(sb, el_feedback, ai, translator.getLocale());
                    }
                }
                break;

            case QTIConstants.MESSAGE_SECTION_INFODEMANDED: // for menu item navigator
                // provide some stats maybe
                final SectionContext sc = ai.getAssessmentContext().getCurrentSectionContext();
                displaySectionInfo(sb, sc, ai, comp, ubu, translator);
                break;

            case QTIConstants.MESSAGE_ASSESSMENT_INFODEMANDED: // at the start of the test
                displayAssessmentInfo(sb, act, ai, comp, ubu, translator);
                break;
            }
        }

        if (info.isRenderItems()) {
            boolean displayForm = true;
            // First check wether we need to render a form.
            // No form is needed if the current item has a matapplet object to be displayed.
            // Matapplets will send their response back directly.
            final SectionContext sct = act.getCurrentSectionContext();
            ItemContext itc = null;
            if (sct != null && !ai.isSectionPage()) {
                itc = sct.getCurrentItemContext();
                if (itc != null) {
                    final Item item = itc.getQtiItem();
                    if (item.getQTIIdent().startsWith("QTIEDIT:FLA:")) {
                        displayForm = false;
                    }
                }
            }

            // do not display form with button in case no more item is open
            if (sct != null && ai.isSectionPage()) {
                displayForm = sct.getItemsOpenCount() > 0;
            }

            sb.append("<form action=\"");
            ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "sitse" });

            sb.append("\" id=\"ofo_iq_item\" method=\"post\">");

            String memoId = null;
            String memoTx = "";
            final boolean memo = comp.provideMemoField();

            if (!ai.isSectionPage()) {
                if (itc != null) {
                    OLAT6824Logger.debug("OLAT-6824 displayItem");
                    displayItem(sb, renderer, ubu, itc, ai, displaySingleItemFeedback);
                    if (memo) {
                        memoId = itc.getIdent();
                        memoTx = ai.getMemo(memoId);
                    }
                } else {
                    OLAT6824Logger.warn("OLAT-6824 [itc==null].", new RuntimeException("OLAT-6824 stacktrace"));
                }
            } else {
                if (sct != null && sct.getItemContextCount() != 0) {
                    OLAT6824Logger.debug("OLAT-6824 displayItems");
                    displayItems(sb, renderer, ubu, sct, ai, displaySingleItemFeedback);
                    if (memo) {
                        memoId = sct.getIdent();
                        memoTx = ai.getMemo(memoId);
                    }
                } else {
                    OLAT6824Logger.warn("OLAT-6824 [sct==null || sct.getItemContextCount() == 0].", new RuntimeException("OLAT-6824 stacktrace"));
                }
            }

            boolean isDefaultMemo = false;
            if (memo) {
                if (memoTx == null) {
                    isDefaultMemo = true;
                    memoTx = translator.translate("qti.memofield.text");
                } else {
                    memoTx = unescape(memoTx);
                }
            }

            sb.append("<div class=\"b_subcolumns\">");
            sb.append("<div class=\"b_c33l\">");

            if (!displaySingleItemFeedback) {
                // render submit button
                sb.append("<input class=\"b_button\" type=\"submit\" name=\"olat_fosm\" value=\"");
                if (ai.isSectionPage()) {
                    sb.append(StringEscapeUtils.escapeHtml(translator.translate("submitMultiAnswers")));
                } else {
                    sb.append(StringEscapeUtils.escapeHtml(translator.translate("submitSingleAnswer")));
                }
                sb.append("\"");
                if (!displayForm) {
                    sb.append(" style=\"display: none;\"");
                }
                sb.append(" />");
            } else {
                // render "next" button to proceed with subsequent item
                sb.append("<a class=\"b_button\" onclick=\"return o2cl()\" href=\"");
                ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "gitnext" });
                final String title = translator.translate("next");
                sb.append("\" title=\"" + StringEscapeUtils.escapeHtml(title) + "\">");
                sb.append("<span>").append(title).append("</title>");
                sb.append("</a>");
            }

            sb.append("</div><div class=\"b_c66r\">");

            if (memo && memoId != null) {
                sb.append("<div class=\"o_qti_item_note_box_title\">");
                sb.append(translator.translate("qti.memofield"));
                sb.append("<div class=\"o_qti_item_note_box\">");
                sb.append("<textarea id=\"o_qti_item_note\" rows=\"2\" spellcheck=\"false\" onchange=\"memo('");
                sb.append(memoId);
                sb.append("', this.value);\" onkeyup=\"resize(this);\" onmouseup=\"resize(this);\"");
                if (isDefaultMemo) {
                    sb.append(" onfocus=\"clrMemo(this);\"");
                }
                sb.append(">");
                sb.append(memoTx);
                sb.append("</textarea>");
                sb.append("</div>");
                sb.append("</div>");
            }

            sb.append("</div>");

            sb.append("</form>");

            sb.append("</div>");

            OLAT6824Logger.debug("OLAT-6824: rendered form [" + sb + "]");
        }

        if (info.getStatus() == QTIConstants.ASSESSMENT_FINISHED) {
            if (info.isFeedback()) {
                final Output outp = info.getCurrentOutput();
                final GenericQTIElement el_feedback = outp.getEl_response();
                if (el_feedback != null) {
                    displayFeedback(sb, el_feedback, ai, null);
                }
            }
        }

        return sb;
    }

    protected static String getFormattedLimit(final long millis) {
        long sSec = millis / 1000;
        final long sMin = sSec / 60;
        sSec = sSec - (sMin * 60);
        final StringOutput sb = new StringOutput();
        sb.append(sMin);
        sb.append("'&nbsp;");
        sb.append(sSec);
        sb.append("\"");
        return sb.toString();
    }

    private StringOutput addItemLink(final Renderer r, final URLBuilder ubu, final Formatter formatter, final AssessmentInstance ai, final ItemContext itc,
            final int sectionPos, final int itemPos, final boolean clickable, final boolean active, final boolean info) {
        final StringOutput sb = new StringOutput();

        sb.append("<td>");
        final String title = itc.getEl_item().attributeValue("title", "no title");
        final String titleShort = Formatter.truncate(title, 27);
        final long maxdur = itc.getDurationLimit();
        final long start = itc.getTimeOfStart();
        final long due = start + maxdur;
        final boolean started = (start != -1);
        final boolean timelimit = (maxdur != -1);
        final String fdue = (started && timelimit ? formatter.formatTimeShort(new Date(due)) : null);
        if (active) {
            sb.append("<div class=\"o_qti_menu_item_active\">");
        } else {
            if (itc.isOpen() && clickable) {
                sb.append("<div class=\"o_qti_menu_item\">");
            } else {
                sb.append("<div class=\"o_qti_menu_item_inactive\">");
            }
        }

        if (clickable) {
            sb.append("<a onclick=\"return o2cl();\" href=\"");
            ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "git" });
            sb.append("?itid=" + itemPos + "&seid=" + sectionPos);
            sb.append("\" title=\"" + StringEscapeUtils.escapeHtml(title) + "\">");
        }

        sb.append("<b>" + (sectionPos + 1) + "." + (itemPos + 1) + ".</b>&nbsp;");
        sb.append(StringEscapeUtils.escapeHtml(titleShort));

        if (clickable) {
            sb.append("</a>");
        }

        sb.append("</div>");
        sb.append("</td>");

        if (!itc.isOpen()) {
            sb.append("<td></td>"); // no time limit symbol
            // add lock image
            sb.append("<td>");
            sb.append("<div class=\"b_small_icon o_qti_closed_icon\" title=\"");
            sb.append(StringEscapeUtils.escapeHtml(r.getTranslator().translate("itemclosed")));
            sb.append("\"></div>");
            sb.append("</td>");
        } else if (info) {
            // max duration info
            sb.append("<td>");
            if (maxdur != -1) {
                sb.append("<div class=\"b_small_icon o_qti_timelimit_icon\" title=\"");
                if (!itc.isStarted()) {
                    sb.append(StringEscapeUtils.escapeHtml(r.getTranslator().translate("timelimit.initial", new String[] { getFormattedLimit(maxdur) })));
                } else {
                    sb.append(StringEscapeUtils.escapeHtml(r.getTranslator().translate("timelimit.running", new String[] { fdue })));
                }
                sb.append("\" ></div>");
            }
            sb.append("</td>");

            sb.append("<td>");
            // attempts info
            final int maxa = itc.getMaxAttempts();
            final int attempts = itc.getTimesAnswered();
            if (maxa != -1) { // only limited times of answers
                sb.append("<div class=\"b_small_icon o_qti_attemptslimit_icon\" title=\"");
                sb.append(StringEscapeUtils.escapeHtml(r.getTranslator().translate("attemptsleft", new String[] { "" + (maxa - attempts) })));
                sb.append("\" ></div>");
            }
            sb.append("</td>");
        }

        sb.append("<td>");
        sb.append("<div id=\"" + itc.getIdent() + "\" class=\"o_qti_menu_item_attempts");
        final String t = Integer.toString(itc.getTimesAnswered());
        final String n = r.getTranslator().translate("qti.marker.title", new String[] { t });
        final String m = r.getTranslator().translate("qti.marker.title.marked", new String[] { t });
        if (ai.isMarked(itc.getIdent())) {
            sb.append("_marked");
        }
        sb.append("\" onclick=\"mark(this, '").append(n).append("','").append(m).append("')\" ");
        sb.append("title=\"");
        sb.append(ai.isMarked(itc.getIdent()) ? m : n);
        sb.append("\">");
        sb.append(t);
        sb.append("</div>");
        sb.append("</td>");

        return sb;
    }

    // menu stuff
    private StringOutput addSectionLink(final Renderer r, final URLBuilder ubu, final Formatter formatter, final SectionContext sc, final int sectionPos,
            boolean clickable, final boolean active, final boolean pagewise) {
        final StringOutput sb = new StringOutput();

        // section link
        sb.append("<td>");
        final String titleShort = Formatter.truncate(sc.getTitle(), 30);
        final long maxdur = sc.getDurationLimit();
        final long start = sc.getTimeOfStart();
        final long due = start + maxdur;
        final boolean started = (start != -1);
        final boolean timelimit = (maxdur != -1);
        final String fdue = (started && timelimit ? formatter.formatTimeShort(new Date(due)) : null);

        if (!sc.isOpen()) {
            clickable = false;
        }

        if (active) {
            if (pagewise) {
                sb.append("<div class=\"o_qti_menu_section_active\">");
            } else {
                sb.append("<div class=\"o_qti_menu_section\">");
            }
        } else {
            if (pagewise) {
                sb.append("<div class=\"o_qti_menu_section_clickable\">");
            } else {
                sb.append("<div class=\"o_qti_menu_section\">");
            }
        }

        if (clickable) {
            sb.append("<a onclick=\"return o2cl()\" href=\"");
            ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "gse" });
            sb.append("?seid=" + sectionPos);
            sb.append("\" title=\"" + StringEscapeUtils.escapeHtml(sc.getTitle()) + "\">");
        }
        sb.append("<b>" + (sectionPos + 1) + ".</b>&nbsp;");
        sb.append(StringEscapeUtils.escapeHtml(titleShort));
        if (clickable) {
            sb.append("</a>");
        }
        sb.append("</div>");
        sb.append("</td>");

        sb.append("<td>");
        if (!sc.isOpen()) {
            sb.append("<div class=\"b_small_icon o_qti_closed_icon\" title=\"");
            sb.append(StringEscapeUtils.escapeHtml(r.getTranslator().translate("itemclosed")));
            sb.append("\"></div>");
        } else {
            // max duration info
            if (maxdur != -1) {
                sb.append("<div class=\"b_small_icon o_qti_timelimit_icon\" title=\"");
                if (!sc.isStarted()) {
                    sb.append(StringEscapeUtils.escapeHtml(r.getTranslator().translate("timelimit.initial", new String[] { getFormattedLimit(maxdur) })));
                } else {
                    sb.append(StringEscapeUtils.escapeHtml(r.getTranslator().translate("timelimit.running", new String[] { fdue })));
                }
                sb.append("\" ></div>");
            }
        }
        sb.append("</td>");

        sb.append("<td colspan=\"2\"></td>");

        return sb;
    }

    /**
     * Method buildMenu.
     * 
     * @return DOCUMENT ME!
     */
    private StringOutput buildMenu(final IQComponent comp, final Translator translator, final Renderer r, final URLBuilder ubu) {
        final StringOutput sb = new StringOutput();
        final AssessmentInstance ai = comp.getAssessmentInstance();
        final AssessmentContext ac = ai.getAssessmentContext();
        final boolean renderSectionTitlesOnly = comp.getMenuDisplayConf().isRenderSectionsOnly();

        sb.append("<div id=\"o_qti_menu\">");
        sb.append("<h4>");
        sb.append(ac.getTitle());
        sb.append("</h4>");

        sb.append("<table border=0 width=\"100%\">");

        // append assessment navigation
        final Formatter formatter = Formatter.getInstance(translator.getLocale());
        final int scnt = ac.getSectionContextCount();
        for (int i = 0; i < scnt; i++) {
            final SectionContext sc = ac.getSectionContext(i);
            boolean clickable = (ai.isSectionPage() && sc.isOpen()) || (!ai.isSectionPage());
            clickable = clickable && !ai.isClosed();
            clickable = clickable && ai.isMenu();

            sb.append("<tr>");
            sb.append(addSectionLink(r, ubu, formatter, sc, i, clickable, ac.getCurrentSectionContextPos() == i, ai.isSectionPage()));
            sb.append("</tr>");

            if (!renderSectionTitlesOnly) {
                // not only sections, but render questions to
                final int icnt = sc.getItemContextCount();
                for (int j = 0; j < icnt; j++) {
                    final ItemContext itc = sc.getItemContext(j);
                    clickable = !ai.isSectionPage() && sc.isOpen() && itc.isOpen();
                    clickable = clickable && !ai.isClosed();
                    clickable = clickable && ai.isMenu();
                    sb.append("<tr>");
                    sb.append(addItemLink(r, ubu, formatter, ai, itc, i, j, clickable, (ac.getCurrentSectionContextPos() == i && sc.getCurrentItemContextPos() == j),
                            !ai.isSurvey()));
                    sb.append("</tr>");
                }
            }
        }
        sb.append("</table>");
        sb.append("</div>");
        return sb;
    }

    private void displayItems(final StringOutput sb, final Renderer renderer, final URLBuilder ubu, final SectionContext sc, final AssessmentInstance ai, boolean readOnly) {
        // display the whole current section on one page
        final List<ItemContext> items = sc.getSectionItemContexts();
        for (final Iterator<ItemContext> iter = items.iterator(); iter.hasNext();) {
            final ItemContext itc = iter.next();
            if (itc.isOpen()) {
                displayItem(sb, renderer, ubu, itc, ai, readOnly);
            } else {
                displayItemClosed(sb, renderer, itc);
            }
        }
    }

    /**
     * Display message : Item is closed, could not be displayed.
     * 
     * @param sb
     * @param renderer
     * @param itc
     */
    private void displayItemClosed(final StringOutput sb, final Renderer renderer, final ItemContext itc) {
        final StringBuilder buffer = new StringBuilder(100);
        buffer.append("<div class=\"b_warning\"><strong>").append(renderer.getTranslator().translate("couldNotDisplayItem")).append("</strong></div>");
        sb.append(buffer);
    }

    private void displayItem(final StringOutput sb, final Renderer renderer, final URLBuilder ubu, final ItemContext itc, final AssessmentInstance ai, boolean readOnly) {
        final StringBuilder buffer = new StringBuilder(1000);
        final Resolver resolver = ai.getResolver();
        final RenderInstructions ri = new RenderInstructions();
        ri.put(RenderInstructions.KEY_STATICS_PATH, resolver.getStaticsBaseURI() + "/");
        ri.put(RenderInstructions.KEY_LOCALE, renderer.getTranslator().getLocale());
        final StringOutput soCommandURI = new StringOutput(50);
        ubu.buildURI(soCommandURI, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "sflash" });
        ri.put(RenderInstructions.KEY_APPLET_SUBMIT_URI, soCommandURI.toString());
        if (itc.getItemInput() != null) {
            ri.put(RenderInstructions.KEY_ITEM_INPUT, itc.getItemInput());
        }
        ri.put(RenderInstructions.KEY_RENDER_TITLE, Boolean.valueOf(ai.isDisplayTitles()));

        if (ai.isAutoEnum()) {
            final String k = renderer.getTranslator().translate("choices.autoenum.keys");
            if (k != null) {
                ri.put(RenderInstructions.KEY_RENDER_AUTOENUM_LIST, k);
            }
        }
        ri.put(RenderInstructions.KEY_RENDER_MODE, readOnly ? RenderInstructions.RENDER_MODE_STATIC : RenderInstructions.RENDER_MODE_FORM);

        itc.getQtiItem().render(buffer, ri);
        sb.append(buffer);
    }

    private void displaySectionInfo(final StringOutput sb, final SectionContext sc, final AssessmentInstance ai, final IQComponent comp, final URLBuilder ubu,
            final Translator translator) {
        // display the sectionInfo
        if (sc == null) {
            return;
        }
        if (ai.isDisplayTitles()) {
            sb.append("<h3>" + StringEscapeUtils.escapeHtml(sc.getTitle()) + "</h3>");
        }
        final Objectives objectives = sc.getObjectives();
        if (objectives != null) {
            final StringBuilder sbTmp = new StringBuilder();
            final Resolver resolver = ai.getResolver();
            final RenderInstructions ri = new RenderInstructions();
            ri.put(RenderInstructions.KEY_STATICS_PATH, resolver.getStaticsBaseURI() + "/");
            objectives.render(sbTmp, ri);
            sb.append(sbTmp);
        }
        // if Menu not visible, or if visible but not selectable, and itemPage sequence (one question per page)
        // show button to navigate to the first question of the current section
        final IQMenuDisplayConf menuDisplayConfig = comp.getMenuDisplayConf();
        if (!menuDisplayConfig.isEnabledMenu() && menuDisplayConfig.isItemPageSequence()) {
            sb.append("<a class=\"b_button\" onclick=\"return o2cl()\" href=\"");
            ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "git" });
            final AssessmentContext ac = ai.getAssessmentContext();
            final int sectionPos = ac.getCurrentSectionContextPos();
            sb.append("?itid=" + 0 + "&seid=" + sectionPos);
            final String title = translator.translate("next");
            sb.append("\" title=\"" + StringEscapeUtils.escapeHtml(title) + "\">");
            sb.append("<span>").append(StringEscapeUtils.escapeHtml(title)).append("</title>");
            sb.append("</a>");
        }
    }

    private void displayAssessmentInfo(final StringOutput sb, final AssessmentContext ac, final AssessmentInstance ai, final IQComponent comp, final URLBuilder ubu,
            final Translator translator) {
        final Objectives objectives = ac.getObjectives();
        if (objectives != null) {
            final StringBuilder sbTmp = new StringBuilder();
            final Resolver resolver = ai.getResolver();
            final RenderInstructions ri = new RenderInstructions();
            ri.put(RenderInstructions.KEY_STATICS_PATH, resolver.getStaticsBaseURI() + "/");
            objectives.render(sbTmp, ri);
            sb.append(sbTmp);
        }
        // if Menu not visible, or if visible but not selectable show button to navigate to the first section panel
        final IQMenuDisplayConf menuDisplayConfig = comp.getMenuDisplayConf();
        if (!menuDisplayConfig.isEnabledMenu()) {
            sb.append("<a class=\"b_button\" onclick=\"return o2cl()\" href=\"");
            ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "gse" });
            sb.append("?seid=" + 0);
            final String title = translator.translate("next");
            sb.append("\" title=\"" + StringEscapeUtils.escapeHtml(title) + "\">");
            sb.append("<span>").append(StringEscapeUtils.escapeHtml(title)).append("</span>");
            sb.append("</a>");
        }
    }

    private void displayFeedback(final StringOutput sb, final GenericQTIElement feedback, final AssessmentInstance ai, final Locale locale) {
        final StringBuilder sbTmp = new StringBuilder();
        final Resolver resolver = ai.getResolver();
        final RenderInstructions ri = new RenderInstructions();
        ri.put(RenderInstructions.KEY_STATICS_PATH, resolver.getStaticsBaseURI() + "/");
        ri.put(RenderInstructions.KEY_LOCALE, locale);
        feedback.render(sbTmp, ri);
        sb.append(sbTmp);
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(final Renderer renderer, final StringOutput target, final Component source, final URLBuilder ubu, final Translator translator,
            final RenderResult renderResult, final String[] args) {

        final IQComponent qticomp = (IQComponent) source;

        if (args[0].equals("menu")) { // render the menu
            target.append(buildMenu(qticomp, translator, renderer, ubu));
        } else if (args[0].equals("qtiform")) { // render the content
            target.append(buildForm(qticomp, translator, renderer, ubu));
        }
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public void renderHeaderIncludes(final Renderer renderer, final StringOutput sb, final Component source, final URLBuilder ubu, final Translator translator,
            final RenderingState rstate) {
        //
    }

    /**
     * org.olat.presentation.framework.components.Component)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(final Renderer renderer, final StringOutput sb, final Component source, final RenderingState rstate) {
        //
    }

    /*
     * Created: 17 April 1997 Author: Bert Bos <bert@w3.org> unescape: http://www.w3.org/International/unescape.java Copyright © 1997 World Wide Web Consortium,
     * (Massachusetts Institute of Technology, European Research Consortium for Informatics and Mathematics, Keio University). All Rights Reserved. This work is
     * distributed under the W3C® Software License [1] in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
     * or FITNESS FOR A PARTICULAR PURPOSE. [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
     */

    private static String unescape(final String s) {
        final StringBuffer sbuf = new StringBuffer();
        final int l = s.length();
        int ch = -1;
        int b, sumb = 0;
        for (int i = 0, more = -1; i < l; i++) {
            /* Get next byte b from URL segment s */
            switch (ch = s.charAt(i)) {
            case '%':
                ch = s.charAt(++i);
                final int hb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                ch = s.charAt(++i);
                final int lb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                b = (hb << 4) | lb;
                break;
            case '+':
                b = ' ';
                break;
            default:
                b = ch;
            }
            /* Decode byte b as UTF-8, sumb collects incomplete chars */
            if ((b & 0xc0) == 0x80) { // 10xxxxxx (continuation byte)
                sumb = (sumb << 6) | (b & 0x3f); // Add 6 bits to sumb
                if (--more == 0) {
                    sbuf.append((char) sumb); // Add char to sbuf
                }
            } else if ((b & 0x80) == 0x00) { // 0xxxxxxx (yields 7 bits)
                sbuf.append((char) b); // Store in sbuf
            } else if ((b & 0xe0) == 0xc0) { // 110xxxxx (yields 5 bits)
                sumb = b & 0x1f;
                more = 1; // Expect 1 more byte
            } else if ((b & 0xf0) == 0xe0) { // 1110xxxx (yields 4 bits)
                sumb = b & 0x0f;
                more = 2; // Expect 2 more bytes
            } else if ((b & 0xf8) == 0xf0) { // 11110xxx (yields 3 bits)
                sumb = b & 0x07;
                more = 3; // Expect 3 more bytes
            } else if ((b & 0xfc) == 0xf8) { // 111110xx (yields 2 bits)
                sumb = b & 0x03;
                more = 4; // Expect 4 more bytes
            } else /* if ((b & 0xfe) == 0xfc) */{ // 1111110x (yields 1 bit)
                sumb = b & 0x01;
                more = 5; // Expect 5 more bytes
            }
            /* We don't test if the UTF-8 encoding is well-formed */
        }
        return sbuf.toString();
    }
}
