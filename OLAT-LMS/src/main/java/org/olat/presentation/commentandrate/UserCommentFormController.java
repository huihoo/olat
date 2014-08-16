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
package org.olat.presentation.commentandrate;

import org.olat.data.commentandrate.UserComment;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commentandrate.UserCommentsManager;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This controller offers a form to edit a comment. When the user presses save, the controller saves the comment to the manager.
 * <p>
 * When initialized without a comment, the form renders as empty message.
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CANCELLED_EVENT when the user canceled the operation</li>
 * <li>Event.CHANGED_EVENT when the user pressed save (new comment or update). Use getComment() to get the updated or new comment object.</li>
 * <li>Event.FAILED_EVENT when the comment could not be saved (probably due to the fact that the comment or the parent has been deleted)</li>
 * </ul>
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentFormController extends FormBasicController {
    private UserCommentsManager commentManager;
    private UserComment parentComment;
    private UserComment toBeUpdatedComment;
    //
    private RichTextElement commentElem;

    /**
     * Constructor for a user comment form controller. Use the UserCommentsAndRatingsController to work with user comments.
     * 
     * @param ureq
     * @param wControl
     * @param parentComment
     * @param toBeUpdatedComment
     * @param commentManager
     */
    UserCommentFormController(UserRequest ureq, WindowControl wControl, UserComment parentComment, UserComment toBeUpdatedComment, UserCommentsManager commentManager) {
        super(ureq, wControl, "userCommentForm");
        this.parentComment = parentComment;
        this.toBeUpdatedComment = toBeUpdatedComment;
        this.commentManager = commentManager;
        //
        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        commentElem = uifactory
                .addRichTextElementForStringDataMinimalistic("commentElem", null, "", -1, -1, false, formLayout, ureq.getUserSession(), getWindowControl());
        commentElem.setMaxLength(4000);
        commentElem.setExtDelay(true);
        FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
        formLayout.add(buttonContainer);
        uifactory.addFormSubmitButton("submit", buttonContainer);
        uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
        // Add parent and parent first / last name
        if (parentComment != null) {
            this.flc.contextPut("parentComment", parentComment);
            User parent = parentComment.getCreator().getUser();
            this.flc.contextPut("parentfirstName", getUserService().getUserProperty(parent, UserConstants.FIRSTNAME, getLocale()));
            this.flc.contextPut("parentLastName", getUserService().getUserProperty(parent, UserConstants.LASTNAME, getLocale()));
        }
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        String commentText = commentElem.getValue();
        boolean allOk = true;
        if (commentText.length() <= 4000) {
            commentElem.clearError();
        } else {
            commentElem.setErrorKey("input.toolong", new String[] { "4000" });
            allOk = false;
        }
        return allOk && super.validateFormLogic(ureq);
    }

    /**
	 */
    @Override
    protected void formOK(UserRequest ureq) {
        String commentText = commentElem.getValue();
        if (StringHelper.containsNonWhitespace(commentText)) {
            if (toBeUpdatedComment == null) {
                if (parentComment == null) {
                    // create new comment
                    this.toBeUpdatedComment = commentManager.createComment(getIdentity(), commentText);
                    // notify listeners that we finished.
                    fireEvent(ureq, Event.CHANGED_EVENT);
                } else {
                    // reply to parent comment
                    this.toBeUpdatedComment = commentManager.replyTo(parentComment, getIdentity(), commentText);
                    if (this.toBeUpdatedComment == null) {
                        showError("comments.coment.reply.error");
                        fireEvent(ureq, Event.FAILED_EVENT);
                    } else {
                        fireEvent(ureq, Event.CHANGED_EVENT);
                    }
                }
            } else {
                this.toBeUpdatedComment = commentManager.updateComment(toBeUpdatedComment, commentText);
                if (this.toBeUpdatedComment == null) {
                    showError("comments.coment.update.error");
                    fireEvent(ureq, Event.FAILED_EVENT);
                } else {
                    fireEvent(ureq, Event.CHANGED_EVENT);
                }
            }
        }
    }

    /**
	 */
    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    /**
     * @return the updated or new created comment. Only package scope
     */
    UserComment getComment() {
        return toBeUpdatedComment;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
