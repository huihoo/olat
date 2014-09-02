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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.filebrowser.components;

import java.util.Iterator;

import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.filebrowser.commands.FolderCommandFactory;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.winmgr.AJAXFlags;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;

/**
 * @author Felix Jost
 */
public class FolderComponentRenderer implements ComponentRenderer {

    private ListRenderer listRenderer;
    private CrumbRenderer crumbRenderer;

    /**
     * Constructor for TableRenderer. Singleton and must be reentrant There must be an empty contructor for the Class.forName() call
     */
    public FolderComponentRenderer() {
        super();
        listRenderer = new ListRenderer();
        crumbRenderer = new CrumbRenderer();
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
        FolderComponent fc = (FolderComponent) source;
        // is called for the current inline html
        int renderType = 0;
        if (args != null && args.length > 0) {
            if (args[0].equals("list"))
                renderType = 0;
            if (args[0].equals("crumb"))
                renderType = 1;
            if (args[0].equals("crumbNoLinks"))
                renderType = 2;
        }
        // get ajax flag for link rendering
        boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();

        if (renderType == 1)
            target.append(crumbRenderer.render(fc, ubu, true, iframePostEnabled));
        else if (renderType == 2)
            target.append(crumbRenderer.render(fc, ubu, false, iframePostEnabled));
        else
            renderList(renderer, target, fc, ubu, translator, iframePostEnabled);
    }

    private void renderList(Renderer r, StringOutput target, FolderComponent fc, URLBuilder ubu, Translator translator, boolean iframePostEnabled) {

        VFSContainer currentContainer = fc.getCurrentContainer();
        boolean canWrite = currentContainer.canWrite() == VFSConstants.YES;
        boolean canDelete = false;
        boolean canVersion = FolderConfig.versionsEnabled(fc.getCurrentContainer());
        for (Iterator<VFSItem> iter = fc.getCurrentContainerChildren().iterator(); iter.hasNext();) {
            VFSItem child = iter.next();
            if (child.canDelete() == VFSConstants.YES) {
                canDelete = true;
                break;
            }
        }

        String formName = "folder" + CodeHelper.getRAMUniqueID();
        target.append("<form  method=\"post\" id=\"" + formName + "\" action=\"");
        ubu.buildURI(target, new String[] { VelocityContainer.COMMAND_ID }, new String[] { FolderRunController.FORM_ACTION },
                iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
        target.append("\" onsubmit=\"if ( b_briefcase_isChecked('").append(formName).append("', '")
                .append(Formatter.escapeSingleAndDoubleQuotes(StringHelper.escapeHtml(translator.translate("alert")).toString()))
                .append("')) { if(o_info.linkbusy) return false; else o_beforeserver(); return true; } else {return false; }\"");
        if (iframePostEnabled) { // add ajax iframe target
            StringOutput so = new StringOutput();
            ubu.appendTarget(so);
            target.append(so.toString());
        }
        target.append(">");

        target.append("<div class=\"o_folder_toolbar \">");
        if (canWrite) {
            // add folder actions: upload file, create new folder, creat new file

            if (canVersion) {
                // deleted files
                target.append("<span><a class=\"b_briefcase_deletedfiles\" href=\"");
                ubu.buildURI(target, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "dfiles" }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
                        : AJAXFlags.MODE_NORMAL);
                target.append("\"");
                if (iframePostEnabled) { // add ajax iframe target
                    StringOutput so = new StringOutput();
                    ubu.appendTarget(so);
                    target.append(so.toString());
                }
                target.append(">");
                target.append(translator.translate("dfiles"));
                target.append("</a></span>");
            }

            if (canWrite) {
                // option new file
                target.append("<span><a class=\"b_button b_small\" href=\"");// b_briefcase_newfile
                ubu.buildURI(target, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "cfile" }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
                        : AJAXFlags.MODE_NORMAL);
                target.append("\"");
                if (iframePostEnabled) { // add ajax iframe target
                    StringOutput so = new StringOutput();
                    ubu.appendTarget(so);
                    target.append(so.toString());
                }
                target.append("><span>");
                target.append(translator.translate("cfile"));
                target.append("</span></a></span>");

                // option new folder
                target.append("<span><a class=\"b_button b_small\" href=\"");// b_briefcase_newfolder
                ubu.buildURI(target, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "cf" }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
                        : AJAXFlags.MODE_NORMAL);
                target.append("\"");
                if (iframePostEnabled) { // add ajax iframe target
                    StringOutput so = new StringOutput();
                    ubu.appendTarget(so);
                    target.append(so.toString());
                }
                target.append("><span>");
                target.append(translator.translate("cf"));
                target.append("</span></a></span>");

                // option upload
                target.append("<span><a class=\"b_button b_small\" href=\""); //
                ubu.buildURI(target, new String[] { VelocityContainer.COMMAND_ID }, new String[] { "ul" }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
                        : AJAXFlags.MODE_NORMAL);
                target.append("\"");
                if (iframePostEnabled) { // add ajax iframe target
                    StringOutput so = new StringOutput();
                    ubu.appendTarget(so);
                    target.append(so.toString());
                }
                target.append("><span>");
                target.append(translator.translate("ul"));
                target.append("</span></a></span>");

            }
        }

        // placeholder for the search
        target.append("</div>");

        // add current file bread crumb path
        target.append(crumbRenderer.render(fc, ubu, true, iframePostEnabled));
        // add file listing for current folder
        target.append(listRenderer.render(fc, ubu, translator, iframePostEnabled));

        if (fc.getCurrentContainerChildren().size() > 0) {
            if (canWrite || canDelete) {

                target.append("<div class=\"b_togglecheck\">");
                target.append("<a href=\"#\" onclick=\"javascript:b_briefcase_toggleCheck('").append(formName).append("', true)\">");
                target.append("<input type=\"checkbox\" checked=\"checked\" disabled=\"disabled\" />");
                target.append(translator.translate("checkall"));
                target.append("</a> <a href=\"#\" onclick=\"javascript:b_briefcase_toggleCheck('").append(formName).append("', false)\">");
                target.append("<input type=\"checkbox\" disabled=\"disabled\" />");
                target.append(translator.translate("uncheckall"));
                target.append("</a></div>");

                target.append("<div class=\"b_briefcase_commandbuttons b_button_group\">");
                if (canDelete) {
                    // delete
                    target.append("<input type=\"submit\" class=\"b_button\" name=\"");
                    target.append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_DEL);
                    target.append("\" value=\"");
                    target.append(StringHelper.escapeHtmlAttribute(translator.translate("del")));
                    target.append("\"/>");
                }

                if (canWrite) {
                    // move
                    target.append("<input type=\"submit\" class=\"b_button\" name=\"");
                    target.append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_MOVE);
                    target.append("\" value=\"");
                    target.append(StringHelper.escapeHtmlAttribute(translator.translate("move")));
                    // copy
                    target.append("\"/><input type=\"submit\" class=\"b_button\" name=\"");
                    target.append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_COPY);
                    target.append("\" value=\"");
                    target.append(StringHelper.escapeHtmlAttribute(translator.translate("copy")));
                    target.append("\"/>");
                }

                if (canWrite) {
                    // zip
                    target.append("<input type=\"submit\" class=\"b_button\" name=\"");
                    target.append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_ZIP);
                    target.append("\" value=\"");
                    target.append(StringHelper.escapeHtmlAttribute(translator.translate("zip")));
                    // unzip
                    target.append("\"/><input type=\"submit\" class=\"b_button\" name=\"");
                    target.append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_UNZIP);
                    target.append("\" value=\"");
                    target.append(StringHelper.escapeHtmlAttribute(translator.translate("unzip")));
                    target.append("\"/>");
                }
                target.append("</div>");
            }
        }

        target.append("</form>");
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        // no JS to add
    }

    /**
     * org.olat.presentation.framework.components.Component)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        // no JS to render
    }

}
