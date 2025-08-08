package com.bfs.rma.payment.service;

import com.bfs.rma.payment.dto.AuthoriseRequestDTO;
import com.bfs.rma.payment.helper.CallBFSApiHelper;
import com.bfs.rma.payment.helper.MapBuilder;
import com.bfs.rma.payment.helper.PGPKIImpl;
import com.bfs.rma.payment.helper.TxnHelper;
import com.bfs.rma.payment.model.TransactionMaster;
import com.bfs.rma.payment.model.TransactionResponse;
import com.bfs.rma.payment.repository.TransactionRepository;
import com.bfs.rma.payment.repository.TransactionResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthoriseService {

    private final TransactionRepository transactionRepo;
    private final TransactionResponseRepository transactionResponseRepo;
    private final PGPKIImpl pgpki;
    private final CallBFSApiHelper apiHelper;
    private final TxnHelper txnHelper;
    private final MapBuilder mapHelper;

    @Value("${beneficiary.benf_id}")
    private String beneficiaryId;

    public Map<String, String> authService(AuthoriseRequestDTO dto) throws Exception {
        log.info("Creating transaction");

        TransactionMaster txn = buildTransaction(dto);
        String sourceString = txnHelper.generateCheckSumString(mapHelper.buildAuthMap(txn));
        log.info("Authorization checksum source string: {}", sourceString);

        String requestChecksum = pgpki.signData(sourceString);
        txn.setBfs_checkSum(requestChecksum);
        log.info("Checksum generated: {}", requestChecksum);

        transactionRepo.save(txn);
        log.info("Authorization request saved");

        log.info("Calling RMA Endpoint");
        Map<String, String> responseMap = apiHelper.callBfsApi("AR", txn);

        saveTransactionResponse(txn, responseMap);
        updateTransactionWithTxnId(txn, responseMap);

        return txnHelper.verifySignatureAndReturnResponse(responseMap);
    }

    private TransactionMaster buildTransaction(AuthoriseRequestDTO dto) {
        TransactionMaster txn = new TransactionMaster();
        txn.setBfs_txnAmount(dto.getTxnAmount());
        txn.setBfs_orderNo("ATHANG" + System.currentTimeMillis());
        txn.setBfs_benfTxnTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        txn.setBfs_paymentDesc(dto.getBfs_paymentDesc());
        txn.setBfs_remitterEmail("stenzin@rma.org.bt");
        txn.setBfs_benfId(beneficiaryId);
        txn.setBfs_benfBankCode("01");
        txn.setBfs_txnCurrency("BTN");
        txn.setBfs_version("1.0");
        return txn;
    }

    private void saveTransactionResponse(TransactionMaster txn, Map<String, String> responseMap) {
        try {
            TransactionResponse response = new TransactionResponse();

            response.setTransaction(txn);
            response.setMsgType("AR");
            response.setBfs_response_type(responseMap.get("bfs_msgType"));
            response.setRawResponse(responseMap.toString());
            response.setBfs_responseCode(responseMap.get("bfs_responseCode"));

            String status = "00".equals(responseMap.getOrDefault("bfs_responseCode", "")) ? "success" : "failed";
            response.setStatus(status);

            transactionResponseRepo.save(response);
            log.info("Saved TransactionResponse with status: {}", status);
        } catch (Exception ex) {
            log.error("Failed to save TransactionResponse", ex);
        }
    }

    private void updateTransactionWithTxnId(TransactionMaster txn, Map<String, String> responseMap) {
        Optional.ofNullable(responseMap.get("bfs_bfsTxnId")).ifPresentOrElse(txnId -> {
            txn.setBfs_bfsTxnId(txnId);
            transactionRepo.save(txn);
            log.info("Updated txn with bfs_bfsTxnId: {}", txnId);
        }, () -> log.warn("bfs_bfsTxnId not found in response: {}", responseMap));
    }


}
