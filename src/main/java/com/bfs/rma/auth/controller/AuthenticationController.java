package com.bfs.rma.auth.controller;

import com.bfs.rma.auth.dto.AuthenticationRequest;
import com.bfs.rma.auth.dto.AuthenticationResponse;
import com.bfs.rma.auth.service.AuthenticationService;
import com.bfs.rma.auth.dto.RegisterRequest;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
        @RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, String>> registerUser(
            @RequestPart("request") @Valid RegisterRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        try {
            request.setPhoto(photo);
            authenticationService.register(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful. Check your email for verification.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {

        return ResponseEntity.ok(authenticationService.authenticate(request));
    }



    @GetMapping("/activate-account")
    public ResponseEntity<Map<String, String>> confirm(@RequestParam String token) throws MessagingException {
        authenticationService.activateAccount(token);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Account activated successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test")
    public Map<String, String> testapi() {
        return Map.of("message", "connected successfully");
    }

}
