package com.bfs.rma.payment.controller;


import com.bfs.rma.payment.dto.AuthoriseRequestDTO;
import com.bfs.rma.payment.service.AuthoriseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class AuthorisationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorisationController.class);

    private final AuthoriseService authorisationService;

    public AuthorisationController(AuthoriseService authorisationService) {
        this.authorisationService = authorisationService;
    }

    @PostMapping("/authorise")
    public Map<String, String> authorise(@RequestBody AuthoriseRequestDTO request) throws Exception {
        logger.info("Controller request thread: {}", Thread.currentThread().getName());
        return authorisationService.authService(request);
    }
}

