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
package org.olat.presentation.admin.version;

import org.olat.presentation.admin.SystemAdminMainController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This is a controller to configure the SimpleVersionConfig, the configuration of the versioning system for briefcase.
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public class VersionAdminController extends FormBasicController {

    private SingleSelection numOfVersions;

    private final String[] keys = new String[] { "0", "2", "3", "4", "5", "10", "25", "50", "-1" };

    private final String[] values = new String[] { "0", "2", "3", "4", "5", "10", "25", "50", "-1" };

    public VersionAdminController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        // use combined translator from system admin main
        setTranslator(PackageUtil.createPackageTranslator(SystemAdminMainController.class, ureq.getLocale(), getTranslator()));
        initForm(this.flc, this, ureq);

        values[0] = getTranslator().translate("version.off");
        values[values.length - 1] = getTranslator().translate("version.unlimited");
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // First add title and context help
        setFormTitle("version.title");
        setFormDescription("version.intro");
        setFormContextHelp(VersionAdminController.class.getPackage().getName(), "version.html", "help.hover.version");

        numOfVersions = uifactory.addDropdownSingleselect("version.numOfVersions", formLayout, keys, values, null);
        final Long maxNumber = getNumOfVersions();
        if (maxNumber == null) {
            numOfVersions.select("0", true);
        } else if (maxNumber.longValue() == -1l) {
            numOfVersions.select("-1", true);
        } else {
            final String str = maxNumber.toString();
            boolean found = false;
            for (final String value : values) {
                if (value.equals(str)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                numOfVersions.select(str, true);
            } else {
                // set a default value if the saved number is not in the list,
                // normally not possible but...
                numOfVersions.select("10", true);
            }
        }

        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("save", buttonLayout);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        final String num = numOfVersions.getSelectedKey();
        if (num == null || num.length() == 0) {
            return;
        }
        setNumOfVersions(num);
        getWindowControl().setInfo("saved");
    }

    private Long getNumOfVersions() {
        return new Long(((SystemPropertiesService) CoreSpringFactory.getBean(SystemPropertiesService.class)).getIntProperty(PropertyLocator.MAXNUMBER_VERSIONS));
    }

    public void setNumOfVersions(final String maxNumber) {
        ((SystemPropertiesService) CoreSpringFactory.getBean(SystemPropertiesService.class)).setProperty(PropertyLocator.MAXNUMBER_VERSIONS, maxNumber);
    }
}
