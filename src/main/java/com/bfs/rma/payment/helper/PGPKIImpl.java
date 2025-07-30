package com.bfs.rma.payment.helper;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;

@Component
public class PGPKIImpl {

    private static final String ALGORITHM = "SHA1withRSA";

    private final KeyLoader keyLoader;

    public PGPKIImpl(KeyLoader keyLoader) {
        this.keyLoader = keyLoader;
    }

    // Sign the data using the private key and return signature in hexadecimal
    public String signData(String data) throws Exception {
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initSign(keyLoader.readPKCS8PrivateKey());
        signature.update(data.getBytes());
        byte[] signedData = signature.sign();
        return bytesToHex(signedData);  // Return signature in hexadecimal format
    }

    // Utility method to generate the SHA-1 hash of the message
    public static byte[] sha1(String message) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return sha1.digest(message.getBytes());
// Return the hash in hexadecimal format
    }

    // Utility method to convert byte array to hexadecimal string
    public static String bytesToHex(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
}
