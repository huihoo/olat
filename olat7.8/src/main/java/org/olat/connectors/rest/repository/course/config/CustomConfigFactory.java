package org.olat.connectors.rest.repository.course.config;

import org.olat.connectors.rest.repository.course.AbstractCourseNodeWebService.CustomConfigDelegate;
import org.olat.data.repository.RepositoryEntry;

public class CustomConfigFactory {

    static ICustomConfigCreator creator = null;

    protected CustomConfigFactory(final ICustomConfigCreator creator) {
        CustomConfigFactory.creator = creator;
    }

    public static CustomConfigDelegate getTestCustomConfig(final RepositoryEntry repoEntry) {
        return CustomConfigFactory.creator.getTestCustomConfig(repoEntry);
    }

    public static CustomConfigDelegate getSurveyCustomConfig(final RepositoryEntry repoEntry) {
        return CustomConfigFactory.creator.getSurveyCustomConfig(repoEntry);
    }

    public interface ICustomConfigCreator {
        public CustomConfigDelegate getTestCustomConfig(RepositoryEntry repoEntry);

        public CustomConfigDelegate getSurveyCustomConfig(RepositoryEntry repoEntry);
    }
}
