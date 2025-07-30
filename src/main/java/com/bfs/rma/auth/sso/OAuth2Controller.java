package com.bfs.rma.auth.sso;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/oauth2")
public class OAuth2Controller {

    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> getToken(HttpServletRequest request) {
        String token = (String) request.getSession().getAttribute("JWT_TOKEN");
        if (token != null) {
            request.getSession().removeAttribute("JWT_TOKEN");
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
