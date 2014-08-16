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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.lms.commons.i18n.I18nManager;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Helper class to replace the translations that are wrapped with some identifier markup by the translator with HTML markup to allow inline editing.
 * 
 * <P>
 * Initial Date: 31.08.2011 <br>
 * 
 * @author Branislav Balaz
 */
/*
 * STATIC_METHOD_REFACTORING this class has been created as replacement of public static method
 * InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML method itself has not yet been refactored as it is now not concern of refactoring aspects
 */
public class LocalizationMarkupHtmlReplacer {

    // patterns to detect localized strings with identifyers
    private static final String decoratedTranslatedPattern = "(" + I18nManager.IDENT_PREFIX + "(.*?)" + I18nManager.IDENT_START_POSTFIX + ").*?("
            + I18nManager.IDENT_PREFIX + "\\2" + I18nManager.IDENT_END_POSTFIX + ")";
    private static final Pattern patternLink = Pattern.compile("<a[^>]*?>(?:<span[^>]*?>)*?[^<>]*?" + decoratedTranslatedPattern + "[^<>]*?(?:</span>*?>)*?</a>");
    private static final Pattern patternInput = Pattern.compile("<input[^>]*?" + decoratedTranslatedPattern + ".*?>");
    private static final Pattern patAttribute = Pattern.compile("<[^>]*?" + decoratedTranslatedPattern + "[^>]*?>");

    private static final String SPAN_TRANSLATION_I18NITEM_OPEN = "<span class=\"b_translation_i18nitem\">";
    private static final String SPAN_CLOSE = "</span>";
    private static final String BODY_TAG = "<body";

    /**
     * Helper method to replace the translations that are wrapped with some identifyer markup by the translator with HTML markup to allow inline editing.
     * <p>
     * This method is public and static to be testable with jUnit.
     * 
     * @param stringWithMarkup
     *            The text that contains translated elements that are wrapped with some identifyers
     * @param inlineTranslationURLBuilder
     *            URI builder used to create the inline translation links
     * @param inlineTrans
     * @return
     */
    public static String replaceLocalizationMarkupWithHTML(String stringWithMarkup, URLBuilder inlineTranslationURLBuilder, Translator inlineTrans) {
        while (stringWithMarkup.indexOf(I18nManager.IDENT_PREFIX) != -1) {
            // calculate positions of next localization identifyer
            int startSPos = stringWithMarkup.indexOf(I18nManager.IDENT_PREFIX);
            int startPostfixPos = stringWithMarkup.indexOf(I18nManager.IDENT_START_POSTFIX);
            String combinedKey = stringWithMarkup.substring(startSPos + I18nManager.IDENT_PREFIX.length(), startPostfixPos);
            int startEPos = startPostfixPos + I18nManager.IDENT_START_POSTFIX.length();
            String endIdent = I18nManager.IDENT_PREFIX + combinedKey + I18nManager.IDENT_END_POSTFIX;
            int endSPos = stringWithMarkup.indexOf(endIdent);
            int endEPos = endSPos + endIdent.length();
            // Build link for this identifyer
            StringOutput link = new StringOutput();
            // Check if we can parse the combined key
            String[] args = combinedKey.split(":");
            if (args.length == 3) {
                InlineTranslationLinkBuilder.buildInlineTranslationLink(args, link, inlineTrans, inlineTranslationURLBuilder);
            } else {
                // ups, can not parse combined key? could be for example because
                // ContactList.setName() replaced : with fancy ¦ which got HTML
                // escaped
                // In any case, we can not produce a translation link for this,
                // do nothing
                stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
                continue;
            }

            // Case 1: translated within a 'a' tag. The tag can contain an
            // optional
            // span tag
            // before and after translated link some other content could be
            // No support for i18n text that does contain HTML markup
            Matcher m = patternLink.matcher(stringWithMarkup);
            boolean foundPos = m.find();
            int wrapperOpen = 0;
            int wrapperClose = 0;
            if (foundPos) {
                wrapperOpen = m.start(0);
                wrapperClose = m.end(0);
                // check if found position does belong to start position
                if (wrapperOpen > startSPos) {
                    foundPos = false;
                } else {
                    // check if link is visible, skip other links
                    int skipPos = stringWithMarkup.indexOf("b_skip", wrapperOpen);
                    if (skipPos > -1 && skipPos < wrapperClose) {
                        stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
                        continue;
                    }
                    // found a valid link pattern, replace it
                    stringWithMarkup = replaceItemWithHTMLMarkupSurrounded(stringWithMarkup, link, startSPos, startEPos, endSPos, endEPos, wrapperOpen, wrapperClose);
                    continue;
                }
            }
            // Case 2: translated within an 'input' tag
            if (!foundPos) {
                m = patternInput.matcher(stringWithMarkup);
                foundPos = m.find();
                if (foundPos) {
                    wrapperOpen = m.start(0);
                    wrapperClose = m.end(0);
                    // check if found position does belong to start position
                    if (wrapperOpen > startSPos)
                        foundPos = false;
                    else {
                        // ignore within a checkbox
                        int checkboxPos = stringWithMarkup.indexOf("checkbox", wrapperOpen);
                        if (checkboxPos != -1 && checkboxPos < startSPos) {
                            stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
                            continue;
                        }
                        // ignore within a radio button
                        int radioPos = stringWithMarkup.indexOf("radio", wrapperOpen);
                        if (radioPos != -1 && radioPos < startSPos) {
                            stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
                            continue;
                        }
                        // found a valid input pattern, replace it
                        stringWithMarkup = replaceItemWithHTMLMarkupSurrounded(stringWithMarkup, link, startSPos, startEPos, endSPos, endEPos, wrapperOpen, wrapperClose);
                        continue;
                    }
                }
            }
            // Case 3: translated within a tag attribute of an element - don't
            // offer
            // inline translation
            m = patAttribute.matcher(stringWithMarkup);
            foundPos = m.find();
            if (foundPos) {
                wrapperOpen = m.start(0);
                wrapperClose = m.end(0);
                // check if found position does belong to start position
                if (wrapperOpen > startSPos)
                    foundPos = false;
                else {
                    // found a patter in within an attribute, skip this one
                    stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
                    continue;
                }
            }
            // Case 4: i18n element in html head - don't offer inline
            // translation
            if (startSPos < stringWithMarkup.indexOf(BODY_TAG)) {
                // found a pattern in the HTML head, skip this one
                stringWithMarkup = replaceItemWithoutHTMLMarkup(stringWithMarkup, startSPos, startEPos, endSPos, endEPos);
                continue;
            }

            // Case 4: default case: normal translation, surround with inline
            // translation link
            StringBuffer tmp = new StringBuffer();
            tmp.append(stringWithMarkup.substring(0, startSPos));
            tmp.append(SPAN_TRANSLATION_I18NITEM_OPEN);
            tmp.append(link);
            tmp.append(stringWithMarkup.substring(startEPos, endSPos));
            tmp.append(SPAN_CLOSE);
            tmp.append(stringWithMarkup.substring(endEPos));
            stringWithMarkup = tmp.toString();
        }
        return stringWithMarkup;
    }

    /**
     * Internal helper to add the html markup surrounding the parent element
     * 
     * @param stringWithMarkup
     * @param link
     * @param startSPos
     * @param startEPos
     * @param endSPos
     * @param endEPos
     * @param wrapperOpen
     * @param wrapperClose
     * @return
     */
    private static String replaceItemWithHTMLMarkupSurrounded(String stringWithMarkup, StringOutput link, int startSPos, int startEPos, int endSPos, int endEPos,
            int wrapperOpen, int wrapperClose) {
        StringBuffer tmp = new StringBuffer();
        tmp.append(stringWithMarkup.substring(0, wrapperOpen));
        tmp.append(SPAN_TRANSLATION_I18NITEM_OPEN);
        tmp.append(link);
        tmp.append(stringWithMarkup.substring(wrapperOpen, startSPos));
        tmp.append(stringWithMarkup.substring(startEPos, endSPos));
        tmp.append(stringWithMarkup.substring(endEPos, wrapperClose));
        tmp.append(SPAN_CLOSE);
        tmp.append(stringWithMarkup.substring(wrapperClose));
        return tmp.toString();
    }

    /**
     * Internal helper to remove the localization identifyers from the code without adding html markup
     * 
     * @param stringWithMarkup
     * @param startSPos
     * @param startEPos
     * @param endSPos
     * @param endEPos
     * @return
     */
    private static String replaceItemWithoutHTMLMarkup(String stringWithMarkup, int startSPos, int startEPos, int endSPos, int endEPos) {
        StringBuffer tmp = new StringBuffer();
        tmp.append(stringWithMarkup.substring(0, startSPos));
        tmp.append(stringWithMarkup.substring(startEPos, endSPos));
        tmp.append(stringWithMarkup.substring(endEPos));
        return tmp.toString();
    }

}
