package com.database.societyproject.finance.service;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.finance.dto.MaintenanceBillRequest;
import com.database.societyproject.finance.repository.MaintenanceBillRepository;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.flat.service.FlatService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
class MaintenanceBillServiceTest {
    private final MaintenanceBillRepository repository = mock(MaintenanceBillRepository.class); private final ChargePlanService planService = mock(ChargePlanService.class); private final FlatService flatService = mock(FlatService.class); private final FlatRepository flatRepository = mock(FlatRepository.class); private final MaintenanceBillService service = new MaintenanceBillService(repository, planService, flatService, flatRepository, mock(com.database.societyproject.security.service.FinanceOperationsAuthorizationService.class));
    @Test void invalidBillingPeriodIsRejected() {
        MaintenanceBillRequest request = new MaintenanceBillRequest(1L, 2L, LocalDate.now(), LocalDate.now().minusDays(1), LocalDate.now(), BigDecimal.ONE);
        assertThrows(InvalidRequestException.class, () -> service.create(request)); verifyNoInteractions(repository, planService, flatService);
    }
}
