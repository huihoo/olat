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

package org.olat.data.reference;

import org.olat.data.commons.database.PersistentObject;
import org.olat.data.resource.OLATResourceImpl;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: May 27, 2004
 * 
 * @author Mike Stock Comment:
 */
public class ReferenceImpl extends PersistentObject implements Reference {

    private static final long serialVersionUID = 4886996977280342420L;
    private OLATResourceImpl source;
    private OLATResourceImpl target;
    private String userdata;
    private static final int USERDATA_MAXLENGTH = 64;

    protected ReferenceImpl() {
        // hibernate
    }

    /**
     * @param source
     * @param target
     * @param userdata
     */
    public ReferenceImpl(final OLATResourceImpl source, final OLATResourceImpl target, final String userdata) {
        this.source = source;
        this.target = target;
        this.userdata = userdata;
    }

    /**
     * @see org.olat.data.reference.Reference#getSource()
     */
    @Override
    public OLATResourceImpl getSource() {
        return source;
    }

    /**
     * @param source
     *            The source to set.
     */
    public void setSource(final OLATResourceImpl source) {
        this.source = source;
    }

    /**
     * @see org.olat.data.reference.Reference#getTarget()
     */
    @Override
    public OLATResourceImpl getTarget() {
        return target;
    }

    /**
     * @param target
     *            The target to set.
     */
    public void setTarget(final OLATResourceImpl target) {
        this.target = target;
    }

    /**
     * @see org.olat.data.reference.Reference#getUserdata()
     */
    @Override
    public String getUserdata() {
        return userdata;
    }

    /**
     * @param userdata
     *            The userdata to set.
     */
    public void setUserdata(final String userdata) {
        if (userdata != null && userdata.length() > USERDATA_MAXLENGTH) {
            throw new AssertException("field userdata of table o_reference too long");
        }
        this.userdata = userdata;
    }

}
