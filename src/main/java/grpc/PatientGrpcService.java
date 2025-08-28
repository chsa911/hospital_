package com.example.hospital.grpc;

import com.example.hospital.entity.Patient;
import com.example.hospital.service.PatientService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService @RequiredArgsConstructor
public class PatientGrpcService extends PatientServiceGrpc.PatientServiceImplBase {

    private final PatientService svc;

    @Override
    public void createPatient(CreatePatientRequest req, StreamObserver<PatientDto> out) {
        Patient p = svc.create(req.getFirstName(), req.getLastName());
        out.onNext(toDto(p)); out.onCompleted();
    }

    @Override
    public void updatePatient(UpdatePatientRequest req, StreamObserver<PatientDto> out) {
        Patient p = svc.update(req.getId(), req.getFirstName(), req.getLastName());
        out.onNext(toDto(p)); out.onCompleted();
    }

    @Override
    public void deletePatient(IdRequest req, StreamObserver<Empty> out) {
        svc.delete(req.getId()); out.onNext(Empty.getDefaultInstance()); out.onCompleted();
    }

    private static PatientDto toDto(Patient p) {
        return PatientDto.newBuilder()
                .setId(p.getId()).setFirstName(p.getFirstName() == null ? "" : p.getFirstName())
                .setLastName(p.getLastName() == null ? "" : p.getLastName())
                .build();
    }
}
