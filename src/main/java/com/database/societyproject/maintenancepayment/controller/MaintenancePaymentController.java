package com.database.societyproject.maintenancepayment.controller;

import com.database.societyproject.maintenancepayment.dto.*;
import com.database.societyproject.maintenancepayment.service.MaintenancePaymentService;
import jakarta.validation.Valid; import java.util.*; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/maintenance-payments")
public class MaintenancePaymentController {
 private final MaintenancePaymentService service; public MaintenancePaymentController(MaintenancePaymentService service){this.service=service;}
 @PostMapping("/online/orders") @PreAuthorize("hasRole('FLAT_OWNER')") public ResponseEntity<OnlineOrderResponse> createOnlineOrder(@Valid @RequestBody OnlineOrderRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.createOnlineOrder(request));}
 @PostMapping("/online/verify") @PreAuthorize("hasRole('FLAT_OWNER')") public MaintenancePaymentResponse verify(@Valid @RequestBody OnlineVerificationRequest request){return service.verify(request);}
 @PostMapping(value="/razorpay/webhook",consumes=MediaType.APPLICATION_JSON_VALUE) public ResponseEntity<Void> webhook(@RequestHeader("X-Razorpay-Signature") String signature,@RequestBody String body){service.webhook(body,signature);return ResponseEntity.ok().build();}
 @PostMapping("/offline") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public ResponseEntity<MaintenancePaymentResponse> offline(@Valid @RequestBody OfflinePaymentRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.recordOffline(request));}
 @GetMapping("/societies/{societyId}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public List<MaintenancePaymentResponse> bySociety(@PathVariable Long societyId){return service.bySociety(societyId);}
 @GetMapping("/mine") @PreAuthorize("hasRole('FLAT_OWNER')") public List<MaintenancePaymentResponse> mine(){return service.mine();}
}
