package com.bfs.rma.payment.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TxnHelper {

    private static final Logger log = LoggerFactory.getLogger(TxnHelper.class);

    private final PGPKIImpl pgpki;

    public TxnHelper(PGPKIImpl pgpki) {
        this.pgpki = pgpki;
    }

    public Map<String, String> verifySignatureAndReturnResponse(Map<String, String> responseMap) throws Exception {
        String responseSignature = responseMap.get("bfs_checkSum");
        responseMap.remove("bfs_checkSum");
        String sourceString = generateCheckSumString(responseMap);
        log.info("Verifying signature for response: {}", sourceString);

        boolean isSignatureValid = pgpki.verifyData(sourceString, responseSignature);

        if (isSignatureValid) {
            log.info("Signature verification passed.");
            return responseMap;
        } else {
            log.warn("Signature verification failed.");
            return Collections.singletonMap("error", "invalid signature");
        }
    }

    public String generateCheckSumString(Map<String, String> hashMap) {
        StringBuilder checkSumStr = new StringBuilder();
        TreeMap<String, String> sortedMap = new TreeMap<>(hashMap);
        Iterator<String> it = sortedMap.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            checkSumStr.append(nullToEmpty((String) sortedMap.get(key)));
            if (it.hasNext()) {
                checkSumStr.append("|");
            }
        }

        return checkSumStr.toString();
    }


    public static String nullToEmpty(String str) {
        return str != null && !str.equals("null") && !str.equals("NULL") ? str : "";
    }

}
