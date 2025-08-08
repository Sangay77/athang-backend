package com.bfs.rma.payment.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;

@Component
public class PGPKIImpl {



    @Value("${keys.bfs.public.key}")
    private Resource bfsPublicKeyResource;

    private static final String ALGORITHM = "SHA1withRSA";

    private final KeyLoader keyLoader;

    public PGPKIImpl(KeyLoader keyLoader) {
        this.keyLoader = keyLoader;
    }

    public String signData(String data) throws Exception {
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initSign(keyLoader.readPKCS8PrivateKey());
        signature.update(data.getBytes());
        byte[] signedData = signature.sign();
        return bytesToHex(signedData);  // Return signature in hexadecimal format
    }

    public static String bytesToHex(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    public boolean verifyData(String constructedCheckSum, String checkSumFromMsg)
            throws Exception {
        PublicKey pubKey = getPublicKeyFromResource();
        Signature verifier = Signature.getInstance(ALGORITHM);
        verifier.initVerify(pubKey);
        verifier.update(constructedCheckSum.getBytes());
        return verifier.verify(HexStringToByteArray(checkSumFromMsg));
    }

    private PublicKey getPublicKeyFromResource() throws Exception {
        try (InputStream in = bfsPublicKeyResource.getInputStream()) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
            return cert.getPublicKey();
        }
    }

    public static byte[] HexStringToByteArray(String strHex) {
        byte[] bytKey = new byte[strHex.length() / 2];
        int y = 0;

        for (int x = 0; x < bytKey.length; ++x) {
            String strbyte = strHex.substring(y, y + 2);
            if (strbyte.equals("FF")) {
                bytKey[x] = -1;
            } else {
                bytKey[x] = (byte) Integer.parseInt(strbyte, 16);
            }

            y += 2;
        }

        return bytKey;
    }

}
