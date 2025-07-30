package com.bfs.rma.payment.service;

import com.bfs.rma.payment.dto.AccountInquiryDTO;
import com.bfs.rma.payment.helper.CallBFSApiHelper;
import com.bfs.rma.payment.helper.PGPKIImpl;
import com.bfs.rma.payment.helper.SourceStringHelper;
import com.bfs.rma.payment.model.TransactionMaster;
import com.bfs.rma.payment.model.TransactionResponse;
import com.bfs.rma.payment.repository.TransactionRepository;
import com.bfs.rma.payment.repository.TransactionResponseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class EnquiryService {

    private static final Logger logger = LoggerFactory.getLogger(EnquiryService.class);

    private final SourceStringHelper sourceStringHelper;
    private final TransactionRepository transactionRepository;
    private final TransactionResponseRepository transactionResponseRepository;
    private final PGPKIImpl pgpki;
    private final CallBFSApiHelper apiHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EnquiryService(SourceStringHelper sourceStringHelper,
                          TransactionRepository transactionRepository,
                          TransactionResponseRepository transactionResponseRepository,
                          PGPKIImpl pgpki,
                          CallBFSApiHelper apiHelper) {
        this.sourceStringHelper = sourceStringHelper;
        this.transactionRepository = transactionRepository;
        this.transactionResponseRepository = transactionResponseRepository;
        this.pgpki = pgpki;
        this.apiHelper = apiHelper;
    }

    public Map<String, String> accountEnquiry(AccountInquiryDTO dto) {
        String txnId = dto.getBfs_bfsTxnId();

        Optional<TransactionMaster> optionalTxn = transactionRepository.findByBfsTxnId(txnId);
        if (optionalTxn.isEmpty()) {
            logger.warn("No transaction found for txn ID: {}", txnId);
            return Map.of("error", "No transaction found for transaction ID: " + txnId);
        }

        TransactionMaster txn = optionalTxn.get();
        logger.info("Transaction found: {}", txn);

        txn.setAccountNumber(dto.getBfs_remitterAccNo());
        txn.setBfs_remitterBankId(dto.getBfs_remitterBankId());

        try {
            String source = sourceStringHelper.constructEnquirySourceString(txn);
            String checksum = pgpki.signData(source);
            txn.setBfs_checkSum(checksum);
            transactionRepository.save(txn);

            logger.info("Calling BFS API with AE message for txn ID {}", txnId);
            Map<String, String> response = apiHelper.callBfsApi("AE", txn);
            logger.info("AE Response: {}", response);

            // Save to transaction_responses
            TransactionResponse txnResponse = new TransactionResponse();
            txnResponse.setTransaction(txn);
            txnResponse.setMsgType("AE");
            txnResponse.setBfs_response_type(response.get("bfs_msgType"));
            txnResponse.setRawResponse(objectMapper.writeValueAsString(response));
            String responseCode = response.getOrDefault("bfs_responseCode", "unknown");
            txnResponse.setBfs_responseCode(responseCode);
            txnResponse.setStatus("00".equals(responseCode) ? "success" : "failed");

            transactionResponseRepository.save(txnResponse);

            return response;

        } catch (Exception e) {
            logger.error("Error during AE enquiry for txn ID {}: {}", txnId, e.getMessage(), e);
            throw new RuntimeException("Error during account enquiry", e);
        }
    }
}
