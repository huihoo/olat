drop table if exists o_noti_sub;

drop table if exists o_noti_pub;

-- remove all "noti_latest_email" properties
delete from o_property where name='noti_latest_email';
