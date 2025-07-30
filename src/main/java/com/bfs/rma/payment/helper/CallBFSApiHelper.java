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
import java.util.Objects;
import java.util.stream.Collectors;

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

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        logger.info("+++ [{}] Response received in {} ms", msgType, durationMs);
        logger.debug("+++ [{}] Raw Response: {}", msgType, responseBody);

        return Arrays.stream(Objects.requireNonNull(responseBody).split("&"))
                .map(kv -> kv.split("="))
                .filter(kv -> kv.length == 2)
                .collect(Collectors.toMap(
                        kv -> kv[0],
                        kv -> decodeValue(kv[1])
                ));
    }

    private MultiValueMap<String, String> buildParams(String msgType, TransactionMaster tx) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        switch (msgType) {
            case "AR":
                params.add("bfs_msgType", "AR");
                params.add("bfs_benfId", tx.getBfs_benfId());
                params.add("bfs_benfTxnTime", tx.getBfs_benfTxnTime());
                params.add("bfs_orderNo", tx.getBfs_orderNo());
                params.add("bfs_benfBankCode", tx.getBfs_benfBankCode());
                params.add("bfs_paymentDesc", tx.getBfs_paymentDesc());
                params.add("bfs_remitterEmail", tx.getBfs_remitterEmail());
                params.add("bfs_txnCurrency", tx.getBfs_txnCurrency());
                params.add("bfs_txnAmount", tx.getBfs_txnAmount());
                params.add("bfs_version", tx.getBfs_version());
                params.add("bfs_checkSum", tx.getBfs_checkSum());
                break;

            case "AE":
                params.add("bfs_msgType", "AE");
                params.add("bfs_benfId", tx.getBfs_benfId());
                params.add("bfs_bfsTxnId", tx.getBfs_bfsTxnId());
                params.add("bfs_checkSum", tx.getBfs_checkSum());
                params.add("bfs_remitterAccNo", tx.getAccountNumber());
                params.add("bfs_remitterBankId", tx.getBfs_remitterBankId());
                break;

            case "DR":
                params.add("bfs_msgType", "DR");
                params.add("bfs_benfId", tx.getBfs_benfId());
                params.add("bfs_bfsTxnId", tx.getBfs_bfsTxnId());
                params.add("bfs_checkSum", tx.getBfs_checkSum());
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
