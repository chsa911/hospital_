# Hospital System – Architecture

## Overview

This project implements a simple **Hospital Information System** using:

- **Spring Boot 3.x**
- **gRPC** (net.devh spring-boot starter) on port **9090**
- **Spring Data JPA + Hibernate**
- **H2 in-memory database**
- **Lombok** for boilerplate reduction

It manages:
- **Hospitals**
- **Patients** (a patient can be registered in **multiple hospitals**)
- **Stays** (a patient’s stay in a hospital)
- **Bills** (derived from stays: total stay duration × daily rate)

---

## Entities (Domain Model)

Below is the domain model (ER diagram).
> Deleting a **Hospital** must **not** delete **Patients**: the many-to-many does **not** cascade REMOVE.  
> Dependent entities (e.g., **Stay**, **Bill**) can be configured to cascade from **Hospital** as needed.

```mermaid
erDiagram
  HOSPITAL ||--o{ PATIENT_HOSPITAL : has
  PATIENT  ||--o{ PATIENT_HOSPITAL : registered_in
  PATIENT  ||--o{ STAY : has
  HOSPITAL ||--o{ STAY : hosts
  PATIENT  ||--o{ BILL : charged
  HOSPITAL ||--o{ BILL : issues

  HOSPITAL {
    long id PK
    string name
    string address
  }

  PATIENT {
    long id PK
    string firstName
    string lastName
  }

  STAY {
    long id PK
    date startDate
    date endDate
    bool cancelled
    long patientId FK
    long hospitalId FK
  }

  BILL {
    long id PK
    decimal amount
    bool paid
    long patientId FK
    long hospitalId FK
  }
