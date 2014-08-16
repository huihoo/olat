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

import java.util.ArrayList;
import java.util.List;

import org.olat.data.commentandrate.UserComment;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commentandrate.CommentAndRatingSecurityCallback;
import org.olat.lms.commentandrate.UserCommentsManager;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.text.TextComponent;
import org.olat.presentation.framework.core.components.text.TextFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.title.TitleInfo;
import org.olat.presentation.framework.core.control.generic.title.TitledWrapperController;
import org.olat.presentation.user.DisplayPortraitController;
import org.olat.system.commons.Formatter;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This controller can display a comment and all it's replies
 * <ul>
 * <li>UserCommentDisplayController.DELETED_EVENT</li>
 * <li>UserCommentDisplayController.COMMENT_COUNT_CHANGED</li>
 * <li>UserCommentDisplayController.COMMENT_DATAMODEL_DIRTY</li>
 * </ul>
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentDisplayController extends BasicController {
    private final CommentAndRatingSecurityCallback securityCallback;
    private final UserCommentsManager commentManager;
    // GUI container
    private final VelocityContainer userCommentDisplayVC;
    // Data model
    private List<UserComment> allComments;
    private List<Controller> replyControllers;
    private UserComment userComment;
    // Delete workflow
    private Link deleteLink;
    private DialogBoxController deleteDialogCtr;
    public static final Event DELETED_EVENT = new Event("comment_deleted");
    public static final Event COMMENT_COUNT_CHANGED = new Event("comment_count_changed");
    public static final Event COMMENT_DATAMODEL_DIRTY = new Event("comment_datamode_dirty");
    // Reply workflow
    private Link replyLink;
    private CloseableModalController replyCmc;
    private UserCommentFormController replyCommentFormCtr;
    private TitledWrapperController replyTitledWrapperCtr;

    UserCommentDisplayController(UserRequest ureq, WindowControl wControl, UserCommentsManager commentManager, UserComment userComment, List<UserComment> allComments,
            CommentAndRatingSecurityCallback securityCallback) {
        super(ureq, wControl);
        this.commentManager = commentManager;
        this.userComment = userComment;
        this.allComments = allComments;
        this.securityCallback = securityCallback;
        // Init view
        this.userCommentDisplayVC = createVelocityContainer("userCommentDisplay");
        this.userCommentDisplayVC.contextPut("formatter", Formatter.getInstance(getLocale()));
        this.userCommentDisplayVC.contextPut("securityCallback", securityCallback);
        this.userCommentDisplayVC.contextPut("comment", userComment);
        // Creator information
        User user = userComment.getCreator().getUser();
        TextComponent creator = TextFactory.createTextComponentFromI18nKey("creator", null, null, null, true, userCommentDisplayVC);
        creator.setText(translate("comments.comment.creator", new String[] { getUserService().getUserProperty(user, UserConstants.FIRSTNAME),
                getUserService().getUserProperty(user, UserConstants.LASTNAME) }));
        Controller avatarCtr = new DisplayPortraitController(ureq, getWindowControl(), userComment.getCreator(), false, true);
        listenTo(avatarCtr);
        this.userCommentDisplayVC.put("avatarCtr", avatarCtr.getInitialComponent());
        // Delete link
        if (securityCallback.canDeleteComment(userComment)) {
            deleteLink = LinkFactory.createCustomLink("deleteLink", "delete", "delete", Link.BUTTON_XSMALL, userCommentDisplayVC, this);
        }
        // Reply link
        if (securityCallback.canReplyToComment(userComment)) {
            replyLink = LinkFactory.createCustomLink("replyLink", "reply", "comments.coment.reply", Link.BUTTON_XSMALL, userCommentDisplayVC, this);
        }
        //
        // Add all replies
        replyControllers = new ArrayList<Controller>();
        buildReplyComments(ureq);
        userCommentDisplayVC.contextPut("replyControllers", replyControllers);
        //
        putInitialPanel(this.userCommentDisplayVC);
    }

    /**
     * Used in velocity container to render replies
     * 
     * @return String with the name used for this comment as velocity component name
     */
    public String getViewCompName() {
        return "comment_" + this.userComment.getKey();
    }

    /**
     * Internal helper to build the view controller for the replies
     * 
     * @param ureq
     */
    private void buildReplyComments(UserRequest ureq) {
        // First remove all old replies
        for (Controller replyController : replyControllers) {
            removeAsListenerAndDispose(replyController);
        }
        replyControllers.clear();
        // Build replies again
        for (UserComment reply : allComments) {
            if (reply.getParent() == null)
                continue;
            if (reply.getParent().getKey().equals(userComment.getKey())) {
                // Create child controller
                UserCommentDisplayController replyCtr = new UserCommentDisplayController(ureq, getWindowControl(), commentManager, reply, allComments, securityCallback);
                replyControllers.add(replyCtr);
                listenTo(replyCtr);
                userCommentDisplayVC.put(replyCtr.getViewCompName(), replyCtr.getInitialComponent());
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Child controllers disposed by basic controller
        replyControllers = null;
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == replyLink) {
            // Init reply workflow
            replyCommentFormCtr = new UserCommentFormController(ureq, getWindowControl(), userComment, null, commentManager);
            listenTo(replyCommentFormCtr);
            User parentUser = userComment.getCreator().getUser();
            String title = translate("comments.coment.reply.title", new String[] { getUserService().getUserProperty(parentUser, UserConstants.FIRSTNAME),
                    getUserService().getUserProperty(parentUser, UserConstants.LASTNAME) });
            TitleInfo titleInfo = new TitleInfo(null, title);
            replyTitledWrapperCtr = new TitledWrapperController(ureq, getWindowControl(), replyCommentFormCtr, null, titleInfo);
            listenTo(replyTitledWrapperCtr);
            replyCmc = new CloseableModalController(getWindowControl(), "close", replyTitledWrapperCtr.getInitialComponent());
            replyCmc.activate();

        } else if (source == deleteLink) {
            // Init delete workflow
            List<String> buttonLabels = new ArrayList<String>();
            boolean hasReplies = false;
            for (UserComment comment : allComments) {
                if (comment.getParent() != null && comment.getParent().getKey().equals(userComment.getKey())) {
                    hasReplies = true;
                    break;
                }
            }
            if (hasReplies) {
                buttonLabels.add(translate("comments.button.delete.without.replies"));
                buttonLabels.add(translate("comments.button.delete.with.replies"));
            } else {
                buttonLabels.add(translate("delete"));
            }
            buttonLabels.add(translate("cancel"));
            String deleteText;
            if (hasReplies) {
                deleteText = translate("comments.dialog.delete.with.replies");
            } else {
                deleteText = translate("comments.dialog.delete");
            }
            deleteDialogCtr = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("comments.dialog.delete.title"), deleteText, buttonLabels);
            listenTo(deleteDialogCtr);
            deleteDialogCtr.activate();
            // Add replies info as user object to retrieve it later when evaluating the events
            deleteDialogCtr.setUserObject(Boolean.valueOf(hasReplies));

        }
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == deleteDialogCtr) {
            boolean hasReplies = ((Boolean) deleteDialogCtr.getUserObject()).booleanValue();
            if (DialogBoxUIFactory.isClosedEvent(event)) {
                // Nothing to do
            } else {
                int buttonPos = DialogBoxUIFactory.getButtonPos(event);
                if (buttonPos == 0) {
                    // Do delete
                    commentManager.deleteComment(userComment, false);
                    allComments.remove(userComment);
                    fireEvent(ureq, DELETED_EVENT);
                    // Inform top level view that it needs to rebuild due to comments that are now unlinked
                    if (hasReplies) {
                        fireEvent(ureq, COMMENT_DATAMODEL_DIRTY);
                    }
                } else if (buttonPos == 1 && hasReplies) {
                    // Delete current comment and all replies. Notify parent, probably needs full redraw
                    commentManager.deleteComment(userComment, true);
                    allComments.remove(userComment);
                    fireEvent(ureq, DELETED_EVENT);
                } else if (buttonPos == 1 && !hasReplies) {
                    // Nothing to do, cancel button
                }
            }
            // Cleanup delete dialog
            removeAsListenerAndDispose(deleteDialogCtr);
            deleteDialogCtr = null;

        } else if (source == replyCmc) {
            // User closed modal dialog (cancel)
            removeAsListenerAndDispose(replyCmc);
            replyCmc = null;
            removeAsListenerAndDispose(replyCommentFormCtr);
            replyCommentFormCtr = null;
            removeAsListenerAndDispose(replyTitledWrapperCtr);
            replyTitledWrapperCtr = null;
        } else if (source == replyCommentFormCtr) {
            // User Saved or canceled form
            replyCmc.deactivate();
            if (event == Event.CHANGED_EVENT) {
                // Update view
                UserComment newReply = replyCommentFormCtr.getComment();
                allComments.add(newReply);
                // Create child controller
                UserCommentDisplayController replyCtr = new UserCommentDisplayController(ureq, getWindowControl(), commentManager, newReply, allComments,
                        securityCallback);
                replyControllers.add(replyCtr);
                listenTo(replyCtr);
                userCommentDisplayVC.put(replyCtr.getViewCompName(), replyCtr.getInitialComponent());
                // notify parent
                fireEvent(ureq, COMMENT_COUNT_CHANGED);
            } else if (event == Event.FAILED_EVENT) {
                // Reply failed - reload everything
                fireEvent(ureq, COMMENT_DATAMODEL_DIRTY);
            }
            removeAsListenerAndDispose(replyCmc);
            replyCmc = null;
            removeAsListenerAndDispose(replyCommentFormCtr);
            replyCommentFormCtr = null;
            removeAsListenerAndDispose(replyTitledWrapperCtr);
            replyTitledWrapperCtr = null;
        } else if (source instanceof UserCommentDisplayController) {
            UserCommentDisplayController replyCtr = (UserCommentDisplayController) source;
            if (event == DELETED_EVENT) {
                // Remove comment from view and re-render.
                replyControllers.remove(replyCtr);
                userCommentDisplayVC.remove(replyCtr.getInitialComponent());
                removeAsListenerAndDispose(replyCtr);
                // Notify parent about this - probably needs complete reload of data model
                fireEvent(ureq, COMMENT_COUNT_CHANGED);
            } else if (event == COMMENT_COUNT_CHANGED || event == COMMENT_DATAMODEL_DIRTY) {
                // Forward to parent, nothing to do here
                fireEvent(ureq, event);
            }
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
