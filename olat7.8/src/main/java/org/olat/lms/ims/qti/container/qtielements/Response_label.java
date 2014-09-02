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

package org.olat.lms.ims.qti.container.qtielements;

import java.util.List;

import org.dom4j.Element;
import org.olat.lms.ims.qti.container.ItemInput;
import org.olat.system.commons.StringHelper;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: 25.11.2004
 * 
 * @author Mike Stock
 */
public class Response_label extends GenericQTIElement {

    /**
     * Comment for <code>xmlClass</code>
     */
    public static final String xmlClass = "response_label";
    private static String PARA = "§";

    /**
     * @param el_element
     */
    public Response_label(final Element el_element) {
        super(el_element);

    }

    /**
	 */
    @Override
    public void render(final StringBuilder buffer, final RenderInstructions ri) {
        final ItemInput iinput = (ItemInput) ri.get(RenderInstructions.KEY_ITEM_INPUT);
        final String responseIdent = (String) ri.get(RenderInstructions.KEY_RESPONSE_IDENT);
        // find parent render_xxx element
        final String renderClass = (String) ri.get(RenderInstructions.KEY_RENDER_CLASS);
        if (renderClass == null) {
            throw new AssertException("Render class must be set previousely to call respnse_label.render.");
        }
        int renderMode = RenderInstructions.RENDER_MODE_FORM;
        if (ri.containsKey(RenderInstructions.KEY_RENDER_MODE)) {
            renderMode = (Integer) ri.get(RenderInstructions.KEY_RENDER_MODE);
        }

        if (renderClass.equals("choice")) {
            // render multiple/single choice
            buffer.append("<div class=\"o_qti_item_choice_option");
            if (!wantBr(ri)) {
                buffer.append("_flow");
            }
            buffer.append("\">");

            Object o = ri.get(RenderInstructions.KEY_RENDER_AUTOENUM_LIST);
            if (o != null) {
                final String[] s = o.toString().split(",");
                o = ri.get(RenderInstructions.KEY_RENDER_AUTOENUM_IDX);

                final int i = o == null ? 0 : Integer.valueOf(o.toString());
                buffer.append("<div class=\"o_qti_item_choice_option_autoenum\">");
                if (s.length > i) {
                    buffer.append("<span>").append(s[i]).append("</span>");
                    ri.put(RenderInstructions.KEY_RENDER_AUTOENUM_IDX, Integer.toString(i + 1));
                }
                buffer.append("</div>");
            }

            final Integer rCardinality = (Integer) ri.get(RenderInstructions.KEY_RESPONSE_RCARDINALITY);
            if (rCardinality == null) {
                throw new AssertException("Cardinality must be set previousely to call respnse_label.render for a render_choice class.");
            }

            buffer.append("<div class=\"o_qti_item_choice_option_input\">");
            if (rCardinality.intValue() == Response_lid.RCARDINALITY_SINGLE) {

                // single choice
                buffer.append("<input id=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent())
                        .append("\" type=\"radio\" class=\"b_radio\" name=\"");
                buffer.append("qti").append(PARA).append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(PARA).append(ri.get(RenderInstructions.KEY_RESPONSE_IDENT))
                        .append(PARA).append("choice");
                buffer.append("\" value=\"").append(getQTIIdent());
                if (iinput != null && !iinput.isEmpty()) {
                    final String response = iinput.getSingle(responseIdent);
                    if (response.equals(getQTIIdent())) {
                        buffer.append("\" checked=\"checked");
                    }
                }
                if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
                    buffer.append("\" disabled=\"disabled");
                }
                buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\" />");

            } else if (rCardinality.intValue() == Response_lid.RCARDINALITY_MULTIPLE) {
                // multiple choice

                buffer.append("<input id=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent())
                        .append("\" type=\"checkbox\" class=\"b_checkbox\" name=\"");
                appendParameterIdent(buffer, ri);
                buffer.append("\" value=\"").append(getQTIIdent());
                if (iinput != null) {
                    final List<String> responses = iinput.getAsList(responseIdent);
                    if (responses != null && responses.contains(getQTIIdent())) {
                        buffer.append("\" checked=\"checked");
                    }
                }
                if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
                    buffer.append("\" disabled=\"disabled");
                }
                buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\" />");
            }
            buffer.append("</div>");

            // support accessibility that plain HTML provides
            buffer.append("<div class=\"o_qti_item_choice_option_value\">");
            buffer.append("<label for=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent()).append("\">");

            super.render(buffer, ri);

            buffer.append("</label>");
            buffer.append("</div>");

            buffer.append("</div>");

        } else if (renderClass.equals("kprim")) {
            buffer.append("<tr><td align=\"center\"><input id=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent())
                    .append("\" type=\"radio\" class=\"b_radio\" name=\"");
            appendParameterIdent(buffer, ri);
            buffer.append("\" value=\"" + getQTIIdent() + ":correct\"");
            if (iinput != null && !iinput.isEmpty()) {
                final List<String> responses = iinput.getAsList(responseIdent);
                if (responses != null && responses.contains(getQTIIdent() + ":correct")) {
                    buffer.append("\" checked=\"checked");
                }
            }
            if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
                buffer.append("\" disabled=\"disabled");
            }
            buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\"/>");
            buffer.append("</td><td align=\"center\"><input id=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent())
                    .append("\" type=\"radio\" class=\"b_radio\" name=\"");
            appendParameterIdent(buffer, ri);
            buffer.append("\" value=\"" + getQTIIdent() + ":wrong\"");
            if (iinput != null && !iinput.isEmpty()) {
                final List<String> responses = iinput.getAsList(responseIdent);
                if (responses != null && responses.contains(getQTIIdent() + ":wrong")) {
                    buffer.append("\" checked=\"checked");
                }
            }
            if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
                buffer.append("\" disabled=\"disabled");
            }
            buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\"/>");
            buffer.append("</td><td>");
            super.render(buffer, ri);
            buffer.append("</td></tr>");
            ri.put(RenderInstructions.KEY_FLOW_LABEL, new Integer(RenderInstructions.RENDER_FLOW_BLOCK));
            addBr(ri, buffer);

        } else if (renderClass.equals("fib")) {
            final Integer rows = (Integer) ri.get(RenderInstructions.KEY_FIB_ROWS);
            final Integer columns = (Integer) ri.get(RenderInstructions.KEY_FIB_COLUMNS);
            final Integer maxlength = (Integer) ri.get(RenderInstructions.KEY_FIB_MAXLENGTH);
            if (rows == null || columns == null || maxlength == null) {
                throw new AssertException("Rows and/or columns attribute not specified for render_fib.");
            }
            if (rows.intValue() > 1) {
                // render as textarea
                buffer.append("<textarea id=\"QTI_").append(getQTIIdent()).append("\" name=\"");
                appendParameterIdent(buffer, ri);
                if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
                    buffer.append("\" readonly=\"readonly");
                }
                buffer.append("\" rows=\"").append(rows).append("\" cols=\"").append(columns)
                        .append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\">");
                if (iinput != null && !iinput.isEmpty() && iinput.getSingle(responseIdent) != null) {
                    buffer.append(iinput.getSingle(getQTIIdent()));
                }
                buffer.append("</textarea>");

            } else {
                // render as input string
                buffer.append("&nbsp;<input id=\"QTI_").append(getQTIIdent()).append("\" name=\"");
                appendParameterIdent(buffer, ri);
                buffer.append("\" type=\"text\" size=\"").append(columns).append("\" maxlength=\"").append(maxlength);
                if (iinput != null && !iinput.isEmpty() && iinput.getSingle(responseIdent) != null) {
                    // OLAT-6989: escaped fillInText
                    String fillInText = StringHelper.escapeHtml(iinput.getSingle(getQTIIdent()));
                    buffer.append("\" value=\"").append(fillInText);
                }
                if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
                    buffer.append("\" disabled=\"disabled");
                }
                buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\" />&nbsp;");
            }
            addBr(ri, buffer);
        }
    }

    private void addBr(final RenderInstructions ri, final StringBuilder buffer) {
        if (wantBr(ri)) {
            buffer.append("<br />");
        }
    }

    private boolean wantBr(final RenderInstructions ri) {
        final Integer flowLabelClass = (Integer) ri.get(RenderInstructions.KEY_FLOW_LABEL);
        if (flowLabelClass != null) {
            return flowLabelClass.intValue() == RenderInstructions.RENDER_FLOW_LIST;
        } else {
            return true;
        }
    }

    private void appendParameterIdent(final StringBuilder buffer, final RenderInstructions ri) {
        buffer.append("qti").append(PARA).append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(PARA).append(ri.get(RenderInstructions.KEY_RESPONSE_IDENT))
                .append(PARA).append(getQTIIdent());
    }
}
