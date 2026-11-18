package com.database.societyproject.security.controller;
import com.database.societyproject.security.dto.*;
import com.database.societyproject.security.service.BlockAuthorityService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/block-authorities") public class BlockAuthorityController {
 private final BlockAuthorityService service; public BlockAuthorityController(BlockAuthorityService service){this.service=service;}
 @PostMapping public ResponseEntity<UserBlockAssignmentResponse> assign(@Valid @RequestBody UserBlockAssignmentRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.assign(request));}
 @GetMapping("/user/{userId}") public List<UserBlockAssignmentResponse> byUser(@PathVariable Long userId){return service.byUser(userId);}
 @DeleteMapping("/{id}") public UserBlockAssignmentResponse deactivate(@PathVariable Long id){return service.deactivate(id);}
}
