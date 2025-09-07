package com.example.hospital.grpc;

import com.example.hospital.entity.Bill;
import com.example.hospital.service.BillingService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    private final BillingService svc;

    @Override
    public void generateBill(BillRequest req, StreamObserver<BillDto> out) {
        try {
            Bill b = svc.generate(req.getPatientId(), req.getHospitalId());
            out.onNext(toDto(b));
            out.onCompleted();
        } catch (NoSuchElementException e) {
            out.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            out.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to generate bill").asRuntimeException());
        }
    }

    @Override
    public void listBillsForPatient(IdRequest req, StreamObserver<BillListResponse> out) {
        try {
            List<Bill> bills = Optional.ofNullable(svc.billsForPatient(req.getId())).orElseGet(List::of);
            var b = BillListResponse.newBuilder();
            bills.forEach(x -> b.addBills(toDto(x)));
            out.onNext(b.build());
            out.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to list bills").asRuntimeException());
        }
    }

    @Override
    public void getOutstandingBalance(BillRequest req, StreamObserver<BillDto> out) {
        try {
            BigDecimal amount = svc.outstanding(req.getPatientId(), req.getHospitalId());
            double amt = amount == null ? 0.0 : amount.doubleValue();
            out.onNext(BillDto.newBuilder()
                    .setId(0)
                    .setPatientId(req.getPatientId())
                    .setHospitalId(req.getHospitalId())
                    .setAmount(amt)
                    .setPaid(false)
                    .build());
            out.onCompleted();
        } catch (NoSuchElementException e) {
            out.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to get outstanding balance").asRuntimeException());
        }
    }

    private static BillDto toDto(Bill b) {
        // Prefer scalar ids if your entity exposes them (recommended to avoid lazy loads)
        Long pid = null, hid = null;
        try {
            // If your Bill has getPatientId()/getHospitalId(), use those.
            var m = Bill.class.getMethods();
            for (var mm : m) {
                if (mm.getName().equals("getPatientId") && mm.getParameterCount() == 0) {
                    pid = (Long) mm.invoke(b);
                } else if (mm.getName().equals("getHospitalId") && mm.getParameterCount() == 0) {
                    hid = (Long) mm.invoke(b);
                }
            }
        } catch (Exception ignore) {}

        if (pid == null && b.getPatient() != null) {
            // fall back to relation if present/initialized
            pid = b.getPatient().getId();
        }
        if (hid == null && b.getHospital() != null) {
            hid = b.getHospital().getId();
        }

        long idSafe  = nzLong(b.getId());
        long pidSafe = nzLong(pid);
        long hidSafe = nzLong(hid);

        double amount = 0.0;
        BigDecimal amt = b.getAmount();
        if (amt != null) amount = amt.doubleValue();

        boolean paid = Boolean.TRUE.equals(b.getPaid());

        return BillDto.newBuilder()
                .setId(idSafe)
                .setPatientId(pidSafe)
                .setHospitalId(hidSafe)
                .setAmount(amount)
                .setPaid(paid)
                .build();
    }

    private static long nzLong(Long v) { return v == null ? 0L : v; }
}
