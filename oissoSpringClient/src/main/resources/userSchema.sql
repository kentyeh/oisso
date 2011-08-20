create table userdata(
      userid  varchar(50) ,
      username varchar(50) ,
      city varchar(50),
      roles text);
--wning to h2 in-memory mode can't persistent datga, so we need to prepare data in advanced.
insert into userdata values('admin','SuperVisor','Kaohsiung','ROLE_ADMIN,ROLE_USER');
insert into userdata values('user','Common User','Taipei','ROLE_USER');