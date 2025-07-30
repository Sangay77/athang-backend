package com.bfs.rma.payment.controller;

import com.bfs.rma.payment.dto.AccountInquiryDTO;
import com.bfs.rma.payment.service.EnquiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class EnquiryController {

    private static final Logger logger = LoggerFactory.getLogger(EnquiryController.class);

    private final EnquiryService enquiryService;

    public EnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }


    @PostMapping("/account-inquiry")
    public Map<String, String> Enquiry(@RequestBody AccountInquiryDTO accountInquiryDTO) throws Exception {
        logger.info("Controller request thread: {}", Thread.currentThread().getName());
        return enquiryService.accountEnquiry(accountInquiryDTO);
    }
}
