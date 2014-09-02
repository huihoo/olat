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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.data.reference.Reference;
import org.olat.data.reference.ReferenceDao;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceImpl;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.IAddController;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ReferenceServiceImpl
 * 
 * <P>
 * Initial Date: 02.05.2011 <br>
 * 
 * @author guido
 */
@Service("referenceService")
public class ReferenceServiceImpl implements ReferenceService {

    @Autowired
    ReferenceDao referenceDao;

    /**
     * [spring]
     */
    private ReferenceServiceImpl() {
        //
    }

    /**
     * @see org.olat.lms.reference.ReferenceService#addReference(org.olat.system.commons.resource.OLATResourceable, org.olat.system.commons.resource.OLATResourceable,
     *      java.lang.String)
     */
    @Override
    public void addReference(OLATResourceable source, OLATResourceable target, String userdata) {
        referenceDao.addReference(source, target, userdata);
    }

    /**
     * @see org.olat.lms.reference.ReferenceService#getReferences(org.olat.system.commons.resource.OLATResourceable)
     */
    @Override
    public List<Reference> getReferences(OLATResourceable source) {
        return referenceDao.getReferences(source);
    }

    /**
     * @see org.olat.lms.reference.ReferenceService#getReferencesTo(org.olat.system.commons.resource.OLATResourceable)
     */
    @Override
    public List<Reference> getReferencesTo(OLATResourceable target) {
        return referenceDao.getReferencesTo(target);
    }

    /**
     * @see org.olat.lms.reference.ReferenceService#hasReferencesTo(org.olat.system.commons.resource.OLATResourceable)
     */
    @Override
    public boolean hasReferencesTo(OLATResourceable target) {
        return referenceDao.hasReferencesTo(target);
    }

    /**
     * @see org.olat.lms.reference.ReferenceService#getReferencesToSummary(org.olat.system.commons.resource.OLATResourceable, java.util.Locale)
     */
    @Override
    public String getReferencesToSummary(final OLATResourceable target, final Locale locale) {
        final Translator translator = PackageUtil.createPackageTranslator(IAddController.class, locale);
        final StringBuilder result = new StringBuilder(100);
        final List refs = getReferencesTo(target);
        if (refs.size() == 0) {
            return null;
        }
        for (final Iterator iter = refs.iterator(); iter.hasNext();) {
            final Reference ref = (Reference) iter.next();
            final OLATResourceImpl source = ref.getSource();

            // special treatment for referenced courses: find out the course title
            if (source.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
                final ICourse course = CourseFactory.loadCourse(source);
                result.append(translator.translate("ref.course", new String[] { course.getCourseTitle() }));
            } else {
                result.append(translator.translate("ref.generic", new String[] { source.getKey().toString() }));
            }
            result.append("<br />");
        }
        return result.toString();
    }

    /**
     * @see org.olat.lms.reference.ReferenceService#delete(org.olat.data.reference.Reference)
     */
    @Override
    public void delete(Reference ref) {
        referenceDao.delete(ref);
    }

    /**
     * @see org.olat.lms.reference.ReferenceService#deleteAllReferencesOf(org.olat.data.resource.OLATResource)
     */
    @Override
    public void deleteAllReferencesOf(OLATResource olatResource) {
        referenceDao.deleteAllReferencesOf(olatResource);

    }

    /**
     * Sets the reference from a course to a shared folder.
     * 
     * @param sharedFolderRe
     * @param course
     */
    public void updateRefTo(final OLATResourceable targetOLATResourceable, final OLATResourceable sourceOLATResourceable, final String refUserDataType) {
        deleteRefTo(sourceOLATResourceable, refUserDataType);
        referenceDao.addReference(sourceOLATResourceable, targetOLATResourceable, refUserDataType);
    }

    /**
     * Deletes the reference from a course to a shared folder.
     * 
     * @param entry
     *            - the course that holds a reference to a sharedfolder
     */
    public void deleteRefTo(final OLATResourceable oLATResourceable, final String refUserDataType) {
        final List repoRefs = getReferences(oLATResourceable);
        for (final Iterator iter = repoRefs.iterator(); iter.hasNext();) {
            final Reference ref = (Reference) iter.next();
            if (ref.getUserdata().equals(refUserDataType)) {
                delete(ref);
                return;
            }
        }
    }

}
