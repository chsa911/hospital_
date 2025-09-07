# Hospital Service (Spring Boot + gRPC)

## What it does
- Manage hospitals & patients
- Register patients in hospitals
- Track stays (create, cancel, query)
- Create bills (days × fixed rate), list them, show outstanding balance
- Quarter summaries (total stay days, billed amount)

## Prerequisites
- Java 17+
- Gradle (wrapper included: `./gradlew`)
- [grpcurl](https://github.com/fullstorydev/grpcurl) for manual testing

## Quickstart
```bash
H2 Console

http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:hospitaldb
User: sa
Password: (empty)
The database is preloaded with demo data from data.sql:
Hospitals: City Hospital, General Hospital
Patients: Alice Meyer, Bob Jones
grpcurl Examples
to be run in git bash:

./gradlew bootRun              # gRPC :9090, HTTP :8080 (H2 console)
# or custom ports:
./gradlew bootRun --args="--server.port=8081 --grpc.server.port=9091"

# Create a patient
grpcurl -plaintext -d '{"firstName":"Alice","lastName":"Meyer"}' \
  localhost:9090 hospital.PatientService/CreatePatient

# List patients
grpcurl -plaintext -d '{}' \
  localhost:9090 hospital.PatientService/ListPatients

# Create a hospital
grpcurl -plaintext -d '{"name":"City Hospital","address":"Main Street 1"}' \
  localhost:9090 hospital.HospitalService/CreateHospital

# List hospitals
grpcurl -plaintext -d '{}' \
  localhost:9090 hospital.HospitalService/ListHospitals

# Register patient 1 in hospital 1
grpcurl -plaintext -d '{"hospitalId":1,"patientId":1}' \
  localhost:9090 hospital.HospitalService/RegisterPatient

# Create a stay
grpcurl -plaintext -d '{"patientId":1,"hospitalId":1,"startDate":"2024-01-01","endDate":"2024-01-10"}' \
  localhost:9090 hospital.StayService/CreateStay

# Cancel a stay
grpcurl -plaintext -d '{"stayId":1}' \
  localhost:9090 hospital.StayService/CancelStay

# List stays of a patient
grpcurl -plaintext -d '{"patientId":1}' \
  localhost:9090 hospital.StayService/ListStaysOfPatient
# Create a bill
grpcurl -plaintext -d '{"patientId":1,"hospitalId":1,"amount":500,"paid":false}' \
  localhost:9090 hospital.BillingService/CreateBill

# List bills
grpcurl -plaintext -d '{}' \
  localhost:9090 hospital.BillingService/ListBills

# Show outstanding balance
grpcurl -plaintext -d '{"patientId":1}' \
  localhost:9090 hospital.BillingService/GetOutstandingBalance

erDiagram
    HOSPITAL ||--o{ PATIENT_HOSPITAL : "registers"
    PATIENT ||--o{ PATIENT_HOSPITAL : "registered_in"
    PATIENT ||--o{ STAY : "has"
    HOSPITAL ||--o{ STAY : "hosts"
    PATIENT ||--o{ BILL : "receives"
    HOSPITAL ||--o{ BILL : "issues"

    HOSPITAL {
      bigint id PK
      string name
      string address
    }
    PATIENT {
      bigint id PK
      string first_name
      string last_name
    }
    PATIENT_HOSPITAL {
      bigint hospital_id FK
      bigint patient_id FK
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
Project Structure

src/main/proto – gRPC service definitions (.proto files)

src/main/java/com/example/hospital/entity – JPA entities

src/main/java/com/example/hospital/grpc – gRPC service implementations

src/main/resources/application.yml – Spring Boot configuration

src/main/resources/schema.sql / data.sql – DB schema & demo data

Run Tests

./gradlew test

Notes

Default logging is INFO; increase in application.yml if needed.
Unhandled application errors in gRPC calls are returned as UNKNOWN by default.
Add @GrpcAdvice mappers to translate common exceptions into clearer gRPC statuses (e.g., ALREADY_EXISTS, INVALID_ARGUMENT).