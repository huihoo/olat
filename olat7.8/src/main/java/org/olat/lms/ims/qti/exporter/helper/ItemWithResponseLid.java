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

package org.olat.lms.ims.qti.exporter.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;
import org.olat.data.qti.QTIResult;
import org.olat.lms.ims.qti.parser.ItemParser;
import org.olat.lms.qti.QTIResultServiceImpl;

/**
 * @author
 */

public class ItemWithResponseLid implements QTIItemObject {
    private boolean isSingle;
    private String itemIdent = null;
    private String itemTitle = null;
    private String itemMinValue = null;
    private String itemMaxValue = null;
    private String itemCutValue = null;

    // CELFI#107
    private String questionText = "";
    // CELFI#107 END

    private String positionsOfResponses = null;
    private final List<String> responseColumnHeaders = new ArrayList<String>(5);
    private final List<String> responseLabelIdents = new ArrayList<String>(5);
    private final List<String> responseLabelMaterials = new ArrayList<String>(5);

    /**
     * Constructor for ItemWithResponseLid.
     * 
     * @param el_item
     */
    public ItemWithResponseLid(final Element el_item) {
        this.itemTitle = el_item.attributeValue("title");
        this.itemIdent = el_item.attributeValue("ident");

        final List<Element> responseLids = el_item.selectNodes(".//response_lid");

        // Question text
        // CELFI#107
        final Node temp = el_item.selectSingleNode(".//presentation/material/mattext");
        if (temp != null) {
            this.questionText = ((Element) temp).getTextTrim();
        }

        int i = 1;
        for (final Iterator<Element> itresponseLid = responseLids.iterator(); itresponseLid.hasNext();) {
            final Element el_responseLid = itresponseLid.next();
            isSingle = el_responseLid.attributeValue("rcardinality").equals("Single");

            final List<Element> labels = el_responseLid.selectNodes(".//response_label");
            final Element decvar = (Element) el_item.selectSingleNode(".//outcomes/decvar");
            if (decvar != null) {
                this.itemMinValue = decvar.attributeValue("minvalue");
                this.itemMaxValue = decvar.attributeValue("maxvalue");
                this.itemCutValue = decvar.attributeValue("cutvalue");
            }

            for (final Iterator<Element> itlabel = labels.iterator(); itlabel.hasNext();) {
                final Element el_label = itlabel.next();
                final String sIdent = el_label.attributeValue("ident");
                responseLabelIdents.add(sIdent);

                final List<Element> materials = el_label.selectNodes(".//mattext");
                final StringBuilder mat = new StringBuilder();
                for (final Iterator<Element> itmaterial = materials.iterator(); itmaterial.hasNext();) {
                    final Element el_material = itmaterial.next();
                    mat.append(el_material.getText());
                }
                responseLabelMaterials.add(mat.length() == 0 ? "IDENT: " + sIdent : mat.toString());
                responseColumnHeaders.add((isSingle ? "R" : "C") + i); // R -> Radio button, C -> Check box
                i++;
            }

        }

    }

    /**
	 */
    @Override
    public int getNumColumnHeaders() {
        return responseColumnHeaders.size();
    }

    /**
	 */
    @Override
    public QTIResult extractQTIResult(final List<QTIResult> resultSet) {
        for (final Iterator<QTIResult> iter = resultSet.iterator(); iter.hasNext();) {
            final QTIResult element = iter.next();
            if (element.getItemIdent().equals(itemIdent)) {
                resultSet.remove(element);
                return element;
            }
        }
        return null;
    }

    private void addTextAndTabs(final List<String> responseColumns, final String s, final int num) {
        for (int i = 0; i < num; i++) {
            responseColumns.add(s);
        }
    }

    @Override
    public String getItemTitle() {
        return itemTitle;
    }

    // CELFI#107
    @Override
    public String getQuestionText() {
        return this.questionText;
    }

    @Override
    public List<String> getResponseColumnHeaders() {
        return responseColumnHeaders;
    }

    /**
	 */
    @Override
    public List<String> getResponseColumns(final QTIResult qtiresult) {
        final List<String> responseColumns = new ArrayList<String>();
        positionsOfResponses = null;
        if (qtiresult == null) {
            // item has not been choosen
            addTextAndTabs(responseColumns, "", getNumColumnHeaders());
        } else {
            final String answer = qtiresult.getAnswer();
            // item submitted without choosing any checkboxes at all
            final boolean submittedWithoutChoosing = answer.equals("[]");
            // test started and finished without submitting item
            final boolean finishedWithoutSubmitting = answer.equals("");

            if (finishedWithoutSubmitting) {
                addTextAndTabs(responseColumns, "", getNumColumnHeaders());
                return responseColumns;
            }
            final String itemIdentifier = qtiresult.getItemIdent();

            // special case KPRIM
            if (itemIdentifier.startsWith(ItemParser.ITEM_PREFIX_KPRIM)) {
                final List<String> answerList = QTIResultServiceImpl.parseResponseLidAnswers(answer);
                final StringBuilder sb = new StringBuilder();

                int pos = 0;
                boolean firstAppendDone = false;
                for (final Iterator<String> iter = responseLabelIdents.iterator(); iter.hasNext();) {
                    final String labelid = iter.next();
                    boolean foundLabelId = false;
                    for (final Iterator<String> iterator = answerList.iterator(); iterator.hasNext();) {
                        final String answerid = iterator.next();
                        if (answerid.startsWith(labelid)) {
                            pos++;
                            if (answerid.endsWith("correct")) {
                                responseColumns.add("+");
                                if (firstAppendDone) {
                                    sb.append(" ");
                                }
                                sb.append(String.valueOf(pos));
                                pos++;
                            } else {
                                responseColumns.add("-");
                                if (firstAppendDone) {
                                    sb.append(" ");
                                }
                                pos++;
                                sb.append(String.valueOf(pos));
                            }
                            firstAppendDone = true;
                            foundLabelId = true;
                        }
                    }
                    if (!foundLabelId) {
                        responseColumns.add(".");
                        if (firstAppendDone) {
                            sb.append(" ");
                        }
                        sb.append("0");
                        firstAppendDone = true;
                        pos = pos + 2;
                    }
                }
                positionsOfResponses = sb.toString();
            } else if (submittedWithoutChoosing) {
                addTextAndTabs(responseColumns, ".", getNumColumnHeaders());
                positionsOfResponses = null;
            } else if (finishedWithoutSubmitting) {
                addTextAndTabs(responseColumns, "", getNumColumnHeaders());
            } else {
                final List<String> answerList = QTIResultServiceImpl.parseResponseLidAnswers(answer);
                final StringBuilder sb = new StringBuilder();
                int pos = 1;
                boolean firstLoopDone = false;
                for (final Iterator<String> iter = responseLabelIdents.iterator(); iter.hasNext();) {
                    final String element = iter.next();
                    if (answerList.contains(element)) {
                        responseColumns.add("1");
                        if (firstLoopDone) {
                            sb.append(" ");
                        }
                        sb.append(String.valueOf(pos));
                        firstLoopDone = true;
                    } else {
                        responseColumns.add("0");
                    }
                    pos++;
                }
                positionsOfResponses = sb.toString();
            }
        }

        return responseColumns;
    }

    @Override
    public TYPE getItemType() {
        return isSingle ? TYPE.R : TYPE.C;
    }

    @Override
    public List<String> getResponseIdentifier() {
        return responseLabelIdents;
    }

    @Override
    public List<String> getResponseLabelMaterials() {
        return responseLabelMaterials;
    }

    /**
	 */
    @Override
    public String getItemIdent() {
        return itemIdent;
    }

    @Override
    public String getItemMinValue() {
        return itemMinValue;
    }

    @Override
    public String getItemMaxValue() {
        return itemMaxValue;
    }

    @Override
    public String getItemCutValue() {
        return itemCutValue;
    }

    @Override
    public boolean hasPositionsOfResponses() {
        return true;
    }

    @Override
    public String getPositionsOfResponses() {
        return positionsOfResponses;
    }

}
