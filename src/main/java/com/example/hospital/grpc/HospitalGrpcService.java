package com.example.hospital.grpc;

import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.service.HospitalService;
import com.example.hospital.grpc.ListHospitalsRequest;
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
        try {
            String name    = req.hasName()    ? req.getName().getValue()    : null;
            String address = req.hasAddress() ? req.getAddress().getValue() : null;
            // adapt to your service signature (accepts nulls to mean "no change")
            Hospital saved = svc.update(req.getId(), name, address);
            out.onNext(toDto(saved));
            out.onCompleted();
        } catch (Exception e) {
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to update hospital").asRuntimeException());
        }
    }
    @Override
    public void deleteHospital(IdRequest req, StreamObserver<Empty> out) {
        svc.delete(req.getId()); out.onNext(Empty.getDefaultInstance()); out.onCompleted();
    }
    @Override
    public void listHospitals(ListHospitalsRequest req, StreamObserver<HospitalListResponse> out) {
        // You can ignore filters for now, or read them:
        // boolean activeOnly = req.hasActiveOnly() && req.getActiveOnly().getValue();
        // String  search     = req.hasSearch() ? req.getSearch().getValue() : null;

        List<Hospital> list = svc.list();
        HospitalListResponse.Builder b = HospitalListResponse.newBuilder();
        list.forEach(h -> b.addHospitals(toDto(h)));
        // If you support pagination later, set page.next_page_token here.
        out.onNext(b.build());
        out.onCompleted();
    }

    @Override
    public void registerPatient(RegisterPatientRequest req, StreamObserver<RegistrationDto> out) {
        try {
            svc.registerPatient(req.getPatientId(), req.getHospitalId()); // your domain call
            RegistrationDto dto = RegistrationDto.newBuilder()
                    .setId(0) // set real registration id if you have it
                    .setPatientId(req.getPatientId())
                    .setHospitalId(req.getHospitalId())
                    .setStatus(RegistrationStatus.REG_STATUS_ACTIVE)
                    .build();
            out.onNext(dto);
            out.onCompleted();
        } catch (Exception e) {
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to register patient").asRuntimeException());
        }
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
