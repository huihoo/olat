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
package org.olat.presentation.webfeed;

import java.util.Date;

import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.ValidatedURL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * This controller is responsible for editing feed information. <br />
 * <h3>Events fired by this controller:</h3>
 * <ul>
 * <li>Event.CHANGED_EVENT</li>
 * <li>Event.CANCELLED_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: Feb 5, 2009 <br>
 * 
 * @author Gregor Wassmann, frentix GmbH, http://www.frentix.com
 */
public class ExternalFeedFormController extends FormBasicController {
    private final Feed feed;
    private TextElement title, description, feedUrl;
    private FormLink cancelButton;

    /**
     * @param ureq
     * @param control
     * @param feed
     */
    public ExternalFeedFormController(final UserRequest ureq, final WindowControl control, final Feed podcast, final Translator translator) {
        super(ureq, control);
        this.feed = podcast;
        setTranslator(translator);
        initForm(ureq);
    }

    /**
	 */
    @Override
    public void formOK(final UserRequest ureq) {
        feed.setTitle(title.getValue());
        feed.setDescription(description.getValue());
        feed.setExternalFeedUrl(feedUrl.isEmpty() ? null : feedUrl.getValue());
        feed.setLastModified(new Date());
        this.fireEvent(ureq, Event.CHANGED_EVENT);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == cancelButton && event.wasTriggerdBy(FormEvent.ONCLICK)) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        }
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        boolean validUrl = false;
        if (feedUrl.isEmpty()) {
            // allowed
            feedUrl.clearError();
            validUrl = true;
        } else {
            // validated feed url
            final String url = feedUrl.getValue();
            final String type = feed.getResourceableTypeName();
            final ValidatedURL validatedUrl = FeedManager.getInstance().validateFeedUrl(url, type);
            if (!validatedUrl.getUrl().equals(url)) {
                feedUrl.setValue(validatedUrl.getUrl());
            }
            switch (validatedUrl.getState()) {
            case VALID:
                feedUrl.clearError();
                validUrl = true;
                break;
            case NO_ENCLOSURE:
                feedUrl.setErrorKey("feed.form.feedurl.invalid.no_media", null);
                break;
            case NOT_FOUND:
                feedUrl.setErrorKey("feed.form.feedurl.invalid.not_found", null);
                break;
            case MALFORMED:
                feedUrl.setErrorKey("feed.form.feedurl.invalid", null);
                break;
            }
        }

        final String descriptionText = description.getValue();
        boolean descOk = true;
        if (descriptionText.length() <= 4000) {
            description.clearError();
        } else {
            description.setErrorKey("input.toolong", new String[] { "4000" });
            descOk = false;
        }
        return descOk && validUrl && super.validateFormLogic(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        this.setFormTitle("feed.edit");

        title = uifactory.addTextElement("title", "feed.title.label", 256, feed.getTitle(), this.flc);
        title.setMandatory(true);
        title.setNotEmptyCheck("feed.form.field.is_mandatory");

        // Description
        // description = formItemsFactory.addTextAreaElement("description", 5000, 0, 2, true, feed.getDescription(),
        // "feed.form.description", this.flc);
        description = uifactory.addRichTextElementForStringDataMinimalistic("description", "feed.form.description", feed.getDescription(), 5, -1, false, formLayout,
                ureq.getUserSession(), getWindowControl());
        description.setMandatory(true);
        description.setNotEmptyCheck("feed.form.field.is_mandatory");
        // The feed url
        feedUrl = uifactory.addTextElement("feedUrl", "feed.form.feedurl", 5000, feed.getExternalFeedUrl(), this.flc);
        feedUrl.setDisplaySize(70);

        final String type = feed.getResourceableTypeName();
        if (type != null && type.indexOf("BLOG") >= 0) {
            feedUrl.setExampleKey("feed.form.feedurl.example", null);
        } else {
            feedUrl.setExampleKey("feed.form.feedurl.example_podcast", null);
        }

        // Submit and cancelButton buttons
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        this.flc.add(buttonLayout);

        uifactory.addFormSubmitButton("submit", buttonLayout);
        cancelButton = uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
    }
}
