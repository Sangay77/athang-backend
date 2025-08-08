package com.bfs.rma.payment.helper;

import com.bfs.rma.payment.model.TransactionMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MapBuilder {

    private static final Logger log = LoggerFactory.getLogger(MapBuilder.class);


    public Map<String, String> buildAuthMap(TransactionMaster txn) {
        Map<String, String> map = new HashMap<>();
        map.put("bfs_benfBankCode", txn.getBfs_benfBankCode());
        map.put("bfs_benfId", txn.getBfs_benfId());
        map.put("bfs_benfTxnTime", txn.getBfs_benfTxnTime());
        map.put("bfs_msgType", "AR");
        map.put("bfs_orderNo", txn.getBfs_orderNo());
        map.put("bfs_paymentDesc", txn.getBfs_paymentDesc());
        map.put("bfs_remitterEmail", txn.getBfs_remitterEmail());
        map.put("bfs_txnAmount", txn.getBfs_txnAmount());
        map.put("bfs_txnCurrency", txn.getBfs_txnCurrency());
        map.put("bfs_version", txn.getBfs_version());
        return map;
    }

    public Map<String, String> buildEnquiryMap(TransactionMaster txn) {
        Map<String, String> map = new HashMap<>();
        map.put("bfs_benfId", txn.getBfs_benfId());
        map.put("bfs_bfsTxnId", txn.getBfs_bfsTxnId());
        map.put("bfs_msgType", "AE");
        map.put("bfs_remitterAccNo", txn.getAccountNumber());
        map.put("bfs_remitterBankId", txn.getBfs_remitterBankId());
        return map;
    }

    public Map<String, String> buildDebitMap(TransactionMaster txn) {
        Map<String, String> map = new HashMap<>();
        map.put("bfs_benfId", txn.getBfs_benfId());
        map.put("bfs_bfsTxnId", txn.getBfs_bfsTxnId());
        map.put("bfs_msgType", "DR");
        map.put("bfs_remitterOtp", txn.getBfs_remitterOtp());
        return map;
    }

}
