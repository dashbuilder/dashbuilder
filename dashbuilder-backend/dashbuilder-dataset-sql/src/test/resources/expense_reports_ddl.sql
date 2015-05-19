CREATE TABLE expense_reports (
  id INTEGER NOT NULL,
  city VARCHAR(50),
  department VARCHAR(50),
  employee VARCHAR(50),
  date TIMESTAMP,
  amount NUMERIC(28,2),
  PRIMARY KEY(id)
);


INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (1, 'Barcelona', 'Engineering', 'Roxie Foraker', date '2015-12-11', 120.35);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (2, 'Barcelona', 'Engineering', 'Roxie Foraker', date '2015-12-01', 1100.1);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (3, 'Barcelona', 'Engineering', 'Roxie Foraker', date '2015-11-01', 900.1);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (4, 'Barcelona', 'Services', 'Jamie Gilbeau', date '2015-10-12', 340.34);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (5, 'Barcelona', 'Services', 'Jamie Gilbeau', date '2015-09-15', 300.0);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (6, 'Barcelona', 'Services', 'Jamie Gilbeau', date '2015-08-17', 152.25);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (7, 'Madrid', 'Services', 'Roxie Foraker', date '2015-07-01', 800.8);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (8, 'Madrid', 'Services', 'Roxie Foraker', date '2015-06-01', 911.11);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (9, 'Madrid', 'Sales', 'Nita Marling', date '2015-05-11', 75.75);      
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (10, 'Madrid', 'Sales', 'Nita Marling', date '2015-03-11', 100.0);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (11, 'Madrid', 'Sales', 'Nita Marling', date '2015-03-16', 220.8);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (12, 'Madrid', 'Sales', 'Nita Marling', date '2015-03-02', 344.9);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (13, 'Brno', 'Support', 'Darryl Innes', date '2015-02-09', 567.89);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (14, 'Brno', 'Support', 'Darryl Innes', date '2015-01-13', 400.4);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (15, 'Brno', 'Support', 'Darryl Innes', date '2015-01-11', 1001.9);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (16, 'Brno', 'Engineering', 'Julio Burdge', date '2014-11-02', 200.2);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (17, 'Brno', 'Engineering', 'Julio Burdge', date '2014-09-01', 159.01);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (18, 'Brno', 'Engineering', 'Julio Burdge', date '2014-08-22', 300.0);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (19, 'Brno', 'Engineering', 'Julio Burdge', date '2014-07-23', 800.24);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (20, 'Brno', 'Sales', 'Neva Hunger', date '2014-06-11', 995.3);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (21, 'Brno', 'Sales', 'Neva Hunger', date '2014-06-11', 234.3);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (22, 'Westford', 'Engineering', 'Kathrine Janas', date '2014-05-17', 233.49);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (23, 'Westford', 'Engineering', 'Kathrine Janas', date '2014-04-12', 1.1);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (24, 'Westford', 'Engineering', 'Kathrine Janas', date '2014-03-13', 1.402);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (25, 'Westford', 'Engineering', 'Kathrine Janas', date '2014-02-13', 490.1);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (26, 'Westford', 'Engineering', 'Kathrine Janas', date '2014-02-09', 600.34);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (27, 'Westford', 'Sales', 'Jerri Preble', date '2013-12-23', 899.03);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (28, 'Westford', 'Sales', 'Jerri Preble', date '2013-11-30', 343.45);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (29, 'Westford', 'Management', 'Donald M. Stanton', date '2013-10-29', 983.03);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (30, 'Westford', 'Management', 'Donald M. Stanton', date '2013-10-11', 43.03);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (31, 'Raleigh', 'Management', 'Hannah B. Mackey', date '2013-09-01', 234.34);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (32, 'Raleigh', 'Management', 'Hannah B. Mackey', date '2013-07-02', 543.45);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (33, 'Raleigh', 'Management', 'Hannah B. Mackey', date '2013-06-02', 193.45);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (34, 'Raleigh', 'Management', 'Loretta R. Havens', date '2013-05-03', 992.2);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (35, 'Raleigh', 'Management', 'Loretta R. Havens', date '2013-04-23', 494.4);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (36, 'Raleigh', 'Engineering', 'Tony L. Crawford', date '2013-02-18', 233.09);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (37, 'Raleigh', 'Engineering', 'Tony L. Crawford', date '2013-02-22', 293.49);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (38, 'Raleigh', 'Engineering', 'Tony L. Crawford', date '2012-12-23', 401.4);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (39, 'Raleigh', 'Engineering', 'Tony L. Crawford', date '2012-07-19', 209.55);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (40, 'Raleigh', 'Support', 'Eileen L. Pereira', date '2012-06-12', 300.01);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (41, 'Raleigh', 'Support', 'Eileen L. Pereira', date '2012-06-13', 450.6);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (42, 'Raleigh', 'Support', 'Eileen L. Pereira', date '2012-06-14', 320.9);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (43, 'Raleigh', 'Support', 'Eileen L. Pereira', date '2012-06-15', 303.9);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (44, 'London', 'Engineering', 'Alan P. Adamson', date '2012-06-12', 404.3);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (45, 'London', 'Engineering', 'Alan P. Adamson', date '2012-05-12', 868.45);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (46, 'London', 'Engineering', 'Alan P. Adamson', date '2012-05-13', 333.45);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (47, 'London', 'Management', 'Patricia J. Behr', date '2012-04-14', 565.56);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (48, 'London', 'Management', 'Patricia J. Behr', date '2012-03-02', 345.45);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (49, 'London', 'Management', 'Patricia J. Behr', date '2012-02-03', 700.66);
INSERT INTO expense_reports (id, city, department, employee, date, amount) VALUES (50, 'London', 'Management', 'Patricia J. Behr', date '2012-01-04', 921.9);