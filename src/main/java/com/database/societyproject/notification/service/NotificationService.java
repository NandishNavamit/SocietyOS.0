package com.database.societyproject.notification.service;

import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.complaint.entity.Complaint;
import com.database.societyproject.finance.entity.MaintenanceBill;
import com.database.societyproject.maintenancepolicy.entity.MaintenancePolicyRequest;
import com.database.societyproject.maintenancebilling.entity.MonthlyMaintenanceBill;
import com.database.societyproject.maintenancepayment.entity.MaintenancePayment;
import com.database.societyproject.generalcomplaint.entity.GeneralComplaint;
import com.database.societyproject.notification.dto.*;
import com.database.societyproject.notification.entity.*;
import com.database.societyproject.notification.repository.NotificationRepository;
import com.database.societyproject.occupancy.entity.FlatOccupancy;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.user.service.UserService;
import com.database.societyproject.visitor.entity.VisitorVisit;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {
    private final NotificationRepository repository;
    private final SocietyService societyService;
    private final UserService userService;
    private final FlatOccupancyRepository occupancyRepository;
    private final EmailDeliveryService emailDelivery;
    private final UserSocietyRoleRepository societyRoles;
    private final SocietyAuthorizationService authorization;

    public NotificationService(NotificationRepository repository, SocietyService societyService,
                               UserService userService, FlatOccupancyRepository occupancyRepository, EmailDeliveryService emailDelivery,
                               UserSocietyRoleRepository societyRoles, SocietyAuthorizationService authorization) {
        this.repository = repository;
        this.societyService = societyService;
        this.userService = userService;
        this.occupancyRepository = occupancyRepository;
        this.emailDelivery = emailDelivery;
        this.societyRoles = societyRoles;
        this.authorization = authorization;
    }

    public NotificationResponse create(NotificationRequest request) {
        authorization.requireManager(request.societyId());
        Society society = societyService.findEntity(request.societyId());
        User recipient = userService.findEntity(request.recipientUserId());
        if (!societyRoles.existsByUserIdAndSocietyIdAndActiveTrue(recipient.getId(), society.getId())) {
            throw new com.database.societyproject.common.exception.InvalidRequestException("The recipient does not have active access to this society");
        }
        return toResponse(save(society, recipient, request.type(), request.title(), request.message()));
    }

    public List<NotificationResponse> createAnnouncement(AnnouncementRequest request) {
        authorization.requireManager(request.societyId());
        Society society = societyService.findEntity(request.societyId());
        return activeFlatOwnersInSociety(society.getId()).stream()
                .map(user -> save(society, user, NotificationType.ANNOUNCEMENT, request.title(), request.message()))
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> createMaintenanceDue(MaintenanceBill bill) {
        Society society = bill.getChargePlan().getSociety();
        String title = "Maintenance payment due";
        String message = "Maintenance bill #" + bill.getId() + " for flat " + bill.getFlat().getFlatNumber()
                + " is due on " + bill.getDueDate() + ".";
        return activeUsersInFlat(bill.getFlat().getId()).stream()
                .map(user -> save(society, user, NotificationType.MAINTENANCE_DUE, title, message))
                .map(this::toResponse)
                .toList();
    }

    /** A policy is not effective until Bootstrap Admin approval; this is the
     * mandatory acknowledgement notice promised to every active flat owner. */
    public List<NotificationResponse> notifyMaintenancePolicyApproved(MaintenancePolicyRequest policy) {
        Society society = policy.getSociety();
        String title = "Maintenance policy approved";
        String message = "Monthly maintenance is ₹" + policy.getMonthlyAmount().stripTrailingZeros().toPlainString()
                + ", due on day " + policy.getDueDayOfMonth() + " of each month, effective "
                + policy.getEffectiveMonth() + ". Late fee: ₹"
                + policy.getLateFeePerOverdueMonth().stripTrailingZeros().toPlainString() + " per overdue month.";
        return activeFlatOwnersInSociety(society.getId()).stream()
                .map(user -> save(society, user, NotificationType.MAINTENANCE_POLICY_CHANGED, title, message))
                .map(this::toResponse)
                .toList();
    }

    /** One notification is made only when a new monthly bill is generated. */
    public void notifyMonthlyBillGenerated(MonthlyMaintenanceBill bill) {
        String message = "Your maintenance bill for flat " + bill.getFlat().getFlatNumber() + " for "
                + bill.getBillingMonth() + " is ₹" + bill.getTotalAmount().stripTrailingZeros().toPlainString()
                + " and is due on " + bill.getDueDate() + ".";
        flatOwnersInFlat(bill.getFlat().getId()).forEach(owner -> save(bill.getSociety(), owner,
                NotificationType.MONTHLY_BILL_GENERATED, "Monthly maintenance bill generated", message));
    }

    public void notifyMaintenancePaymentCompleted(MaintenancePayment payment) {
        if (payment.getPayer() == null || !Boolean.TRUE.equals(payment.getPayer().getActive())) return;
        save(payment.getSociety(), payment.getPayer(), NotificationType.PAYMENT_CONFIRMED, "Payment received",
                "Payment of ₹" + payment.getAmount().stripTrailingZeros().toPlainString() + " was recorded on " + payment.getPaymentDate() + ".");
    }

    public void notifyGeneralComplaintCreated(GeneralComplaint complaint) {
        User owner = complaint.getRaisedByUser();
        if (Boolean.TRUE.equals(owner.getActive())) save(complaint.getSociety(), owner, NotificationType.COMPLAINT_UPDATE,
                "Complaint received", "Your complaint ‘" + complaint.getTitle() + "’ has been submitted.");
    }

    public void notifyGeneralComplaintUpdated(GeneralComplaint complaint) {
        User owner = complaint.getRaisedByUser();
        if (!Boolean.TRUE.equals(owner.getActive())) return;
        String suffix = complaint.getResolutionNote() == null ? "" : " Resolution: " + complaint.getResolutionNote();
        save(complaint.getSociety(), owner, NotificationType.COMPLAINT_UPDATE, "Complaint updated",
                "Your complaint ‘" + complaint.getTitle() + "’ is now " + complaint.getStatus().name().replace('_', ' ').toLowerCase() + "." + suffix);
    }

    public void notifyInvitationAccepted(com.database.societyproject.onboarding.entity.SocietyInvitation invitation) {
        User inviter = invitation.getInvitedBy();
        if (inviter != null && Boolean.TRUE.equals(inviter.getActive())) save(invitation.getSociety(), inviter,
                NotificationType.INVITATION_ACCEPTED, "Invitation accepted",
                invitation.getEmail() + " accepted the " + invitation.getInvitedRole().name().replace('_', ' ').toLowerCase() + " invitation.");
    }

    public void notifyPaymentConfirmed(MaintenanceBill bill, BigDecimal amount) {
        Society society = bill.getChargePlan().getSociety();
        String title = "Payment received";
        String message = "Payment of " + amount + " was recorded for maintenance bill #" + bill.getId() + ".";
        activeUsersInFlat(bill.getFlat().getId()).forEach(user ->
                save(society, user, NotificationType.PAYMENT_CONFIRMED, title, message));
    }

    public void notifyComplaintCreated(Complaint complaint) {
        notifyComplaint(complaint, "Complaint received", "Your complaint #" + complaint.getId() + " has been registered.");
    }

    public void notifyComplaintUpdated(Complaint complaint) {
        notifyComplaint(complaint, "Complaint updated", "Your complaint #" + complaint.getId()
                + " is now " + complaint.getStatus() + ".");
    }

    public void notifyVisitorCheckedIn(VisitorVisit visit) {
        if (visit.getHostResident() == null) return;
        User recipient = visit.getHostResident().getUser();
        if (!Boolean.TRUE.equals(recipient.getActive())) return;
        save(visit.getSociety(), recipient, NotificationType.VISITOR_ALERT, "Visitor checked in",
                visit.getVisitor().getFullName() + " has checked in for: " + visit.getPurpose());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findByUserId(Long userId, boolean unreadOnly) {
        userService.findEntity(userId);
        List<Notification> notifications = unreadOnly
                ? repository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                : repository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findBySocietyId(Long societyId) {
        authorization.requireManager(societyId);
        societyService.findEntity(societyId);
        return repository.findBySocietyIdOrderByCreatedAtDesc(societyId).stream().map(this::toResponse).toList();
    }

    public NotificationResponse markRead(Long id) {
        Notification notification = findEntity(id);
        if (!Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        return toResponse(notification);
    }

    public long markAllReadForCurrentUser() {
        Long userId = authorization.currentUserId();
        List<Notification> unread = repository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(notification -> { notification.setRead(true); notification.setReadAt(LocalDateTime.now()); });
        return unread.size();
    }

    private void notifyComplaint(Complaint complaint, String title, String message) {
        User recipient = complaint.getRaisedByResident().getUser();
        if (Boolean.TRUE.equals(recipient.getActive())) {
            save(complaint.getSociety(), recipient, NotificationType.COMPLAINT_UPDATE, title, message);
        }
    }

    private Notification save(Society society, User recipient, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setSociety(society);
        notification.setRecipientUser(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        Notification saved = repository.save(notification);
        emailDelivery.sendIfEnabled(recipient.getEmail(), "SocietyOS: " + title, message);
        return saved;
    }

    private List<User> activeUsersInFlat(Long flatId) {
        return activeUsers(occupancyRepository.findByFlatIdAndActiveTrue(flatId));
    }

    private List<User> activeUsersInSociety(Long societyId) {
        return activeUsers(occupancyRepository.findByFlatBlockSocietyIdAndActiveTrue(societyId));
    }

    private List<User> activeFlatOwnersInSociety(Long societyId) {
        return societyRoles.findBySocietyIdAndRoleAndActiveTrue(societyId, UserRole.FLAT_OWNER).stream()
                .map(role -> role.getUser())
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .toList();
    }

    private List<User> flatOwnersInFlat(Long flatId) {
        return occupancyRepository.findByFlatIdAndActiveTrue(flatId).stream()
                .filter(item -> item.getOccupancyType() == com.database.societyproject.occupancy.entity.OccupancyType.OWNER
                        && item.getResidentRole() == com.database.societyproject.occupancy.entity.ResidentRole.PRIMARY)
                .map(item -> item.getResident().getUser()).filter(user -> Boolean.TRUE.equals(user.getActive())).toList();
    }

    private List<User> activeUsers(List<FlatOccupancy> occupancies) {
        Map<Long, User> uniqueUsers = occupancies.stream()
                .map(FlatOccupancy::getResident)
                .filter(resident -> Boolean.TRUE.equals(resident.getActive()))
                .map(resident -> resident.getUser())
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .collect(Collectors.toMap(User::getId, Function.identity(), (first, ignored) -> first));
        return List.copyOf(uniqueUsers.values());
    }

    private Notification findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification", id));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getSociety().getId(),
                notification.getRecipientUser().getId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.getRead(), notification.getReadAt(), notification.getCreatedAt());
    }
}
