package org.olat.connectors.rest.repository.course.config;

import org.olat.connectors.rest.repository.course.AbstractCourseNodeWebService.CustomConfigDelegate;
import org.olat.connectors.rest.repository.course.config.CustomConfigFactory.ICustomConfigCreator;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.presentation.course.nodes.iq.IQEditController;

public class OlatCustomConfigCreator implements ICustomConfigCreator {

    protected OlatCustomConfigCreator() {
        //
    }

    @Override
    public CustomConfigDelegate getTestCustomConfig(final RepositoryEntry repoEntry) {
        return new OlatTestCustomConfig(repoEntry);
    }

    @Override
    public CustomConfigDelegate getSurveyCustomConfig(final RepositoryEntry repoEntry) {
        return new OlatSurveyCustomConfig(repoEntry);
    }

    /* CustomConfigDelegate implementations */
    public class OlatTestCustomConfig implements CustomConfigDelegate {
        private final RepositoryEntry testRepoEntry;

        @Override
        public boolean isValid() {
            return testRepoEntry != null;
        }

        public OlatTestCustomConfig(final RepositoryEntry testRepoEntry) {
            this.testRepoEntry = testRepoEntry;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            moduleConfig.set(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY, testRepoEntry.getSoftkey());
        }
    }

    public class OlatSurveyCustomConfig implements CustomConfigDelegate {
        private final RepositoryEntry surveyRepoEntry;

        public OlatSurveyCustomConfig(final RepositoryEntry surveyRepoEntry) {
            this.surveyRepoEntry = surveyRepoEntry;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            moduleConfig.set(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY, surveyRepoEntry.getSoftkey());
            moduleConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, new Boolean(true));
            moduleConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
            moduleConfig.set(IQEditController.CONFIG_KEY_TYPE, AssessmentInstance.QMD_ENTRY_TYPE_SURVEY);
            moduleConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE);
        }
    }

}
