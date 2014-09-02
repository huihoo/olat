package de.bps.onyx.plugin.restapi.config;

import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.connectors.rest.repository.course.AbstractCourseNodeWebService.CustomConfigDelegate;
import org.olat.connectors.rest.repository.course.config.CustomConfigFactory.ICustomConfigCreator;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.course.nodes.iq.IQEditController;

public class OnyxCustomConfigCreator implements ICustomConfigCreator {

	public OnyxCustomConfigCreator() {
		//
	}

	@Override
	public CustomConfigDelegate getTestCustomConfig(final RepositoryEntry repoEntry) {
		return new OnyxTestCustomConfig(repoEntry);
	}

	@Override
	public CustomConfigDelegate getSurveyCustomConfig(final RepositoryEntry repoEntry) {
		return new OnyxSurveyCustomConfig(repoEntry);
	}

	/* CustomConfigDelegate implementations */
	public class OnyxTestCustomConfig implements CustomConfigDelegate {
		private final RepositoryEntry testRepoEntry;

		@Override
		public boolean isValid() {
			return testRepoEntry != null;
		}

		public OnyxTestCustomConfig(final RepositoryEntry testRepoEntry) {
			this.testRepoEntry = testRepoEntry;
		}

		@Override
		public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
			moduleConfig.set(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY, testRepoEntry.getSoftkey());
			if (OnyxModule.isOnyxTest(testRepoEntry.getOlatResource())) {
				moduleConfig.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI2);
			}

		}
	}

	public class OnyxSurveyCustomConfig implements CustomConfigDelegate {
		private final RepositoryEntry surveyRepoEntry;

		public OnyxSurveyCustomConfig(final RepositoryEntry surveyRepoEntry) {
			this.surveyRepoEntry = surveyRepoEntry;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
			moduleConfig.set(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY, surveyRepoEntry.getSoftkey());
			moduleConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, new Boolean(true));
			moduleConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
			moduleConfig.set(IQEditController.CONFIG_KEY_TYPE, AssessmentInstance.QMD_ENTRY_TYPE_SURVEY);
			moduleConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE);
			if (OnyxModule.isOnyxTest(surveyRepoEntry)) {
				moduleConfig.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI2);
			}
		}
	}
}
