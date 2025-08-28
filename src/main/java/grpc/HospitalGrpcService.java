package com.example.hospital.grpc;

import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.service.HospitalService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService @RequiredArgsConstructor
public class HospitalGrpcService extends HospitalServiceGrpc.HospitalServiceImplBase {

    private final HospitalService svc;

    @Override
    public void createHospital(CreateHospitalRequest req, StreamObserver<HospitalDto> out) {
        Hospital saved = svc.create(req.getName(), req.getAddress());
        out.onNext(toDto(saved)); out.onCompleted();
    }

    @Override
    public void updateHospital(UpdateHospitalRequest req, StreamObserver<HospitalDto> out) {
        Hospital saved = svc.update(req.getId(), req.getName(), req.getAddress());
        out.onNext(toDto(saved)); out.onCompleted();
    }

    @Override
    public void deleteHospital(IdRequest req, StreamObserver<Empty> out) {
        svc.delete(req.getId()); out.onNext(Empty.getDefaultInstance()); out.onCompleted();
    }

    @Override
    public void listHospitals(Empty req, StreamObserver<HospitalListResponse> out) {
        List<Hospital> list = svc.list();
        var b = HospitalListResponse.newBuilder();
        list.forEach(h -> b.addHospitals(toDto(h)));
        out.onNext(b.build()); out.onCompleted();
    }

    @Override
    public void registerPatient(RegisterPatientRequest req, StreamObserver<Empty> out) {
        svc.registerPatient(req.getPatientId(), req.getHospitalId());
        out.onNext(Empty.getDefaultInstance()); out.onCompleted();
    }

    @Override
    public void listPatientsOfHospital(IdRequest req, StreamObserver<PatientListResponse> out) {
        List<Patient> pts = svc.patientsOfHospital(req.getId());
        var b = PatientListResponse.newBuilder();
        pts.forEach(p -> b.addPatients(PatientDto.newBuilder()
                .setId(p.getId()).setFirstName(p.getFirstName()).setLastName(p.getLastName()).build()));
        out.onNext(b.build()); out.onCompleted();
    }

    @Override
    public void listHospitalsOfPatient(IdRequest req, StreamObserver<HospitalListResponse> out) {
        List<Hospital> hs = svc.hospitalsOfPatient(req.getId());
        var b = HospitalListResponse.newBuilder();
        hs.forEach(h -> b.addHospitals(toDto(h)));
        out.onNext(b.build()); out.onCompleted();
    }

    private static HospitalDto toDto(Hospital h) {
        return HospitalDto.newBuilder()
                .setId(h.getId()).setName(h.getName() == null ? "" : h.getName())
                .setAddress(h.getAddress() == null ? "" : h.getAddress())
                .build();
    }
}
