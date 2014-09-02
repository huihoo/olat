
create table if not exists ck_delegation (
  	id bigint not null  AUTO_INCREMENT,
	delegator VARCHAR(100) not null,
	delegatee VARCHAR(100) not null,
	modified_date timestamp default  CURRENT_TIMESTAMP,
	primary key (id)
)engine InnoDB;

alter table ck_delegation add unique (delegator,delegatee);
alter table ck_course add column synchronizable TINYINT(1) NOT NULL default 1;