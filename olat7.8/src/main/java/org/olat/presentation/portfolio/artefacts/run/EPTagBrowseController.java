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
package org.olat.presentation.portfolio.artefacts.run;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.portfolio.EPArtefactTagCloud;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * a recursive tag-browser showing a tag-tree the deeper you go in, the less common tags of available resources are shown
 * <P>
 * Initial Date: 10.11.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPTagBrowseController extends BasicController {

    private final String activeCSS = "b_toggle b_small b_on";
    private final String inactiveCSS = "b_toggle b_small";
    private final String disabledCSS = "b_toggle b_small";

    private final VelocityContainer mainVc;
    private final EPFrontendManager ePFMgr;
    private final List<Link> tagLinks = new ArrayList<Link>();
    private List<AbstractArtefact> allUsersArtefacts;

    public EPTagBrowseController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        mainVc = createVelocityContainer("tagbrowser");
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);

        final List<String> tagList = ePFMgr.getUsersTagsOfArtefactType(getIdentity());
        int count = 0;
        final List<String> realTags = getRealArtefactTags();
        for (final String tag : tagList) {
            if (realTags.contains(tag)) {
                final String id = "tag_" + count++;
                final Link tagLink = LinkFactory.createLink(id, mainVc, this);
                tagLink.setCustomEnabledLinkCSS(inactiveCSS);
                tagLink.setCustomDisplayText(tag);
                tagLink.setCustomDisabledLinkCSS(disabledCSS);
                tagLink.setUserObject(new TagWrapper(tag));
                tagLinks.add(tagLink);
            }
        }
        mainVc.contextPut("tags", tagLinks);
        putInitialPanel(mainVc);
    }

    // filter available tags for such of non-existing artefacts
    private List<String> getRealArtefactTags() {
        allUsersArtefacts = ePFMgr.getArtefactPoolForUser(getIdentity());
        if (allUsersArtefacts == null) {
            return new ArrayList<String>();
        }
        final HashSet<String> realTags = new HashSet<String>();
        for (final AbstractArtefact abstractArtefact : allUsersArtefacts) {
            final List<String> thisTags = ePFMgr.getArtefactTags(abstractArtefact);
            realTags.addAll(thisTags);
        }
        final List<String> res = new ArrayList<String>();
        res.addAll(realTags);
        return res;
    }

    /**
	 */
    @SuppressWarnings("unused")
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing
        if (tagLinks.contains(source)) {
            final Link tagLink = (Link) source;
            final TagWrapper tagWrapper = (TagWrapper) tagLink.getUserObject();
            if (TagState.possible.equals(tagWrapper.getState())) {
                tagWrapper.setState(TagState.selected);
                final List<AbstractArtefact> artefacts = recalculateTagCloud();
                fireEvent(ureq, new EPTagBrowseEvent(artefacts));
            } else if (TagState.selected.equals(tagWrapper.getState())) {
                tagWrapper.setState(TagState.possible);
                final List<AbstractArtefact> artefacts = recalculateTagCloud();
                fireEvent(ureq, new EPTagBrowseEvent(artefacts));
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        tagLinks.clear();
    }

    private List<AbstractArtefact> recalculateTagCloud() {
        final List<String> selectedTags = getSelectedTags();
        if (selectedTags.isEmpty()) {
            for (final Link tagLink : tagLinks) {
                final TagWrapper tagWrapper = (TagWrapper) tagLink.getUserObject();
                tagLink.setCustomEnabledLinkCSS(inactiveCSS);
                tagLink.setEnabled(true);
                tagWrapper.setState(TagState.possible);
            }
            return allUsersArtefacts;
        }

        final EPArtefactTagCloud artefactsAndTags = ePFMgr.getArtefactsAndTagCloud(getIdentity(), selectedTags);
        final List<AbstractArtefact> filteredArtefacts = artefactsAndTags.getArtefacts();
        final Set<String> newTags = artefactsAndTags.getTags();

        if (newTags != null && !newTags.isEmpty()) {
            for (final Link tagLink : tagLinks) {
                final TagWrapper tagWrapper = (TagWrapper) tagLink.getUserObject();
                final String tag = tagWrapper.getTag();
                switch (tagWrapper.getState()) {
                case selected:
                    tagLink.setCustomEnabledLinkCSS(activeCSS);
                    tagLink.setEnabled(true);
                    break;
                case possible:
                    if (!newTags.contains(tag)) {
                        tagWrapper.setState(TagState.unpossible);
                        tagLink.setEnabled(false);
                    } else {
                        tagLink.setCustomEnabledLinkCSS(inactiveCSS);
                        tagLink.setEnabled(true);
                    }
                    break;
                case unpossible:
                    if (newTags.contains(tag)) {
                        tagWrapper.setState(TagState.possible);
                        tagLink.setCustomEnabledLinkCSS(inactiveCSS);
                        tagLink.setEnabled(true);
                    } else {
                        tagLink.setEnabled(false);
                    }
                    break;
                }
            }
        }

        return filteredArtefacts;
    }

    private List<String> getSelectedTags() {
        final List<String> tags = new ArrayList<String>();
        for (final Link tagLink : tagLinks) {
            final TagWrapper tagWrapper = (TagWrapper) tagLink.getUserObject();
            if (TagState.selected.equals(tagWrapper.getState())) {
                tags.add(tagWrapper.getTag());
            }
        }
        return tags;
    }

    private class TagWrapper {
        private final String tag;
        private TagState state;

        public TagWrapper(final String tag) {
            this.tag = tag;
            state = TagState.possible;
        }

        public TagState getState() {
            return state;
        }

        public void setState(final TagState state) {
            this.state = state;
        }

        public String getTag() {
            return tag;
        }
    }

    private enum TagState {
        selected, possible, unpossible
    }
}
