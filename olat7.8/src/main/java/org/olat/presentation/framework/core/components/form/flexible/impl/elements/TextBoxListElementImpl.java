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
package org.olat.presentation.framework.core.components.form.flexible.impl.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextBoxListElement;
import org.olat.presentation.framework.core.components.textboxlist.TextBoxListComponent;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * TODO: rhaag Class Description for TextBoxListElementImpl
 * <P>
 * Initial Date: 27.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class TextBoxListElementImpl extends AbstractTextElement implements TextBoxListElement {

    private TextBoxListElementComponent component;

    public TextBoxListElementImpl(String name, String inputHint, Map<String, String> initialItems, Translator translator) {
        super(name);
        this.component = new TextBoxListElementComponent(this, name, inputHint, initialItems, translator);
        setTranslator(translator);
    }

    @Override
    protected Component getFormItemComponent() {
        return component;
    }

    @Override
    public void evalFormRequest(UserRequest ureq) {
        component.dispatchRequest(ureq);
    }

    @Override
    public void setTranslator(Translator translator) {
        // wrap package translator with fallback form translator
        Translator elmTranslator = PackageUtil.createPackageTranslator(TextBoxListElementImpl.class, translator.getLocale(), translator);
        super.setTranslator(elmTranslator);
    }

    /**
     * Gets the escaped values from form.
     */
    @Override
    public String getValue() {
        String paramVal = getRootForm().getRequestParameter("textboxlistinput" + getFormDispatchId());
        return paramVal;
    }

    /**
     * Parses the value list from <code>values</code> and unescapes them.
     */
    @Override
    public List<String> getValueList() {
        String values = getValue();
        List<String> allCleaned = new ArrayList<String>();
        if (StringHelper.containsNonWhitespace(values)) {
            List<String> items = Arrays.asList(values.split(","));
            for (String item : items) {
                String cleanedItem = item.trim();
                allCleaned.add(StringHelper.unescapeHtml(cleanedItem));
            }
            if (!component.isAllowDuplicates())
                TextBoxListComponent.removeDuplicates(allCleaned);
        }
        return allCleaned;
    }

    @Override
    public void setAutoCompleteContent(List<String> tagL) {
        component.setAutoCompleteContent(tagL);
    }

    @Override
    public void setAutoCompleteContent(Map<String, String> tagM) {
        component.setAutoCompleteContent(tagM);
    }

    @Override
    public void setNoFormSubmit(boolean noFormSubmit) {
        component.setNoFormSubmit(noFormSubmit);
    }

}
