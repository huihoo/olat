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

package org.olat.presentation.user.administration;

import java.util.List;
import java.util.Locale;

import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Feb 6, 2006
 * 
 * @author gnaegi Description: Factory to create a table controller using the extended identity table model. The idea is to have one single point for identities list that
 *         can be created using different factory methods. The implemented methods are stable, however the class itself is work in progress.
 */
public class ExtendedIdentitiesTableControllerFactory {
    private static final String PACKAGE = PackageUtil.getPackageName(ExtendedIdentitiesTableControllerFactory.class);
    public static final String COMMAND_VCARD = "show.vcard";
    public static final String COMMAND_SELECTUSER = "select.user";

    /**
     * @param args
     */
    public static TableController createController(final ExtendedIdentitiesTableDataModel dataModel, final UserRequest ureq, final WindowControl wControl,
            final boolean actionEnabled) {
        final Locale loc = ureq.getLocale();
        Translator trans = new PackageTranslator(PACKAGE, loc);
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setDownloadOffered(true);
        tableConfig.setPreferencesOffered(true, "ExtendedIdentitiesTable");
        tableConfig.setTableEmptyMessage(trans.translate("error.no.user.found"));

        trans = getUserService().getUserPropertiesConfig().getTranslator(trans);
        final TableController tableCtr = new TableController(tableConfig, ureq, wControl, trans);
        dataModel.addColumnDescriptors(tableCtr, trans);
        tableCtr.setTableDataModel(dataModel);
        return tableCtr;
    }

    public static ExtendedIdentitiesTableDataModel createTableDataModel(final UserRequest ureq, final List identities, final boolean actionEnabled) {
        return new ExtendedIdentitiesTableDataModel(ureq, identities, actionEnabled);
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
