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
package org.olat.system.commons.configuration;

/**
 * run the propertiesToEnum.sh in the root dir to generate an up to date list of available properties
 * 
 * e.g. propertiesToEnum.sh | sort and paste it back into here
 * 
 * <P>
 * Initial Date: 25.05.2011 <br>
 * 
 * @author guido
 */
public enum PropertyLocator {

    ALLOW_LOADTEST_MODE("allow.loadtest.mode"),

    APPLICATION_NAME("application.name"),

    ARCHIVE_DIR("archive.dir"),

    ARCHIVE_DIR_COMMENT("archive.dir.comment"),

    ARCHIVE_DIR_VALUES("archive.dir.values"),

    ASSESSMENTPLUGIN_ACTIVATE("assessmentplugin.activate"),

    AUTO_UPGRADE_DATABASE("auto.upgrade.database"),

    BUILD_VERSION("build.version"),

    CLUSTER_MODE("cluster.mode"),

    CLUSTER_SINGLETON_SERVICES("cluster.singleton.services"),

    CODEPOINT_JMS_BROKER_URL("codepoint.jms.broker.url"),

    CODEPOINT_SERVER_ENABLED("codepoint_server.enabled"),

    COURSE_CACHE_ELEMENTS("course.cache.elements"),

    COURSE_DISPLAY_PARTICIPANTS_COUNT("course.display.participants.count"),

    COURSE_NODE_BASICLTI_ENABLED("course.node.basiclti.enabled"),

    COURSE_NODE_CHECKLIST_ENABLED("course.node.checklist.enabled"),

    COURSE_NODE_DATEENROLLMENT_ENABLED("course.node.dateenrollment.enabled"),

    COURSE_NODE_INFOMESSAGE_ENABLED("course.node.infomessage.enabled"),

    COURSE_NODE_LINKLIST_ENABLED("course.node.linklist.enabled"),

    COURSE_NODE_PORTFOLIO_ENABLED("course.node.portfolio.enabled"),

    DB_HIBERNATE_C3P0_MAXSIZE("db.hibernate.c3p0.maxsize"),

    DB_HIBERNATE_C3P0_MINSIZE("db.hibernate.c3p0.minsize"),

    DB_HIBERNATE_C3P0_UNRETURNEDCONNECTIONTIMEOUT("db.hibernate.c3p0.unreturnedConnectionTimeout"),

    DB_HIBERNATE_DDL_AUTO("db.hibernate.ddl.auto"),

    DB_HOST("db.host"),

    DB_HOST_PORT("db.host.port"),

    DB_HOST_PORT_VALUES("db.host.port.values"),

    DB_NAME("db.name"),

    DB_PASS("db.pass"),

    DB_SHOW_SQL("db.show_sql"),

    DB_URL_OPTIONS_MYSQL("db.url.options.mysql"),

    DB_USER("db.user"),

    DB_VENDOR("db.vendor"),

    DB_VENDOR_VALUES_COMMENT("db.vendor.values.comment"),

    DB_VENDOR_VALUES("db.vendor.values"),

    DEFAULTCHARSET("defaultcharset"),

    DEFAULTLANG("defaultlang"),

    ENABLEDLANGUAGES("enabledLanguages"),

    FALLBACKLANG_COMMENT("fallbacklang.comment"),

    FALLBACKLANG("fallbacklang"),

    FALLBACKLANG_VALUES("fallbacklang.values"),

    FOLDER_MAXULMB_COMMENT("folder.maxulmb.comment"),

    FOLDER_MAXULMB("folder.maxulmb"),

    FOLDER_QUOTAMB("folder.quotamb"),

    FOLDER_ROOT_COMMENT("folder.root.comment"),

    FOLDER_ROOT("folder.root"),

    FOLDER_ROOT_VALUES("folder.root.values"),

    GENERATE_INDEX_AT_STARTUP("generate.index.at.startup"),

    GUIDEMO_ENABLED("guidemo.enabled"),

    HIBERNATE_CACHING_CLUSTER_CLASS("hibernate.caching.cluster.class"),

    HIBERNATE_CACHING_SINGLEVM_CLASS("hibernate.caching.singlevm.class"),

    HIBERNATE_USE_SECOND_LEVEL_CACHE("hibernate.use.second.level.cache"),

    I18N_APPLICATION_OPT_SRC_DIR("i18n.application.opt.src.dir"),

    I18N_APPLICATION_SRC_DIR("i18n.application.src.dir"),

    INSTANCE_ID("instance.id"),

    INSTANTMESSAGING_ADMIN_PASSWORD("instantMessaging.admin.password"),

    INSTANTMESSAGING_ADMIN_USERNAME("instantMessaging.admin.username"),

    INSTANTMESSAGING_ENABLE("instantMessaging.enable"),

    INSTANTMESSAGING_GENERATETESTUSERS("instantMessaging.generateTestUsers"),

    INSTANTMESSAGING_MULTIPLEINSTANCES("instantMessaging.multipleInstances"),

    INSTANTMESSAGING_REPLACESTRINGFOREMAILAT("instantMessaging.replaceStringForEmailAt"),

    INSTANTMESSAGING_SERVER_NAME("instantMessaging.server.name"),

    INSTANTMESSAGING_SYNC_LEARNING_GROUPS("instantMessaging.sync.learning.groups"),

    INSTANTMESSAGING_SYNC_PERSONAL_GROUPS("instantMessaging.sync.personal.groups"),

    IS_TRANSLATION_SERVER("is.translation.server"),

    JMS_BROKER_URL("jms.broker.url"),

    JMX_RMI_PORT("jmx.rmi.port"),

    KEEPUSEREMAILAFTERDELETION("keepUserEmailAfterDeletion"),

    KEEPUSERLOGINAFTERDELETION("keepUserLoginAfterDeletion"),

    LANGUAGE_ENABLE("language.enable"),

    LANGUAGE_PARAM("language.param"),

    LAYOUT_THEME("layout.theme"),

    LDAP_ACTIVEDIRECTORY("ldap.activeDirectory"),

    LDAP_ATTRIBUTENAME_EMAIL("ldap.attributename.email"),

    LDAP_ATTRIBUTENAME_FIRSTNAME("ldap.attributename.firstName"),

    LDAP_ATTRIBUTENAME_LASTNAME("ldap.attributename.lastName"),

    LDAP_ATTRIBUTENAME_USERIDENTIFYER("ldap.attributename.useridentifyer"),

    LDAP_CACHELDAPPWDASOLATPWDONLOGIN("ldap.cacheLDAPPwdAsOLATPwdOnLogin"),

    LDAP_CONVERTEXISTINGLOCALUSERSTOLDAPUSERS("ldap.convertExistingLocalUsersToLDAPUsers"),

    LDAP_DATEFORMAT("ldap.dateFormat"),

    LDAP_DATEFORMAT_VALUES("ldap.dateFormat.values"),

    LDAP_DEFAULT("ldap.default"),

    LDAP_DELETEREMOVEDLDAPUSERSONSYNC("ldap.deleteRemovedLDAPUsersOnSync"),

    LDAP_DELETEREMOVEDLDAPUSERSPERCENTAGE("ldap.deleteRemovedLDAPUsersPercentage"),

    LDAP_ENABLE("ldap.enable"),

    LDAP_LDAPBASES("ldap.ldapBases"),

    LDAP_LDAPSYNCCRONSYNCEXPRESSION("ldap.ldapSyncCronSyncExpression"),

    LDAP_LDAPSYNCCRONSYNC("ldap.ldapSyncCronSync"),

    LDAP_LDAPSYNCONSTARTUP("ldap.ldapSyncOnStartup"),

    LDAP_LDAPSYSTEMDN("ldap.ldapSystemDN"),

    LDAP_LDAPSYSTEMPW("ldap.ldapSystemPW"),

    LDAP_LDAPURL("ldap.ldapUrl"),

    LDAP_LDAPURL_VALUES("ldap.ldapUrl.values"),

    LDAP_LDAPUSERCREATEDTIMESTAMPATTRIBUTE("ldap.ldapUserCreatedTimestampAttribute"),

    LDAP_LDAPUSERLASTMODIFIEDTIMESTAMPATTRIBUTE("ldap.ldapUserLastModifiedTimestampAttribute"),

    LDAP_LDAPUSEROBJECTCLASS("ldap.ldapUserObjectClass"),

    LDAP_LDAPUSERPASSORDATTRIBUTE("ldap.ldapUserPassordAttribute"),

    LDAP_PROPAGATEPASSWORDCHANGEDONLDAPSERVER("ldap.propagatePasswordChangedOnLdapServer"),

    LDAP_SSLENABLED("ldap.sslEnabled"),

    LDAP_TRUSTSTORELOCATION("ldap.trustStoreLocation"),

    LDAP_TRUSTSTOREPWD("ldap.trustStorePwd"),

    LDAP_TRUSTSTORETYPE("ldap.trustStoreType"),

    LOCALIZATION_CACHE("localization.cache"),

    LOG_ANONYMOUS("log.anonymous"),

    LOG_DIR_COMMENT("log.dir.comment"),

    LOG_DIR("log.dir"),

    LOG_DIR_VALUES("log.dir.values"),

    LOGIN_ENABLEGUESTLOGINLINKS("login.enableGuestLoginLinks"),

    LOGIN_INVITATIONLOGIN("login.invitationLogin"),

    LOGIN_USING_USERNAME_OR_EMAIL_ENABLED("login.using.username.or.email.enabled"),

    MAIL_ATTACHMENT_MAXSIZE("mail.attachment.maxsize"),

    MAXNUMBER_VERSIONS("maxnumber.versions"),

    NODE_ID("node.id"),

    OLAT_DEBUG_COMMENT("olat.debug.comment"),

    OLAT_DEBUG("olat.debug"),

    OLATPROVIDER_DEFAULT("olatprovider.default"),

    OLATPROVIDER_ENABLE("olatprovider.enable"),

    ONYX_BASE_DIR("onyx.base.dir"),

    ONYX_KEYSTORE_FILE("onyx.keystore.file"),

    ONYX_KEYSTORE_PASS("onyx.keystore.pass"),

    ONYX_KEYSTORE_TYPE("onyx.keystore.type"),

    ONYX_PLUGIN_CONFIGNAME("onyx.plugin.configname"),

    ONYX_PLUGIN_WSLOCATION("onyx.plugin.wslocation"),

    ONYX_REPORTER_WEBSERVICE("onyx.reporter.webservice"),

    ONYX_TRUSTSTORE_FILE("onyx.truststore.file"),

    ONYX_TRUSTSTORE_PASS("onyx.truststore.pass"),

    ONYX_TRUSTSTORE_TYPE("onyx.truststore.type"),

    ONYX_UPDATE_RESULTS_JOB("onyx.update.results.job"),

    PASSWORD_CHANGE_ALLOWED("password.change.allowed"),

    PORTFOLIO_MAP_STYLES("portfolio.map.styles"),

    PORTFOLIO_MAP_STYLES_VALUES("portfolio.map.styles.values"),

    PORTFOLIO_OFFER_PUBLIC_MAP_LIST("portfolio.offer.public.map.list"),

    PORTLET_BOOKMARKS_ENABLED("portlet.bookmarks.enabled"),

    PORTLET_CALENDAR_ENABLED("portlet.calendar.enabled"),

    PORTLET_DIDYOUKNOW_ENABLED("portlet.didYouKnow.enabled"),

    PORTLET_EFFICIENCYSTATEMENTS_ENABLED("portlet.efficiencyStatements.enabled"),

    PORTLET_GROUPS_ENABLED("portlet.groups.enabled"),

    PORTLET_INFOMESSAGES_ENABLED("portlet.infomessages.enabled"),

    PORTLET_INFOMSG_ENABLED("portlet.infomsg.enabled"),

    PORTLET_MACARTNEY_BASEURI("portlet.macartney.baseUri"),

    PORTLET_MACARTNEY_ENABLED("portlet.macartney.enabled"),

    PORTLET_NOTES_ENABLED("portlet.notes.enabled"),

    PORTLET_NOTIFICATIONS_ENABLED("portlet.notifications.enabled"),

    PORTLET_QUICKSTART_ENABLED("portlet.quickstart.enabled"),

    PORTLET_REPOSITORY_STUDENT_ENABLED("portlet.repository.student.enabled"),

    PORTLET_REPOSITORY_TEACHER_ENABLED("portlet.repository.teacher.enabled"),

    PORTLET_SHIBLOGIN_ENABLED("portlet.shiblogin.enabled"),

    PORTLET_SYSINFO_FILEPATH("portlet.sysinfo.filepath"),

    PORTLET_SYSINFO_URL("portlet.sysinfo.url"),

    PORTLET_SYSTEM_EVENTS_ENABLED("portlet.system.events.enabled"),

    PORTLET_ZENTRALSTELLE_ENABLED("portlet.zentralstelle.enabled"),

    PROJECT_BUILD_HOME_DIRECTORY("project.build.home.directory"),

    PROPERTY_LOCATOR_ITCASE_DUMMY("property.locator.itcase.dummy"),

    REGISTRATION_DISCLAIMERADDITIONALCHECKBOX("registration.disclaimerAdditionalCheckbox"),

    REGISTRATION_DISCLAIMERADDITIONALINKTEXT("registration.disclaimerAdditionaLinkText"),

    REGISTRATION_ENABLEDISCLAIMER("registration.enableDisclaimer"),

    REGISTRATION_ENABLENOTIFICATIONEMAIL("registration.enableNotificationEmail"),

    REGISTRATION_ENABLESELFREGISTRATION("registration.enableSelfRegistration"),

    REGISTRATION_NOTIFICATIONEMAIL("registration.notificationEmail"),

    REGISTRATION_PRESET_USERNAME_ALLOWCHANGES("registration.preset.username.allowChanges"),

    REGISTRATION_PRESET_USERNAME_DOMAIN("registration.preset.username.domain"),

    REGISTRATION_PRESET_USERNAME("registration.preset.username"),

    REGISTRATION_PRESET_USERNAME_SHIBBOLETHATTRIBUTE("registration.preset.username.shibbolethAttribute"),

    REGISTRATION_PRESET_USERNAME_VALUES("registration.preset.username.values"),

    REQUEST_BASED_IPS("request.based.ips"),

    RESTART_WINDOW_END("restart.window.end"),

    RESTART_WINDOW_START("restart.window.start"),

    SAPCAMPUSMGNTEXTENSION_ENABLED("SAPCampusMgntExtension.enabled"),

    SEARCH_BROKER_URL("search.broker.url"),

    SEARCH_INDEXING_CRONJOB_EXPRESSION("search.indexing.cronjob.expression"),

    SEARCH_INDEXING_CRONJOB("search.indexing.cronjob"),

    SEARCH_SERVICE("search.service"),

    SERVER_DOMAINNAME("server.domainname"),

    SERVER_MODJK_ENABLED("server.modjk.enabled"),

    SERVER_PORT("server.port"),

    SERVER_PORT_SSL("server.port.ssl"),

    SERVER_SESSION_TIMEOUT("server.session.timeout"),

    SHIBBOLETH_DEFAULTUID("shibboleth.defaultUID"),

    SHIBBOLETH_ENABLE("shibboleth.enable"),

    SHIBBOLETHGENERIC_DEFAULT("shibbolethGeneric.default"),

    SHIBBOLETHGENERIC_ENABLE("shibbolethGeneric.enable"),

    SHIBBOLETHUZH_DEFAULT("shibbolethUZH.default"),

    SHIBBOLETHUZH_ENABLE("shibbolethUZH.enable"),

    SHIBBOLETH_WAYF_ADDITIONALIDPS("shibboleth.wayf.additionalIDPs"),

    SHIBBOLETH_WAYFRETURNURL("shibboleth.wayfReturnUrl"),

    SHIBBOLETH_WAYFSPENTITYID("shibboleth.wayfSPEntityID"),

    SHIBBOLETH_WAYFSPHANDLERURL("shibboleth.wayfSPHandlerURL"),

    SHIBBOLETH_WAYFSPSAMLDSURL("shibboleth.wayfSPSamlDSURL"),

    SYSTEM_REG_PUBLISH_WEBSITE("system.reg.publish.website"),

    SYSTEM_REG_DESC_WEBSITE("system.reg.desc.website"),

    SYSTEM_REG_NOTIFY_NEW_RELEASES("system.reg.notify.new.releases"),

    SYSTEM_REG_INSTANCE_IDENTIFYER("system.reg.instance.identifyer"),

    SYSTEM_REG_SECRET_KEY("system.reg.secret.key"),

    SYSTEM_REG_LOCATION("system.reg.location"),

    SYSTEM_REG_LOCATION_COORD("system.reg.location.coord"),

    SYSTEM_REG_EMAIL("system.reg.email"),

    SMTP_HOST("smtp.host"),

    SMTP_PWD("smtp.pwd"),

    SMTP_SSLCHECKCERTIFICATE("smtp.sslCheckCertificate"),

    SMTP_SSLENABLED("smtp.sslEnabled"),

    SMTP_USER("smtp.user"),

    SUPPORTEMAIL("supportemail"),

    USERDATA_DIR_COMMENT("userdata.dir.comment"),

    USERDATA_DIR("userdata.dir"),

    USERNAMES_TO_LEVELS("usernames.to.levels"),

    USER_GENERATETESTUSERS("user.generateTestUsers"),

    WEBDAV_LINKS_ENABLED("webdav.links.enabled"),

    SOURCE_VIEW_HG_REPO("src.view.hg.repo");

    private String propertyName;

    private PropertyLocator(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * access the enum value
     * 
     * @return
     */
    public String getPropertyName() {
        return propertyName;
    }
}
