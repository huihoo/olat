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
package org.olat.presentation.course.statistic;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ClicksPerNode
 * <P>
 * Initial Date: 23.12.2009 <br>
 * 
 * @author patrickb
 */
public class ClicksPerNode extends FormBasicController {

    private final Long resourceableId;

    public ClicksPerNode(final UserRequest ureq, final WindowControl wControl, final Long resourceableId) {
        super(ureq, wControl);
        this.resourceableId = resourceableId;
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        // TODO Auto-generated method stub

    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // TODO Auto-generated method stub
        setFormTitle("clickspernode.title");
        uifactory.addStaticTextElement("clickspernode.info", null, translate("clickspernode.info"), formLayout);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

}
