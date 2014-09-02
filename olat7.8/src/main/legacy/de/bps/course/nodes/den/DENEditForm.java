/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.course.nodes.den;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.ENCourseNode;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.DENCourseNode;

public class DENEditForm extends FormBasicController {

	private final ModuleConfiguration moduleConfig;

	private MultipleSelectionElement enableCancelEnroll;
	private FormSubmit subm;

	/**
	 * Constructor of date enrollment creation and edit gui
	 * 
	 * @param ureq
	 * @param wControl
	 * @param moduleConfig
	 */
	public DENEditForm(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration moduleConfig) {
		super(ureq, wControl);
		this.moduleConfig = moduleConfig;

		initForm(this.flc, this, ureq);
	}

	@Override
	protected void doDispose() {
		// nothing
	}

	@Override
	protected void formOK(final UserRequest ureq) {
		final Boolean cancelEnrollEnabled = enableCancelEnroll.getSelectedKeys().size() == 1;
		moduleConfig.set(DENCourseNode.CONF_CANCEL_ENROLL_ENABLED, cancelEnrollEnabled);

		// Inform all listeners about the changed condition
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

		final Boolean initialCancelEnrollEnabled = (Boolean) moduleConfig.get(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED);

		enableCancelEnroll = uifactory.addCheckboxesHorizontal("enableCancelEnroll", "form.enableCancelEnroll", formLayout, new String[] { "ison" }, new String[] { "" },
				null);
		enableCancelEnroll.select("ison", initialCancelEnrollEnabled);

		subm = new FormSubmit("subm", "submit");

		formLayout.add(subm);
	}

	/**
	 * @return ModuleConfiguration
	 */
	public ModuleConfiguration getModuleConfiguration() {
		return moduleConfig;
	}

}
