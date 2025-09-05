# Data Model

## Entities
- **Hospital**: id, name, address, Set<Patient>
- **Patient**: id, firstName, lastName, Set<Hospital>
- **Stay**: id, patient_id, hospital_id, startDate, endDate, cancelled
- **Bill**: id, patient_id, hospital_id, amount, paid

## Relationships
- Hospital ↔ Patient: Many-to-Many (join table `patient_hospital`)
- Patient ↔ Stay: One-to-Many
- Patient ↔ Bill: One-to-Many

## ER Diagram (Mermaid)
```mermaid
erDiagram
  HOSPITAL ||--o{ PATIENT_HOSPITAL : has
  PATIENT ||--o{ PATIENT_HOSPITAL : has
  PATIENT ||--o{ STAY : books
  HOSPITAL ||--o{ STAY : hosts
  PATIENT ||--o{ BILL : charged
  HOSPITAL ||--o{ BILL : issues

  HOSPITAL {
    bigint id PK
    string name
    string address
  }
  PATIENT {
    bigint id PK
    string firstName
    string lastName
  }
  STAY {
    bigint id PK
    bigint patient_id FK
    bigint hospital_id FK
    date start_date
    date end_date
    boolean cancelled
  }
  BILL {
    bigint id PK
    bigint patient_id FK
    bigint hospital_id FK
    decimal amount
    boolean paid
  }
```
