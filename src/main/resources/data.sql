INSERT INTO USERS (name, email, last_name)
VALUES ('User', 'user@yandex.ru', 'UserLastName'),
       ('Admin', 'admin@gmail.com', 'AdminLastName'),
       ('Guest', 'guest@gmail.com', 'GuestLastName');

INSERT INTO USER_ROLE (role, user_id)
VALUES ('USER', 1),
       ('ADMIN', 2),
       ('USER', 2);