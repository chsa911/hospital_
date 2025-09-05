# gRPC API

Generated from `src/main/proto/hospital.proto`.

## Services

### PatientService
- `CreatePatient(CreatePatientRequest) returns (PatientDto)`
- `UpdatePatient(UpdatePatientRequest) returns (PatientDto)`
- `DeletePatient(IdRequest) returns (Empty)`

### HospitalService
- `CreateHospital(CreateHospitalRequest) returns (HospitalDto)`
- `UpdateHospital(UpdateHospitalRequest) returns (HospitalDto)`
- `DeleteHospital(IdRequest) returns (Empty)`
- `ListHospitals(Empty) returns (HospitalListResponse)`
- `RegisterPatient(RegisterPatientRequest) returns (Empty)`
- `ListPatientsOfHospital(IdRequest) returns (PatientListResponse)`
- `ListHospitalsOfPatient(IdRequest) returns (HospitalListResponse)`

### StayService
- `CreateStay(StayRequest) returns (StayDto)`
- `CancelStay(IdRequest) returns (StayDto)`
- `ListStaysOfPatient(IdRequest) returns (StayListResponse)`
- `GetQuarterSummary(QuarterRequest) returns (QuarterSummary)`

### BillingService
- `GenerateBill(BillRequest) returns (BillDto)`
- `ListBillsForPatient(IdRequest) returns (BillListResponse)`
- `GetOutstandingBalance(BillRequest) returns (BillDto)`

## Example grpcurl
```bash
grpcurl -plaintext -d '{"patientId":1,"hospitalId":1}'   localhost:9090 hospital.BillingService/GetOutstandingBalance
```
