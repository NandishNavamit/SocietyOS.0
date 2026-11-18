package com.database.societyproject.finance.service;

import com.database.societyproject.common.exception.*;
import com.database.societyproject.complaint.entity.*;
import com.database.societyproject.complaint.repository.ComplaintRepository;
import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.entity.*;
import com.database.societyproject.finance.repository.*;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import com.database.societyproject.security.service.FinanceOperationsAuthorizationService;
import com.database.societyproject.user.repository.UserRepository;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceChargeService {
    private final ServiceChargeRepository charges; private final ComplaintRepository complaints; private final ChargePlanRepository plans; private final MaintenanceBillRepository bills; private final NotificationService notifications; private final SocietyAuthorizationService authorization; private final UserRepository users; private final FinanceOperationsAuthorizationService financeAccess;
    public ServiceChargeService(ServiceChargeRepository c, ComplaintRepository co, ChargePlanRepository p, MaintenanceBillRepository b, NotificationService n, SocietyAuthorizationService a, UserRepository u, FinanceOperationsAuthorizationService f) { charges=c; complaints=co; plans=p; bills=b; notifications=n; authorization=a; users=u; financeAccess=f; }
    public ServiceChargeResponse issue(ServiceChargeRequest request) {
        Complaint complaint=complaints.findById(request.complaintId()).orElseThrow(()->new ResourceNotFoundException("Complaint",request.complaintId())); Long societyId=complaint.getSociety().getId(); financeAccess.requireOperator(societyId);
        if(complaint.getStatus()!=ComplaintStatus.RESOLVED&&complaint.getStatus()!=ComplaintStatus.CLOSED) throw new InvalidRequestException("A service charge can only be issued after the complaint is resolved or closed");
        if(complaint.getFlat()==null) throw new InvalidRequestException("A service charge requires a complaint linked to a flat");
        if(charges.existsByComplaintIdAndStatus(complaint.getId(),ServiceChargeStatus.ISSUED)) throw new DuplicateResourceException("An active service charge already exists for this complaint");
        if(request.dueDate().isBefore(LocalDate.now())) throw new InvalidRequestException("Due date cannot be in the past");
        ChargePlan plan=new ChargePlan(); plan.setSociety(complaint.getSociety()); plan.setName("Service charge: "+request.description()); plan.setAmount(request.amount()); plan.setFrequency(ChargeFrequency.ONE_TIME); plan.setDueDayOfMonth(request.dueDate().getDayOfMonth()); plan.setActive(false); plans.save(plan);
        MaintenanceBill bill=new MaintenanceBill(); bill.setChargePlan(plan); bill.setFlat(complaint.getFlat()); bill.setBillingPeriodStart(LocalDate.now()); bill.setBillingPeriodEnd(LocalDate.now()); bill.setDueDate(request.dueDate()); bill.setAmount(request.amount()); bill.setChargeDescription(request.description()); bills.save(bill);
        ServiceCharge charge=new ServiceCharge(); charge.setSociety(complaint.getSociety()); charge.setComplaint(complaint); charge.setBill(bill); charge.setDescription(request.description()); charge.setAmount(request.amount()); charge.setVendorName(request.vendorName()); charge.setInvoiceNumber(request.invoiceNumber()); charge.setInternalNote(request.internalNote()); charge.setCreatedBy(users.getReferenceById(authorization.currentUserId())); ServiceCharge saved=charges.save(charge); notifications.createMaintenanceDue(bill); return response(saved);
    }
    @Transactional(readOnly=true) public List<ServiceChargeResponse> bySociety(Long societyId) { financeAccess.requireOperator(societyId); return charges.findBySocietyIdOrderByCreatedAtDesc(societyId).stream().map(this::response).toList(); }
    public ServiceChargeResponse voidCharge(Long id) { ServiceCharge charge=charges.findById(id).orElseThrow(()->new ResourceNotFoundException("Service charge",id)); financeAccess.requireOperator(charge.getSociety().getId()); if(charge.getStatus()==ServiceChargeStatus.VOID) return response(charge); if(charge.getBill().getPaidAmount().signum()>0) throw new InvalidRequestException("A charge with payments cannot be voided; use a credit or refund process"); charge.setStatus(ServiceChargeStatus.VOID); charge.setVoidedAt(LocalDateTime.now()); charge.getBill().setStatus(BillStatus.VOID); return response(charge); }
    private ServiceChargeResponse response(ServiceCharge c) { MaintenanceBill b=c.getBill(); return new ServiceChargeResponse(c.getId(),c.getSociety().getId(),c.getComplaint().getId(),c.getComplaint().getTitle(),b.getId(),b.getFlat().getFlatNumber(),c.getDescription(),c.getAmount(),b.getDueDate(),c.getVendorName(),c.getInvoiceNumber(),c.getStatus(),c.getCreatedAt()); }
}
