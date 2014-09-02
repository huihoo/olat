-- tables for campuskurs
create table if not exists ck_export (
	id bigint not null,
	file_name varchar(255) not null,
	timestamp datetime not null,
	export_date datetime not null,
	primary key (id)
)engine InnoDB;

create table if not exists ck_course (
	id bigint not null,
	olat_id bigint,
	short varchar(255) not null,
	title varchar(255) not null,
	e_learning_supported char(1),
	language varchar(255) not null,
	category varchar(255) not null,
	lv_nr varchar(255) not null,
	start_date date,
	end_date date,
	vvz_link varchar(255) not null,
	semester char(255),
	ipz char(1),
	modified_date datetime,
  	version mediumint unsigned not null,
	primary key (id)
)engine InnoDB;

create table if not exists ck_lecturer (
	id bigint not null,
	first_name varchar(255) not null,
	last_name varchar(255) not null,
	email varchar(255) not null,
	modified_date datetime,
	version mediumint unsigned not null,
	primary key (id)
)engine InnoDB;

create table if not exists ck_lecturer_course (
	course_id bigint not null,
	lecturer_id bigint not null,
	primary key (course_id, lecturer_id)
)engine InnoDB;

create table if not exists ck_student (
	id bigint not null,
	registration_nr varchar(255) not null,
	first_name varchar(255) not null,
	last_name varchar(255) not null,
	email varchar(255) not null,
	modified_date datetime,
	version mediumint unsigned not null,
	primary key (id)
)engine InnoDB;

create table if not exists ck_student_course (
	course_id bigint not null,
	student_id bigint not null,
	primary key (course_id, student_id)
)engine InnoDB;

create table if not exists ck_event (
	id bigint not null,
	course_id bigint not null,
	date date not null,
	start time not null,
	end time not null,
	modified_date datetime,
	version mediumint unsigned not null,
	primary key (id)
)engine InnoDB;

create table if not exists ck_text (
	id bigint not null,
	course_id bigint not null,
	type varchar(255) not null,
	line_seq bigint not null,
	line varchar(255) not null,
	modified_date datetime,
	version mediumint unsigned not null,
	primary key (id)
)engine InnoDB;

create table if not exists ck_import_statistic (
    id bigint not null,
    step_id int,
    step_name varchar(255) not null,
    status varchar(255) not null,
    start_time datetime,
    end_time datetime,
    read_count bigint not null,
    write_count bigint not null,
    read_skip_count bigint,
    write_skip_count bigint,
    process_skip_count bigint,
    commit_count bigint,
    rollback_count bigint,
    primary key (id)
)engine InnoDB;

create table if not exists ck_skip_item (
    id bigint not null,
    job_execution_id INT,
    job_name VARCHAR(100),
    step_execution_id INT,
    step_name VARCHAR(100),
    type VARCHAR(100),
    item VARCHAR(2000),
    msg VARCHAR(2000),
     primary key (id)
)engine InnoDB;

create table if not exists ck_olat_user (
    sap_user_id bigint not null,
 	olat_user_name VARCHAR(100),
 	sap_user_type VARCHAR(100),
    primary key (sap_user_id)
)engine InnoDB;
        
alter table ck_lecturer_course add constraint ck_lecturer_course_f01 foreign key (course_id) references ck_course (id);
alter table ck_lecturer_course add constraint ck_lecturer_course_f02 foreign key (lecturer_id) references ck_lecturer (id);
alter table ck_student_course add constraint ck_student_course_f01 foreign key (course_id) references ck_course (id);
alter table ck_student_course add constraint ck_student_course_f02 foreign key (student_id) references ck_student (id);
alter table ck_event add constraint ck_event_f01 foreign key (course_id) references ck_course (id);
alter table ck_text add constraint ck_text_f01 foreign key (course_id) references ck_course (id);


alter table ck_student_course add unique (student_id, course_id);
alter table ck_lecturer_course add unique (lecturer_id, course_id);