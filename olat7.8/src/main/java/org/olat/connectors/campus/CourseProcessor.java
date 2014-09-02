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
package org.olat.connectors.campus;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.olat.data.course.campus.Course;
import org.olat.data.course.campus.DaoManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is an implementation of {@link ItemProcessor} that validates the input Course item, <br>
 * modifies it according to some criteria and returns it as output Course item.<br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class CourseProcessor implements ItemProcessor<Course, Course> {
    private static final Logger LOG = LoggerHelper.getLogger();

    @Autowired
    DaoManager daoManager;

    private Set<Long> processedIdsSet;

    private List<Long> enabledOrgs;

    private Map<String, String> semesterMap = new HashMap<String, String>();

    private static final String WHITESPACE = " ";

    @PostConstruct
    public void init() {
        processedIdsSet = new HashSet<Long>();
        enabledOrgs = daoManager.getIdsOfAllEnabledOrgs();
    }

    @PreDestroy
    public void cleanUp() {
        processedIdsSet.clear();
        enabledOrgs.clear();
    }

    /**
     * Sets the Map of semesters
     * 
     * @param semesterMap
     *            the Map of semesters
     */
    public void setSemesterMap(Map<String, String> semesterMap) {
        this.semesterMap = semesterMap;
    }

    /**
     * Returns null if the input course has been already processed, <br>
     * otherwise modifies it according to some criteria and returns it as output
     * 
     * @param course
     *            the Course to be processed
     */
    public Course process(Course course) throws Exception {

        // JUST IGNORE THE DUPLICATES
        if (!CampusUtils.addIfNotAlreadyProcessed(processedIdsSet, course.getId())) {
            LOG.debug("This is a duplicate of this course [" + course.getId() + "]");
            return null;
        }

        course.setModifiedDate(new Date());

        if (course.getTitle().contains(CampusUtils.SEMICOLON_REPLACEMENT)) {
            course.setTitle(StringUtils.replace(course.getTitle(), CampusUtils.SEMICOLON_REPLACEMENT, CampusUtils.SEMICOLON));
        }

        String shortSemester = buildShortSemester(course.getSemester());
        if (shortSemester != null) {
            course.setShortSemester(shortSemester);
            course.setTitle(shortSemester.concat(WHITESPACE).concat(course.getTitle()));
        }

        if (enabledOrgs.isEmpty()) {
            if (course.getIpz().equalsIgnoreCase("X")) {
                course.setEnabled("1");
            }
        } else if (enabledOrgs.contains(course.getOrg1()) || enabledOrgs.contains(course.getOrg2()) || enabledOrgs.contains(course.getOrg3())
                || enabledOrgs.contains(course.getOrg4()) || enabledOrgs.contains(course.getOrg5())) {
            course.setEnabled("1");
        }

        return course;
    }

    /**
     * Build the shortSemester from the given semester
     * 
     * @param semester
     *            The semester from which the shortSemester will be built
     */
    private String buildShortSemester(String semester) {
        String shortSemester = null;

        String[] split = StringUtils.split(semester, WHITESPACE);
        if (split != null) {
            String yy = (split[1] != null) ? split[1].substring(2) : "";
            if (split[0] != null) {
                shortSemester = yy.concat(semesterMap.get(split[0].substring(0, 1)));
            }
        }
        return shortSemester;
    }

}
