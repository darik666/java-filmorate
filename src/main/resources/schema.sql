create table if not exists PUBLIC.FILMS
(
    FILM_ID      INTEGER auto_increment
        primary key,
    FILM_NAME    CHARACTER VARYING not null,
    DESCRIPTION  CHARACTER VARYING,
    RELEASE_DATE DATE,
    DURATION     INTEGER
);

create table if not exists PUBLIC.GENRES
(
    GENRE_ID   INTEGER           not null,
    GENRE_NAME CHARACTER VARYING not null,
    constraint "GENRE_pk"
        primary key (GENRE_ID)
);

create table if not exists PUBLIC.FILM_GENRES
(
    FILM_ID  INTEGER not null,
    GENRE_ID INTEGER not null,
    constraint FILM_GENRES_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references PUBLIC.FILMS,
    constraint FILM_GENRES_GENRES_GENRE_ID_FK
        foreign key (GENRE_ID) references PUBLIC.GENRES
);

create table if not exists PUBLIC.MPA
(
    MPA_ID   INTEGER not null,
    MPA_NAME CHARACTER VARYING,
    constraint "MPA_pk"
        primary key (MPA_ID)
);

create table if not exists PUBLIC.FILM_MPA
(
    FILM_ID INTEGER not null,
    MPA_ID  INTEGER not null,
    constraint "FILM_MPA_pk"
        primary key (FILM_ID),
    constraint FILM_MPA_MPA_MPA_ID_FK
        foreign key (MPA_ID) references PUBLIC.MPA
);

create table if not exists PUBLIC.USERS
(
    USER_ID    INTEGER auto_increment,
    USER_EMAIL CHARACTER VARYING not null,
    USER_LOGIN CHARACTER VARYING not null,
    USER_NAME  CHARACTER VARYING,
    BIRTHDAY   DATE,
    constraint USERS_PK
        primary key (USER_ID)
);

create table if not exists PUBLIC.FRIENDS
(
    USER_ID       INTEGER not null,
    FRIEND_ID     INTEGER not null,
    FRIEND_STATUS CHARACTER VARYING,
    constraint "FRIENDS_pk"
        primary key (USER_ID, FRIEND_ID),
    constraint FRIENDS_USERS_USER_ID_FK_2
        foreign key (FRIEND_ID) references PUBLIC.USERS
);
create table if not exists PUBLIC.LIKES
(
    FILM_ID INTEGER not null,
    USER_ID INTEGER not null,
    constraint "LIKES_pk"
        primary key (FILM_ID, USER_ID),
    constraint LIKES_USERS_USER_ID_FK
        foreign key (USER_ID) references PUBLIC.USERS
);