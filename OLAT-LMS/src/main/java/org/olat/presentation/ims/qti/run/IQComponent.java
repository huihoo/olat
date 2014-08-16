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

package org.olat.presentation.ims.qti.run;

import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Initial Date: Mar 3, 2004
 * 
 * @author Mike Stock
 */
public class IQComponent extends Component {
    private static final ComponentRenderer RENDERER = new IQComponentRenderer();

    private final AssessmentInstance ai;
    private final IQMenuDisplayConf mdc;
    private final boolean provideMemoField;

    /**
	 * 
	 */
    public IQComponent(final String name, final Translator translator, final AssessmentInstance ai, final IQMenuDisplayConf menuDispConf, final boolean provideMemoField) {
        super(name, translator);
        this.ai = ai;
        this.mdc = menuDispConf;
        this.provideMemoField = provideMemoField;
    }

    /**
	 */
    @Override
    protected void doDispatchRequest(final UserRequest ureq) {
        if (ureq.getParameter("cid") != null) {
            fireEvent(ureq, new Event(ureq.getParameter("cid")));
        }
    }

    /**
     * @return
     */
    public AssessmentInstance getAssessmentInstance() {
        return ai;
    }

    /**
     * @return Returns the mdc.
     */
    public IQMenuDisplayConf getMenuDisplayConf() {
        return mdc;
    }

    @Override
    public ComponentRenderer getHTMLRendererSingleton() {
        return RENDERER;
    }

    public boolean provideMemoField() {
        return provideMemoField;
    }

}
