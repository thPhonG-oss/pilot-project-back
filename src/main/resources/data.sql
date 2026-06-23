INSERT INTO EMPLOYEE (VISA, FIRST_NAME, LAST_NAME, BIRTH_DATE)
VALUES ('QMV', 'Quy', 'Van', '1990-02-03'),
       ('NQN', 'Quan', 'Nguyen', '1999-12-03'),
       ('HNH', 'Hanh', 'Ho', '1992-05-12'),
       ('TDN', 'Nhan', 'Tran', '1994-06-17'),
       ('TPG', 'A', 'Nguyen Van', '1999-01-01'),
       ('NPQ', 'B', 'Nguyen Van', '1991-12-02'),
       ('LMT', 'Linh', 'Mai', '1988-03-15'),
       ('PVT', 'Phuong', 'Vo', '1993-07-22'),
       ('DKT', 'Duc', 'Kim', '1990-11-08'),
       ('SHN', 'Son', 'Hoang', '1995-04-18')
;

INSERT INTO GROUPS (GROUP_LEADER_ID)
VALUES (1),
       (2),
       (3),
       (4),
       (5);

INSERT INTO PROJECT (PROJECT_NUMBER, NAME, CUSTOMER, STATUS, START_DATE, END_DATE, GROUP_ID)
VALUES
    ('3116', 'Facturation/Encaissements', 'Les Rataites Populaires', 'NEW', '2004-02-25', null, 1),
    ('3118', 'GKBWEB', 'GKB', 'FIN', '2002-10-10', '2003-10-10', 1),
    ('7157', 'MGBAHM-Maint2015', 'MGB Tourism', 'INP', '2006-09-24', null, 2),
    ('7174', 'SOMED-SPITEX MAINT', 'SOMED-SPITEX MAINT', 'NEW', '2015-10-05', null, 2),
    ('7199', 'ARCHIMEDE-2015-3.14', 'Les Retaites Populaires', 'NEW', '2005-05-29', null, 3),
    ('8000', 'EmpowerHR', 'MCM retails', 'NEW', '2009-10-12', null, 3),
    ('8001', 'TAC English', 'Dol', 'NEW', '2019-04-30', null, 3),
    ('4201', 'Insurance Portal', 'Swiss Life', 'PLA', '2024-01-15', '2025-12-31', 1),
    ('4205', 'Mobile Banking', 'UBS', 'INP', '2023-06-01', null, 2),
    ('4210', 'Legacy Migration', 'PostFinance', 'FIN', '2020-03-10', '2022-08-20', 3),
    ('4215', 'Customer Portal Refresh', 'AXA', 'NEW', '2025-01-20', null, 4),
    ('4220', 'Data Warehouse', 'Migros', 'PLA', '2025-03-01', null, 4),
    ('4225', 'API Gateway', 'SBB', 'INP', '2024-09-01', null, 5),
    ('4230', 'HR Self-Service', 'Nestle', 'NEW', '2025-06-01', null, 5)
;

INSERT INTO PROJECT_EMPLOYEE(PROJECT_ID, EMPLOYEE_ID)
VALUES (1, 1),
       (1, 2),
       (2, 1),
       (2, 4),
       (3, 3),
       (3, 4),
       (4, 4),
       (4, 2),
       (5, 3),
       (5, 5),
       (6, 5),
       (6, 6),
       (7, 2),
       (7, 6),
       (8, 1),
       (8, 7),
       (9, 3),
       (9, 8),
       (10, 4),
       (10, 9),
       (11, 4),
       (11, 10),
       (12, 5),
       (12, 7),
       (13, 5),
       (13, 8),
       (14, 6),
       (14, 9),
       (14, 10);
