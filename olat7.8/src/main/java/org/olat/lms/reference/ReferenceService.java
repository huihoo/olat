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
package org.olat.lms.reference;

import java.util.List;
import java.util.Locale;

import org.olat.data.reference.Reference;
import org.olat.data.resource.OLATResource;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * offers methods form adding, deleting and finding references to a certain olat resource. E.g. in which courses is a a resource in use
 * 
 * <P>
 * Initial Date: 02.05.2011 <br>
 * 
 * @author guido
 */
public interface ReferenceService {

    /**
     * Add a new reference. The meaning of source and target is such as the source references the target.
     * 
     * @param source
     * @param target
     * @param userdata
     */
    public abstract void addReference(final OLATResourceable source, final OLATResourceable target, final String userdata);

    /**
     * List all references the source holds.
     * 
     * @param source
     * @return List of renerences.
     */
    public abstract List<Reference> getReferences(final OLATResourceable source);

    /**
     * List all sources which hold references to the target.
     * 
     * @param target
     * @return List of references.
     */
    public abstract List<Reference> getReferencesTo(final OLATResourceable target);

    /**
     * Check whether any references to the target exist.
     * 
     * @param target
     * @return True if references exist.
     */
    public abstract boolean hasReferencesTo(final OLATResourceable target);

    /**
     * Get an HTML summary of existing references or null if no references exist.
     * 
     * @param target
     * @param locale
     * @return HTML fragment or null if no references exist.
     */
    public abstract String getReferencesToSummary(final OLATResourceable target, final Locale locale);

    /**
     * @param Reference
     */
    public abstract void delete(final Reference ref);

    /**
     * Only for cleanup : Delete all references of an OLAT-resource.
     * 
     * @param olatResource
     *            an OLAT-Resource
     */
    public abstract void deleteAllReferencesOf(final OLATResource olatResource);

    public void updateRefTo(final OLATResourceable targetOLATResourceable, final OLATResourceable sourceOLATResourceable, final String refUserDataType);

    public void deleteRefTo(final OLATResourceable oLATResourceable, final String refUserDataType);

}
