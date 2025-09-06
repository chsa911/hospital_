# Hospital Service (Spring Boot + gRPC)

## What it does
- Manage hospitals & patients
- Register patients in hospitals
- Track stays (create, cancel, query)
- Create bills (days × fixed rate), list them, show outstanding balance
- Quarter summaries (total stay days, billed amount)

## Quickstart
```bash
./gradlew bootRun              # gRPC :9090, HTTP :8080 (H2 console)
# or custom ports:
./gradlew bootRun --args="--server.port=8081 --grpc.server.port=9090"
```

### H2 Console
http://localhost:8080/h2-console  
JDBC: `jdbc:h2:mem:hospitaldb`  
User: `sa` / Password: *(empty)*

## grpcurl examples
```bash
grpcurl -plaintext -d '{"firstName":"Alice","lastName":"Meyer"}'   localhost:9090 hospital.PatientService/CreatePatient

grpcurl -plaintext -d '{"name":"City Hospital","address":"Main Street 1"}'   localhost:9090 hospital.HospitalService/CreateHospital
```

## Run tests
```bash
./gradlew test
```
