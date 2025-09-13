package com.example.hospital.grpc;

import com.example.hospital.entity.Stay;
import com.example.hospital.service.StayService;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.google.type.Money;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class StayGrpcService extends StayServiceGrpc.StayServiceImplBase {

    private final StayService svc;
    private static final BigDecimal DAILY_RATE = BigDecimal.valueOf(200.0); // example fixed rate

    // --- RPCs ---

    @Override
    public void createStay(CreateStayRequest req, StreamObserver<StayDto> out) {
        try {
            LocalDate start = req.hasAdmitAt() ? toLocalDate(req.getAdmitAt()) : null;
            LocalDate end   = req.hasDischargeAt() ? toLocalDate(req.getDischargeAt()) : null;

            if (start != null && end != null && end.isBefore(start)) {
                out.onError(io.grpc.Status.INVALID_ARGUMENT
                        .withDescription("discharge date cannot be before admit date")
                        .asRuntimeException());
                return;
            }

            Stay s = svc.create(req.getPatientId(), req.getHospitalId(), start, end);
            out.onNext(toDto(s));
            out.onCompleted();
        } catch (Exception e) {
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to create stay").asRuntimeException());
        }
    }

    @Override
    public void cancelStay(CancelStayRequest req, StreamObserver<StayDto> out) {
        try {
            Stay s = svc.cancel(req.getId());  // you can pass req.getCancelReason() into svc if you wired it
            out.onNext(toDto(s));
            out.onCompleted();
        } catch (java.util.NoSuchElementException e) {
            out.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Stay " + req.getId() + " not found")
                    .asRuntimeException());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            out.onError(io.grpc.Status.FAILED_PRECONDITION
                    .withDescription("Cannot cancel stay: " + e.getMostSpecificCause().getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            out.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to cancel stay")
                    .asRuntimeException());
        }
    }


    @Override
    public void listStays(StayListRequest req, StreamObserver<StayListResponse> out) {
        try {
            List<Stay> stays = svc.listByPatient(req.getPatientId());
            StayListResponse.Builder b = StayListResponse.newBuilder();
            stays.forEach(s -> b.addStays(toDto(s)));
            out.onNext(b.build());
            out.onCompleted();
        } catch (Exception e) {
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to list stays").asRuntimeException());
        }
    }

    @Override
    public void getQuarterSummary(QuarterRequest req, StreamObserver<QuarterSummary> out) {
        try {
            LocalDate qStart = quarterStart(req.getYear(), req.getQuarter());
            LocalDate qEnd   = quarterEnd(req.getYear(), req.getQuarter());

            List<Stay> stays = svc.overlapping(req.getPatientId(), qStart, qEnd);

            long totalDays = stays.stream()
                    .filter(s -> s.getStartDate() != null && s.getEndDate() != null)
                    .mapToLong(s -> ChronoUnit.DAYS.between(s.getStartDate(), s.getEndDate()) + 1)
                    .sum();

            BigDecimal billed = DAILY_RATE.multiply(BigDecimal.valueOf(totalDays));

            QuarterSummary resp = QuarterSummary.newBuilder()
                    .setTotalBillableDays(totalDays)
                    .setBilledAmount(toMoney("EUR", billed))
                    .build();

            out.onNext(resp);
            out.onCompleted();
        } catch (IllegalArgumentException iae) {
            out.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(iae.getMessage()).asRuntimeException());
        } catch (Exception e) {
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to compute quarter summary").asRuntimeException());
        }
    }

    // --- Mapping helpers ---

    private static StayDto toDto(Stay s) {
        StayDto.Builder b = StayDto.newBuilder()
                .setId(s.getId())
                .setPatientId(s.getPatient().getId())
                .setHospitalId(s.getHospital().getId());

        if (s.getStartDate() != null) {
            b.setAdmitAt(toTs(s.getStartDate()));
        }
        if (s.getEndDate() != null) {
            b.setDischargeAt(toTs(s.getEndDate()));
        }

        StayStatus status =
                (s.getStartDate() == null && s.getEndDate() == null) ? StayStatus.STAY_STATUS_CANCELLED
                        : (s.getEndDate() == null)                             ? StayStatus.STAY_STATUS_ONGOING
                        : StayStatus.STAY_STATUS_COMPLETED;
        b.setStatus(status);

        return b.build(); // <-- missing before
    } // <-- and this closing brace was missing

    private static LocalDate toLocalDate(Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos())
                .atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Timestamp toTs(LocalDate d) {
        return Timestamps.fromMillis(d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    private static Money toMoney(String currency, BigDecimal amount) {
        BigDecimal abs = amount.abs();
        BigDecimal unitsBd = abs.setScale(0, RoundingMode.DOWN);
        BigDecimal nanosBd = abs.subtract(unitsBd).movePointRight(9).setScale(0, RoundingMode.HALF_UP);

        long units = unitsBd.longValueExact();
        int nanos = nanosBd.intValueExact();
        if (nanos == 1_000_000_000) { units += 1; nanos = 0; }
        if (amount.signum() < 0) units = -units;

        return Money.newBuilder()
                .setCurrencyCode(currency)
                .setUnits(units)
                .setNanos(nanos)
                .build();
    }

    private static LocalDate quarterStart(int y, int q) {
        return switch (q) {
            case 1 -> LocalDate.of(y, 1, 1);
            case 2 -> LocalDate.of(y, 4, 1);
            case 3 -> LocalDate.of(y, 7, 1);
            case 4 -> LocalDate.of(y, 10, 1);
            default -> throw new IllegalArgumentException("Quarter must be 1..4");
        };
    }

    private static LocalDate quarterEnd(int y, int q) {
        return switch (q) {
            case 1 -> LocalDate.of(y, 3, 31);
            case 2 -> LocalDate.of(y, 6, 30);
            case 3 -> LocalDate.of(y, 9, 30);
            case 4 -> LocalDate.of(y, 12, 31);
            default -> throw new IllegalArgumentException("Quarter must be 1..4");
        };
    }
}
