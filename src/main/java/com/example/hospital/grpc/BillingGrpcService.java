package com.example.hospital.grpc;

import com.example.hospital.entity.Bill;
import com.example.hospital.service.BillingService;
import com.google.type.Money;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    private final BillingService svc;

    // ---- RPCs ----

    @Override
    public void generateBill(GenerateBillRequest req, StreamObserver<BillDto> out) {
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
    public void listBillsForPatient(ListBillsRequest req, StreamObserver<BillListResponse> out) {
        try {
            long patientId = req.getPatientId();
            List<Bill> bills = Optional.ofNullable(svc.billsForPatient(patientId)).orElseGet(List::of);

            BillListResponse.Builder resp = BillListResponse.newBuilder();
            bills.forEach(b -> resp.addBills(toDto(b)));

            out.onNext(resp.build());
            out.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to list bills").asRuntimeException());
        }
    }

    @Override
    public void getOutstandingBalance(BalanceRequest req, StreamObserver<BalanceResponse> out) {
        try {
            BigDecimal amount = svc.outstanding(req.getPatientId(), req.getHospitalId());
            BalanceResponse resp = BalanceResponse.newBuilder()
                    .setOutstandingBalance(toMoney(defaultCurrency(), nz(amount)))
                    .build();
            out.onNext(resp);
            out.onCompleted();
        } catch (NoSuchElementException e) {
            out.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
            out.onError(io.grpc.Status.INTERNAL.withDescription("Failed to get outstanding balance").asRuntimeException());
        }
    }

    // ---- Mapping helpers ----

    private BillDto toDto(Bill b) {
        long idSafe  = b.getId() == null ? 0L : b.getId();
        long pidSafe = (b.getPatient()  != null && b.getPatient().getId()  != null) ? b.getPatient().getId()  : 0L;
        long hidSafe = (b.getHospital() != null && b.getHospital().getId() != null) ? b.getHospital().getId() : 0L;

        BigDecimal total = nz(b.getAmount());
        boolean paidFlag = Boolean.TRUE.equals(b.getPaid());
        BigDecimal amountPaid = paidFlag ? total : BigDecimal.ZERO;
        BigDecimal outstanding = paidFlag ? BigDecimal.ZERO : total;

        return BillDto.newBuilder()
                .setId(idSafe)
                .setPatientId(pidSafe)
                .setHospitalId(hidSafe)
                // if you don't split tax, treat subtotal = total
                .setSubtotalAmount(toMoney(defaultCurrency(), total))
                .setTaxAmount(toMoney(defaultCurrency(), BigDecimal.ZERO))
                .setTotalAmount(toMoney(defaultCurrency(), total))
                .setAmountPaid(toMoney(defaultCurrency(), amountPaid))
                .setOutstandingAmount(toMoney(defaultCurrency(), outstanding))
                .build();
    }

    private static long nz(Long v) { return v == null ? 0L : v; }
    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private static String defaultCurrency() {
        // TODO: ideally fetch from hospital currency; EUR as a safe default
        return "EUR";
    }

    /** Convert decimal to google.type.Money (units + nanos). */
    private static Money toMoney(String currency, BigDecimal amount) {
        // Handle negatives correctly
        BigDecimal abs = amount.abs();
        BigDecimal unitsBd = abs.setScale(0, RoundingMode.DOWN);
        BigDecimal nanosBd = abs.subtract(unitsBd).movePointRight(9).setScale(0, RoundingMode.HALF_UP);

        long units = unitsBd.longValueExact();
        int nanos = nanosBd.intValueExact();
        if (nanos == 1_000_000_000) { units += 1; nanos = 0; }

        // apply sign
        if (amount.signum() < 0) units = -units;

        return Money.newBuilder()
                .setCurrencyCode(currency)
                .setUnits(units)
                .setNanos(nanos)
                .build();
    }
}
