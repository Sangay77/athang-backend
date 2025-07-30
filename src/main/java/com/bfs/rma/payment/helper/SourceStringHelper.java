package com.bfs.rma.payment.helper;

import com.bfs.rma.payment.model.TransactionMaster;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SourceStringHelper {


    public String constructAuthorizationSourceString(TransactionMaster transactionMaster) {

        Map<String, String> authMap = new HashMap();

        authMap.put("bfs_benfBankCode", transactionMaster.getBfs_benfBankCode());
        authMap.put("bfs_txnCurrency", transactionMaster.getBfs_txnCurrency());
        authMap.put("bfs_version", transactionMaster.getBfs_version());
        authMap.put("bfs_paymentDesc", transactionMaster.getBfs_paymentDesc());
        authMap.put("bfs_benfId", transactionMaster.getBfs_benfId());
        authMap.put("bfs_msgType", "AR");
        authMap.put("bfs_orderNo", transactionMaster.getBfs_orderNo());
        authMap.put("bfs_benfTxnTime", transactionMaster.getBfs_benfTxnTime());
        authMap.put("bfs_txnAmount", transactionMaster.getBfs_txnAmount());
        authMap.put("bfs_remitterEmail", transactionMaster.getBfs_remitterEmail());
        return sortbykey(authMap);
    }

    public String constructEnquirySourceString(TransactionMaster transactionMaster) {

        Map<String, String> authMap = new HashMap();

        authMap.put("bfs_benfId", transactionMaster.getBfs_benfId());
        authMap.put("bfs_msgType", "AE");
        authMap.put("bfs_bfsTxnId", transactionMaster.getBfs_bfsTxnId());
        authMap.put("bfs_remitterAccNo", transactionMaster.getAccountNumber());
        authMap.put("bfs_remitterBankId", transactionMaster.getBfs_remitterBankId());
        return sortbykey(authMap);
    }

    public String constructDebitSourceString(TransactionMaster transactionMaster) {

        Map<String, String> authMap = new HashMap();

        authMap.put("bfs_benfId", transactionMaster.getBfs_benfId());
        authMap.put("bfs_msgType", "DR");
        authMap.put("bfs_bfsTxnId", transactionMaster.getBfs_bfsTxnId());
        authMap.put("bfs_remitterOtp", transactionMaster.getBfs_remitterOtp());
        return sortbykey(authMap);
    }

    public String sortbykey(Map<String, String> map) {

        StringBuilder sourceString = new StringBuilder();
        map.keySet().stream().sorted().forEach(key -> {
            String value = map.get(key);
            sourceString.append(value).append("|");
        });

        return String.valueOf(sourceString.deleteCharAt(sourceString.length() - 1));
    }
}
