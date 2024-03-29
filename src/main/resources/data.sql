

delete from FILM_MPA cascade;
delete from MPA;
delete from FILM_GENRES;
delete from GENRES;

INSERT INTO GENRES (GENRE_ID, GENRE_NAME) VALUES (1, 'Комедия');
INSERT INTO GENRES (GENRE_ID, GENRE_NAME) VALUES (2, 'Драма');
INSERT INTO GENRES (GENRE_ID, GENRE_NAME) VALUES (3, 'Мультфильм');
INSERT INTO GENRES (GENRE_ID, GENRE_NAME) VALUES (4, 'Триллер');
INSERT INTO GENRES (GENRE_ID, GENRE_NAME) VALUES (5, 'Документальный');
INSERT INTO GENRES (GENRE_ID, GENRE_NAME) VALUES (6, 'Боевик');

INSERT INTO MPA (MPA_ID, MPA_NAME) VALUES (1, 'G');
INSERT INTO MPA (MPA_ID, MPA_NAME) VALUES (2, 'PG');
INSERT INTO MPA (MPA_ID, MPA_NAME) VALUES (3, 'PG-13');
INSERT INTO MPA (MPA_ID, MPA_NAME) VALUES (4, 'R');
INSERT INTO MPA (MPA_ID, MPA_NAME) VALUES (5, 'NC-17');