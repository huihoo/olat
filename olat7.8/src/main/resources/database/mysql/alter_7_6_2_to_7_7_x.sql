create table if not exists ck_org (
	id bigint not null,
	short_name varchar(50) not null,
	name varchar(255) not null,
	modified_date datetime,
	primary key (id)
)engine InnoDB;

alter table o_repositoryentry modify displayname VARCHAR(255);
alter table o_repositoryentry modify resourcename VARCHAR(255);
alter table o_catentry modify name VARCHAR(255);

alter table ck_course change short short_title varchar(255) not null;
alter table ck_course add column short_semester char(4);
alter table ck_course add column enabled char(1)  NOT NULL default '0';

alter table ck_olat_user add column kind_of_mapping VARCHAR(6) NOT NULL default 'MANUAL';
alter table ck_olat_user add column mapping_timestamp timestamp default  CURRENT_TIMESTAMP;
update ck_olat_user set mapping_timestamp=now();
	
alter table ck_course drop version;
alter table ck_lecturer drop version;
alter table ck_student drop version;
alter table ck_event drop version;
alter table ck_text drop version;



create or replace view ck_not_mapped_students as 
select * from ck_student s where s.id in
(select sc.student_id from ck_student_course sc, ck_course c  
where sc.course_id = c.id  and c.enabled='1')
and s.id not in(select sap_user_id from ck_olat_user ou where ou.sap_user_type= 'STUDENT'); 

create or replace view ck_not_mapped_lecturers as 
select * from ck_lecturer l where l.id in
(select lc.lecturer_id from ck_lecturer_course lc, ck_course c  
where lc.course_id = c.id  and c.enabled='1')
and l.id not in(select sap_user_id from ck_olat_user ou where ou.sap_user_type= 'LECTURER'); 

create or replace view ck_horizontal_userproperty as
select u.fk_user_id, i.status,
        max(case when u.propname = 'firstname' then u.propvalue end) as first_name,
        max(case when u.propname = 'lastname'  then u.propvalue end) as last_name,
        max(case when u.propname = 'email'     then u.propvalue end) as email,
        max(case when u.propname = 'institutionaluseridentifier' then u.propvalue end) as useridentifier
    from o_userproperty u, o_bs_identity i
    where u.fk_user_id=i.fk_user_id
    and i.status<>199
    group by u.fk_user_id;
    
create or replace view ck_students_to_be_mapped_manually as 
select count(sub.id) as count, 
 sub.registration_nr as sap_matrikelnr, sub2.useridentifier as olat_matrikelnr,
 sub.first_name as  sap_firstname, sub2.first_name as  olat_firstname,
 sub.last_name as  sap_lastname, sub2.last_name as  olat_lastname,
 sub.email as  sap_email, sub2.email as  olat_email
 from ck_not_mapped_students sub, 
 ck_horizontal_userproperty sub2
 where sub.registration_nr=sub2.useridentifier
or sub.email=sub2.email
or(sub.first_name=sub2.first_name and sub.last_name=sub2.last_name)
group by sub.id;

create or replace view ck_lecturers_to_be_mapped_manually as 
select count(sub.id) as count, 
 sub.id as sap_personalnr, sub2.useridentifier as olat_personalnr,
 sub.first_name as  sap_firstname, sub2.first_name as  olat_firstname,
 sub.last_name as  sap_lastname, sub2.last_name as  olat_lastname,
 sub.email as  sap_email, sub2.email as  olat_email
 from ck_not_mapped_lecturers sub, 
 ck_horizontal_userproperty sub2
 where sub.id=sub2.useridentifier
or sub.email=sub2.email
or(sub.first_name=sub2.first_name and sub.last_name=sub2.last_name)
group by sub.id;



