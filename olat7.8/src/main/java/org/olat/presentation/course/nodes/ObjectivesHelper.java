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

package org.olat.presentation.course.nodes;

import java.util.Locale;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class ObjectivesHelper {

    private static final String PACKAGE = PackageUtil.getPackageName(ObjectivesHelper.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(ObjectivesHelper.class);

    /**
     * Creates a velocity container that displays the given learning objective
     * 
     * @param learningObjectives
     *            The learning objective
     * @param ureq
     *            The user request
     * @return the wrapper component
     * @deprecated use the method with Locale instead. The locale is in ureq.getLocale().
     */
    public static Component createLearningObjectivesComponent(final String learningObjectives, final UserRequest ureq) {
        return createLearningObjectivesComponent(learningObjectives, ureq.getLocale());
    }

    public static Component createLearningObjectivesComponent(final String learningObjectives, final Locale locale) {
        final VelocityContainer vc = new VelocityContainer("learningObjs", VELOCITY_ROOT + "/objectives.html", new PackageTranslator(PACKAGE, locale), null);
        vc.contextPut("learningObjectives", learningObjectives);
        return vc;
    }
}
