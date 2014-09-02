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
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat.presentation.framework.core.render.intercept.debug;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.StringMediaResource;
import org.olat.lms.commons.util.SourceHelper;
import org.olat.presentation.framework.common.plaintexteditor.PlainTextEditorController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.delegating.DelegatingComponent;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.dev.IDE;
import org.olat.presentation.framework.core.dev.Util;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.render.intercept.DebugHelper;
import org.olat.presentation.framework.core.render.intercept.InterceptHandler;
import org.olat.presentation.framework.core.render.intercept.InterceptHandlerInstance;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Settings;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

/**
 * Description:<br>
 * <P>
 * Initial Date: 20.05.2006 <br>
 * 
 * @author Felix Jost
 */
public class GuiDebugDispatcherController extends BasicController implements InterceptHandler, InterceptHandlerInstance {

    private URLBuilder debugURLBuilder;
    private DelegatingComponent dc;
    private Map<String, Component> idToComponent = new HashMap<String, Component>();
    private PlainTextEditorController vcEditorController;
    private Panel mainP;
    /* TODO: STATIC_METHOD_REFACTORING copied from SourceViewController DRY duplication! */
    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=utf-8";
    private final static String HTML_START = "<html><body>";
    private final static String HTML_STOP = "</body></html>";

    /**
     * @param ureq
     * @param wControl
     *            needed for subsequent debug-actions e.g. on a modal screen
     */
    public GuiDebugDispatcherController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);

        dc = new DelegatingComponent("deleg", new ComponentRenderer() {

            @Override
            public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
                // save urlbuilder for later use (valid only for one request scope thus
                // transient, normally you may not save the url builder for later usage)
                debugURLBuilder = ubu;
            }

            @Override
            public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
                // void
            }

            @Override
            public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
                // void
            }
        });
        /*
         * {
         * 
         * @Override /*public boolean isDirty() { return true; } };
         */
        dc.addListener(this);
        dc.setDomReplaceable(false);
        mainP = putInitialPanel(dc);
        mainP.setDomReplaceable(false);
    }

    /**
     * no trailing slash
     * 
     * @param c
     * @return
     */
    private String getFolderRootFor(Controller c) {
        String cpack = c.getClass().getPackage().getName();
        String res = SourceHelper.getSourcePath() + "/" + cpack.replace('.', '/');
        return res;
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == dc) {
            String cid = ureq.getParameter("cid");
            Component infoComponent = idToComponent.get(cid);

            String com = ureq.getParameter("com");
            // ------- open java IDE -------
            if (com.equals("ojava")) {
                // open java editor with the given java class source
                IDE ide = (IDE) CoreSpringFactory.getBean(IDE.class);
                if (ide != null) {
                    String cl = ureq.getParameter("class");
                    // cl e.g. org.olat.presentation.MyClass
                    // ide does not work yet, just show sourcecode in new browser window
                    try {
                        ureq.getDispatchResult().setResultingMediaResource(showjavaSource(cl));
                    } catch (IOException e) {
                        getWindowControl()
                                .setError(
                                        "Could not render java source code. Make sure you have set the source path (olat and olatcore) in the config (olat.properties) and have the source files there available");
                    }
                    // ide.openJavaSource(cl);
                } else {
                    // no ide configured... todo info msg
                }
            } else if (com.equals("vc")) {
                // ------- open velocity container for editing -------
                VelocityContainer vc = (VelocityContainer) infoComponent;
                String velocityTemplatePath = SourceHelper.getSourcePath() + "/" + vc.getPage();
                VFSLeaf vcContentFile = new LocalFileImpl(new File(velocityTemplatePath));
                boolean readOnly = Settings.isReadOnlyDebug();
                vcEditorController = new PlainTextEditorController(ureq, getWindowControl(), vcContentFile, "utf-8", true, true, null);
                vcEditorController.setReadOnly(readOnly);
                vcEditorController.addControllerListener(this);
                VelocityContainer vcWrap = createVelocityContainer("vcWrapper");
                if (readOnly)
                    vcWrap.contextPut("readOnly", Boolean.TRUE);
                vcWrap.put("editor", vcEditorController.getInitialComponent());
                getWindowControl().pushAsModalDialog(DebugHelper.createDebugProtectedWrapper(vcWrap));
            }
        }
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == vcEditorController) {
            // saving was already done by editor, just pop
            getWindowControl().pop();
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public ComponentRenderer createInterceptComponentRenderer(final ComponentRenderer originalRenderer) {
        return new ComponentRenderer() {

            @Override
            public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
                if (debugURLBuilder != null && !DebugHelper.isProtected(source)) {
                    // remember source for later debug info access
                    long did = source.getDispatchID();
                    String didS = String.valueOf(did);
                    idToComponent.put(didS, source);
                    int lev = renderResult.getNestedLevel();

                    String cname = source.getClass().getName();
                    String cnameShort = cname.substring(cname.lastIndexOf('.') + 1);
                    // header before component

                    sb.append("<div class=\"oocgrid_d1\">");
                    sb.append(""
                            + "<div>"
                            + "  <span id=\"o_guidebugst"
                            + did
                            + "\" onmouseover=\"o_debu_show(this.parentNode.parentNode, $('o_guidebugtt"
                            + did
                            + "'))\" "
                            +
                            // " onmouseout=\"o_debu_hide(this.parentNode.parentNode,
                            // $('o_guidebugtt"+did+"'))\" "+
                            ">"
                            + source.getComponentName()
                            + " ("
                            + cnameShort
                            + ")"
                            + "&nbsp;&nbsp;&nbsp;"
                            + "</span>"
                            + "</div>"
                            + "<div style=\"position:relative\">"
                            + "	<div id=\"o_guidebugtt"
                            + did
                            + "\" style=\"position:absolute; top:0px; left:24px; height:auto; width:auto; display:none; padding:5px; border: 1px solid black; margin: 0px; z-index:999; font-size:11px; background-color: #BBF;\" "
                            +
                            // does not work as it should? " onmouseout=\"o_debu_hide($('o_guidebugst"+did+"'),$('o_guidebugtt"+did+"'))\" "+
                            ">");

                    sb.append("Info: <b>").append(source.getComponentName())
                            .append("</b> (" + cnameShort + ") id:" + String.valueOf(source.getDispatchID() + "&nbsp; level:" + lev));

                    // offer velocity editor if appropriate.
                    // todo: let component provide component-specific editors
                    if (source instanceof VelocityContainer) {
                        VelocityContainer vcc = (VelocityContainer) source;
                        sb.append("<br />velocity: <a href=\"");
                        debugURLBuilder.buildURI(sb, new String[] { "cid", "com" }, new String[] { String.valueOf(did), "vc" });
                        sb.append("\">").append("page:").append(vcc.getPage() + "</a>");
                    }

                    Controller listC = Util.getListeningControllerFor(source);
                    if (listC != null) {
                        sb.append("<br /><b>controller:</b> <a  target=\"_blank\" href=\"");
                        String controllerClassName = listC.getClass().getName();
                        debugURLBuilder.buildURI(sb, new String[] { "cid", "com", "class" }, new String[] { String.valueOf(did), "ojava", controllerClassName });
                        sb.append("\">");
                        sb.append(controllerClassName);
                        sb.append("</a>");
                    }

                    sb.append("<br /><i>listeners</i>: ");
                    if (!source.isEnabled()) {
                        sb.append(" NOT ENABLED");
                    }
                    String listeners = source.getListenerInfo();
                    sb.append(listeners);
                    if (!source.isVisible()) {
                        sb.append("<br />INVISIBLE");
                    }
                    sb.append("<br />");

                    // we must let the original renderer do its work so that the collecting translator is callbacked.
                    // we save the result in a new var since it is too early to append it to the 'stream' right now.
                    StringOutput sbOrig = new StringOutput();
                    try {
                        originalRenderer.render(renderer, sbOrig, source, ubu, translator, renderResult, args);
                    } catch (Exception e) {
                        String emsg = "exception while rendering component '" + source.getComponentName() + "' (" + source.getClass().getName() + ") "
                                + source.getListenerInfo() + "<br />Message of exception: " + e.getMessage();
                        sbOrig.append("<span style=\"color:red\">Exception</span><br /><pre>" + emsg + "</pre>");
                    }

                    sb.append("</div>");

                    // add original component
                    sb.append(sbOrig);
                    sb.append("</div></div>");
                } else {
                    // e.g. when the render process take place before the delegating
                    // component of this controller here was rendered.
                    // the delegating component should be placed near the <html> tag in
                    // order to be rendered first.
                    // the contentpane of the window and the first implementing container
                    // will not be provided with debug info, which is on purpose,
                    // since those are contents from the chiefcontroller which control the
                    // window.

                    // render original component
                    originalRenderer.render(renderer, sb, source, ubu, translator, renderResult, args);
                }
            }

            @Override
            public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
                originalRenderer.renderHeaderIncludes(renderer, sb, source, ubu, translator, rstate);
            }

            @Override
            public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
                originalRenderer.renderBodyOnLoadJSFunctionCall(renderer, sb, source, rstate);
            }
        };
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public InterceptHandlerInstance createInterceptHandlerInstance() {
        // clear all previous data and return this.
        // otherwise this map would collect all components from all clicks, but we
        // need only one click
        debugURLBuilder = null;
        idToComponent.clear();
        return this;
    }

    /**
     * @param showDebugInfo
     */
    public void setShowDebugInfo(boolean showDebugInfo) {
        if (showDebugInfo) {
            mainP.setContent(dc);
        } else {
            mainP.setContent(null);
        }
    }

    /* STATIC_METHOD_REFACTORING moved from SourceViewController and made private */
    /**
     * provide a class name with path and you will get an string media resource you can display
     * 
     * @param cl
     * @return
     * @throws IOException
     */
    private MediaResource showjavaSource(String cl) throws IOException {
        JavaSource jsource = null;
        cl = cl.replace('.', '/');
        String javaSourcePath = SourceHelper.getSourcePath() + "/" + cl + ".java";
        File file = new File(javaSourcePath);
        StringWriter writer = new StringWriter();
        writer.append(HTML_START);
        if (file.exists()) {
            jsource = new JavaSourceParser().parse(file);
            // Create a converter and write the JavaSource object as Html
            JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
            converter.convert(jsource, JavaSourceConversionOptions.getDefault(), writer);
        } else {
            writer.append("<html><body><h3>The source file could not be found in the following path:<br>" + javaSourcePath
                    + "<br>Check if configured source path in brasatoconfig.xml is correct.</h3></body></html>");
        }

        StringMediaResource mr = new StringMediaResource();
        mr.setContentType(TEXT_HTML_CHARSET_UTF_8);
        writer.append(HTML_STOP);
        mr.setData(writer.toString());
        return mr;

    }

}
