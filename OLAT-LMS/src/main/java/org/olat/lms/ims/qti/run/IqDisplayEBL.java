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
package org.olat.lms.ims.qti.run;

import java.io.File;

import org.olat.lms.ims.qti.process.AssessmentFactory;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.system.exception.AssertException;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 12.10.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class IqDisplayEBL {

    public AssessmentInstance getAssessment(IqDisplayParameterObjectEBL iqDisplayParameterObject) {
        AssessmentInstance assessment = null;

        if (iqDisplayParameterObject.getRepositorySoftKey() != null) {
            final String resourcePathInfo = iqDisplayParameterObject.getCallingResourceId() + File.separator + iqDisplayParameterObject.getCallingResourceDetail();
            assessment = AssessmentFactory.createAssessmentInstance(iqDisplayParameterObject.getIdentity(), iqDisplayParameterObject.getModuleConfiguration(),
                    iqDisplayParameterObject.isPreview(), resourcePathInfo);
        } else if (iqDisplayParameterObject.getResolver() != null) {
            assessment = AssessmentFactory.createAssessmentInstance(iqDisplayParameterObject.getResolver(), iqDisplayParameterObject.getPersister(),
                    iqDisplayParameterObject.getModuleConfiguration());
        }

        if (checkAssessment(assessment)) {
            throw new AssertException("Assessment Instance was null or no sections/items found.");
        }

        return assessment;
    }

    private boolean checkAssessment(AssessmentInstance assessment) {
        return assessment == null || assessment.getAssessmentContext().getSectionContext(0).getItemContextCount() == 0;
    }

}
