package com.bfs.rma.payment.service;

import com.bfs.rma.payment.dto.DebitRequestDTO;
import com.bfs.rma.payment.helper.CallBFSApiHelper;
import com.bfs.rma.payment.helper.MapBuilder;
import com.bfs.rma.payment.helper.PGPKIImpl;
import com.bfs.rma.payment.helper.TxnHelper;
import com.bfs.rma.payment.model.TransactionMaster;
import com.bfs.rma.payment.model.TransactionResponse;
import com.bfs.rma.payment.repository.TransactionRepository;
import com.bfs.rma.payment.repository.TransactionResponseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebitService {

    private final TransactionRepository transactionRepository;
    private final TransactionResponseRepository transactionResponseRepository;
    private final PGPKIImpl pgpki;
    private final CallBFSApiHelper apiHelper;
    private final TxnHelper txnHelper;
    private final ObjectMapper objectMapper;
    private final MapBuilder mapHelper;


    public Map<String, String> debit(DebitRequestDTO dto) {
        String txnId = dto.getBfs_bfsTxnId();

        TransactionMaster transaction = transactionRepository.findByBfsTxnId(txnId)
                .orElse(null);

        if (transaction == null) {
            log.warn("Transaction not found for bfs_bfsTxnId: {}", txnId);
            return Map.of(
                    "status", "failed",
                    "message", "Transaction not found for ID: " + txnId,
                    "failedCode", "not_found"
            );
        }

        try {
            prepareTransactionForDebit(transaction, dto);

            log.info("Calling BFS API with DR message for txn ID {}", txnId);
            Map<String, String> responseMap = apiHelper.callBfsApi("DR", transaction);
            log.info("Received BFS response: {}", responseMap);

            transactionRepository.save(transaction);
            saveTransactionResponse(transaction, responseMap);

            return txnHelper.verifySignatureAndReturnResponse(responseMap);

        } catch (Exception e) {
            log.error("Error during debit processing for txn ID {}: {}", txnId, e.getMessage(), e);
            throw new RuntimeException("Debit transaction failed: " + e.getMessage(), e);
        }
    }

    private void prepareTransactionForDebit(TransactionMaster txn, DebitRequestDTO dto) throws Exception {
        txn.setBfs_remitterOtp(dto.getBfs_remitterOtp());

        Map<String, String> debitMap = mapHelper.buildDebitMap(txn);
        String source = txnHelper.generateCheckSumString(debitMap);
        String checksum = pgpki.signData(source);

        txn.setBfs_checkSum(checksum);
        log.debug("Prepared signed debit transaction: {}", txn);
    }

    private void saveTransactionResponse(TransactionMaster txn, Map<String, String> response) {
        try {
            String responseCode = response.getOrDefault("bfs_debitAuthCode", "unknown");

            TransactionResponse txnResponse = new TransactionResponse();
            txnResponse.setTransaction(txn);
            txnResponse.setMsgType("DR");
            txnResponse.setBfs_response_type(response.get("bfs_msgType"));
            txnResponse.setRawResponse(objectMapper.writeValueAsString(response)); // safer serialization
            txnResponse.setBfs_responseCode(responseCode);
            txnResponse.setStatus("00".equals(responseCode) ? "success" : "failed");

            transactionResponseRepository.save(txnResponse);
            log.info("Transaction response saved with status: {}", txnResponse.getStatus());

        } catch (Exception e) {
            log.error("Failed to save TransactionResponse", e);
        }
    }
}
