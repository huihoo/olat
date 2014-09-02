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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.core.control.generic.layouter;

import java.util.Iterator;
import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost<br>
 *         TODO:fj:b make a generic layouter like swings gridbaglayout or such
 */
public class LayoutController extends DefaultController {
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(LayoutController.class);

    private VelocityContainer myContent;
    private List controllers;

    /**
     * @deprecated brasato:: improve / usage?? at least use mycontroller.add(component comp) - as in swing / layout
     * @param controllers
     */
    @Deprecated
    public LayoutController(Layouter layouter, List controllers) {
        super(null);
        // TODO:fj:b Layoutmanagers which make sense for the web
        if (layouter != null && layouter != Layouts.VERTICAL)
            throw new AssertException("not implemented yet!");
        this.controllers = controllers;

        myContent = new VelocityContainer("layouter", VELOCITY_ROOT + "/vertical.html", null, null);

        int cnt = controllers.size();
        String[] names = new String[cnt];
        for (int i = 0; i < cnt; i++) {
            String cName = "c" + i; // c0 c1 c2
            names[i] = cName;
            Controller cont = (Controller) controllers.get(i);
            myContent.put(cName, cont.getInitialComponent());
        }
        myContent.contextPut("names", names);
        setInitialComponent(myContent);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // nothing to listen to
    }

    /**
	 */
    @Override
    protected void doDispose() {
        for (Iterator it_conts = controllers.iterator(); it_conts.hasNext();) {
            Controller cont = (Controller) it_conts.next();
            cont.dispose();
        }
    }

}
