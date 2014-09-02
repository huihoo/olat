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
package org.olat.presentation.portfolio.artefacts.collect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextBoxListElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * controller to provide tag-suggestion and let user select tags for this artefact
 * <P>
 * Initial Date: 27.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectStepForm01 extends StepFormBasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final AbstractArtefact artefact;
    private final EPFrontendManager ePFMgr;
    private TextBoxListElement tagC;

    @SuppressWarnings("unused")
    public EPCollectStepForm01(final UserRequest ureq, final WindowControl wControl, final Form rootForm, final StepsRunContext runContext, final int layout,
            final String customLayoutPageName, final AbstractArtefact artefact) {
        super(ureq, wControl, rootForm, runContext, FormBasicController.LAYOUT_CUSTOM, "step01tagging");
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);

        this.artefact = artefact;
        initForm(this.flc, this, ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final Map<String, String> tagLM = new HashMap<String, String>();
        Collection<String> itemsToUse = null;
        final Collection<String> initialItems = ePFMgr.getArtefactTags(artefact);
        @SuppressWarnings("unchecked")
        final Collection<String> setTags = (List<String>) getFromRunContext("artefactTagsList");
        if (setTags != null) {
            // set some tags in wizzard already, use those
            itemsToUse = setTags;
        } else if (initialItems != null) {
            itemsToUse = initialItems;
        }
        if (itemsToUse != null) {
            for (final String tag : itemsToUse) {
                tagLM.put(tag, tag);
            }
        }
        tagC = uifactory.addTextBoxListElement("artefact.tags", null, "tag.input.hint", tagLM, formLayout, getTranslator());
        tagC.setNoFormSubmit(true);
        tagC.addActionListener(this, FormEvent.ONCHANGE);
        final Map<String, String> allUsersTags = ePFMgr.getUsersMostUsedTags(getIdentity(), 50);
        tagC.setAutoCompleteContent(allUsersTags);

        // show a list of the 50 most used tags to be clickable
        final List<FormLink> userTagLinks = new ArrayList<FormLink>();
        int i = 0;
        for (final Iterator<Entry<String, String>> iterator = allUsersTags.entrySet().iterator(); iterator.hasNext();) {
            final Entry<String, String> entry = iterator.next();

            final String tag = StringHelper.escapeHtml(entry.getKey());
            final FormLink tagLink = uifactory.addFormLink("tagU" + i, tag, null, formLayout, Link.NONTRANSLATED);

            tagLink.setUserObject(entry.getValue());
            userTagLinks.add(tagLink);
            i++;
        }
        this.flc.contextPut("userTagLinks", userTagLinks);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        super.formInnerEvent(ureq, source, event);
        if (source == tagC) {
            final FormEvent event2 = event;
            log.info("got" + event2.toString());
        } else if (source instanceof FormLink) {
            final FormLink link = (FormLink) source;
            if (link.getName().startsWith("tag")) {
                final List<String> actualTagList = tagC.getValueList();
                final String tag = (String) link.getUserObject();
                List<String> setTags = new ArrayList<String>();
                if (containsRunContextKey("artefactTagsList")) {
                    setTags = (List<String>) getFromRunContext("artefactTagsList");
                }
                setTags.add(tag); // escapeHtml?
                // merge actual tags with presets
                if (actualTagList.size() != 0) {
                    setTags.addAll(actualTagList);
                }
                removeDuplicate(setTags);
                addToRunContext("artefactTagsList", setTags);
                // refresh gui
                this.flc.setDirty(true);
                initForm(ureq);
            }
        }
    }

    private static void removeDuplicate(final List<String> arlList) {
        final HashSet<String> h = new HashSet<String>(arlList);
        arlList.clear();
        arlList.addAll(h);
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        final List<String> actualTagList = tagC.getValueList();
        if (!containsRunContextKey("artefactTagsList")) {
            // only add on first run, as it will get overwritten later on!
            addToRunContext("artefactTagsList", actualTagList);
        } else {
            // try to update on changes, but not with empty list -> this is the case while validating other steps
            if (actualTagList.size() != 0) {
                addToRunContext("artefactTagsList", actualTagList);
            }

        }
        // force repaint when navigating back and forth
        this.flc.setDirty(true);
        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

}
