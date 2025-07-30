package com.bfs.rma.auth.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController {

    @GetMapping
    public String authorise(HttpServletRequest request) {

        return "Hi" + request.getSession().getId();
    }

    @GetMapping("/csrf-token")
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @PostMapping
    public Map<String, String> home() {
        return Map.of("message", "BYE");
    }
}
