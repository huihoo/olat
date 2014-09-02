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
package org.olat.presentation.ims.qti.editor;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.commons.filter.Filter;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.ims.qti.objects.Material;
import org.olat.lms.ims.qti.objects.Mattext;
import org.olat.lms.ims.qti.objects.QTIObject;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Material edit form controller in rich text editor style. All material elements are merged into a single one with a single text element.
 * 
 * @fires Event.CANCELLED_EVENT, Event.DONE_EVENT, QTIObjectBeforeChangeEvent
 *        <P>
 *        Initial Date: Jul 10, 2009 <br>
 * @author gwassmann
 */
public class MaterialFormController extends FormBasicController {
    private final QTIEditorPackageEBL qtiPackage;
    private final Material mat;
    private RichTextElement richText;
    private final boolean isRestrictedEditMode;
    private String htmlContent = "";

    public MaterialFormController(final UserRequest ureq, final WindowControl control, final Material mat, final QTIEditorPackageEBL qtiPackage,
            final boolean isRestrictedEditMode) {
        super(ureq, control, FormBasicController.LAYOUT_VERTICAL);
        this.mat = mat;
        this.qtiPackage = qtiPackage;
        this.htmlContent = mat.renderAsHtmlForEditor();

        this.isRestrictedEditMode = isRestrictedEditMode;
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to get rid of
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        String newHtml = richText.getRawValue(); // trust authors, don't to XSS filtering
        // the text fragment is saved in a cdata, remove cdata from movie plugin
        newHtml = newHtml.replace("// <![CDATA[", "").replace("// ]]>", "");
        // Strip unnecessary BR tags at the beginning and the end which are added
        // automaticall by mysterious tiny code and cause problems in FIB questions. (OLAT-4363)
        // Use explicit return which create a P tag if you want a line break.
        if (newHtml.startsWith("<br />") && newHtml.length() > 6) {
            newHtml = newHtml.substring(6);
        }
        if (newHtml.endsWith("<br />") && newHtml.length() > 6) {
            newHtml = newHtml.substring(0, newHtml.length() - 6);
        }
        // Remove any conditional comments due to strange behavior in test (OLAT-4518)
        final Filter conditionalCommentFilter = FilterFactory.getConditionalHtmlCommentsFilter();
        newHtml = conditionalCommentFilter.filter(newHtml);
        //
        if (htmlContent.equals(newHtml)) {
            // No changes. Cancel editing.
            fireEvent(ureq, Event.CANCELLED_EVENT);
        } else {
            if (isRestrictedEditMode) {
                // In restricted edit mode, if the content has changed, write a memento
                // (by firing the before change event).
                final QTIObjectBeforeChangeEvent qobce = new QTIObjectBeforeChangeEvent();
                qobce.init(mat.getId(), htmlContent);
                fireEvent(ureq, qobce);
            }
            // Collect the content of all MatElements in a single text element
            // (text/html) and save it (for Material objects with multiple elements
            // such as images, videos, text, breaks, etc. this can be regarded as
            // "lazy migration" to the new rich text style).
            final Mattext textHtml = new Mattext(newHtml);
            // A single text/html element will be left over.
            final List<QTIObject> elements = new ArrayList<QTIObject>(1);
            elements.add(textHtml);
            mat.setElements(elements);
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        richText = uifactory.addRichTextElementForStringData("mce", null, htmlContent, 14, -1, false, true,
                qtiPackage.getBaseDir(getTranslator().translate("qti.basedir.displayname")), null, formLayout, ureq.getUserSession(), getWindowControl());

        final RichTextConfiguration richTextConfig = richText.getEditorConfiguration();
        // disable <p> element for enabling vertical layouts
        richTextConfig.disableRootParagraphElement();
        // set upload dir to the media dir
        richTextConfig.setFileBrowserUploadRelPath("media");
        // manually enable the source edit button
        richTextConfig.setQuotedConfigValue(RichTextConfiguration.THEME_ADVANCED_BUTTONS3_ADD, RichTextConfiguration.SEPARATOR_BUTTON + ","
                + RichTextConfiguration.CODE_BUTTON);
        // allow script tags...
        richTextConfig.setQuotedConfigValue(RichTextConfiguration.INVALID_ELEMENTS, RichTextConfiguration.INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE_WITH_SCRIPT);
        richTextConfig.setQuotedConfigValue(RichTextConfiguration.EXTENDED_VALID_ELEMENTS, "script[src,type,defer]");

        uifactory.addFormSubmitButton("submit", formLayout);
    }

    /**
     * @return The material
     */
    public Material getMaterial() {
        return mat;
    }
}
