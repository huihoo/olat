package org.olat.presentation.i18n;

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
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.lms.commons.i18n.I18nItem;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.preferences.Preferences;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.delegating.DelegatingComponent;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.render.intercept.InterceptHandler;
import org.olat.presentation.framework.core.render.intercept.InterceptHandlerInstance;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This class acts both as the render intercepter and as the inline translation tool dispatcher. For each detected translated GUI element it will add a hover event which
 * triggers an edit link.
 * <p>
 * When the server is configured as translation server, the inline translation tool will start in language translation mode. Otherwhise it will start in language
 * customizing mode (overlay edit)
 * <P>
 * Initial Date: 16.09.2008 <br>
 * 
 * @author gnaegi
 */
public class InlineTranslationInterceptHandlerController extends BasicController implements InterceptHandlerInstance, InterceptHandler {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String ARG_BUNDLE = "bundle";
    private static final String ARG_KEY = "key";

    private URLBuilder inlineTranslationURLBuilder;
    private DelegatingComponent delegatingComponent;
    private TranslationToolI18nItemEditCrumbController i18nItemEditCtr;
    private CloseableModalController cmc;
    private Panel mainP;

    /**
     * Constructor
     * 
     * @param ureq
     * @param control
     */
    InlineTranslationInterceptHandlerController(UserRequest ureq, WindowControl control) {
        super(ureq, control);
        // the deleagating component is ony used to provide the
        // inlineTranslationURLBuilder to be able to create the translation tool
        // links
        delegatingComponent = new DelegatingComponent("delegatingComponent", new ComponentRenderer() {
            @Override
            public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
                // save urlbuilder for later use (valid only for one
                // request scope thus
                // transient, normally you may not save the url builder
                // for later usage)
                inlineTranslationURLBuilder = ubu;
            }

            @Override
            public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
                // void
            }

            @Override
            public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
                // trigger js method that adds hover events - in some
                // conditions method is not available (in iframes)
                sb.append("if (Object.isFunction(b_attach_i18n_inline_editing)) {b_attach_i18n_inline_editing();}");
            }
        });
        delegatingComponent.addListener(this);
        delegatingComponent.setDomReplaceable(false);

        mainP = putInitialPanel(delegatingComponent);
        mainP.setDomReplaceable(false);
    }

    /**
	 */
    @Override
    public InterceptHandlerInstance createInterceptHandlerInstance() {
        return this;
    }

    @Override
    public ComponentRenderer createInterceptComponentRenderer(final ComponentRenderer originalRenderer) {
        return new ComponentRenderer() {
            @Override
            public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
                // ------------- show translator keys
                // we must let the original renderer do its work so that the
                // collecting translator is callbacked.
                // we save the result in a new var since it is too early to
                // append it
                // to the 'stream' right now.
                StringOutput sbOrig = new StringOutput();
                try {
                    originalRenderer.render(renderer, sbOrig, source, ubu, translator, renderResult, args);
                } catch (Exception e) {
                    String emsg = "exception while rendering component '" + source.getComponentName() + "' (" + source.getClass().getName() + ") "
                            + source.getListenerInfo() + "<br />Message of exception: " + e.getMessage();
                    sbOrig.append("<span style=\"color:red\">Exception</span><br /><pre>" + emsg + "</pre>");
                }

                String rendered = sbOrig.toString();
                String renderedWithHTMLMarkup = LocalizationMarkupHtmlReplacer.replaceLocalizationMarkupWithHTML(rendered, inlineTranslationURLBuilder, getTranslator());
                sb.append(renderedWithHTMLMarkup);
            }

            /**
             * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder,
             * org.olat.presentation.framework.translator.Translator, org.olat.presentation.framework.render.RenderingState)
             */
            @Override
            public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
                originalRenderer.renderHeaderIncludes(renderer, sb, source, ubu, translator, rstate);
            }

            /**
             * org.olat.presentation.framework.render.StringOutput, org.olat.presentation.framework.components.Component,
             * org.olat.presentation.framework.render.RenderingState)
             */
            @Override
            public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
                originalRenderer.renderBodyOnLoadJSFunctionCall(renderer, sb, source, rstate);
            }
        };
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == delegatingComponent) {
            String bundle = ureq.getParameter(ARG_BUNDLE);
            String key = ureq.getParameter(ARG_KEY);
            // The argument ARG_IDENT is not used for dispatching right now
            if (log.isDebugEnabled()) {
                log.debug("Got event to launch inline translation tool for bundle::" + bundle + " and key::" + key, null);
            }
            if (StringHelper.containsNonWhitespace(bundle) && StringHelper.containsNonWhitespace(key)) {
                // Get userconfigured reference locale
                Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
                List<String> referenceLangs = I18nModule.getTransToolReferenceLanguages();
                String referencePrefs = (String) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_PREFERRED_REFERENCE_LANG, referenceLangs.get(0));
                I18nManager i18nMgr = I18nManager.getInstance();
                Locale referenceLocale = i18nMgr.getLocaleOrNull(referencePrefs);
                // Set target local to current user language
                Locale targetLocale = i18nMgr.getLocaleOrNull(ureq.getLocale().toString());
                if (I18nModule.isOverlayEnabled() && !I18nModule.isTransToolEnabled()) {
                    // use overlay locale when in customizing mode
                    targetLocale = I18nModule.getOverlayLocales().get(targetLocale);
                }
                List<I18nItem> i18nItems = i18nMgr.findExistingAndMissingI18nItems(referenceLocale, targetLocale, bundle, false);
                i18nMgr.sortI18nItems(i18nItems, true, true); // sort with
                                                              // priority
                // Initialize inline translation controller
                if (i18nItemEditCtr != null)
                    removeAsListenerAndDispose(i18nItemEditCtr);
                // Disable inline translation markup while inline translation
                // tool is
                // running -
                // must be done before instantiating the translation controller
                i18nMgr.setMarkLocalizedStringsEnabled(ureq.getUserSession(), false);
                i18nItemEditCtr = new TranslationToolI18nItemEditCrumbController(ureq, getWindowControl(), i18nItems, referenceLocale, !I18nModule.isTransToolEnabled());
                listenTo(i18nItemEditCtr);
                // set current key from the package as current translation item
                for (I18nItem item : i18nItems) {
                    if (item.getKey().equals(key)) {
                        i18nItemEditCtr.initialzeI18nitemAsCurrentItem(ureq, item);
                        break;
                    }
                }
                // Open in modal window
                if (cmc != null)
                    removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", i18nItemEditCtr.getInitialComponent());
                listenTo(cmc);
                cmc.activate();
            } else {
                log.error("Can not launch inline translation tool, bundle or key empty! bundle::" + bundle + " key::" + key, null);
            }
        }
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Controller source, @SuppressWarnings("unused") Event event) {
        if (source == cmc) {
            // user closed dialog, go back to inline translation mode
            I18nManager.getInstance().setMarkLocalizedStringsEnabled(ureq.getUserSession(), true);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // controllers autodisposed by basic controller
        inlineTranslationURLBuilder = null;
        delegatingComponent = null;
        i18nItemEditCtr = null;
        cmc = null;
    }

}
