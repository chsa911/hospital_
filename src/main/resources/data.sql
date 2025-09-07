-- data.sql : Demo data for Hospital Service

-- Hospitals
INSERT INTO hospital (id, name, address) VALUES (1, 'City Hospital', '123 Main Street');
INSERT INTO hospital (id, name, address) VALUES (2, 'General Hospital', '456 Oak Avenue');
-- after the seed INSERTs for id 1 and 2
ALTER TABLE hospital ALTER COLUMN id RESTART WITH 3;

-- Patients
INSERT INTO patient (id, first_name, last_name) VALUES (1, 'Alice', 'Meyer');
INSERT INTO patient (id, first_name, last_name) VALUES (2, 'Bob', 'Jones');
ALTER TABLE patient ALTER COLUMN id RESTART WITH 3;

-- Patient registrations
INSERT INTO patient_hospital (hospital_id, patient_id) VALUES (1, 1);
INSERT INTO patient_hospital (hospital_id, patient_id) VALUES (1, 2);
INSERT INTO patient_hospital (hospital_id, patient_id) VALUES (2, 1);

-- Stays
INSERT INTO stay (id, patient_id, hospital_id, start_date, end_date, cancelled)
VALUES (1, 1, 1, '2024-01-10', '2024-01-20', false);
INSERT INTO stay (id, patient_id, hospital_id, start_date, end_date, cancelled)
VALUES (2, 1, 1, '2024-02-01', '2024-02-05', false);
INSERT INTO stay (id, patient_id, hospital_id, start_date, end_date, cancelled)
VALUES (3, 2, 1, '2024-03-01', '2024-03-03', true);
ALTER TABLE stay ALTER COLUMN id RESTART WITH 4;
-- Bills
INSERT INTO bill (id, patient_id, hospital_id, amount, paid)
VALUES (1, 1, 1, 3200.00, false);
INSERT INTO bill (id, patient_id, hospital_id, amount, paid)
VALUES (2, 2, 1, 600.00, true);
ALTER TABLE bill ALTER COLUMN id RESTART WITH 3;