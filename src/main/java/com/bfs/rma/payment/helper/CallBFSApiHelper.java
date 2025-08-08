package com.bfs.rma.payment.helper;

import com.bfs.rma.payment.model.TransactionMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bfs.rma.payment.helper.TxnHelper.nullToEmpty;

@Component
public class CallBFSApiHelper {

    private final WebClient.Builder webClientBuilder;

    @Value("${beneficiary.benf_id}")
    private String benfId;

    @Value("${beneficiary.rma_api_url}")
    private String url;

    private static final Logger logger = LoggerFactory.getLogger(CallBFSApiHelper.class);

    public CallBFSApiHelper(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Map<String, String> callBfsApi(String msgType, TransactionMaster transaction) {
        MultiValueMap<String, String> params = buildParams(msgType, transaction);
        logger.info("+++ [{}] Request Params: {}", msgType, params);

        long startTime = System.currentTimeMillis();

        String responseBody = webClientBuilder.build()
                .post()
                .uri(url)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(params)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        long durationMs = System.currentTimeMillis() - startTime;
        logger.info("+++ [{}] Response received in {} ms", msgType, durationMs);
        logger.info("+++ [{}] Raw Response: {}", msgType, responseBody);

        return Arrays.stream(responseBody.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(pair -> pair.length >= 1 && !pair[0].isEmpty()) // ensure key exists
                .collect(Collectors.toMap(
                        pair -> pair[0],
                        pair -> nullToEmpty(pair.length == 2 ? decodeValue(pair[1]) : null),
                        (existing, replacement) -> replacement // merge function to avoid collision errors
                ));
    }

    private MultiValueMap<String, String> buildParams(String msgType, TransactionMaster tx) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("bfs_msgType", msgType);
        params.add("bfs_benfId", tx.getBfs_benfId());
        params.add("bfs_checkSum", tx.getBfs_checkSum());

        switch (msgType) {
            case "AR":
                params.add("bfs_benfTxnTime", tx.getBfs_benfTxnTime());
                params.add("bfs_orderNo", tx.getBfs_orderNo());
                params.add("bfs_benfBankCode", tx.getBfs_benfBankCode());
                params.add("bfs_paymentDesc", tx.getBfs_paymentDesc());
                params.add("bfs_remitterEmail", tx.getBfs_remitterEmail());
                params.add("bfs_txnCurrency", tx.getBfs_txnCurrency());
                params.add("bfs_txnAmount", tx.getBfs_txnAmount());
                params.add("bfs_version", tx.getBfs_version());
                break;

            case "AE":
                params.add("bfs_bfsTxnId", tx.getBfs_bfsTxnId());
                params.add("bfs_remitterAccNo", tx.getAccountNumber());
                params.add("bfs_remitterBankId", tx.getBfs_remitterBankId());
                break;

            case "DR":
                params.add("bfs_bfsTxnId", tx.getBfs_bfsTxnId());
                params.add("bfs_remitterOtp", tx.getBfs_remitterOtp());
                break;

            default:
                throw new IllegalArgumentException("Invalid BFS message type: " + msgType);
        }

        return params;
    }

    private static String decodeValue(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
