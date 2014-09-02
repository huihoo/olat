create or replace view ck_horizontal_student_userproperty as
select u.fk_user_id, i.status,
        max(case when u.propname = 'firstname' then u.propvalue end) as first_name,
        max(case when u.propname = 'lastname'  then u.propvalue end) as last_name,
        max(case when u.propname = 'email'     then u.propvalue end) as email,
        max(case when u.propname = 'institutionalMatriculationNumber' then u.propvalue end) as useridentifier
    from o_userproperty u, o_bs_identity i
    where u.fk_user_id=i.fk_user_id
    and i.status<>199
    group by u.fk_user_id;
    
create or replace view ck_horizontal_lecturer_userproperty as
select u.fk_user_id, i.status,
        max(case when u.propname = 'firstname' then u.propvalue end) as first_name,
        max(case when u.propname = 'lastname'  then u.propvalue end) as last_name,
        max(case when u.propname = 'email'     then u.propvalue end) as email,
        max(case when u.propname = 'institutionalEmployeeNumber' then u.propvalue end) as useridentifier
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
 ck_horizontal_student_userproperty sub2
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
 ck_horizontal_lecturer_userproperty sub2
 where sub.id=sub2.useridentifier
or sub.email=sub2.email
or(sub.first_name=sub2.first_name and sub.last_name=sub2.last_name)
group by sub.id;

