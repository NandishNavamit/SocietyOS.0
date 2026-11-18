package com.database.societyproject.finance.service;
import com.database.societyproject.finance.entity.*;
import com.database.societyproject.common.exception.*;
import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.repository.PaymentRepository;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.security.service.FinanceOperationsAuthorizationService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class PaymentService {
    private final PaymentRepository repository; private final MaintenanceBillService billService; private final NotificationService notificationService; private final FinanceOperationsAuthorizationService financeAccess;
    public PaymentService(PaymentRepository repository, MaintenanceBillService billService, NotificationService notificationService, FinanceOperationsAuthorizationService financeAccess) { this.repository = repository; this.billService = billService; this.notificationService = notificationService; this.financeAccess=financeAccess; }
    public PaymentResponse create(PaymentRequest r) { MaintenanceBill bill = billService.findEntity(r.billId()); BigDecimal outstanding = bill.getAmount().subtract(bill.getPaidAmount()); if (r.amount().compareTo(outstanding) > 0) throw new InvalidRequestException("Payment amount cannot exceed the outstanding bill amount"); financeAccess.requireOperator(bill.getFlat().getBlock().getSociety().getId()); Payment payment = new Payment(); payment.setBill(bill); payment.setAmount(r.amount()); payment.setPaymentDate(r.paymentDate()); payment.setPaymentMethod(r.paymentMethod()); payment.setReferenceNumber(r.referenceNumber()); payment.setNotes(r.notes()); Payment saved = repository.save(payment); refreshBill(bill); notificationService.notifyPaymentConfirmed(bill, saved.getAmount()); return toResponse(saved); }
    @Transactional(readOnly = true) public List<PaymentResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public PaymentResponse findById(Long id) { Payment payment=findEntity(id); financeAccess.requireOperator(payment.getBill().getFlat().getBlock().getSociety().getId()); return toResponse(payment); }
    @Transactional(readOnly = true) public List<PaymentResponse> findByBillId(Long id) { MaintenanceBill bill=billService.findEntity(id); financeAccess.requireOperator(bill.getFlat().getBlock().getSociety().getId()); return repository.findByBillId(id).stream().map(this::toResponse).toList(); }
    public PaymentResponse voidPayment(Long id) { Payment payment = findEntity(id); financeAccess.requireOperator(payment.getBill().getFlat().getBlock().getSociety().getId()); if (payment.getStatus() == PaymentStatus.VOID) return toResponse(payment); payment.setStatus(PaymentStatus.VOID); refreshBill(payment.getBill()); return toResponse(payment); }
    private Payment findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", id)); }
    private void refreshBill(MaintenanceBill bill) { billService.refreshStatus(bill, repository.totalByBillIdAndStatus(bill.getId(), PaymentStatus.COMPLETED)); }
    private PaymentResponse toResponse(Payment p) { return new PaymentResponse(p.getId(), p.getBill().getId(), p.getAmount(), p.getPaymentDate(), p.getPaymentMethod(), p.getReferenceNumber(), p.getNotes(), p.getStatus(), p.getCreatedAt()); }
}
