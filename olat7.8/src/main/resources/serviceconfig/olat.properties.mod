# http://www.olat.org
#
# Licensed under the Apache License, Version 2.0 (the "License"); <br>
# you may not use this file except in compliance with the License.<br>
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,<br>
# software distributed under the License is distributed on an "AS IS" BASIS, <br>
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
# See the License for the specific language governing permissions and <br>
# limitations under the License.
#
# Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
# University of Zurich, Switzerland.
 
 
########################################################################
#
# Default configuration file for OLAT
#
# This is the main OLAT configuration file. You should ensure that
# you have read and understood the OLAT documentation beforehand.
# In all likelihood you will need to alter some of the settings
# below to suit your environment.
#
# You should begin by creating an empty olat.local.properties file and put 
# this into a container dependent location (e.g. $CATALINA_HOME/lib dir)
# or if you are inside Eclipse you may put it into the webapp/WEB-INF/src directory.
# The olat.local.properties files takes precedence over olat.properties
#
# Irrespective of platform we always recommend the use of forward 
# slashes in path names.
#
########################################################################


########################################################################
# Application directories and parameters
########################################################################

# application data directory. Default value is set by -Djava.io.tmpdir
# TODO OB: olatdata still fix path segment??
userdata.dir=

# directory for archived materials pending deletion. Default value is ${userdata.dir}/deleted_archive
archive.dir=

# application logs directory. Default value is ${userdata.dir}/logs
log.dir=

# content directory. Default value is ${userdata.dir}/bcroot
folder.root=

# maximum file upload limit in MB
folder.maxulmb=50

# TODO OB: was bedeutet der parameter genau?
folder.quotamb=50


########################################################################
# Application settings
########################################################################

# enabled locales (ISO code e.g. en_US)
enabledLanguages=en,de,fr,it

# TODO OB: Kanban erstellen: system conf für languages disablen (I18nModule.properties rauswerfen)
# default locale
defaultlang=en

# TODO OB: Kanban erstellen: fallback=default => fallback entfernen
fallbacklang=en

# default file encoding (e.g. ISO-8859-1, UTF-8)
defaultcharset=ISO-8859-1

# TODO OB: what for??
project.build.home.directory=

# TODO Oliver Kanban: war artifacts => remove property
build.version=7.7.0.2-SNAPSHOT


########################################################################
# SMTP (mail) settings
########################################################################

# set to enable mail service [disabled|enabled]
is.mail.service=disabled

# TODO OB Kanban: wenn is.mail.service=enabled dann smtp.host konfiguriert!!
# mail server host
smtp.host=localhost

# TODO OB: check usage
smtp.user=
smtp.pwd=
smtp.sslEnabled=false
smtp.sslCheckCertificate=false

# address to which support emails are to be sent
# TODO OB: mandatory if mail enabled
supportemail=support@mydomain.com
# maximum size for email attachements (in MB)
mail.attachment.maxsize=5


########################################################################
# User registration
########################################################################

# set to enable self registration [true|false]
registration.enableSelfRegistration=false

# TODO OB Kanban (low prio): review and simplify
# ask user to accept a disclaimer at first login. Use i18n tool to customize disclaimer message
#registration.enableDisclaimer=true
# add a second checkbox to the disclaimer
#3registration.disclaimerAdditionalCheckbox=false
# add a link to the disclaimer with a file download
#registration.disclaimerAdditionaLinkText=false
# setting for the bean interceptor in the registration process, disabled mean that no interceptor
# is desired. Standard implementation are: byShibbolethAttribute which generate a username based
# from a shibboleth attribute, byEmail which generate a username based on the email from the user
# which try to registrate itself
#registration.preset.username.values=disabled,byEmail,byShibbolethAttribute
#registration.preset.username=disabled
# setting for byEmail implementation: a domain restriction to preset the username
#registration.preset.username.domain=
# settings for byShibbolethAttribute implementation:
# allowChanges the new user to changes its username (only when using byShibbolethAttribute)
#registration.preset.username.allowChanges=true
# the shibboleth attribute to use to preset the username
#registration.preset.username.shibbolethAttribute=Shib-SwissEP-UniqueID


########################################################################
# login settings
########################################################################

# set to enable guest login [true|false]
login.enableGuestLoginLinks=true
# set to enable invitation login for ePortfolio [true|false]
login.invitationLogin=true
# set to enable login via email or username [true|false]
login.using.username.or.email.enabled=true
# TODO OB Kanban: disable functionality
password.change.allowed=true
# TODO OB Kanban: remove props / code wie false
keepUserEmailAfterDeletion=false
keepUserLoginAfterDeletion=false


########################################################################
# notifications
########################################################################

# cron expression for delivery of notification
notification.synchronize.publishers.cron.expression=0 30 4 * * ?
# email from which notifications are sent
notification.mail.from.address=notification@mydomain.com
# TODO OB/GH Kanban: anschauen (evtl. JNDI) 
olat.web.url=https://www.myolat.com
# TODO SH: comment
notification.news.days=31


####################################################
# QTI configuration
####################################################

# set to enable QTI implementation [Olat]
assessmentplugin.activate=Olat


########################################################################
# OLAT technical settings
########################################################################

# identifier for OLAT installation comprising of up to ten alphabetic characters.
# same identifier must be used for all nodes in the same cluster.
instance.id=myolat
# set to select theme
layout.theme = default
# set to generate test users [true|false]
user.generateTestUsers=false
# set to enable debug mode [true|false]
olat.debug=false
# set to enable OLAT GUI demonstration code [true|false]
guidemo.enabled=true
# TODO OB olat.local.props
# set to source code path
src.view.hg.repo=http://hg.olat.org/repos/OLAT-7.3.x/raw-file/tip


# cache localization files
# TODO OB: what's going on => olat.local.props or Eclipse
localization.cache=true
#number of elements to cache in course cache
# TODO OB: what's going on??
course.cache.elements=500
# required only for performance and functional testing
# TODO OB: move to olat.local.props
allow.loadtest.mode=false
# Portlets enabled by default
# TODO OB: remove code
# TODO OB: remove?
portlet.didYouKnow.enabled=true
# set to enable "My Groups" in home [true|false]
portlet.groups.enabled=true
# set to enable "Bookmarks" in home [true|false]
portlet.bookmarks.enabled=true
# set to enable "Notes" in home [true|false]
portlet.notes.enabled=true
# set to enable "Notifications" in home [true|false]
portlet.notification.enabled=true
# set to enable "Transcripts" in home [true|false]
portlet.efficiencyStatements.enabled=true
# TODO OB: check??
portlet.quickstart.enabled=true
# set to enable "Calendar" in home [true|false]
portlet.calendar.enabled=true
# Optional portlets, disabled by default
# TODO OB: rausfinden
portlet.repository.student.enabled=false
portlet.repository.teacher.enabled=false
# the info message portlet is a counterpart of the course building block
# course.node.infomessage, see further down this file. 
# TODO OB: rausfinden
portlet.infomessages.enabled=true
# set to enable "Campus Course" in home [true|false]
portlet.campuscourse.enabled=true

# set to enable export of grades for external student information system (e.g. SAP/CM) [true|false]
SAPCampusMgntExtension.enabled=false

# set to enable display of webdav links [true|false]
webdav.links.enabled=true


########################################################################
# Web application container (e.g., Tomcat) settings
########################################################################

# TODO OB/GH Kanban: move to container / enable system props config (over olat.(local).props)
server.port.ssl=0

# TODO OB: evtl. server.domainname/server.port => domainname statt port
server.domainname=localhost
# the port on which the container is listening
server.port=8080
server.modjk.enabled=false
# OLAT JMX server port (must be unique per node in a cluster)
# TODO OB/GH: move to container
jmx.rmi.port=3000


########################################################################
# Database settings
########################################################################

# set to enable automatic upgrade of database schema [true|false]
auto.upgrade.database=true
# TODO OB: remove props => only MySQL
db.vendor=mysql

# the server hosting the database
db.host=localhost
db.host.port=3306

db.name=olat
db.user=olat
db.pass=olat

# optional database parameters
db.url.options.mysql=?useUnicode=true&characterEncoding=UTF-8

# enable database debugging (seldom required except for developers)
# TODO OB: move to local.props
db.show_sql=false
# validate, update, create, create-drop are valid entries. With embedded hsqldb use "update".
# set to "validate"  for validating hbm.xml or annotations against setup script setupDatabase.sql.
# set no value to disable hibernate auto capabilities.
db.hibernate.ddl.auto=

# configure the database connection pool size
db.hibernate.c3p0.minsize=20
db.hibernate.c3p0.maxsize=50


########################################################################
# Fonts for jsMath Formula Editor (part of html editor and wiki)
########################################################################
unpack.fonts.comment=set to false if you do not require special image fonts of to speed up development cycle (unpacking takes some time as the zip contains more than 20'000 files!)
# TODO OB: check if still necessary
unpack.fonts=false


########################################################################
# Instant Messaging (optional)
########################################################################

# if enabled then the IM Server must be running before OLAT is started!
instantMessaging.enable=false
#set the server domain name. It must be a proper dns name, localhost does not work for groupchat
instantMessaging.server.name=jabber.myolat.com
instantMessaging.server.name.comment=set the server domain name. It must be a proper dns name, localhost does not work for groupchat
# permit multiple OLAT instances to use the same IM server
# each instance will append its ID to user-/group name
instantMessaging.multipleInstances=false
# when using multiple OLAT instances if you use email addresses as OLAT
# usernames then the '@' must be replaced !
instantMessaging.replaceStringForEmailAt=_at_
# only required for testing/debugging purposes
instantMessaging.generateTestUsers=false
# an "admin" account must be present, do not change this username!
instantMessaging.admin.username=admin
instantMessaging.admin.password=admin
#if true all personal groups are synchronized with the im server
instantMessaging.sync.personal.groups=true
#if true all learning groups (all groups from all courses!) are synchronized with the im server.
#ATTENTION: On a server with many courses and groups this can generate thousand of groups and therefore
#generete millions of presence messages. Check openfire reguarly if set to true!
instantMessaging.sync.learning.groups=true

#whether to display current course participant count in the course toolbox
course.display.participants.count=true


########################################################################
# Fulltext Search settings
########################################################################
generate.index.at.startup=true
restart.window.start=0
restart.window.end=24
# Enable search-service for only one node per cluster [ enabled | disabled ]
search.service=enabled
# Enable triggering indexer via cron-job instead at startup [ enabled | disabled ]
# When enabled , configure 'generate.index.at.startup=false'
search.indexing.cronjob=disabled
# Example '0 0 18 * * ?' start indexer at 18:00 ever day
search.indexing.cronjob.expression=0 0 18 * * ?


########################################################################
# Security
########################################################################

shibboleth.enable=false
# set the name of the Shibboleth attribute used to identify authorized users
shibboleth.defaultUID=defaultUID
# the Authentication and Authorization Infrastructure (AAI) is a
# federated identity management system used in Switzerland that
# supports the sending of a locale code within an AAI request using
# a key as named in the language.param setting
language.enable=false
language.param=YOUR_PARAM_NAME

#these settings are university of zurich specific
shibboleth.wayfSPEntityID=
shibboleth.wayfSPHandlerURL=
shibboleth.wayfSPSamlDSURL=
shibboleth.wayfReturnUrl=

#you can manuall add additional IDP servers. See org/olat/portal/shiblogin/_content/portlet.html
#for an example
shibboleth.wayf.additionalIDPs=
#The auth provider you set to default will be the one you see when you access the loginpage, alternate providers are shows as links below
#enable and or set the basic login provider (username/password) on the loginpage active and or default
olatprovider.enable=true
olatprovider.default=true

#enable and or set the custom uzh shib login provider on the loginpage active and or default
shibbolethUZH.enable=false
shibbolethUZH.default=false

#enable and or set the generic shib login provider on the loginpage active and or default
shibbolethGeneric.enable=false
shibbolethGeneric.default=false

########################################################################
# Clustering settings
########################################################################

# set to Cluster to enable, otherwise use SingleVM to disable cluster features
cluster.mode=SingleVM
# each node requires a unique ID (1-64) starting at "1"
node.id=1
# certain servies (e.g., notifications, course logger etc. are not
# cluster capable and can only run on a single node - set this to
# enabled on that node
cluster.singleton.services=enabled
# JMS broker url's where the path (localhost:61700) - defines the local adress and local port :
# SingleVM jms.broker.url
jms.broker.url=vm://embedded?broker.persistent=false
search.broker.url=vm://embedded?broker.persistent=false
codepoint.jms.broker.url=vm://embedded?broker.persistent=false
# Cluster (remote) jms.broker.url
#jms.broker.url=failover:(tcp://localhost:61616?wireFormat.maxInactivityDuration=0)
#search.broker.url=failover:(tcp://localhost:61616?wireFormat.maxInactivityDuration=0)
#codepoint.jms.broker.url=failover:(tcp://localhost:61616?wireFormat.maxInactivityDuration=0)


# enable/disable codepoint/breakpoint framework
codepoint_server.enabled=false

#####
#query cache config for singlevm/cluster
#####
#cluster need hibernate.caching.cluster.class set and hibernate.caching.singlevm.class empty and second level cache to false
#for the cluster version you have to add treecache.xml to the classpath, see olat3/conf/trecache.xml for an example
hibernate.caching.singlevm.class=net.sf.ehcache.hibernate.EhCacheProvider
#hibernate.caching.cluster.class=org.hibernate.cache.jbc2.SharedJBossCacheRegionFactory
hibernate.caching.cluster.class=
hibernate.caching.use.query.cache=true
hibernate.use.second.level.cache=true

# TODO OB: Kann LDAP entfernt werden??
#####
# LDAP configuration parameters (optional)
# for advanced config options see webapp/WEB-INF/src/serviceconfig/org/olat/ldap/_spring/olatextconfig.xml
#####
ldap.enable=false
# is ldap your default provider? true or false
ldap.default=false
ldap.activeDirectory=false
# The date format is not the same for OpenLDAP (yyyyMMddHHmmss'Z') or 
# ActiveDirectory (yyyyMMddHHmmss'.0Z')
ldap.dateFormat=yyyyMMddHHmmss'Z'
ldap.dateFormat.values=yyyyMMddHHmmss'Z',yyyyMMddHHmmss'.0Z'
# The LDAP Provider from the Oracle's JDKs (standard and JRockit) allows the use of multiple LDAP servers.
# Write the URLs of all the servers with a space as separator and a trailing slash.
ldap.ldapUrl=ldap://ldap.olat.org:389
ldap.ldapUrl.values=ldap://ldap1.olat.org:389/ ldap://ldap2.olat.org:389/ ldap://ldap3.olat.org:389/
# System user: used for getting all users and connection testing
ldap.ldapSystemDN=CN=Frentix,OU=Benutzer,DC=olat,DC=ch
ldap.ldapSystemPW=ldap4olat
# List of bases where to find users. To use multiple bases you must edit the config file manually
ldap.ldapBases=OU=Students,DC=olat,DC=ch
# SSL configuration for LDAP
ldap.sslEnabled=false
ldap.trustStoreLocation=/usr/lib/j2sdk1.5-sun/jre/lib/security/cacerts
ldap.trustStorePwd=changeit
ldap.trustStoreType=JKS
# When users log in via LDAP, the system can keep a copy of the password as encrypted
# hash in the database. This makes OLAT more independent from an offline LDAP server
# and users can use their LDAP password to use the WebDAV functionality.
# If you have a mixed environment where some users have webDAV passwords and some have 
# only local OLAT user accounts, you have to set this flag to 'true.
# When setting to true (recommended), make sure you configured password.change.allowed=false
# unless you also set ldap.propagatePasswordChangedOnLdapServer=true
ldap.cacheLDAPPwdAsOLATPwdOnLogin=true
# Change the password on the LDAP server too
ldap.propagatePasswordChangedOnLdapServer=false
# When the system detects an LDAP user that does already exist in OLAT but is not marked
# as LDAP user, the OLAT user can be converted to an LDAP managed user.
# When enabling this feature you should make sure that you don't have a user 'administrator'
# in your ldapBases (not a problem but not recommended)
ldap.convertExistingLocalUsersToLDAPUsers=false
# Users that have been created vial LDAP sync but now can't be found on the LDAP anymore
# can be deleted automatically. If unsure, set to false and delete those users manually
# in the user management.
ldap.deleteRemovedLDAPUsersOnSync=false
# Sanity check when deleteRemovedLDAPUsersOnSync is set to 'true': if more than the defined
# percentages of user accounts are not found on the LDAP server and thus recognized as to be
# deleted, the LDAP sync will not happen and require a manual triggering of the delete job
# from the admin interface. This should prevent accidential deletion of OLAT user because of
# temporary LDAP problems or user relocation on the LDAP side.
# Value= 0 (never delete) to 100 (always delete).
ldap.deleteRemovedLDAPUsersPercentage=50
# Should users be created and synchronized automatically on OLAT startup? Set this option to
# 'true' to create and sync all LDAP users in a batch manner on each OLAT startup.If you set 
# this configuration to 'false', the users will be generated on-the-fly when they log in
# For the cron syntax see http://quartz.sourceforge.net/javadoc/org/quartz/CronTrigger.html
ldap.ldapSyncOnStartup=true
# Independent of ldap.ldapSyncOnStartup, users can be created / synced in a batch manner
# peridically. Set ldap.ldapSyncCronSync=true if you want such a behaviour and set an 
# appropriate cron expression to define the interval. The default cron expression will 
# sync the LDAP database with the OLAT database each hour.
ldap.ldapSyncCronSync=${ldap.ldapSyncOnStartup}
ldap.ldapSyncCronSyncExpression=0 0 * * * ?
# Configuration for syncing user attributes during login or cron and batch sync (examples are
# for an active directory)
ldap.ldapUserObjectClass=person
ldap.ldapUserCreatedTimestampAttribute=whenCreated
ldap.ldapUserLastModifiedTimestampAttribute=whenChanged
# OpenLDAP is userPassword, ActiveDirectory is unicodePwd
ldap.ldapUserPassordAttribute=userPassword
# Define mapping of user attributes. Only the mandatory attributes are defined here, see the
# config file for advanced user attributes mapping
ldap.attributename.useridentifyer=sAMAccountName
ldap.attributename.email=mail
ldap.attributename.firstName=givenName
ldap.attributename.lastName=sn

#####
# Build properties
#####
application.name=OLAT

#####
# OLAT logging
#####
log.anonymous=false



########################################
# Course building blocks, every course building block can be disabled by adding a property here and reference it in
# appropriate spring config file (by default course building blocks are enabled)
########################################
course.node.linklist.enabled=false
course.node.checklist.enabled=false
course.node.dateenrollment.enabled=false
course.node.basiclti.enabled=true
course.node.portfolio.enabled=true
course.node.infomessage.enabled=true

########################################
# Options for e-portfolio
########################################
#List of styles avaialbe for the e-portfolio maps. The separator is a ,
portfolio.map.styles=default,comic,leather
portfolio.map.styles.values=default,comic,leather
portfolio.deadline.cron.expression=0 5 */8 * * ?

#offer a list of all maps shared to public. Don't enable if > 500 public maps exist! The maps can still be viewed by opening a users vcard.
portfolio.offer.public.map.list=true

#######################################
# Versioning config
#######################################
# Max versions: -1 is unlimited, 0 is no versioning, 1 - n is the exact max. number of versions allowed
maxnumber.versions=0

#######################################
# Services enable/disable
#######################################
thumbnail.generation.service.enabled=true
thumbnail.generation.image.enabled=true
thumbnail.generation.pdf.enabled=true

#######################################
# ThreadLocalLogLevelManager allows logging of a certain ips/usernames by setting only this theads loggers to e.g. debug 
#######################################
request.based.ips=
usernames.to.levels=

#######################################
# for Integration Testing used only, used by SystemPropertiesLoaderTest 
#######################################
property.locator.itcase.dummy=default_olat_properties_value

#######################################
# system registration service on olat.org 
#######################################
system.reg.publish.website=false
system.reg.desc.website=
system.reg.notify.new.releases=false
system.reg.instance.identifyer=
system.reg.secret.key=

#############################
# Campus Course Configuration
#############################
campus.template.course.resourceable.id=85657029811935
campus.template.supportedLanguages=DE,EN,FR,IT
campus.template.defaultLanguage=DE
campus.template.course.groupA.name=Campusgruppe A
campus.template.course.groupB.name=Campusgruppe B
# co-owners will be added as owner to all created campuskurs, delimiter ',' e.g. co_owner1,co_owner2
campus.course.default.co.owner.usernames=
# Import Interval
campus.import.process.cron.expression=0 0 08,12,18 * * ?
# Import Directory
campus.import.process.sap.resources=file:${userdata.dir}/campus/sap/resources
campus.import.process.sap.files.suffix=_CUS&CURRENT_NOETZLIP.csv
# Quote character for parsing the lines of the csv files
campus.import.process.lineTokenizer.quoteCharacter=#
# UserMapping Interval
campus.userMapping.process.cron.expression=0 30 08,12,18 * * ?
# enable/disable synchronizeTitleAndDescription
campus.enable.synchronizeTitleAndDescription=true
# The string identifying the campusKurs within the description
campus.description.startsWith.string=campuskurs:,campus course:,cours campus:,corso campo:
campus.courseCreator.process.cron.expression=0 30 08,12,18 * * ?
