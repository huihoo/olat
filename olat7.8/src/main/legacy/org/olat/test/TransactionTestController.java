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

package org.olat.test;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.core.logging.Tracing;
import org.olat.data.DB;
import org.olat.data.DBFactory;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;

/**
 * Description:<br>
 * <P>
 * Initial Date: 08.11.2007 <br>
 * 
 * @author christian guretzki
 */
public class TransactionTestController extends BasicController {

	private final VelocityContainer myContent = createVelocityContainer("transactiontest");

	private final Panel panel = new Panel("panel");
	private final Link linkNoTrans;
	private final Link linkTrans;
	private final Link linkMixTrans;
	private final Link linkNoTransE;
	private final Link linkTransE;
	private final Link linkMixTransE;
	private final Link resetLink;
	private final Link listPropertiesLink;

	private final String propertyCategory = "transactionTest";
	String propertyKey1 = "TestTransactionalKey-1";
	String testValue1 = "TestTransactionalValue-1";
	String propertyKey2 = "TestTransactionalKey-2";
	String testValue2 = "TestTransactionalValue-2";

	public TransactionTestController(final UserRequest ureq, final WindowControl wControl) {
		super(ureq, wControl);
		// we pass a variable to the velocity container

		// links and buttons are also components
		linkNoTrans = LinkFactory.createLink("transaction.test.no", myContent, this);
		linkTrans = LinkFactory.createLink("transaction.test.yes", myContent, this);
		linkMixTrans = LinkFactory.createLink("transaction.test.mixed", myContent, this);

		linkNoTransE = LinkFactory.createLink("transaction.test.no.error", myContent, this);
		linkTransE = LinkFactory.createLink("transaction.test.yes.error", myContent, this);
		linkMixTransE = LinkFactory.createLink("transaction.test.mixed.error", myContent, this);

		resetLink = LinkFactory.createLink("transaction.test.reset", myContent, this);
		listPropertiesLink = LinkFactory.createLink("transaction.test.list.properties", myContent, this);

		// panels are content holders that are initially empty and can be filled with different contents
		// the panel itself stays in the layout and if you are in AJAX mode only the new content gets sended
		// and replace by dom replacement.
		myContent.put("panel", panel);
		panel.setContent(null);
		// our velocity contrainer will be the first component to display
		// when somebody decieds to render the GUI of this controller.
		putInitialPanel(myContent);
	}

	private String listAllProperties() {
		final StringBuilder buf = new StringBuilder();
		final PropertyManager pm = PropertyManager.getInstance();
		final PropertyImpl p1 = pm.findProperty(null, null, null, propertyCategory, propertyKey1);
		if (p1 == null) {
			buf.append(propertyKey1 + "=null");
		} else {
			buf.append(propertyKey1 + "=" + p1.getStringValue());
		}
		buf.append("<br />");
		final PropertyImpl p2 = pm.findProperty(null, null, null, propertyCategory, propertyKey2);
		if (p2 == null) {
			buf.append(propertyKey2 + "=null");
		} else {
			buf.append(propertyKey2 + "=" + p2.getStringValue());
		}
		buf.append("<br />");
		return buf.toString();
	}

	/**
	 * This dispatches component events...
	 * 
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.components.Component, org.olat.presentation.framework.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		// first check, which component this event comes from...
		if (source == linkNoTrans) {
			doTestNonTransactional();
		} else if (source == linkTrans) {
			doTestTransactional();
		} else if (source == linkMixTrans) {
			doTestMixTransactional();
		} else if (source == linkNoTransE) {
			doTestNonTransactionalError();
		} else if (source == linkTransE) {
			doTestTransactionalError();
		} else if (source == linkMixTransE) {
			doTestMixTransactionalError();
		} else if (source == resetLink) {
			doReset();
		} else if (source == listPropertiesLink) {
			getWindowControl().setInfo(listAllProperties());
		}
	}

	private void doTestNonTransactional() {
		final PropertyManager pm = PropertyManager.getInstance();
		final PropertyImpl p1 = pm.createPropertyInstance(null, null, null, propertyCategory, propertyKey1, null, null, "doTestNonTransactional_1", null);
		pm.saveProperty(p1);
		// name is null => generated DB error => rollback
		final PropertyImpl p2 = pm.createPropertyInstance(null, null, null, propertyCategory, propertyKey2, null, null, "doTestNonTransactional_2", null);
		pm.saveProperty(p2);
		getWindowControl().setInfo("Properties<br />TestTransactionalKey-1=doTestNonTransactional_1,<br />TestTransactionalKey-2=doTestNonTransactional_2 created");
	}

	private void doTestTransactional() {
		final DB db = DBFactory.getInstance();
		final PropertyManager pm = PropertyManager.getInstance();
		final PropertyImpl p1 = pm.createPropertyInstance(null, null, null, propertyCategory, propertyKey1, null, null, "doTestTransactional_1", null);
		pm.saveProperty(p1);
		final PropertyImpl p2 = pm.createPropertyInstance(null, null, null, propertyCategory, propertyKey2, null, null, "doTestTransactional_2", null);
		pm.saveProperty(p2);
		getWindowControl().setInfo("Properties<br />TestTransactionalKey-1=doTestTransactional_1,<br />TestTransactionalKey-2=doTestTransactional_2 created");
	}

	private void doTestMixTransactional() {
		final DB db = DBFactory.getInstance();
		final PropertyManager pm = PropertyManager.getInstance();
		final PropertyImpl p1 = pm.createPropertyInstance(null, null, null, propertyCategory, propertyKey1, null, null, "doTestMixTransactional_1", null);
		pm.saveProperty(p1);
		final PropertyImpl p2 = pm.createPropertyInstance(null, null, null, propertyCategory, propertyKey2, null, null, "doTestMixTransactional_2", null);
		pm.saveProperty(p2);
		getWindowControl().setInfo("Properties<br />TestTransactionalKey-1=doTestTransactional_1,<br />TestTransactionalKey-2=doTestTransactional_2 created");
	}

	private void doTestNonTransactionalError() {
		final PropertyManager pm = PropertyManager.getInstance();
		final PropertyImpl p1 = pm.createPropertyInstance(null, null, null, propertyCategory, propertyKey1, null, null, "doTestNonTransactionalError_1", null);
		pm.saveProperty(p1);
		// name is null => generated DB error => no rollback of p1
		final PropertyImpl p2 = pm.createPropertyInstance(null, null, null, propertyCategory, null, null, null, "doTestNonTransactionalError_2", null);
		pm.saveProperty(p2);
		getWindowControl().setError("Should generate error for rollback!");
		log.error("Should generate error and not reach this code");
	}

	private void doTestTransactionalError() {
		final DB db = DBFactory.getInstance();
		final PropertyManager pm = PropertyManager.getInstance();
		final PropertyImpl p1 = pm.createPropertyInstance(null, null, null, propertyCategory, propertyKey1, null, null, "doTestTransactionalError_1", null);
		pm.saveProperty(p1);
		// name is null => generated DB error => rollback
		final PropertyImpl p2 = pm.createPropertyInstance(null, null, null, propertyCategory, null, null, null, "doTestTransactionalError_2", null);
		pm.saveProperty(p2);
		getWindowControl().setError("Should generate error for rollback!");
		log.error("Should generate error for rollback and not reach this code");
	}

	private void doTestMixTransactionalError() {
		final DB db = DBFactory.getInstance();
		final PropertyManager pm = PropertyManager.getInstance();
		final PropertyImpl p1 = pm.createPropertyInstance(null, null, null, propertyCategory, propertyKey1, null, null, "doTestMixTransactional_1", null);
		pm.saveProperty(p1);
		// name is null => generated DB error => rollback
		final PropertyImpl p2 = pm.createPropertyInstance(null, null, null, propertyCategory, null, null, null, "doTestMixTransactional_2", null);
		pm.saveProperty(p2);
		getWindowControl().setError("Should generate error for rollback!");
		log.error("Should generate error for rollback and not reach this code");
	}

	private void doReset() {
		PropertyManager.getInstance().deleteProperties(null, null, null, propertyCategory, null);
		getWindowControl().setInfo("All transaction-test property were deleted.");
	}

	/**
	 * This dispatches controller events...
	 * 
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Controller source, final Event event) {
		// at this time, we do not have any other controllers we'd like to listen for
		// events to...

		// If you have a formular or a table component in your velocity file the
		// events (like clicking an element in the table)
		// this method gets called and the event can be handeled
	}

	@Override
	protected void doDispose() {
		// use this method to finish thing at the end of the livetime of this controller
		// like closing files or connections...
		// this method does no get called automatically, you have to maintain the controller chain
		// and make sure that you call dispose on the place where you create the controller
	}

}
