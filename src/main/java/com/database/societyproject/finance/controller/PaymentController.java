package com.database.societyproject.finance.controller;
import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/payments") @PreAuthorize("denyAll()") public class PaymentController {
    private final PaymentService service; public PaymentController(PaymentService service) { this.service = service; }
    @PostMapping public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest r) { return ResponseEntity.status(201).body(service.create(r)); }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<PaymentResponse> all() { return service.findAll(); } @GetMapping("/{id}") public PaymentResponse one(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/bill/{billId}") public List<PaymentResponse> bill(@PathVariable Long billId) { return service.findByBillId(billId); }
    @PostMapping("/{id}/void") public PaymentResponse voidPayment(@PathVariable Long id) { return service.voidPayment(id); }
}
