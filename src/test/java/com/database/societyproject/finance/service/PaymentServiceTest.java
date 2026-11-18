package com.database.societyproject.finance.service;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import com.database.societyproject.finance.entity.*;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.finance.dto.PaymentRequest;
import com.database.societyproject.finance.repository.PaymentRepository;
import com.database.societyproject.notification.service.NotificationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
class PaymentServiceTest {
    private final PaymentRepository repository = mock(PaymentRepository.class); private final MaintenanceBillService billService = mock(MaintenanceBillService.class); private final PaymentService service = new PaymentService(repository, billService, mock(NotificationService.class), mock(com.database.societyproject.security.service.FinanceOperationsAuthorizationService.class));
    @Test void overpaymentIsRejected() {
        MaintenanceBill bill = new MaintenanceBill(); bill.setAmount(new BigDecimal("100.00")); bill.setPaidAmount(new BigDecimal("50.00")); when(billService.findEntity(1L)).thenReturn(bill);
        PaymentRequest request = new PaymentRequest(1L, new BigDecimal("51.00"), LocalDate.now(), PaymentMethod.CASH, null, null);
        assertThrows(InvalidRequestException.class, () -> service.create(request)); verifyNoInteractions(repository);
    }
}
