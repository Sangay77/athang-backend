package com.bfs.rma.payment.service;

import com.bfs.rma.payment.dto.AccountInquiryDTO;
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
public class EnquiryService {

    private final TransactionRepository transactionRepository;
    private final TransactionResponseRepository transactionResponseRepository;
    private final PGPKIImpl pgpki;
    private final CallBFSApiHelper apiHelper;
    private final ObjectMapper objectMapper;
    private final TxnHelper txnHelper;
    private final MapBuilder mapHelper;


    public Map<String, String> accountEnquiry(AccountInquiryDTO dto) {
        String txnId = dto.getBfs_bfsTxnId();

        TransactionMaster txn = transactionRepository.findByBfsTxnId(txnId)
                .orElseThrow(() -> {
                    log.warn("No transaction found for txn ID: {}", txnId);
                    return new IllegalArgumentException("Transaction not found: " + txnId);
                });

        log.info("Transaction found: {}", txn);

        updateTransactionDetails(txn, dto);

        try {
            signTransaction(txn);

            log.info("Calling BFS API with AE message for txn ID: {}", txnId);
            Map<String, String> responseMap = apiHelper.callBfsApi("AE", txn);
            log.info("AE Response: {}", responseMap);

            saveTransactionResponse(txn, responseMap);

            return txnHelper.verifySignatureAndReturnResponse(responseMap);

        } catch (Exception ex) {
            log.error("Error during AE enquiry for txn ID {}: {}", txnId, ex.getMessage(), ex);
            throw new RuntimeException("Error during account enquiry", ex);
        }
    }

    private void updateTransactionDetails(TransactionMaster txn, AccountInquiryDTO dto) {
        txn.setAccountNumber(dto.getBfs_remitterAccNo());
        txn.setBfs_remitterBankId(dto.getBfs_remitterBankId());
    }

    private void signTransaction(TransactionMaster txn) throws Exception {
        Map<String, String> enquiryMap = mapHelper.buildEnquiryMap(txn);
        String source = txnHelper.generateCheckSumString(enquiryMap);
        String checksum = pgpki.signData(source);
        txn.setBfs_checkSum(checksum);
        transactionRepository.save(txn);
        log.debug("Signed and saved transaction with checksum.");
    }

    private void saveTransactionResponse(TransactionMaster txn, Map<String, String> response) {
        try {
            TransactionResponse txnResponse = new TransactionResponse();
            txnResponse.setTransaction(txn);
            txnResponse.setMsgType("AE");
            txnResponse.setBfs_response_type(response.get("bfs_msgType"));
            txnResponse.setRawResponse(objectMapper.writeValueAsString(response));

            String responseCode = response.getOrDefault("bfs_responseCode", "unknown");
            txnResponse.setBfs_responseCode(responseCode);
            txnResponse.setStatus("00".equals(responseCode) ? "success" : "failed");

            transactionResponseRepository.save(txnResponse);
            log.info("TransactionResponse saved with status: {}", txnResponse.getStatus());
        } catch (Exception e) {
            log.error("Failed to save TransactionResponse", e);
        }
    }
}
