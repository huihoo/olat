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
package org.olat.presentation.examples.guidemo.guisoa;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hibernate.exception.ConstraintViolationException;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.lms.commons.LearnServices;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Tests that the some of the @Retriable notificationService methods are really retried in case of failure, this is why we try to simulate a failure by calling subscribe
 * in 2 parallel threads.
 * 
 * Initial Date: 10.05.2012 <br>
 * 
 * @author lavinia
 */

public class TransactionRetryerGUIDemoController extends BasicController {

    private Panel panel;
    private final VelocityContainer mainVC;
    private final Link subscribeButton;
    private final Link unsubscribeButton;
    private final Link checkSubscriptionButton;

    private NotificationLearnService notificationLearnService;

    private ThreadPoolExecutor threadPoolExecutor;

    // publisher info
    private final Long CONTEXT_ID = Long.valueOf(1);
    private static Long SOURCE_ID; // this should outlive the creation and disposal of this controller, in this VM
    private final Long SUBCONTEXT_ID = Long.valueOf(3);
    private NotificationSubscriptionContext notificationSubscriptionContext;

    private String forumSourceType = ForumNotificationTypeHandler.FORUM_SOURCE_TYPE;
    private ContextType courseContextType = Publisher.ContextType.COURSE;

    /**
     * @param ureq
     * @param wControl
     */
    public TransactionRetryerGUIDemoController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        mainVC = createVelocityContainer("transactionRetryerDemo");
        subscribeButton = LinkFactory.createButton("button.subscribe", mainVC, this);
        unsubscribeButton = LinkFactory.createButton("button.unsubscribe", mainVC, this);
        checkSubscriptionButton = LinkFactory.createButton("button.check.subscription", mainVC, this);
        mainVC.put("usersearchholder", panel = new Panel("mainPanel"));

        notificationLearnService = getService(LearnServices.notificationLearnService);

        putInitialPanel(mainVC);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(2);
        threadPoolExecutor = createNewThreadPoolExecutor(workQueue);

        SOURCE_ID = new Long(2);// System.currentTimeMillis();
        notificationSubscriptionContext = new NotificationSubscriptionContext(ureq.getIdentity(), forumSourceType, SOURCE_ID, courseContextType, CONTEXT_ID,
                SUBCONTEXT_ID);

        if (source == subscribeButton) {

            try {
                for (int i = 0; i < 2; i++) {
                    // execute subscribe in 2 threads
                    threadPoolExecutor.execute(new SubscribeRunnable(notificationSubscriptionContext));
                }
                this.showInfo("info.at.successfull.subscribe");

            } catch (ConstraintViolationException e) {
                this.showError("error.at.subscribe");
            }
        } else if (source == checkSubscriptionButton) {
            checkIfSuccessfulSubscribed(notificationSubscriptionContext);
        } else if (source == unsubscribeButton) {
            notificationLearnService.unSubscribe(notificationSubscriptionContext);
        }
    }

    private void checkIfSuccessfulSubscribed(NotificationSubscriptionContext notificationSubscriptionContext) {
        if (notificationSubscriptionContext == null) {
            this.showWarning("info.at.not.subscribe");
            return;
        }
        boolean isSubscriptionSuccessfully = notificationLearnService.isSubscribed(notificationSubscriptionContext);
        if (isSubscriptionSuccessfully) {
            this.showInfo("info.at.check.subscribe");
        } else {
            this.showWarning("info.at.failed.subscribe");
        }
    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    private ThreadPoolExecutor createNewThreadPoolExecutor(BlockingQueue<Runnable> workQueue) {
        int corePoolSize = 2;
        int maximumPoolSize = 10;
        long keepAliveTime = 10000; // millis?
        TimeUnit unit = TimeUnit.MILLISECONDS;
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        return threadPoolExecutor;
    }

    /**
     * Calls subscribe.
     * 
     * 
     * 
     * @author lavinia
     */
    class SubscribeRunnable implements Runnable {
        private NotificationSubscriptionContext notificationSubscriptionContext;

        SubscribeRunnable(NotificationSubscriptionContext notificationSubscriptionContext_) {
            notificationSubscriptionContext = notificationSubscriptionContext_;
        }

        @Override
        public void run() {
            System.out.println("EXECUTE subscribe IN A NEW THREAD");
            notificationLearnService.subscribe(notificationSubscriptionContext);
        }
    }

}
