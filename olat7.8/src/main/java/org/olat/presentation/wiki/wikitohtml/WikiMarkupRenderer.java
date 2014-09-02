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

package org.olat.presentation.wiki.wikitohtml;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.control.winmgr.AJAXFlags;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Description:<br>
 * render part of the component, where the html output surrounding the transformed wiki syntax gets added
 * <P>
 * Initial Date: May 17, 2006 <br>
 * 
 * @author guido
 */
public class WikiMarkupRenderer implements ComponentRenderer {

    protected WikiMarkupRenderer() {
        //
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(final Renderer renderer, final StringOutput sb, final Component source, final URLBuilder ubu, final Translator translator,
            final RenderResult renderResult, final String[] args) {
        final WikiMarkupComponent wikiComp = (WikiMarkupComponent) source;

        final AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
        final boolean iframePostEnabled = flags.isIframePostEnabled();

        // ParserInput parserInput = wikiComp.getParserInput();

        final ParserInput input = new ParserInput();
        input.setWikiUser(null);
        input.setAllowSectionEdit(false);
        input.setDepth(10);
        input.setContext("");
        // input.setTableOfContents(null);
        input.setLocale(new Locale("en"));
        // input.setVirtualWiki(Long.toString(wikiComp.getOres().getResourceableId()));
        input.setTopicName("dummy");
        input.setUserIpAddress("0.0.0.0");
        final OlatWikiDataHandler dataHandler = new OlatWikiDataHandler(wikiComp.getOres(), wikiComp.getImageBaseUri());
        input.setDataHandler(dataHandler);

        final StringOutput out = new StringOutput();
        ubu.buildURI(out, null, null, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
        String uri = out.toString();

        ParserDocument parsedDoc = null;

        final String uniqueId = "o_wiki" + wikiComp.getDispatchID();
        try {
            uri = URLDecoder.decode(uri, "utf-8");
            input.setVirtualWiki(uri.substring(1, uri.length() - 1));
            if (iframePostEnabled) {
                final StringOutput so = new StringOutput();
                ubu.appendTarget(so);
                input.setURLTarget(so.toString());
            }
            sb.append("<div style=\"min-height:" + wikiComp.getMinHeight() + "px\" id=\"");
            sb.append(uniqueId);
            sb.append("\">");

            final AbstractParser parser = new JFlexParser(input);
            parsedDoc = parser.parseHTML(wikiComp.getWikiContent());
        } catch (final UnsupportedEncodingException e) {
            // encoding utf-8 should be ok
        } catch (final Exception e) {
            throw new OLATRuntimeException(this.getClass(), "error while rendering wiki page with content:" + wikiComp.getWikiContent(), e);
        }
        // Use global js math formatter for latex formulas
        // OLAT-1024: output escaping, cannot filterXSS the content because it doesn't show me the edit article link
        sb.append(Formatter.formatLatexFormulas(parsedDoc.getContent()));
        sb.append("</div>");
        // set targets of media, image and external links to target "_blank"
        sb.append("<script type=\"text/javascript\">/* <![CDATA[ */ ");
        sb.append("changeAnchorTargets('").append(uniqueId).append("');");
        sb.append("/* ]]> */</script>");
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderHeaderIncludes(final Renderer renderer, final StringOutput sb, final Component source, final URLBuilder ubu, final Translator translator,
            final RenderingState rstate) {
        //
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(final Renderer renderer, final StringOutput sb, final Component source, final RenderingState rstate) {
        //
    }

}
