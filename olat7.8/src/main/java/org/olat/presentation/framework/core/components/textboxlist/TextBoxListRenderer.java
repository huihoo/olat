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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.framework.core.components.textboxlist;

import java.util.Map;
import java.util.Map.Entry;

import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormJSHelper;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.TextBoxListElementComponent;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.TextBoxListElementImpl;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * renderer for the textboxlist-component can be used in a flexiform mode or without and will then provide its own form
 * <P>
 * Initial Date: 23.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class TextBoxListRenderer implements ComponentRenderer {

    private boolean isFlexible;

    public TextBoxListRenderer(boolean isFlexible) {
        this.isFlexible = isFlexible;
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    @SuppressWarnings("unused")
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
        Translator fullTrans = PackageUtil.createPackageTranslator(TextBoxListRenderer.class, translator.getLocale(), translator);

        TextBoxListComponent tbComp;
        String formRefId;
        TextBoxListElementImpl te = null;

        if (!isFlexible) {
            tbComp = (TextBoxListComponent) source;
            formRefId = tbComp.getComponentName() + "_" + tbComp.hashCode();
        } else {
            tbComp = (TextBoxListElementComponent) source;
            te = ((TextBoxListElementComponent) tbComp).getTextElementImpl();
            formRefId = te.getRootForm().getFormName();
        }
        String dispatchId = tbComp.getFormDispatchId();
        if (tbComp.isEnabled()) {
            if (!isFlexible) { // build own form, when used as a plain component
                sb.append("<form method=\"post\" name=\"").append(formRefId).append("\" id=\"").append(formRefId).append("\" action=\"");
                ubu.buildURI(sb, null, null, 0);
                sb.append("\"");
                sb.append(" onsubmit=\"if(o_info.linkbusy) return false; else o_beforeserver(); return true;\" ");
                sb.append(">");
            }

            sb.append("<ol class=\"textbox-outer\">");
            sb.append("<li id=\"textbox-list").append(dispatchId).append("\" class=\"input-text\">");

            // TODO: epf: RH: provide the initial input as value (json-string), set jsonInputValue: true, loadfromInput: true
            sb.append("<input type=\"text\" value=\"\" id=\"textboxlistinput").append(dispatchId).append("\" ");
            sb.append("name=\"textboxlistinput");
            sb.append(dispatchId);
            sb.append("\"");
            if (isFlexible) {
                // TODO: epf: RH: this doesn't work, as the input-field isn't manipulated by user, but javascript.
                // if formInnerEvents need to work, then have an own javascript method to run onAdd / onRemove and
                StringBuilder rawData = FormJSHelper.getRawJSFor(te.getRootForm(), "textboxlistinput" + dispatchId, te.getAction());
                // sb.append(rawData.toString());
            }
            sb.append("/>");

            // building the initial content-list
            sb.append("<div class=\"textboxlist-auto\" id=\"textboxlist-auto").append(dispatchId).append("\">");

            sb.append("<ul class=\"feed\">");
            Map<String, String> initItems = tbComp.getInitialItems();
            if (initItems != null) {
                for (Entry<String, String> item : initItems.entrySet()) {
                    sb.append("<li value=\"");
                    // value became an html attribute, so it must be properly escaped
                    String value;
                    if (StringHelper.containsNonWhitespace(item.getValue()))
                        value = item.getValue();
                    else
                        value = item.getKey();
                    value = StringHelper.escapeHtmlAttribute(value);
                    sb.append(value);

                    sb.append("\">");
                    sb.append(StringHelper.escapeHtml(item.getKey()));
                    sb.append("</li>");
                }
            }

            sb.append("</ul>");
            sb.append("</div>");
            sb.append("</li>");
            sb.append("</ol>");
            if (!isFlexible) {
                sb.append("</form>");
            }

            // instantiating the JS-code for the textboxlist
            sb.append(FormJSHelper.getJSStart());
            sb.append("tlist = new ProtoMultiSelect('textboxlistinput").append(dispatchId).append("', 'textboxlist-auto").append(dispatchId).append("',{ newValues: ");
            sb.append(Boolean.toString(tbComp.isAllowNewValues()));
            sb.append(", ");

            if (tbComp.getProvider() != null) {
                // use autocomplete provider instead of prebuilt-map
                String mapperUri = tbComp.getMapperUri();
                sb.append("fetchFile: '").append(mapperUri).append("', ");
            }
            sb.append("jsonInputValue: false,");

            String setHintKey = tbComp.getInputHint();
            String inputHint;
            if (!StringHelper.containsNonWhitespace(setHintKey))
                inputHint = fullTrans.translate("default.input.hint");
            else
                inputHint = tbComp.getTranslator().translate(setHintKey);

            sb.append("inputMessage: '").append(inputHint).append("',");
            sb.append("sortResults: true, ");
            sb.append("autoResize: true, ");
            if (tbComp.getMaxResults() > 0 && tbComp.getMaxResults() != 10) {// 10 is the default
                sb.append("results: ").append(tbComp.getMaxResults()).append(", ");
                sb.append("maxResults: ").append(tbComp.getMaxResults()).append(", ");
            }
            sb.append("encodeEntities: false, ");
            sb.append("addPrefix: '").append(fullTrans.translate("add.new.element.prefix")).append("', ");
            sb.append("searchMessage: '").append(fullTrans.translate("please.wait.searching")).append("', ");
            // OLAT-6776: french translation contains apostrophe
            sb.append("moreMessage: \"").append(fullTrans.translate("more.results.found.specify.search")).append("\", ");
            sb.append("allowDuplicates: ").append(Boolean.toString(tbComp.isAllowDuplicates())).append(", ");
            sb.append("loadFromInput: false,");
            sb.append("fetchParameters: 'keyword', ");
            if (!tbComp.isNoFormSubmit()) {
                sb.append("onEmptyInput: function(input){ ");
                sb.append("document.forms['").append(formRefId).append("'].submit();} , ");

                sb.append("onUserAdd: function(input){"); // alert('bla' + new Hash(input).inspect()); }, ");
                sb.append("document.forms['").append(formRefId).append("'].submit();} , ");

                sb.append("onUserRemove: function(input){"); // alert('bla' + new Hash(input).inspect()); }, ");
                sb.append("document.forms['").append(formRefId).append("'].submit();} , ");
            }
            sb.append("regexSearch: false });");

            if (tbComp.getProvider() == null) {
                // use a prebuilt-map to look for autocompletion
                sb.append("var myjson = ");
                sb.append(tbComp.getAutoCompleteJSON()); // TODO: LD: 1024: this is not escaped, how to escape?
                sb.append(";");
                sb.append("myjson.each(function(t){tlist.autoFeed(t)});");
            }

            sb.append(FormJSHelper.getJSEnd());

        } else {
            // read only view
            String readOnlyContent = StringHelper.escapeHtml(tbComp.getReadOnlyContent());
            if (readOnlyContent.length() > 0) {
                sb.append("<div class=\"b_with_small_icon_left b_tag_icon\">");
                FormJSHelper.appendReadOnly(readOnlyContent, sb);
                sb.append("</div>");
            } else {
                FormJSHelper.appendReadOnly("-", sb);
            }
        }

    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderingState)
     */
    @SuppressWarnings({ "unused", "deprecation" })
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        // nothing
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    @SuppressWarnings("unused")
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        // nothing to load
    }

}
