
##
## application-data.sql
##
## Torque will not autogenerate these files anymore - please run
## this SQL code maually to get your application up and running
##

INSERT INTO AUTHOR (AUTH_ID, FIRST_NAME, LAST_NAME) values (1, 'Donald', 'Knuth');
INSERT INTO AUTHOR (AUTH_ID, FIRST_NAME, LAST_NAME) values (2, 'Mickey', 'Mouse' );
INSERT INTO AUTHOR (AUTH_ID, FIRST_NAME, LAST_NAME) values (3, 'Bill', 'Bryson' );

INSERT INTO BOOK ( BOOK_ID, AUTH_ID, TITLE, SUBJECT ) values ( 1, 1, 'The Art of Computer Programming', 'Computer Science');
INSERT INTO BOOK ( BOOK_ID, AUTH_ID, TITLE, SUBJECT ) values ( 2, 2, 'Disney: Behind the Scenes', 'Fiction' );
INSERT INTO BOOK ( BOOK_ID, AUTH_ID, TITLE, SUBJECT ) values ( 3, 3, 'A Walk in the Woods', 'Fiction');