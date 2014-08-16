create table if not exists o_tst_daotest (
  dao_id bigint not null,
  version mediumint unsigned not null,
  name varchar(2048),
  description varchar(2048),
  primary key (dao_id)
) engine InnoDB;

create table if not exists sy_subscriber (
   id bigint unsigned not null,
   version mediumint unsigned not null,
   identity_id bigint not null,
   notification_interval varchar(255),
   subscription_option varchar(255),
   primary key (id)
) engine InnoDB;

create table if not exists sy_channel (
   subscriber_id bigint unsigned not null,
   value varchar(255)
) engine InnoDB;

create table if not exists sy_subscription (
   id bigint unsigned not null,
   version mediumint unsigned not null,
   publisher_id bigint unsigned not null,
   subscriber_id bigint unsigned not null,
   creation_date datetime not null,
   last_notified_date datetime,
   status varchar(255) not null,
   primary key (id)
) engine InnoDB;

create table if not exists sy_publisher (
   id bigint unsigned not null,
   version mediumint unsigned not null,
   source_id bigint unsigned not null,
   source_type varchar(255) not null,
   context_id bigint unsigned not null,
   context_type varchar(255) not null,
   subcontext_id bigint unsigned,
   primary key (id)
) engine InnoDB;

create table if not exists sy_event (
   id bigint unsigned not null,
   version mediumint unsigned not null,
   subscription_id bigint unsigned not null,
   status varchar(255) not null,
   creation_date datetime not null,
   primary key (id)
) engine InnoDB;

create table if not exists sy_attribute (
   event_id bigint unsigned not null,
   attribute_key varchar(255),
   attribute_value varchar(255)
) engine InnoDB;

create index sy_subscriber_i01 on sy_subscriber(identity_id);
create index sy_channel_i01 on sy_channel(subscriber_id);
create index sy_subscription_i01 on sy_subscription(publisher_id);
create index sy_subscription_i02 on sy_subscription(subscriber_id);
create index sy_event_i01 on sy_event(subscription_id);
create index sy_event_i02 on sy_event(creation_date);

alter table sy_subscriber add constraint sy_subscriber_f01 foreign key (identity_id) references o_bs_identity (id);
alter table sy_channel add constraint sy_channel_f01 foreign key (subscriber_id) references sy_subscriber (id);
alter table sy_subscription add constraint sy_subscription_f01 foreign key (publisher_id) references sy_publisher (id);
alter table sy_subscription add constraint sy_subscription_f02 foreign key (subscriber_id) references sy_subscriber (id);
alter table sy_event add constraint sy_event_f01 foreign key (subscription_id) references sy_subscription (id);
alter table sy_attribute add constraint sy_attribute_f01 foreign key (event_id) references sy_event (id);

alter table sy_publisher add unique (context_id,context_type,source_id,source_type);
alter table sy_subscriber add unique (identity_id);
alter table sy_subscription add unique (publisher_id,subscriber_id);