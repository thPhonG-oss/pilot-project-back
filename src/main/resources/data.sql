INSERT INTO EMPLOYEE (VISA, FIRST_NAME, LAST_NAME, BIRTH_DATE)
VALUES ('QMV', 'Quy', 'Van', '1990-02-03'),
       ('NQN', 'Quan', 'Nguyen', '1999-12-03'),
       ('HNH', 'Hanh', 'Ho', '1992-05-12'),
       ('TDN', 'Nhan', 'Tran', '1994-06-17'),
       ('TPG', 'A', 'Nguyen Van', '1999-01-01'),
       ('NPQ', 'B' , 'Nguyen Van', '1991-12-02')
;

INSERT INTO GROUPS (GROUP_LEADER_ID)
VALUES (1),
       (2),
       (3);

INSERT INTO PROJECT (PROJECT_NUMBER, NAME, CUSTOMER, STATUS, START_DATE, END_DATE, GROUP_ID)
VALUES
    ('3116', 'Facturation/Encaissements', 'Les Rataites Populaires', 'NEW', '2004-02-25', null, 1),
    ('3118', 'GKBWEB', 'GKB', 'FIN', '2002-10-10', '2003-10-10', 1),
    ('7157', 'MGBAHM-Maint2015', 'MGB Tourism', 'INP', '2006-09-24', null, 2),
    ('7174', 'SOMED-SPITEX MAINT', 'SOMED-SPITEX MAINT', 'NEW', '2015-10-05', null, 2),
    ('7199', 'ARCHIMEDE-2015-3.14', 'Les Retaites Populaires', 'NEW', '2005-05-29', null, 3),
    ('8000', 'EmpowerHR', 'MCM retails', 'NEW', '2009-10-12', null, 3),
    ('8001', 'TAC English', 'Dol', 'NEW', '2019-04-30', null, 3)
    ;

INSERT INTO PROJECT_EMPLOYEE(PROJECT_ID, EMPLOYEE_ID)
VALUES (1, 1),
       (1, 2),
       (2, 1),
       (2, 4),
       (3, 3),
       (3, 4),
       (4, 4),
       (4, 2);