alter table o_bs_authentication add column newcredential varchar(255);
alter table o_bs_authentication add column lastmodified datetime after version;
create index  newcredential_idx on o_bs_authentication (newcredential);