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
package org.olat.presentation.i18n;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Helper class to build the inline translation link.
 * 
 * <P>
 * Initial Date: 31.08.2011 <br>
 * 
 * @author Branislav Balaz
 */
/*
 * STATIC_METHOD_REFACTORING this class has been created as replacement of public static method InlineTranslationInterceptHandlerController.buildInlineTranslationLink
 * method itself has not yet been refactored as it is now not concern of refactoring aspects
 */
public class InlineTranslationLinkBuilder {

    private static final String ARG_BUNDLE = "bundle";
    private static final String ARG_KEY = "key";
    private static final String ARG_IDENT = "id";

    /**
     * Helper method to build the inline translation link.
     * <p>
     * Public and static so that it can be used by the jUnit testcase
     * 
     * @param arguments
     *            e.g. bundle.name:key.name:ramuniqueid
     * @param link
     * @param inlineTrans
     * @param inlineTranslationURLBuilder
     */
    public static void buildInlineTranslationLink(String[] arguments, StringOutput link, Translator inlineTrans, URLBuilder inlineTranslationURLBuilder) {
        link.append("<a class=\"b_translation_i18nitem_launcher\" style=\"display:none\" href=\"");
        inlineTranslationURLBuilder.buildURI(link, new String[] { ARG_BUNDLE, ARG_KEY, ARG_IDENT }, arguments);
        link.append("\" title=\"");
        String combinedKey = arguments[0] + ":" + arguments[1];
        if (I18nModule.isTransToolEnabled()) {
            link.append(StringEscapeUtils.escapeHtml(inlineTrans.translate("inline.translate", new String[] { combinedKey })));
        } else {
            link.append(StringEscapeUtils.escapeHtml(inlineTrans.translate("inline.customize.translate", new String[] { combinedKey })));
        }
        link.append("\"></a>");
    }

}
