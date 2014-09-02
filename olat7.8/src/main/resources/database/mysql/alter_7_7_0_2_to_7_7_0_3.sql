alter table ck_lecturer_course add column modified_date datetime;

update ck_lecturer_course set modified_date=now();

alter table ck_student_course add column modified_date datetime;

update ck_student_course set modified_date=now();