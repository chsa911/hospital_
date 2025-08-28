package com.example.hospital.grpc;

import com.example.hospital.entity.Bill;
import com.example.hospital.service.BillingService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;

@GrpcService @RequiredArgsConstructor
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    private final BillingService svc;

    @Override
    public void generateBill(BillRequest req, StreamObserver<BillDto> out) {
        Bill b = svc.generate(req.getPatientId(), req.getHospitalId());
        out.onNext(toDto(b)); out.onCompleted();
    }

    @Override
    public void listBillsForPatient(IdRequest req, StreamObserver<BillListResponse> out) {
        List<Bill> bills = svc.billsForPatient(req.getId());
        var b = BillListResponse.newBuilder();
        bills.forEach(x -> b.addBills(toDto(x)));
        out.onNext(b.build()); out.onCompleted();
    }

    @Override
    public void getOutstandingBalance(BillRequest req, StreamObserver<BillDto> out) {
        BigDecimal amount = svc.outstanding(req.getPatientId(), req.getHospitalId());
        out.onNext(BillDto.newBuilder()
                .setId(0).setPatientId(req.getPatientId()).setHospitalId(req.getHospitalId())
                .setAmount(amount.doubleValue()).setPaid(false).build());
        out.onCompleted();
    }

    private static BillDto toDto(Bill b) {
        return BillDto.newBuilder()
                .setId(b.getId())
                .setPatientId(b.getPatient().getId())
                .setHospitalId(b.getHospital().getId())
                .setAmount(b.getAmount() == null ? 0.0 : b.getAmount().doubleValue())
                .setPaid(Boolean.TRUE.equals(b.getPaid()))
                .build();
    }
}
