package com.bfs.rma.payment;

import com.bfs.rma.payment.dto.AccountInquiryDTO;
import com.bfs.rma.payment.dto.AuthoriseRequestDTO;
import com.bfs.rma.payment.dto.DebitRequestDTO;
import com.bfs.rma.payment.service.AuthoriseService;
import com.bfs.rma.payment.service.DebitService;
import com.bfs.rma.payment.service.EnquiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final AuthoriseService authorisationService;
    private final DebitService debitService;
    private final EnquiryService enquiryService;

    public PaymentController(AuthoriseService authorisationService,
                             DebitService debitService,
                             EnquiryService enquiryService) {
        this.authorisationService = authorisationService;
        this.debitService = debitService;
        this.enquiryService = enquiryService;
    }

    @PostMapping("/authorise-request")
    public Map<String, String> authorise(@RequestBody AuthoriseRequestDTO request) throws Exception {
        logger.info("Authorise request thread: {}", Thread.currentThread().getName());
        return authorisationService.authService(request);
    }

    @PostMapping("/account-inquiry")
    public Map<String, String> enquiry(@RequestBody AccountInquiryDTO accountInquiryDTO) throws Exception {
        logger.info("Account inquiry request thread: {}", Thread.currentThread().getName());
        return enquiryService.accountEnquiry(accountInquiryDTO);
    }

    @PostMapping("/debit-request")
    public Map<String, String> debit(@RequestBody DebitRequestDTO debitRequestDTO) {
        logger.info("Debit request thread: {}", Thread.currentThread().getName());
        return debitService.debit(debitRequestDTO);
    }
}
