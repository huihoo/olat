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

package org.olat.presentation.group.securitygroup.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.events.MultiIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.WizardController;
import org.olat.presentation.group.securitygroup.UserControllerFactory;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * This wizard controller has three steps:<br>
 * 1) enter list of login names<br>
 * 2) verify matched users<br>
 * 3) add optional mail message
 * <p>
 * Step 3 is optional and only executed when mailTemplate is not NULL.
 * <p>
 * Note that his wizard does only collect data, it does not process any of them. At the end a MultiIdentityChoosenEvent is fired that contains the selected identities and
 * the mail template. The parent controller is expected to do something with those users.
 * <p>
 * Initial Date: Jan 25, 2005
 * 
 * @author Felix Jost, Florian Gnägi
 */

public class UsersToGroupWizardController extends WizardController {

    private final SecurityGroup securityGroup;
    private final BaseSecurity securityManager;
    private final VelocityContainer mainVc;
    private final UserIdsForm usersForm;
    private TableController newTableC;
    private List<Identity> oks;
    private final Link nextButton;
    private final Link backButton;

    // TODO:fj:b WizardController does not need to be a super class!

    /**
     * assumes that the user seeing this controller has full access rights to the group (add/remove users)
     */
    public UsersToGroupWizardController(final UserRequest ureq, final WindowControl wControl, final SecurityGroup aSecurityGroup) {
        // wizard has two or there steps depending whether the mail template should
        // be configured or not
        super(ureq, wControl, 2);
        setBasePackage(UsersToGroupWizardController.class);

        this.securityGroup = aSecurityGroup;
        this.securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);

        mainVc = createVelocityContainer("index");

        nextButton = LinkFactory.createButtonSmall("finish", mainVc, this);
        backButton = LinkFactory.createButtonSmall("back", mainVc, this);

        usersForm = new UserIdsForm(ureq, wControl);
        listenTo(usersForm);

        // init wizard title and set step 1
        setWizardTitle(translate("import.title"));
        setNextWizardStep(translate("import.title.select"), usersForm.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // default wizard will lissen to cancel wizard event
        super.event(ureq, source, event);

        if (source == nextButton) {
            // wizard stops here - no mail template to fill out
            fireEvent(ureq, new MultiIdentityChosenEvent(this.oks));
        } else if (source == backButton) {
            // go back one step in wizard
            setBackWizardStep(translate("import.title.select"), usersForm.getInitialComponent());
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == usersForm) {
            if (event == Event.DONE_EVENT) {
                // calc stuff, preview

                final List existIdents = securityManager.getIdentitiesOfSecurityGroup(securityGroup);
                oks = new ArrayList<Identity>();
                final List<String> anonymous = new ArrayList<String>();
                final List<String> notFounds = new ArrayList<String>();
                final List<String> alreadyIn = new ArrayList<String>();

                // get the logins
                final String inp = usersForm.getLoginsString();
                final String[] lines = inp.split("\r?\n");
                for (int i = 0; i < lines.length; i++) {
                    final String username = lines[i].trim();
                    if (!username.equals("")) { // skip empty lines
                        final Identity ident = securityManager.findIdentityByName(username);
                        if (ident == null) { // not found, add to not-found-list
                            notFounds.add(username);
                        } else if (getBaseSecurityEBL().isAnonymous(ident)) {
                            anonymous.add(username);
                        } else {
                            // check if already in group
                            final boolean inGroup = containsIdentity(existIdents, ident);
                            if (inGroup) {
                                // added to warning: already in group
                                alreadyIn.add(ident.getName());
                            } else {
                                // ok to add -> preview (but filter duplicate entries)
                                if (!containsIdentity(oks, ident)) {
                                    oks.add(ident);
                                }
                            }
                        }
                    }
                }
                // push table and other infos to velocity
                removeAsListenerAndDispose(newTableC);
                newTableC = UserControllerFactory.createTableControllerFor(null, oks, ureq, getWindowControl(), null);
                listenTo(newTableC);

                mainVc.put("table", newTableC.getInitialComponent());
                mainVc.contextPut("isanonymous", listNames(anonymous));
                mainVc.contextPut("notfound", listNames(notFounds));
                mainVc.contextPut("alreadyin", listNames(alreadyIn));
                mainVc.contextPut("usercount", new Integer(oks.size()));
                // set table page as next wizard step
                setNextWizardStep(translate("import.title.finish"), mainVc);
            }
        }
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    private String listNames(final List names) {
        final StringBuilder sb = new StringBuilder();
        final int cnt = names.size();
        for (int i = 0; i < cnt; i++) {
            final String identname = (String) names.get(i);
            sb.append(identname);
            if (i < cnt - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    protected void doDispose() {
        //
    }

    private boolean containsIdentity(List<Identity> listOfIdentity, Identity identity) {
        Long key = identity.getKey();

        for (Identity entry : listOfIdentity) {
            if (entry.getKey().equals(key)) {
                return true;
            }
        }
        return false;

    }
}

/**
 * Description:<br>
 * Input field for entering user names
 * <P>
 * 
 * @author Felix Jost
 */
class UserIdsForm extends FormBasicController {
    private TextElement idata;

    /**
     * @param name
     * @param trans
     */
    public UserIdsForm(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        initForm(ureq);
    }

    /**
	 */
    public boolean validate() {
        return !idata.isEmpty("form.legende.mandatory");
    }

    public String getLoginsString() {
        return idata.getValue();
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);

    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        idata = uifactory.addTextAreaElement("addusers", "form.addusers", -1, 15, 40, true, " ", formLayout);
        idata.setExampleKey("form.names.example", null);
        uifactory.addFormSubmitButton("next", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }
}
