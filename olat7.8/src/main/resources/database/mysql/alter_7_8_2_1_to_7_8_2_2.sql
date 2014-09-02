update o_bs_authentication set credential = newcredential 
where provider not in ('OLAT','WEBDAV') and credential is null and newcredential is not null;