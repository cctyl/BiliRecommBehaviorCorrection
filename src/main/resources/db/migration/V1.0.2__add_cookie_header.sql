
CREATE TABLE cookie_header_data
(
    id    char(30) NOT NULL,
    url VARCHAR(350) NOT NULL,
	ckey varchar(50) null,
	cvalue varchar(200) null,
	type varchar(50)  null,
	created_date DATE  null,
	last_modified_date DATE  null,
	is_deleted tinyint(1)  default 0,
	version int  default 1,
    CONSTRAINT pk_dict PRIMARY KEY (id)
);


CREATE TABLE config
(
    id    char(30) NOT NULL,
    url VARCHAR(350) NOT NULL,
	name varchar(50) null,
	value varchar(200) null,
	created_date DATE  null,
	last_modified_date DATE  null,
	is_deleted tinyint(1)  default 0,
	version int  default 1,
    CONSTRAINT pk_dict PRIMARY KEY (id)
);

