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
package org.olat.presentation.framework.core.control.winmgr;

import java.util.ArrayList;
import java.util.List;

import org.olat.presentation.framework.core.control.state.ExtendedControllerState;
import org.olat.presentation.framework.core.control.state.GUIPath;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class GUIPathImpl implements GUIPath {
    private List<ExtendedControllerState> extStateList = new ArrayList<ExtendedControllerState>(8);

    /*
     * (non-Javadoc)
     */
    @Override
    public void addStateInfo(ExtendedControllerState extstate) {
        extStateList.add(extstate);
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public ExtendedControllerState getExtendedControllerStateAt(int pos) {
        return extStateList.get(pos);
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public int getExtendedControllerStateCount() {
        return extStateList.size();
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public boolean isSame(GUIPath other) {
        // compare all controllerstates
        int osize = other.getExtendedControllerStateCount();
        if (osize != extStateList.size())
            return false;
        // H: at least the same number of entries: now we must compare one by one
        for (int i = 0; i < osize; i++) {
            ExtendedControllerState ostate = other.getExtendedControllerStateAt(i);
            ExtendedControllerState mystate = this.getExtendedControllerStateAt(i);
            if (!ostate.isSame(mystate))
                return false;
        }
        return true;

    }

    @Override
    public String toString() {
        int size = extStateList.size();
        StringBuilder sb = new StringBuilder(size + ":\n");
        for (int i = size - 1; i >= 0; i--) {
            ExtendedControllerState ecstate = extStateList.get(i);
            sb.append("[").append(ecstate.toString()).append("]\n");
        }
        return sb.toString();
    }

}
