pragma foreign_keys = off;
drop table if exists user;
drop table if exists person;
drop table if exists event;
drop table if exists authtoken;
drop table if exists enum_gender;
pragma foreign_keys = on;

create table enum_gender(
	gender		text	not null	primary key
);
insert into enum_gender(gender) values
	('f'),
	('m');

create table user (
	username	text	not null	primary key,
	"password" 	text	not null,
	email 		text	not null,
	firstName	text	not null,
	lastName	text	not null,
	gender		text	not null,
	personID	text				unique,
	foreign key(gender)		references enum_gender,
	foreign key(personID)	references person(personID)
);

create table person(
	personID			text	not null	primary key,
	associatedUsername	text	not null,
	firstName			text	not null,
	lastName			text 	not null,
	gender				text	not null,
	fatherID			text,
	motherID			text,
	spouseID			text,
	foreign key(gender)				references enum_gender,
	foreign key(associatedUsername)	references user(username),
	foreign key(fatherID)			references person(personID),
	foreign key(motherID)			references person(personID),
	foreign key(spouseID)			references person(personID)
);

create table event(
	eventID				text	not null	primary key,
	associatedUsername	text	not null,
	personID			text	not null,
	latitude			real	not null,
	longitude			real	not null,
	country				text 	not null,
	city				text	not null,
	eventType			text 	not null,
	year				int		not null
		check(year > 0),
	foreign key(associatedUsername)	references user(username),
	foreign key(personID)			references person(personID)
);

create table authtoken(
	authtoken 	text	not null	primary key,
	username	text	not null,
	foreign key(username)	references user(username)
);
