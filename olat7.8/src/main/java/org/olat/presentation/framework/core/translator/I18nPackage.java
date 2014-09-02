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
package org.olat.presentation.framework.core.translator;

/**
 * 
 * Creating the enums automatically with a shell command line:
 * 
 * cd to /src/main/java and run and run ./i18nPackage.sh | sort
 * 
 * <P>
 * Initial Date: 04.05.2011 <br>
 * 
 * @author guido
 */
public enum I18nPackage {

    // TODO path are used with slases, up to now the dots where coming from classes

    ADMIN_CACHE_("/org/olat/presentation/admin/cache/"),

    ADMIN_CLUSTER_("/org/olat/presentation/admin/cluster/"),

    ADMIN_CONFIGURATION_("/org/olat/presentation/admin/configuration/"),

    ADMIN_EXTENSIONS_("/org/olat/presentation/admin/extensions/"),

    ADMIN_INSTANTMESSAGING_("/org/olat/presentation/admin/instantMessaging/"),

    ADMIN_JMX_("/org/olat/presentation/admin/jmx/"),

    ADMIN_LAYOUT_("/org/olat/presentation/admin/layout/"),

    ADMIN_NOTIFICATIONS_("/org/olat/presentation/admin/notifications/"),

    ADMIN_("/org/olat/presentation/admin/"),

    ADMIN_POLICY_("/org/olat/presentation/admin/policy/"),

    ADMIN_PROPERTIES_("/org/olat/presentation/admin/properties/"), ADMIN_QUOTA_("/org/olat/presentation/admin/quota/"),

    ADMIN_REGISTRATION_("/org/olat/presentation/admin/registration/"),

    ADMIN_REST_("/org/olat/presentation/admin/rest/"), ADMIN_SEARCH_("/org/olat/presentation/admin/search/"),

    ADMIN_STATISTICS_("/org/olat/presentation/admin/statistics/"),

    ADMIN_SYSINFO_LOGGING_("/org/olat/presentation/admin/sysinfo/logging/"),

    ADMIN_SYSINFO_("/org/olat/presentation/admin/sysinfo/"),

    ADMIN_VERSION_("/org/olat/presentation/admin/version/"),

    BOOKMARK_("/org/olat/presentation/bookmark/"),

    CALENDAR_("/org/olat/presentation/calendar/"),

    CAMPUSMGNT_CONTROLLER_("/org/olat/presentation/campusmgnt/controller/"),

    CAMPUSMGNT_("/org/olat/presentation/campusmgnt/"),

    CATALOG_("/org/olat/presentation/catalog/"),

    CHECKLIST_("/org/olat/presentation/checklist/"),

    COLLABORATION_("/org/olat/presentation/collaboration/"),

    COMMENTANDRATE_("/org/olat/presentation/commentandrate/"),

    COMMONS_FILECHOOSER_("/org/olat/presentation/commons/filechooser/"),

    CONTACTFORM_("/org/olat/presentation/contactform/"),

    COURSE_ARCHIVER_("/org/olat/presentation/course/archiver/"),

    COURSE_ASSESSMENT_("/org/olat/presentation/course/assessment/"),

    COURSE_CONDITION_("/org/olat/presentation/course/condition/"),

    COURSE_CONFIG_("/org/olat/presentation/course/config/"),

    COURSE_EDITOR_("/org/olat/presentation/course/editor/"),

    COURSE_NODES_BASICLTI_("/org/olat/presentation/course/nodes/basiclti/"),

    COURSE_NODES_BC_("/org/olat/presentation/course/nodes/bc/"),

    COURSE_NODES_CAL_("/org/olat/presentation/course/nodes/cal/"),

    COURSE_NODES_CO_("/org/olat/presentation/course/nodes/co/"),

    COURSE_NODES_CP_("/org/olat/presentation/course/nodes/cp/"),

    COURSE_NODES_DIALOG_("/org/olat/presentation/course/nodes/dialog/"),

    COURSE_NODES_EN_("/org/olat/presentation/course/nodes/en/"),

    COURSE_NODES_FEED_BLOG_("/org/olat/presentation/course/nodes/feed/blog/"),

    COURSE_NODES_FEED_("/org/olat/presentation/course/nodes/feed/"),

    COURSE_NODES_FEED_PODCAST_("/org/olat/presentation/course/nodes/feed/podcast/"),

    COURSE_NODES_FO_("/org/olat/presentation/course/nodes/fo/"),

    COURSE_NODES_INFO_("/org/olat/presentation/course/nodes/info/"),

    COURSE_NODES_IQ_("/org/olat/presentation/course/nodes/iq/"),

    COURSE_NODES_MS_("/org/olat/presentation/course/nodes/ms/"),

    COURSE_NODES_("/org/olat/presentation/course/nodes/"),

    COURSE_NODES_PORTFOLIO_("/org/olat/presentation/course/nodes/portfolio/"),

    COURSE_NODES_PROJECTBROKER_("/org/olat/presentation/course/nodes/projectbroker/"),

    COURSE_NODES_SCORM_("/org/olat/presentation/course/nodes/scorm/"),

    COURSE_NODES_SP_("/org/olat/presentation/course/nodes/sp/"),

    COURSE_NODES_ST_("/org/olat/presentation/course/nodes/st/"),

    COURSE_NODES_TA_("/org/olat/presentation/course/nodes/ta/"),

    COURSE_NODES_TU_("/org/olat/presentation/course/nodes/tu/"),

    COURSE_NODES_WIKI_("/org/olat/presentation/course/nodes/wiki/"),

    COURSE_("/org/olat/presentation/course/"),

    COURSE_REPOSITORY_("/org/olat/presentation/course/repository/"),

    COURSE_RUN_("/org/olat/presentation/course/run/"),

    COURSE_RUN_PREVIEW_("/org/olat/presentation/course/run/preview/"),

    COURSE_STATISTIC_DAILY_("/org/olat/presentation/course/statistic/daily/"),

    COURSE_STATISTIC_DAYOFWEEK_("/org/olat/presentation/course/statistic/dayofweek/"),

    COURSE_STATISTIC_HOMEORG_("/org/olat/presentation/course/statistic/homeorg/"),

    COURSE_STATISTIC_HOUROFDAY_("/org/olat/presentation/course/statistic/hourofday/"),

    COURSE_STATISTIC_("/org/olat/presentation/course/statistic/"),

    COURSE_STATISTIC_ORGTYPE_("/org/olat/presentation/course/statistic/orgtype/"),

    COURSE_STATISTIC_STUDYBRANCH3_("/org/olat/presentation/course/statistic/studybranch3/"),

    COURSE_STATISTIC_STUDYLEVEL_("/org/olat/presentation/course/statistic/studylevel/"),

    COURSE_STATISTIC_WEEKLY_("/org/olat/presentation/course/statistic/weekly/"),

    COURSE_WIZARD_CREATE_("/org/olat/presentation/course/wizard/create/"),

    DIALOGELEMENTS_("/org/olat/presentation/dialogelements/"),

    EXAMPLES_GUIDEMO_CSSJS_("/org/olat/presentation/examples/guidemo/cssjs/"),

    EXAMPLES_GUIDEMO_DEMOEXTENSION_CONTROLLER_("/org/olat/presentation/examples/guidemo/demoextension/controller/"),

    EXAMPLES_GUIDEMO_DEMOEXTENSION_("/org/olat/presentation/examples/guidemo/demoextension/"),

    EXAMPLES_GUIDEMO_DEMO_POLL_("/org/olat/presentation/examples/guidemo/demo/poll/"),

    EXAMPLES_GUIDEMO_ERROR_("/org/olat/presentation/examples/guidemo/error/"),

    EXAMPLES_GUIDEMO_GUISOA_("/org/olat/presentation/examples/guidemo/guisoa/"),

    EXAMPLES_GUIDEMO_("/org/olat/presentation/examples/guidemo/"),

    EXAMPLES_GUIDEMO_WEBLOG_("/org/olat/presentation/examples/guidemo/weblog/"),

    EXAMPLES_GUIDEMO_WIZARD_("/org/olat/presentation/examples/guidemo/wizard/"),

    EXAMPLES_HELLOWORLDPACKAGE_("/org/olat/presentation/examples/helloworldpackage/"),

    EXAMPLES_("/org/olat/presentation/examples/"),

    FILEBROWSER_META_("/org/olat/presentation/filebrowser/meta/"),

    FILEBROWSER_("/org/olat/presentation/filebrowser/"),

    FILEBROWSER_VERSION_("/org/olat/presentation/filebrowser/version/"),

    FORUM_("/org/olat/presentation/forum/"),

    FRAMEWORK_COMMON_CONTEXTHELP_("/org/olat/presentation/framework/common/contextHelp/"),

    FRAMEWORK_COMMON_FILECHOOSER_("/org/olat/presentation/framework/common/filechooser/"),

    FRAMEWORK_COMMON_HTMLEDITOR_("/org/olat/presentation/framework/common/htmleditor/"),

    FRAMEWORK_COMMON_HTMLPAGEVIEW_("/org/olat/presentation/framework/common/htmlpageview/"),

    FRAMEWORK_COMMON_LINKCHOOSER_("/org/olat/presentation/framework/common/linkchooser/"),

    FRAMEWORK_COMMON_NAVIGATION_("/org/olat/presentation/framework/common/navigation/"),

    FRAMEWORK_COMMON_PLAINTEXTEDITOR_("/org/olat/presentation/framework/common/plaintexteditor/"),

    FRAMEWORK_CORE_CHIEFCONTROLLERS_("/org/olat/presentation/framework/core/chiefcontrollers/"),

    FRAMEWORK_CORE_COMPONENTS_FORM_FLEXIBLE_IMPL_ELEMENTS_("/org/olat/presentation/framework/core/components/form/flexible/impl/elements/"),

    ELEMENTS_RICHTEXT_("/org/olat/presentation/framework/core/components/form/flexible/impl/elements/richText/"),

    ELEMENTS_RICHTEXT_PLUGINS_OLATMATHEDITOR("/org/olat/presentation/framework/core/components/form/flexible/impl/elements/richText/plugins/olatmatheditor/"),

    ELEMENTS_RICHTEXT_PLUGINS_OLATMOVIEVIEWER("/org/olat/presentation/framework/core/components/form/flexible/impl/elements/richText/plugins/olatmovieviewer/"),

    ELEMENTS_RICHTEXT_PLUGINS_OLATSMILEYS("/org/olat/presentation/framework/core/components/form/flexible/impl/elements/richText/plugins/olatsmileys/"),

    FRAMEWORK_CORE_COMPONENTS_FORM_("/org/olat/presentation/framework/core/components/form/"),

    FRAMEWORK_CORE_COMPONENTS_FORM__STATIC_JS_JSCALENDAR_("/org/olat/presentation/framework/core/components/form/_static/js/jscalendar/"),

    FRAMEWORK_CORE_COMPONENTS_TABLE_("/org/olat/presentation/framework/core/components/table/"),

    FRAMEWORK_CORE_COMPONENTS_TEXTBOXLIST_("/org/olat/presentation/framework/core/components/textboxlist/"),

    FRAMEWORK_CORE_COMPONENTS_TREE_("/org/olat/presentation/framework/core/components/tree/"),

    FRAMEWORK_CORE_CONTROL_FLOATINGRESIZABLEDIALOG_("/org/olat/presentation/framework/core/control/floatingresizabledialog/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_AJAX_AUTOCOMPLETION_("/org/olat/presentation/framework/core/control/generic/ajax/autocompletion/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_AJAX_TREE_("/org/olat/presentation/framework/core/control/generic/ajax/tree/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_CHOICE_("/org/olat/presentation/framework/core/control/generic/choice/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_CLONE_("/org/olat/presentation/framework/core/control/generic/clone/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_DIALOG_("/org/olat/presentation/framework/core/control/generic/dialog/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_DOCKING_("/org/olat/presentation/framework/core/control/generic/docking/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_FOLDER_("/org/olat/presentation/framework/core/control/generic/folder/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_IFRAME_("/org/olat/presentation/framework/core/control/generic/iframe/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_LOCK_("/org/olat/presentation/framework/core/control/generic/lock/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_MODAL_("/org/olat/presentation/framework/core/control/generic/modal/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_PORTAL_("/org/olat/presentation/framework/core/control/generic/portal/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_SPACESAVER_("/org/olat/presentation/framework/core/control/generic/spacesaver/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_TITLE_("/org/olat/presentation/framework/core/control/generic/title/"),

    FRAMEWORK_CORE_CONTROL_GENERIC_WIZARD_("/org/olat/presentation/framework/core/control/generic/wizard/"),

    FRAMEWORK_CORE_CONTROL_("/org/olat/presentation/framework/core/control/"),

    FRAMEWORK_CORE_CONTROL_RECORDER_("/org/olat/presentation/framework/core/control/recorder/"),

    FRAMEWORK_CORE_DEV_CONTROLLER_("/org/olat/presentation/framework/core/dev/controller/"),

    FRAMEWORK_CORE_EXCEPTION_("/org/olat/presentation/framework/core/exception/"),

    FRAMEWORK_CORE_FORMELEMENTS_("/org/olat/presentation/framework/core/formelements/"),

    FRAMEWORK_CORE_MEDIA_FILERESOURCE_("/org/olat/presentation/framework/core/media/fileresource/"),

    FRAMEWORK_CORE_("/org/olat/presentation/framework/core/"),

    FRAMEWORK_CORE_UTIL_BANDWIDTH_("/org/olat/presentation/framework/core/util/bandwidth/"),

    FRAMEWORK_LAYOUT_FULLWEBAPP_("/org/olat/presentation/framework/layout/fullWebApp/"),

    FRAMEWORK_LAYOUT_("/org/olat/presentation/framework/layout/"),

    GLOSSARY_("/org/olat/presentation/glossary/"),

    GROUP_AREA_("/org/olat/presentation/group/area/"),

    GROUP_BUDDYGROUP_("/org/olat/presentation/group/buddygroup/"),

    GROUP_CONTEXT_("/org/olat/presentation/group/context/"),

    GROUP_DELETE_("/org/olat/presentation/group/delete/"),

    GROUP_EDIT_("/org/olat/presentation/group/edit/"),

    GROUP_LEARNINGGROUP_("/org/olat/presentation/group/learninggroup/"),

    GROUP_LEARN_("/org/olat/presentation/group/learn/"),

    GROUP_MAIN_("/org/olat/presentation/group/main/"),

    GROUP_MANAGEMENT_("/org/olat/presentation/group/management/"),

    GROUP_("/org/olat/presentation/group/"),

    GROUP_RIGHTGROUP_("/org/olat/presentation/group/rightgroup/"),

    GROUP_RUN_("/org/olat/presentation/group/run/"),

    GROUP_SECURITYGROUP_("/org/olat/presentation/group/securitygroup/"),

    GROUP_SECURITYGROUP_WIZARD_("/org/olat/presentation/group/securitygroup/wizard/"),

    GROUP_WIZARD_("/org/olat/presentation/group/wizard/"),

    HOME_("/org/olat/presentation/home/"),

    I18N_DEVTOOLS_("/org/olat/presentation/i18n/devtools/"),

    I18N_("/org/olat/presentation/i18n/"),

    IMS_CP_("/org/olat/presentation/ims/cp/"),

    IMS_QTI_DISPLAY_("/org/olat/presentation/ims/qti/display/"),

    IMS_QTI_EDITOR_("/org/olat/presentation/ims/qti/editor/"),

    IMS_QTI_EXPORTER_("/org/olat/presentation/ims/qti/exporter/"),

    IMS_QTI_("/org/olat/presentation/ims/qti/"),

    INFOMESSAGE_("/org/olat/presentation/infomessage/"),

    INSTANTMESSAGING_GROUPCHAT_("/org/olat/presentation/instantmessaging/groupchat/"),

    INSTANTMESSAGING_("/org/olat/presentation/instantmessaging/"),

    INSTANTMESSAGING_ROSTERANDCHAT_("/org/olat/presentation/instantmessaging/rosterandchat/"),

    MARKING_("/org/olat/presentation/marking/"),

    NOTE_("/org/olat/presentation/note/"),

    NOTIFICATIONS_BC_("/org/olat/presentation/notifications/bc/"),

    NOTIFICATIONS_("/org/olat/presentation/notifications/"),

    FALLBACK("/org/olat/presentation/"),

    PORTAL_CALENDAR_("/org/olat/presentation/portal/calendar/"),

    PORTAL_DIDYOUKNOW_("/org/olat/presentation/portal/didYouKnow/"),

    PORTAL_IFRAME_("/org/olat/presentation/portal/iframe/"),

    PORTAL_INFOMESSAGE_("/org/olat/presentation/portal/infomessage/"),

    PORTAL_INFOMSG_("/org/olat/presentation/portal/infomsg/"),

    PORTAL_MACARTNEY_("/org/olat/presentation/portal/macartney/"),

    PORTAL_QUICKSTART_("/org/olat/presentation/portal/quickstart/"),

    PORTAL_REPOSITORY_("/org/olat/presentation/portal/repository/"),

    PORTAL_SHIBLOGIN_("/org/olat/presentation/portal/shiblogin/"),

    PORTAL_ZSUZ_("/org/olat/presentation/portal/zsuz/"),

    PORTFOLIO_ARTEFACTS_COLLECT_("/org/olat/presentation/portfolio/artefacts/collect/"),

    PORTFOLIO_ARTEFACTS_VIEW_DETAILS_("/org/olat/presentation/portfolio/artefacts/view/details/"),

    PORTFOLIO_ARTEFACTS_VIEW_("/org/olat/presentation/portfolio/artefacts/view/"),

    PORTFOLIO_FILTER_("/org/olat/presentation/portfolio/filter/"),

    PORTFOLIO_("/org/olat/presentation/portfolio/"),

    PORTFOLIO_STRUCTEL_EDIT_("/org/olat/presentation/portfolio/structel/edit/"),

    PORTFOLIO_STRUCTEL_("/org/olat/presentation/portfolio/structel/"),

    PORTFOLIO_STRUCTEL_VIEW_("/org/olat/presentation/portfolio/structel/view/"),

    REGISTRATION_("/org/olat/presentation/registration/"),

    REPOSITORY_DELETE_("/org/olat/presentation/repository/delete/"),

    REPOSITORY_("/org/olat/presentation/repository/"),

    SCORM_ARCHIVER_("/org/olat/presentation/scorm/archiver/"),

    SCORM_ASSESSMENT_("/org/olat/presentation/scorm/assessment/"),

    SCORM_("/org/olat/presentation/scorm/"),

    SEARCH_("/org/olat/presentation/search/"),

    SECURITY_AUTHENTICATION_LDAP_("/org/olat/presentation/security/authentication/ldap/"),

    SECURITY_AUTHENTICATION_("/org/olat/presentation/security/authentication/"),

    SECURITY_AUTHENTICATION_SHIBBOLETH_("/org/olat/presentation/security/authentication/shibboleth/"),

    SHAREDFOLDER_("/org/olat/presentation/sharedfolder/"),

    USER_ADMINISTRATION_BULKCHANGE_("/org/olat/presentation/user/administration/bulkchange/"),

    USER_ADMINISTRATION_DELETE_("/org/olat/presentation/user/administration/delete/"),

    USER_ADMINISTRATION_GROUPS_("/org/olat/presentation/user/administration/groups/"),

    USER_ADMINISTRATION_IMPORTWIZZARD_("/org/olat/presentation/user/administration/importwizzard/"),

    USER_ADMINISTRATION_("/org/olat/presentation/user/administration/"),

    USER_("/org/olat/presentation/user/"),

    WEBFEED_BLOG_("/org/olat/presentation/webfeed/blog/"),

    WEBFEED_("/org/olat/presentation/webfeed/"),

    WEBFEED_PODCAST_("/org/olat/presentation/webfeed/podcast/"),

    WEBFEED_PORTFOLIO_("/org/olat/presentation/webfeed/portfolio/"),

    WIKI_("/org/olat/presentation/wiki/"),

    WIKI_PORTFOLIO_("/org/olat/presentation/wiki/portfolio/");

    private String baseLocation;

    /**
     * enum constructor
     */
    private I18nPackage(String baseLocation) {
        this.baseLocation = baseLocation;
    }

    /**
     * access the enum value
     * 
     * @return
     */
    public String getBaseLocation() {
        return this.baseLocation;
    }

}
