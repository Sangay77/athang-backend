package com.bfs.rma.payment.service;

import com.bfs.rma.payment.dto.AuthoriseRequestDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
public class AuthoriseService {

    private static final Logger logger = LoggerFactory.getLogger(AuthoriseService.class);

    private final TransactionRepository transactionRepo;
    private final TransactionResponseRepository transactionResponseRepo;
    private final SourceStringHelper sourceStringHelper;
    private final PGPKIImpl pgpki;
    private final CallBFSApiHelper apiHelper;

    @Value("${beneficiary.benf_id}")
    private String benf_id;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthoriseService(TransactionRepository transactionRepo,
                            TransactionResponseRepository transactionResponseRepo,
                            SourceStringHelper sourceStringHelper,
                            PGPKIImpl pgpki,
                            CallBFSApiHelper apiHelper) {
        this.transactionRepo = transactionRepo;
        this.transactionResponseRepo = transactionResponseRepo;
        this.sourceStringHelper = sourceStringHelper;
        this.pgpki = pgpki;
        this.apiHelper = apiHelper;
    }

    public Map<String, String> authService(AuthoriseRequestDTO dto) throws Exception {
        TransactionMaster txn = new TransactionMaster();
        txn.setBfs_txnAmount(dto.getTxnAmount());
        txn.setBfs_orderNo("NOKO" + System.currentTimeMillis());
        txn.setBfs_benfTxnTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        txn.setBfs_paymentDesc(dto.getBfs_paymentDesc());
        txn.setBfs_remitterEmail("stenzin@rma.org.bt");
        txn.setBfs_benfId(benf_id);
        txn.setBfs_benfBankCode("01");
        txn.setBfs_txnCurrency("BTN");
        txn.setBfs_version("1.0");

        String sourceString = sourceStringHelper.constructAuthorizationSourceString(txn);
        String checksum = pgpki.signData(sourceString);
        txn.setBfs_checkSum(checksum);

        logger.info("AuthoriseRequest Set: {}", txn);
        transactionRepo.save(txn);

        Map<String, String> responseMap = apiHelper.callBfsApi("AR", txn);

        // Save response in transaction_responses
        try {
            String responseJson = objectMapper.writeValueAsString(responseMap);

            TransactionResponse response = new TransactionResponse();
            response.setTransaction(txn);
            response.setMsgType("AR");
            response.setBfs_response_type(responseMap.get("bfs_msgType"));
            response.setRawResponse(responseJson);
            response.setBfs_responseCode(responseMap.get("bfs_responseCode"));

            String code = responseMap.getOrDefault("bfs_responseCode", "unknown");
            response.setStatus("00".equals(code) ? "success" : "failed");

            transactionResponseRepo.save(response);
            logger.info("TransactionResponse saved with status: {}", response.getStatus());
        } catch (Exception e) {
            logger.error("Failed to save TransactionResponse", e);
        }

        // Update transaction with BFS txn ID if present
        String txnId = responseMap.get("bfs_bfsTxnId");
        if (txnId != null) {
            txn.setBfs_bfsTxnId(txnId);
            transactionRepo.save(txn);
            logger.info("Transaction updated with bfs_bfsTxnId: {}", txnId);
        } else {
            logger.warn("bfs_bfsTxnId not found in response: {}", responseMap);
        }

        return responseMap;
    }
}
