-- remove classpath references from o_olatresource
update o_olatresource set resname='BGContextImpl' where resname = 'org.olat.group.context.BGContextImpl';
delete from o_olatresource where resname = 'org.olat.Quota';

-- remove classpath references from o_property
update o_property set textvalue='InstantMessagingModule::issynced' where textvalue = 'org.olat.instantMessaging.InstantMessagingModule::issynced';
delete from o_property where textvalue LIKE '%org.olat.instantMessaging%';

-- remove classpath references from o_userrating for contexthelp ratings
delete from o_userrating where resname = 'contexthelp';

-- migrate classpath references from o_lifecycle and ProjectBroker
-- org.olat.course.nodes.projectbroker.datamodel.ProjectImpl => 2bf03af16b534be3e6f95417646028de
update o_lifecycle set persistenttypename='ProjectImpl' where persistenttypename = '2bf03af16b534be3e6f95417646028de';
-- migrate classpath references from o_lifecycle and User-Deletion
update o_lifecycle set persistenttypename='IdentityImpl' where persistenttypename = 'org.olat.basesecurity.IdentityImpl';
-- migrate classpath references from o_lifecycle and Repository-Deletion
update o_lifecycle set persistenttypename='RepositoryEntry' where persistenttypename = 'org.olat.repository.RepositoryEntry';
-- migrate classpath references from o_lifecycle and Group-Deletion
update o_lifecycle set persistenttypename='BusinessGroupImpl' where persistenttypename = 'org.olat.group.BusinessGroupImpl';
