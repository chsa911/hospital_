-- schema.sql : DDL for Hospital Service (H2)

CREATE TABLE IF NOT EXISTS hospital (
  id        BIGINT PRIMARY KEY,
  name      VARCHAR(255) NOT NULL,
  address   VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS patient (
  id          BIGINT PRIMARY KEY,
  first_name  VARCHAR(255) NOT NULL,
  last_name   VARCHAR(255) NOT NULL
);

-- Join table for the many-to-many between patient and hospital
-- (column order matches your INSERTs: hospital_id, patient_id)
CREATE TABLE IF NOT EXISTS patient_hospital (
  hospital_id BIGINT NOT NULL,
  patient_id  BIGINT NOT NULL,
  PRIMARY KEY (hospital_id, patient_id),
  CONSTRAINT fk_ph_hospital FOREIGN KEY (hospital_id) REFERENCES hospital(id),
  CONSTRAINT fk_ph_patient  FOREIGN KEY (patient_id)  REFERENCES patient(id)
);

CREATE TABLE IF NOT EXISTS stay (
  id          BIGINT PRIMARY KEY,
  patient_id  BIGINT NOT NULL,
  hospital_id BIGINT NOT NULL,
  start_date  DATE   NOT NULL,
  end_date    DATE,
  cancelled   BOOLEAN NOT NULL,
  CONSTRAINT fk_stay_patient  FOREIGN KEY (patient_id)  REFERENCES patient(id),
  CONSTRAINT fk_stay_hospital FOREIGN KEY (hospital_id) REFERENCES hospital(id)
);

CREATE TABLE IF NOT EXISTS bill (
  id          BIGINT PRIMARY KEY,
  patient_id  BIGINT NOT NULL,
  hospital_id BIGINT NOT NULL,
  amount      DECIMAL(12,2) NOT NULL,
  paid        BOOLEAN NOT NULL,
  CONSTRAINT fk_bill_patient  FOREIGN KEY (patient_id)  REFERENCES patient(id),
  CONSTRAINT fk_bill_hospital FOREIGN KEY (hospital_id) REFERENCES hospital(id)
);
