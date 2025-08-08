//package com.bfs.rma.payment.helper;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import org.junit.jupiter.api.Test;
//
//import java.security.PublicKey;
//
//public class VerifyChecksumTest {
//
//    @Test
//    void testExtractPublicKeyFromCertFile() {
//        String certPath = "/home/noko/NOKO/personal_noko/Athang/rma/src/main/resources/pki/BFS.crt";
//        PublicKey publicKey = VerifyChecksum.extractPublicKeyFromCertFile(certPath);
//        assertNotNull(publicKey, "Public key should not be null");
//        System.out.println("Extracted public key algorithm: " + publicKey.getAlgorithm());
//    }
//
//    @Test
//    void testVerifyCheckSum_ValidSignature() {
//        String certPath = "/home/noko/NOKO/personal_noko/Athang/rma/src/main/resources/pki/BFS.crt";
//        PublicKey publicKey = VerifyChecksum.extractPublicKeyFromCertFile(certPath);
//
//        SourceStringHelper()
//
//        // This should be the original message that was signed
//        String message = "Test message for signature";
//
//        // This must be the valid hex signature for the above message,
//        // created using the private key corresponding to the BFS.crt public key
//        String signatureHex = "abcdef1234567890...";  // Replace with real signature in hex!
//
//        boolean isValid = VerifyChecksum.verifyCheckSum(message, signatureHex, publicKey);
//        assertTrue(isValid, "Signature should be valid");
//    }
//
//    @Test
//    void testVerifyCheckSum_InvalidSignature() {
//        String certPath = "src/test/resources/BFS.crt";
//        PublicKey publicKey = VerifyChecksum.extractPublicKeyFromCertFile(certPath);
//
//        String message = "Test message for signature";
//
//        // This is an invalid signature (random or altered)
//        String invalidSignatureHex = "00FFAA11";
//
//        boolean isValid = VerifyChecksum.verifyCheckSum(message, invalidSignatureHex, publicKey);
//        assertFalse(isValid, "Signature should be invalid");
//    }
//}
