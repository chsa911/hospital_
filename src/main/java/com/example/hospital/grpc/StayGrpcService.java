package com.example.hospital.grpc;

import com.example.hospital.entity.Stay;
import com.example.hospital.service.StayService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.List;

@GrpcService @RequiredArgsConstructor
public class StayGrpcService extends StayServiceGrpc.StayServiceImplBase {

    private final StayService svc;
    private static final double DAILY_RATE = 200.0;

    @Override
    public void createStay(StayRequest req, StreamObserver<StayDto> out) {
        Stay s = svc.create(req.getPatientId(), req.getHospitalId(),
                LocalDate.parse(req.getStartDate()), LocalDate.parse(req.getEndDate()));
        out.onNext(toDto(s)); out.onCompleted();
    }

    @Override
    public void cancelStay(IdRequest req, StreamObserver<StayDto> out) {
        Stay s = svc.cancel(req.getId());
        out.onNext(toDto(s)); out.onCompleted();
    }

    @Override
    public void listStaysOfPatient(IdRequest req, StreamObserver<StayListResponse> out) {
        List<Stay> stays = svc.listByPatient(req.getId());
        var b = StayListResponse.newBuilder();
        stays.forEach(s -> b.addStays(toDto(s)));
        out.onNext(b.build()); out.onCompleted();
    }

    @Override
    public void getQuarterSummary(QuarterRequest req, StreamObserver<QuarterSummary> out) {
        var start = quarterStart(req.getYear(), req.getQuarter());
        var end   = quarterEnd(req.getYear(), req.getQuarter());
        var stays = svc.overlapping(req.getPatientId(), start, end);

        long totalDays = stays.stream()
                .filter(s -> !s.isCancelled())
                .mapToLong(s -> java.time.temporal.ChronoUnit.DAYS.between(s.getStartDate(), s.getEndDate()) + 1)
                .sum();
        double billed = totalDays * DAILY_RATE;

        out.onNext(QuarterSummary.newBuilder().setTotalDays(totalDays).setBilledAmount(billed).build());
        out.onCompleted();
    }

    private static StayDto toDto(Stay s) {
        return StayDto.newBuilder()
                .setId(s.getId()).setPatientId(s.getPatient().getId()).setHospitalId(s.getHospital().getId())
                .setStartDate(s.getStartDate().toString()).setEndDate(s.getEndDate().toString())
                .setCancelled(s.isCancelled())
                .build();
    }

    private static java.time.LocalDate quarterStart(int y, int q) {
        return switch (q) { case 1->java.time.LocalDate.of(y,1,1); case 2->java.time.LocalDate.of(y,4,1);
            case 3->java.time.LocalDate.of(y,7,1); case 4->java.time.LocalDate.of(y,10,1);
            default->throw new IllegalArgumentException("Quarter 1..4"); };
    }
    private static java.time.LocalDate quarterEnd(int y, int q) {
        return switch (q) { case 1->java.time.LocalDate.of(y,3,31); case 2->java.time.LocalDate.of(y,6,30);
            case 3->java.time.LocalDate.of(y,9,30); case 4->java.time.LocalDate.of(y,12,31);
            default->throw new IllegalArgumentException("Quarter 1..4"); };
    }
}
