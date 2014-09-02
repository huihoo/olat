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

package org.olat.lms.group;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.resource.OLATResource;
import org.olat.data.user.User;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.learn.DefaultContextTranslationHelper;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.exception.OLATRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Christian Guretzki
 */

@Component
public class BusinessGroupArchiver implements Initializable {

    private static final String DELIMITER = "\t";
    private static final String EOL = "\n";
    private final static String ALL_IN_ONE_FILE_NAME_PREFIX = "members";
    private final static String ZIP_WITH_FILE_PER_GROUP_NAME_PREFIX = "members";

    private static final String FILE_PER_GROUP_OR_AREA_INCL_GROUP_MEMBERSHIP = "memberlistwizard.archive.type.filePerGroupOrAreaInclGroupMembership"; // used as well
    private static final String FILE_PER_GROUP_OR_AREA = "memberlistwizard.archive.type.filePerGroupOrArea"; // used as well as translation key
    private static final String ALL_IN_ONE = "memberlistwizard.archive.type.allInOne";
    private static String OWNER = "owner";
    private static String PARTICIPANT = "participant";
    private static String WAITING = "waiting";

    private Translator translator;
    private Map<Locale, Translator> translatorMap;
    private List<UserPropertyHandler> userPropertyHandlers;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    UserService userService;
    @Autowired
    BGContextDao contextManager;
    @Autowired
    BGAreaDao areaManager;
    @Autowired
    I18nModule i18nModule;

    /**
	 */
    private BusinessGroupArchiver() {
        //
    }

    @Override
    @PostConstruct
    public void init() {
        translator = PackageUtil.createPackageTranslator(I18nPackage.GROUP_, i18nModule.getDefaultLocale());
        translator = userService.getUserPropertiesConfig().getTranslator(translator);
        // get user property handlers used in this group archiver
        userPropertyHandlers = userService.getUserPropertyHandlersFor(BusinessGroupArchiver.class.getCanonicalName(), true);

    }

    /**
     * Retrives a PackageTranslator for the input locale.
     * 
     * @param locale
     * @return
     */
    protected Translator getPackageTranslator(final Locale locale) {
        if (i18nModule.getDefaultLocale().equals(locale)) {
            return translator;
        } else {
            if (translatorMap == null) {
                translatorMap = new HashMap<Locale, Translator>();
            }
            if (translatorMap.containsKey(locale)) {
                return translatorMap.get(locale);
            } else {
                Translator trans = PackageUtil.createPackageTranslator(I18nPackage.GROUP_, locale);
                translatorMap.put(locale, trans);
                return trans;
            }
        }
    }

    public void archiveGroup(final BusinessGroup businessGroup, final File archiveFile) {
        FileUtils.save(archiveFile, toXls(businessGroup), "utf-8");
    }

    private String toXls(final BusinessGroup businessGroup) {
        final StringBuffer buf = new StringBuffer();
        // Export Header
        buf.append(translator.translate("archive.group.name"));
        buf.append(DELIMITER);
        buf.append(businessGroup.getName());
        buf.append(DELIMITER);
        buf.append(translator.translate("archive.group.type"));
        buf.append(DELIMITER);
        buf.append(businessGroup.getType());
        buf.append(DELIMITER);
        buf.append(translator.translate("archive.group.description"));
        buf.append(DELIMITER);
        buf.append(FilterFactory.getHtmlTagsFilter().filter(businessGroup.getDescription()));
        buf.append(EOL);

        appendIdentityTable(buf, businessGroup.getOwnerGroup(), translator.translate("archive.header.owners"));
        appendIdentityTable(buf, businessGroup.getPartipiciantGroup(), translator.translate("archive.header.partipiciant"));

        if (businessGroup.getWaitingListEnabled()) {
            appendIdentityTable(buf, businessGroup.getWaitingGroup(), translator.translate("archive.header.waitinggroup"));
        }
        return buf.toString();
    }

    private void appendIdentityTable(final StringBuffer buf, final SecurityGroup group, final String title) {
        if (group != null) {
            appendTitle(buf, title);
            appendIdentityTableHeader(buf);
            for (final Iterator iter = baseSecurity.getIdentitiesAndDateOfSecurityGroup(group).iterator(); iter.hasNext();) {
                final Object[] element = (Object[]) iter.next();
                final Identity identity = (Identity) element[0];
                final Date addedTo = (Date) element[1];
                appendIdentity(buf, identity, addedTo);
            }
        }
    }

    private void appendTitle(final StringBuffer buf, final String title) {
        buf.append(EOL);
        buf.append(title);
        buf.append(EOL);
    }

    private void appendIdentity(final StringBuffer buf, final Identity owner, final Date addedTo) {
        final Locale loc = translator.getLocale();
        // add the identities user name
        buf.append(owner.getName());
        buf.append(DELIMITER);
        // add all user properties
        for (final UserPropertyHandler propertyHandler : userPropertyHandlers) {
            final String value = propertyHandler.getUserProperty(owner.getUser(), loc);
            if (StringHelper.containsNonWhitespace(value)) {
                buf.append(value);
            }
            buf.append(DELIMITER);
        }
        // add the added-to date
        buf.append(addedTo.toString());
        buf.append(EOL);
    }

    private void appendIdentityTableHeader(final StringBuffer buf) {
        // first the identites name
        buf.append(translator.translate("table.user.login"));
        buf.append(DELIMITER);
        // second the users properties
        for (final UserPropertyHandler propertyHandler : userPropertyHandlers) {
            final String label = translator.translate(propertyHandler.i18nColumnDescriptorLabelKey());
            buf.append(label);
            buf.append(DELIMITER);
        }
        // third the users added-to date
        buf.append(translator.translate("table.subject.addeddate"));
        buf.append(EOL);
    }

    public void archiveBGContext(final BGContext context, final File archiveFile) {
        FileUtils.save(archiveFile, toXls(context), "utf-8");
    }

    private String toXls(final BGContext context) {
        final StringBuffer buf = new StringBuffer();
        // Export Context Header
        buf.append(translator.translate("archive.group.context.name"));
        buf.append(DELIMITER);
        buf.append(context.getName());
        buf.append(DELIMITER);
        buf.append(translator.translate("archive.group.context.type"));
        buf.append(DELIMITER);
        buf.append(context.getGroupType());
        buf.append(DELIMITER);
        buf.append(translator.translate("archive.group.context.description"));
        buf.append(DELIMITER);
        buf.append(FilterFactory.getHtmlTagsFilter().filter(context.getDescription()));
        buf.append(EOL);
        final List groups = contextManager.getGroupsOfBGContext(context);
        for (final Iterator iter = groups.iterator(); iter.hasNext();) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            buf.append(toXls(group));
            buf.append(EOL);
            buf.append(EOL);
        }
        return buf.toString();
    }

    /**
     * Creates an temp CSV (comma separated) file containing the members info (namely with the columns specified in "columnList"), the area info (for the filtered
     * "areaList"), and separated in role sections: owners, participants and waiting.
     * 
     * @param context
     * @param columnList
     * @param areaList
     * @param archiveType
     * @param userLocale
     * @return the output file which could be an CSV or a zip file depending on the input archiveType.
     */
    public File archiveAreaMembers(final BGContext context, final List<String> columnList, final List<BGArea> areaList, final String archiveType, final Locale locale,
            final String userCharset) {

        final List<Member> owners = new ArrayList<Member>();
        final List<Member> participants = new ArrayList<Member>();
        final List<Member> waitings = new ArrayList<Member>();

        final List areas = areaManager.findBGAreasOfBGContext(context);
        for (final Iterator areaIterator = areas.iterator(); areaIterator.hasNext();) {
            final BGArea area = (BGArea) areaIterator.next();
            if (areaList.contains(area)) { // rely on the equals() method of the BGArea impl
                final List areaBusinessGroupList = areaManager.findBusinessGroupsOfArea(area);
                for (final Iterator groupIterator = areaBusinessGroupList.iterator(); groupIterator.hasNext();) {
                    final BusinessGroup group = (BusinessGroup) groupIterator.next();
                    if (group.getOwnerGroup() != null) {
                        final Iterator ownerIterator = baseSecurity.getIdentitiesAndDateOfSecurityGroup(group.getOwnerGroup()).iterator();
                        addMembers(area.getKey(), ownerIterator, owners, OWNER);
                    }
                    if (group.getPartipiciantGroup() != null) {
                        final Iterator participantsIterator = baseSecurity.getIdentitiesAndDateOfSecurityGroup(group.getPartipiciantGroup()).iterator();
                        addMembers(area.getKey(), participantsIterator, participants, PARTICIPANT);
                    }
                    if (group.getWaitingGroup() != null) {
                        final Iterator waitingIterator = baseSecurity.getIdentitiesAndDateOfSecurityGroup(group.getWaitingGroup()).iterator();
                        addMembers(area.getKey(), waitingIterator, waitings, WAITING);
                    }
                }
            }
        }
        final Translator trans = getPackageTranslator(locale);
        final List<OrganisationalEntity> organisationalEntityList = getOrganisationalEntityList(areaList);
        return generateArchiveFile(context, owners, participants, waitings, columnList, organisationalEntityList, trans.translate("archive.areas"), archiveType, locale,
                userCharset);
    }

    /**
     * Creates an temp CSV (comma separated) file containing the members info (namely with the columns specified in "columnList"), the groups info (for the filtered
     * "groupList"), and separated in role sections: owners, participants and waiting.
     * 
     * @param context
     * @param columnList
     * @param groupList
     * @param archiveType
     * @param userLocale
     * @return the output file which could be an CSV or a zip file depending on the input archiveType.
     */
    public File archiveGroupMembers(final BGContext context, final List<String> columnList, final List<BusinessGroup> groupList, final String archiveType,
            final Locale locale, final String userCharset) {

        final List<Member> owners = new ArrayList<Member>();
        final List<Member> participants = new ArrayList<Member>();
        final List<Member> waitings = new ArrayList<Member>();

        final List groups = contextManager.getGroupsOfBGContext(context);
        for (final Iterator iter = groups.iterator(); iter.hasNext();) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            if (groupList.contains(group)) { // rely on the equals() method of the BusinessGroup impl
                if (group.getOwnerGroup() != null) {
                    final Iterator ownerIterator = baseSecurity.getIdentitiesAndDateOfSecurityGroup(group.getOwnerGroup()).iterator();
                    addMembers(group.getKey(), ownerIterator, owners, OWNER);
                }
                if (group.getPartipiciantGroup() != null) {
                    final Iterator participantsIterator = baseSecurity.getIdentitiesAndDateOfSecurityGroup(group.getPartipiciantGroup()).iterator();
                    addMembers(group.getKey(), participantsIterator, participants, PARTICIPANT);
                }
                if (group.getWaitingGroup() != null) {
                    final Iterator waitingIterator = baseSecurity.getIdentitiesAndDateOfSecurityGroup(group.getWaitingGroup()).iterator();
                    addMembers(group.getKey(), waitingIterator, waitings, WAITING);
                }
            }
        }

        final Translator trans = getPackageTranslator(locale);
        final List<OrganisationalEntity> organisationalEntityList = getOrganisationalEntityList(groupList);
        return generateArchiveFile(context, owners, participants, waitings, columnList, organisationalEntityList, trans.translate("archive.groups"), archiveType, locale,
                userCharset);
    }

    /**
     * @param context
     * @return a List with the course titles associated with the input BGContext.
     */
    private List<String> getCourseTitles(final BGContext context) {
        final List<String> courseTitles = new ArrayList<String>();
        final List resources = contextManager.findOLATResourcesForBGContext(context);
        for (final Iterator iter = resources.iterator(); iter.hasNext();) {
            final OLATResource resource = (OLATResource) iter.next();
            if (resource.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
                final ICourse course = CourseFactory.loadCourse(resource);
                courseTitles.add(course.getCourseTitle());
            }
        }
        return courseTitles;
    }

    private File generateArchiveFile(final BGContext context, final List<Member> owners, final List<Member> participants, final List<Member> waitings,
            final List<String> columnList, final List<OrganisationalEntity> organisationalEntityList, final String orgEntityTitle, final String archiveType,
            final Locale userLocale, final String charset) {
        // TODO: sort member lists
        File outFile = null;
        final Translator trans = getPackageTranslator(userLocale);
        final String archiveTitle = trans.translate("archive.title") + ": " + DefaultContextTranslationHelper.translateIfDefaultContextName(context, trans);
        try {
            if (ALL_IN_ONE.equals(archiveType)) {
                // File tempDir = getTempDir();
                outFile = archiveAllInOne(context, owners, participants, waitings, archiveTitle, columnList, organisationalEntityList, orgEntityTitle, userLocale,
                        ALL_IN_ONE_FILE_NAME_PREFIX, null, charset);
            } else if (FILE_PER_GROUP_OR_AREA_INCL_GROUP_MEMBERSHIP.equals(archiveType)) {
                outFile = archiveFilePerGroupInclGroupmembership(context, owners, participants, waitings, archiveTitle, columnList, organisationalEntityList,
                        orgEntityTitle, userLocale, charset);
            } else if (FILE_PER_GROUP_OR_AREA.equals(archiveType)) {
                outFile = archiveFilePerGroup(context, owners, participants, waitings, columnList, organisationalEntityList, orgEntityTitle, userLocale, charset);
            }
        } catch (final IOException e) {
            throw new OLATRuntimeException(BusinessGroupArchiver.class, "could not create temp file", e);
        }
        return outFile;
    }

    /**
     * Generates a single file for all groups. <br>
     * It is the responsability of the caller to delete the returned file after download.
     * 
     * @param owners
     * @param participants
     * @param waitings
     * @param columnList
     * @param groupList
     * @param userLocale
     * @return the generated file located into the temp dir.
     */
    private File archiveAllInOne(final BGContext context, final List<Member> owners, final List<Member> participants, final List<Member> waitings,
            final String contextName, final List<String> columnList, final List<OrganisationalEntity> organisationalEntityList, final String orgEntityTitle,
            final Locale userLocale, String fileNamePrefix, final File tempDir, final String charset) throws IOException {
        File outFile = null;
        final StringBuffer stringBuffer = new StringBuffer();

        final Translator trans = getPackageTranslator(userLocale);
        final Translator propertyHandlerTranslator = userService.getUserPropertiesConfig().getTranslator(translator);
        appendContextInfo(stringBuffer, context, userLocale);
        if (owners.size() > 0) {
            appendSection(stringBuffer, trans.translate("archive.header.owners"), owners, columnList, organisationalEntityList, orgEntityTitle,
                    propertyHandlerTranslator, OWNER);
        }
        if (participants.size() > 0) {
            appendSection(stringBuffer, trans.translate("archive.header.partipiciant"), participants, columnList, organisationalEntityList, orgEntityTitle,
                    propertyHandlerTranslator, PARTICIPANT);
        }
        if (waitings.size() > 0) {
            appendSection(stringBuffer, trans.translate("archive.header.waitinggroup"), waitings, columnList, organisationalEntityList, orgEntityTitle,
                    propertyHandlerTranslator, WAITING);
        }
        appendInternInfo(stringBuffer, contextName, userLocale);
        // prefix must be at least 3 chars
        // add two of _ more if this is not the case
        fileNamePrefix = fileNamePrefix + "_";
        fileNamePrefix = fileNamePrefix.length() >= 3 ? fileNamePrefix : fileNamePrefix + "__";
        outFile = File.createTempFile(fileNamePrefix, ".xls", tempDir);
        FileUtils.save(outFile, stringBuffer.toString(), charset);
        // FileUtils.saveString(outFile, stringBuffer.toString());
        String outFileName = outFile.getName();
        outFileName = outFileName.substring(0, outFileName.lastIndexOf("_"));
        outFileName += ".xls";
        final File renamedFile = new File(outFile.getParentFile(), outFileName);
        final boolean succesfullyRenamed = outFile.renameTo(renamedFile);
        if (succesfullyRenamed) {
            outFile = renamedFile;
        }

        return outFile;
    }

    private void appendInternInfo(final StringBuffer buf, final String title, final Locale userLocale) {
        final Translator trans = getPackageTranslator(userLocale);
        buf.append(EOL);
        buf.append(trans.translate("archive.interninfo"));
        buf.append(EOL);
        buf.append(title);
        buf.append(EOL);
    }

    /**
     * @return a temporary dir in the default temporary-file directory.
     * @throws IOException
     */
    private File getTempDir() throws IOException {
        // prefix must be at least 3 chars
        final File tempDir = File.createTempFile("temp", "archive");
        if (tempDir.delete()) {
            tempDir.mkdir();
        }
        return tempDir;
    }

    /**
     * Generates a CSV file per group and then creates a zip with them.
     * 
     * @param owners
     * @param participants
     * @param waitings
     * @param contextName
     * @param columnList
     * @param groupList
     * @param userLocale
     * @return the output zip file located into the temp dir.
     */
    private File archiveFilePerGroupInclGroupmembership(final BGContext context, final List<Member> owners, final List<Member> participants, final List<Member> waitings,
            final String contextName, final List<String> columnList, final List<OrganisationalEntity> groupList, final String orgEntityTitle, final Locale userLocale,
            final String charset) {
        final Set<String> outFiles = new HashSet<String>();
        File root = null;
        File tempDir = null;
        try {
            tempDir = getTempDir();
            final Iterator<OrganisationalEntity> groupIterator = groupList.iterator();
            while (groupIterator.hasNext()) {
                final OrganisationalEntity group = groupIterator.next();
                final List<Member> groupOwners = getFilteredList(owners, group, BusinessGroupArchiver.OWNER);
                final List<Member> groupParticipants = getFilteredList(participants, group, BusinessGroupArchiver.PARTICIPANT);
                final List<Member> groupWaiting = getFilteredList(waitings, group, BusinessGroupArchiver.WAITING);

                final File filePerGroup = archiveAllInOne(context, groupOwners, groupParticipants, groupWaiting, contextName, columnList, groupList, orgEntityTitle,
                        userLocale, group.getName(), tempDir, charset);
                if (root == null && filePerGroup != null) {
                    root = filePerGroup.getParentFile();
                }
                outFiles.add(filePerGroup.getName());
            }
            // prefix must be at least 3 chars
            final File zipFile = File.createTempFile(ZIP_WITH_FILE_PER_GROUP_NAME_PREFIX, ".zip");
            zipFile.delete();
            final boolean successfully = ZipUtil.zip(outFiles, root, zipFile, true);
            if (successfully) {
                return zipFile;
            }
        } catch (final IOException e) {
            throw new OLATRuntimeException(BusinessGroupArchiver.class, "could not create temp file", e);
        } finally {
            if (tempDir != null) {
                FileUtils.deleteDirsAndFiles(tempDir, true, true);
            }
        }
        return null;
    }

    /**
     * Generates a CSV file per group and then creates a zip with them.
     * 
     * @param owners
     * @param participants
     * @param waitings
     * @param contextName
     * @param columnList
     * @param groupList
     * @param userLocale
     * @return the output zip file located into the temp dir.
     */
    private File archiveFilePerGroup(final BGContext context, final List<Member> owners, final List<Member> participants, final List<Member> waitings,
            final List<String> columnList, final List<OrganisationalEntity> groupList, final String orgEntityTitle, final Locale userLocale, final String charset) {
        final Set<String> outFiles = new HashSet<String>();
        File root = null;
        File tempDir = null;
        try {
            tempDir = getTempDir();
            final Iterator<OrganisationalEntity> groupIterator = groupList.iterator();
            while (groupIterator.hasNext()) {
                final OrganisationalEntity group = groupIterator.next();
                final List<Member> groupOwners = getFilteredList(owners, group, OWNER);
                final List<Member> groupParticipants = getFilteredList(participants, group, PARTICIPANT);
                final List<Member> groupWaiting = getFilteredList(waitings, group, WAITING);

                final File filePerGroup = archiveFileSingleGroup(context, groupOwners, groupParticipants, groupWaiting, columnList, groupList, orgEntityTitle,
                        userLocale, group.getName(), tempDir, charset);
                if (root == null && filePerGroup != null) {
                    root = filePerGroup.getParentFile();
                }
                outFiles.add(filePerGroup.getName());
            }
            // prefix must be at least 3 chars
            final File zipFile = File.createTempFile(ZIP_WITH_FILE_PER_GROUP_NAME_PREFIX, ".zip");
            zipFile.delete();
            final boolean successfully = ZipUtil.zip(outFiles, root, zipFile, true);
            if (successfully) {
                return zipFile;
            }
        } catch (final IOException e) {
            throw new OLATRuntimeException(BusinessGroupArchiver.class, "could not create temp file", e);
        } finally {
            if (tempDir != null) {
                FileUtils.deleteDirsAndFiles(tempDir, true, true);
            }
        }
        return null;
    }

    /**
     * Save one group to xls file.
     * 
     * @param context
     * @param groupOwners
     * @param groupParticipants
     * @param groupWaiting
     * @param columnList
     * @param organisationalEntityList
     * @param orgEntityTitle
     * @param userLocale
     * @param fileNamePrefix
     * @param tempDir
     * @param charset
     * @return
     * @throws IOException
     */
    private File archiveFileSingleGroup(final BGContext context, final List<Member> groupOwners, final List<Member> groupParticipants, final List<Member> groupWaiting,
            final List<String> columnList, final List<OrganisationalEntity> organisationalEntityList, final String orgEntityTitle, final Locale userLocale,
            String fileNamePrefix, final File tempDir, final String charset) throws IOException {
        File outFile = null;
        final StringBuffer stringBuffer = new StringBuffer();

        final Translator trans = getPackageTranslator(userLocale);
        final Translator propertyHandlerTranslator = userService.getUserPropertiesConfig().getTranslator(translator);
        // choice element has only one selected entry
        final List<String> titles = getCourseTitles(context);
        final Iterator<String> titleIterator = titles.iterator();
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, userLocale);
        final String formattedDate = dateFormat.format(new Date());

        // coursename
        stringBuffer.append(EOL);
        stringBuffer.append(trans.translate("archive.coursename"));
        stringBuffer.append(EOL);
        while (titleIterator.hasNext()) {
            stringBuffer.append(titleIterator.next());
        }
        stringBuffer.append(EOL);
        stringBuffer.append(EOL);

        // groupname
        stringBuffer.append(trans.translate("group.name"));
        stringBuffer.append(EOL);
        stringBuffer.append(fileNamePrefix);
        stringBuffer.append(EOL);
        stringBuffer.append(EOL);

        // date
        stringBuffer.append(trans.translate("archive.date"));
        stringBuffer.append(EOL);
        stringBuffer.append(formattedDate);
        stringBuffer.append(EOL);

        // members
        if (groupOwners.size() > 0) {
            appendSection(stringBuffer, trans.translate("archive.header.owners"), groupOwners, columnList, new ArrayList<OrganisationalEntity>(), "",
                    propertyHandlerTranslator, OWNER);
        }
        if (groupParticipants.size() > 0) {
            appendSection(stringBuffer, trans.translate("archive.header.partipiciant"), groupParticipants, columnList, new ArrayList<OrganisationalEntity>(), "",
                    propertyHandlerTranslator, PARTICIPANT);
        }
        if (groupWaiting.size() > 0) {
            appendSection(stringBuffer, trans.translate("archive.header.waitinggroup"), groupWaiting, columnList, new ArrayList<OrganisationalEntity>(), "",
                    propertyHandlerTranslator, WAITING);
        }
        // appendInternInfo(stringBuffer, contextName, userLocale);
        // prefix must be at least 3 chars
        // add two of _ more if this is not the case
        fileNamePrefix = fileNamePrefix + "_";
        fileNamePrefix = fileNamePrefix.length() >= 3 ? fileNamePrefix : fileNamePrefix + "__";
        fileNamePrefix = fileNamePrefix.replaceAll("[*?\"<>/\\\\:]", "_"); // nicht erlaubte Zeichen in Dateinamen
        final String[] search = new String[] { "ß", "ä", "ö", "ü", "Ä", "Ö", "Ü", " " };
        final String[] replace = new String[] { "ss", "ae", "oe", "ue", "Ae", "Oe", "Ue", "_" };
        for (int i = 0; i < search.length; i++) {
            fileNamePrefix = fileNamePrefix.replaceAll(search[i], replace[i]);
        }
        outFile = File.createTempFile(fileNamePrefix, ".xls", tempDir);
        FileUtils.save(outFile, stringBuffer.toString(), charset);
        // FileUtils.saveString(outFile, stringBuffer.toString());
        String outFileName = outFile.getName();
        outFileName = outFileName.substring(0, outFileName.lastIndexOf("_"));
        outFileName += ".xls";
        final File renamedFile = new File(outFile.getParentFile(), outFileName);
        final boolean succesfullyRenamed = outFile.renameTo(renamedFile);
        if (succesfullyRenamed) {
            outFile = renamedFile;
        }

        return outFile;
    }

    /**
     * Filters the input "member" list, and returns only a sublist with the members of the input "group".
     * 
     * @param members
     * @param group
     * @param role
     * @return the list with only the members of the input group.
     */
    private List<Member> getFilteredList(final List<Member> members, final OrganisationalEntity group, final String role) {
        final List<Member> filteredList = new ArrayList<Member>();
        final Iterator<Member> memberListIterator = members.iterator();
        while (memberListIterator.hasNext()) {
            final Member currMember = memberListIterator.next();
            if (currMember.getOrganisationalEntityRoleList().contains(new OrganisationalEntityRole(group.getKey(), role))) {
                filteredList.add(currMember);
            }
        }
        return filteredList;
    }

    /**
     * Wraps the identities from "identityIterator" into Members, and adds the members to the "members" list.
     * 
     * @param group
     * @param memberIterator
     * @param members
     * @param roleName
     */
    private void addMembers(final Long entityKey, final Iterator identityIterator, final List<Member> members, final String roleName) {
        while (identityIterator.hasNext()) {
            final Object[] element = (Object[]) identityIterator.next();
            final Identity identity = (Identity) element[0];
            final OrganisationalEntityRole role = new OrganisationalEntityRole(entityKey, roleName);
            final Member member = new Member(identity, new ArrayList());
            member.getOrganisationalEntityRoleList().add(role);
            if (!members.contains(member)) {
                members.add(member);
            } else {
                final Iterator<Member> memberSetIterator = members.iterator();
                while (memberSetIterator.hasNext()) {
                    final Member currMember = memberSetIterator.next();
                    if (currMember.equals(member)) {
                        currMember.getOrganisationalEntityRoleList().add(role);
                    }
                }
            }
        }
    }

    /**
     * Appends course names and archive date.
     * 
     * @param buf
     * @param context
     * @param userLocale
     */
    private void appendContextInfo(final StringBuffer buf, final BGContext context, final Locale userLocale) {
        final List<String> titles = getCourseTitles(context);
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, userLocale);
        final String formattedDate = dateFormat.format(new Date());
        final Translator trans = getPackageTranslator(userLocale);
        buf.append(EOL);
        buf.append(trans.translate("archive.coursename"));
        buf.append(DELIMITER);
        buf.append(trans.translate("archive.date"));
        buf.append(EOL);

        final Iterator<String> titleIterator = titles.iterator();
        int i = 0;
        while (titleIterator.hasNext()) {
            buf.append(titleIterator.next());
            buf.append(DELIMITER);
            if (i < 1) {
                buf.append(formattedDate);
            }
            buf.append(EOL);
            i++;
        }
    }

    /**
     * Appends header labels to the input stringBuffer as follows: first the columnList items in that order and next the group list items.
     * 
     * @param buf
     * @param title
     * @param columList
     * @param groupList
     */
    private void appendHeader(final StringBuffer buf, final String title, final List<String> columnList, final List<OrganisationalEntity> organisationalEntityList,
            final String orgEntityTitle, final Translator trans) {
        buf.append(EOL);
        buf.append(title);
        final int colSize = columnList.size();
        for (int i = 0; i < colSize; i++) {
            buf.append(DELIMITER);
        }
        buf.append(orgEntityTitle);
        buf.append(EOL);
        final Iterator<String> columnIterator = columnList.iterator();
        while (columnIterator.hasNext()) {
            final String columnKey = columnIterator.next();
            buf.append(trans.translate(columnKey));
            buf.append(DELIMITER);
        }

        final Iterator<OrganisationalEntity> groupIterator = organisationalEntityList.iterator();
        while (groupIterator.hasNext()) {
            final OrganisationalEntity group = groupIterator.next();
            buf.append(group.getName());
            buf.append(DELIMITER);
        }
        buf.append(EOL);
    }

    /**
     * Appends member info to the input stringBuffer.
     * 
     * @param buf
     * @param member
     * @param columnList
     * @param groupList
     * @param role
     */
    private void appendMember(final StringBuffer buf, final Member member, final List<String> columnList, final List<OrganisationalEntity> groupList, final String role) {
        if (columnList.contains("username")) {
            buf.append(member.getIdentity().getName());
            buf.append(DELIMITER);
        }

        // get selected user properties and append
        final User user = member.getIdentity().getUser();
        for (final String column : columnList) {
            final String key = column.substring(column.lastIndexOf(".") + 1);
            if (!key.contains("username")) {
                final String value = userService.getUserProperty(user, key); // use default locale
                buf.append((value == null ? "" : value));
                buf.append(DELIMITER);
            }
        }

        final List<OrganisationalEntityRole> groupRoleList = member.getOrganisationalEntityRoleList();
        final Iterator<OrganisationalEntity> groupIterator = groupList.iterator();
        while (groupIterator.hasNext()) {
            final OrganisationalEntity group = groupIterator.next();
            final OrganisationalEntityRole groupRole = new OrganisationalEntityRole(group.getKey(), role);
            if (groupRoleList.contains(groupRole)) {
                buf.append("X");
            }
            buf.append(DELIMITER);
        }
        buf.append(EOL);
    }

    /**
     * Appends the section header and next the members.
     * 
     * @param stringBuffer
     * @param sectionTitle
     * @param members
     * @param columnList
     * @param groupList
     * @param trans
     * @param role
     */
    private void appendSection(final StringBuffer stringBuffer, final String sectionTitle, final List<Member> members, final List<String> columnList,
            final List<OrganisationalEntity> organisationalEntityList, final String orgEntityTitle, final Translator trans, final String role) {

        appendHeader(stringBuffer, sectionTitle, columnList, organisationalEntityList, orgEntityTitle, trans);
        final Iterator<Member> memberIterator = members.iterator();
        while (memberIterator.hasNext()) {
            final Member member = memberIterator.next();
            appendMember(stringBuffer, member, columnList, organisationalEntityList, role);
        }
    }

    /**
     * Converts a list of items of a certain type (BusinessGroup,BGArea) in a list of OrganisationalEntitys.
     * 
     * @param itemList
     * @return
     */
    private List<OrganisationalEntity> getOrganisationalEntityList(final List itemList) {
        final List<OrganisationalEntity> entryList = new ArrayList<OrganisationalEntity>();
        final Iterator itemIterator = itemList.iterator();
        while (itemIterator.hasNext()) {
            final Object item = itemIterator.next();
            if (item instanceof BusinessGroup) {
                final BusinessGroup group = (BusinessGroup) item;
                entryList.add(new OrganisationalEntity(group.getKey(), group.getName()));
            } else if (item instanceof BGArea) {
                final BGArea area = (BGArea) item;
                entryList.add(new OrganisationalEntity(area.getKey(), area.getName()));
            }
        }
        return entryList;
    }

    /**
     * Description:<br>
     * An organisational entity is a Group or an Area. Encapsulates the entityKey and the role in the group.
     * <P>
     * Initial Date: 26.07.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class OrganisationalEntityRole {
        private Long entityKey;
        private String roleInGroup;

        public OrganisationalEntityRole(final Long entityKey, final String roleInGroup) {
            super();
            this.entityKey = entityKey;
            this.roleInGroup = roleInGroup;
        }

        public String getRoleInGroup() {
            return roleInGroup;
        }

        public void setRoleInGroup(final String roleInGroup) {
            this.roleInGroup = roleInGroup;
        }

        public Long getEntityKey() {
            return entityKey;
        }

        public void setEntityKey(final Long groupKey) {
            this.entityKey = groupKey;
        }

        @Override
        public boolean equals(final Object obj) {
            final OrganisationalEntityRole that = (OrganisationalEntityRole) obj;
            return this.entityKey.equals(that.getEntityKey()) && this.getRoleInGroup().equals(that.getRoleInGroup());
        }

        @Override
        public int hashCode() {
            return getEntityKey().intValue() + getRoleInGroup().hashCode();
        }

    }

    /**
     * Description:<br>
     * Encapsulates an <code>Identity</code> and a list of <code>OrganisationalEntityRole</code> of the <code>Identity</code>.
     * <P>
     * Initial Date: 26.07.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class Member {
        private Identity identity;
        private List<OrganisationalEntityRole> organisationalEntityRoleList;

        public Member(final Identity identity, final List<OrganisationalEntityRole> groupRoleList) {
            super();
            this.identity = identity;
            this.organisationalEntityRoleList = groupRoleList;
        }

        public List<OrganisationalEntityRole> getOrganisationalEntityRoleList() {
            return organisationalEntityRoleList;
        }

        public void setOrganisationalEntityRoleList(final List<OrganisationalEntityRole> groupRoleList) {
            this.organisationalEntityRoleList = groupRoleList;
        }

        public Identity getIdentity() {
            return identity;
        }

        public void setIdentity(final Identity identity) {
            this.identity = identity;
        }

        /**
         * Compares the identity of the members.
         * 
         */
        @Override
        public boolean equals(final Object obj) {
            try {
                final Member groupMember = (Member) obj;
                return this.identity.equals(groupMember.identity);
            } catch (final Exception ex) {
                // nothing to do
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.identity.hashCode();
        }
    }

    private class OrganisationalEntity {
        private Long key;
        private String name;

        public OrganisationalEntity(final Long key, final String name) {
            super();
            this.key = key;
            this.name = name;
        }

        public Long getKey() {
            return key;
        }

        public void setKey(final Long key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

}
