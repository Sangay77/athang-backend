package com.bfs.rma.payment.service;

import com.bfs.rma.payment.dto.DebitRequestDTO;
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
public class DebitService {

    private static final Logger logger = LoggerFactory.getLogger(DebitService.class);

    private final SourceStringHelper sourceStringHelper;
    private final TransactionRepository transactionRepository;
    private final TransactionResponseRepository transactionResponseRepository;
    private final PGPKIImpl pgpki;
    private final CallBFSApiHelper apiHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DebitService(SourceStringHelper sourceStringHelper,
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

    public Map<String, String> debit(DebitRequestDTO debitRequestDTO) {
        String bfsTxnId = debitRequestDTO.getBfs_bfsTxnId();

        Optional<TransactionMaster> optionalTransaction = transactionRepository.findByBfsTxnId(bfsTxnId);
        if (optionalTransaction.isEmpty()) {
            logger.warn("Transaction not found for bfs_bfsTxnId: {}", bfsTxnId);
            return Map.of(
                    "status", "failed",
                    "message", "Transaction not found for ID: " + bfsTxnId,
                    "failedCode", "not_found"
            );
        }

        TransactionMaster transaction = optionalTransaction.get();
        logger.info("Transaction loaded for debit: {}", transaction);

        try {
            transaction.setBfs_remitterOtp(debitRequestDTO.getBfs_remitterOtp());

            String sourceString = sourceStringHelper.constructDebitSourceString(transaction);
            String checksum = pgpki.signData(sourceString);
            transaction.setBfs_checkSum(checksum);

            logger.info("Prepared DebitRequest: {}", transaction);

            Map<String, String> responseMap = apiHelper.callBfsApi("DR", transaction);
            logger.info("Received BFS response: {}", responseMap);

            // Save updated transaction
            transactionRepository.save(transaction);

            // Save to transaction_responses table
            TransactionResponse txnResponse = new TransactionResponse();
            txnResponse.setTransaction(transaction);
            txnResponse.setMsgType("DR");
            txnResponse.setRawResponse(objectMapper.writeValueAsString(responseMap));
            txnResponse.setBfs_response_type(responseMap.get("bfs_msgType"));
            String authCode = responseMap.getOrDefault("bfs_debitAuthCode", "unknown");
            txnResponse.setBfs_responseCode(authCode);
            txnResponse.setStatus("00".equals(authCode) ? "success" : "failed");

            transactionResponseRepository.save(txnResponse);

            // Return response with status
            responseMap.put("status", "00".equals(authCode) ? "success" : "failed");
            if (!"00".equals(authCode)) {
                responseMap.put("failedCode", authCode);
            }

            return responseMap;

        } catch (Exception ex) {
            logger.error("Error during debit processing for txn ID {}: {}", bfsTxnId, ex.getMessage(), ex);
            throw new RuntimeException("Debit transaction failed: " + ex.getMessage(), ex);
        }
    }
}
