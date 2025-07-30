package com.bfs.rma.payment.controller;

import com.bfs.rma.payment.dto.DebitRequestDTO;
import com.bfs.rma.payment.service.DebitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/payment")
public class DebitController {

    private final DebitService debitService;

    private static final Logger logger = LoggerFactory.getLogger(DebitController.class);

    public DebitController(DebitService debitService) {
        this.debitService = debitService;
    }

    @PostMapping("/debit-request")
    public Map<String, String> debit(@RequestBody DebitRequestDTO debitRequestDTO) {
        logger.info("Debit Controller request thread: {}", Thread.currentThread().getName());
        return debitService.debit(debitRequestDTO);
    }

}
