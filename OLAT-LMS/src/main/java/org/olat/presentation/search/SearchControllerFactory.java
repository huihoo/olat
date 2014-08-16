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

package org.olat.presentation.search;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.search.document.ContextHelpDocument;
import org.olat.lms.search.document.ResultDocument;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * O
 * <P>
 * Initial Date: 3 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Component
public class SearchControllerFactory implements SearchServiceUIFactory {

    private static final Logger log = LoggerHelper.getLogger();

    private final Map<String, ResultControllerCreator> resultControllerCreator = new HashMap<String, ResultControllerCreator>();

    protected SearchControllerFactory() {
    }

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private BusinessGroupService businessGroupService;
    @Autowired
    private UserService userService;

    /**
     * 
     * @param creator
     */
    public void addResultControllerCreator(final ResultControllerCreator creator) {
        resultControllerCreator.put(creator.getFileType(), creator);
    }

    @Override
    public SearchController createInputController(final UserRequest ureq, final WindowControl wControl, final DisplayOption displayOption, final Form mainForm) {
        String resourceUrl = null;
        final BusinessControl bc = wControl.getBusinessControl();
        if (bc != null) {
            resourceUrl = bc.getAsString();
        }

        SearchInputController searchCtrl;
        if (mainForm == null) {
            searchCtrl = new SearchInputController(ureq, wControl, resourceUrl, displayOption);
        } else {
            searchCtrl = new SearchInputController(ureq, wControl, resourceUrl, displayOption, mainForm);
        }
        return searchCtrl;
    }

    @Override
    public SearchController createSearchController(final UserRequest ureq, final WindowControl wControl) {
        String resourceUrl = null;
        final BusinessControl bc = wControl.getBusinessControl();
        if (bc != null) {
            resourceUrl = bc.getAsString();
        }
        return new ResultsSearchController(ureq, wControl, resourceUrl);
    }

    @Override
    public ResultController createController(final UserRequest ureq, final WindowControl wControl, final Form mainForm, final ResultDocument document) {
        final String documentType = document.getDocumentType();
        if (resultControllerCreator.containsKey(documentType)) {
            final ResultControllerCreator creator = resultControllerCreator.get(documentType);
            return creator.createController(ureq, wControl, mainForm, document);
        }

        ResultController ctrl;
        if (ContextHelpDocument.TYPE.equals(documentType)) {
            ctrl = new ContextHelpResultController(ureq, wControl, mainForm, document);
        } else {
            ctrl = new StandardResultController(ureq, wControl, mainForm, document);
        }
        return ctrl;
    }

    @Override
    public String getBusinessPathLabel(final String token, final List<String> allTokens, final Locale locale) {
        try {
            final String[] splitted = token.split("[:]");
            if (splitted != null && splitted.length == 2) {
                final String tokenType = splitted[0];
                final String tokenKey = splitted[1];
                if ("RepositoryEntry".equals(tokenType)) {
                    final RepositoryEntry re = repositoryService.lookupRepositoryEntry(Long.parseLong(tokenKey));
                    return re.getDisplayname();
                }
                if ("CourseNode".equals(tokenType)) {
                    final String repoKey = allTokens.get(0).split("[:]")[1];
                    final RepositoryEntry re = repositoryService.lookupRepositoryEntry(Long.parseLong(repoKey));
                    final ICourse course = CourseFactory.loadCourse(re.getOlatResource());
                    final CourseNode courseNode = course.getRunStructure().getNode(tokenKey);
                    return courseNode.getShortTitle();
                }
                if ("Identity".equals(tokenType)) {
                    final Identity identity = baseSecurity.loadIdentityByKey(Long.parseLong(tokenKey));
                    return userService.getFirstAndLastname(identity.getUser());
                }
                if ("BusinessGroup".equals(tokenType)) {
                    final BusinessGroup bg = businessGroupService.loadBusinessGroup(Long.parseLong(tokenKey), true);
                    return bg.getName();
                }

                final Translator translator = PackageUtil.createPackageTranslator(this.getClass(), locale);
                final String translated = translator.translate(tokenType);
                if (translated == null || translated.length() > 64) {
                    return token;// no translation, translator return an error
                }
                return translated;
            }
        } catch (final Exception ex) {
            log.warn("Problem to decipher business path token: " + token, ex);
        }
        return token;
    }

}
